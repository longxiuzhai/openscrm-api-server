package cn.openscrm.api.customerconfig.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.customerconfig.dto.CustomerRemarkRequest;
import cn.openscrm.api.customerconfig.dto.CustomerRemarkResponse;
import cn.openscrm.api.customerconfig.dto.InfoRemarkResponse;
import cn.openscrm.api.customerconfig.dto.RemarkOptionRequest;
import cn.openscrm.api.persistence.entity.CustomerInfoDisplayRulePo;
import cn.openscrm.api.persistence.entity.CustomerRemarkPo;
import cn.openscrm.api.persistence.entity.RemarkOptionPo;
import cn.openscrm.api.persistence.mapper.CustomerRemarkPoMapper;
import cn.openscrm.api.persistence.mapper.RemarkOptionPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CustomerRemarkConfigService {

    private final CustomerRemarkPoMapper remarkMapper;
    private final RemarkOptionPoMapper optionMapper;
    private final CustomerInfoConfigService customerInfoConfigService;
    private final SnowflakeIdGenerator idGenerator;

    public CustomerRemarkConfigService(CustomerRemarkPoMapper remarkMapper,
                                       RemarkOptionPoMapper optionMapper,
                                       CustomerInfoConfigService customerInfoConfigService,
                                       SnowflakeIdGenerator idGenerator) {
        this.remarkMapper = remarkMapper;
        this.optionMapper = optionMapper;
        this.customerInfoConfigService = customerInfoConfigService;
        this.idGenerator = idGenerator;
    }

    public InfoRemarkResponse get(String extCorpId) {
        InfoRemarkResponse response = new InfoRemarkResponse();
        CustomerInfoDisplayRulePo displayRule = customerInfoConfigService.getDisplayRule(extCorpId);
        response.setDisplayRules(displayRule);
        List<CustomerRemarkPo> remarks = remarkMapper.selectList(new QueryWrapper<CustomerRemarkPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .orderByAsc("rank_num", "created_at"));
        response.setRemark(hydrateOptions(remarks));
        return response;
    }

    @Transactional
    public CustomerRemarkResponse create(String extCorpId, CustomerRemarkRequest request) {
        if (!StringUtils.hasText(request.getFieldType()) || !StringUtils.hasText(request.getFieldName())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        LocalDateTime now = LocalDateTime.now();
        CustomerRemarkPo remark = new CustomerRemarkPo();
        remark.setId(idGenerator.nextId());
        remark.setExtCorpId(extCorpId);
        remark.setName(request.getFieldName());
        remark.setFieldType(request.getFieldType());
        remark.setHasStaffUsed(false);
        remark.setRankNum(nextRankNum(extCorpId));
        remark.setCreatedAt(now);
        remark.setUpdatedAt(now);
        remarkMapper.insert(remark);
        if ("option_text".equals(request.getFieldType()) && request.getOptionNameList() != null) {
            for (String optionName : request.getOptionNameList()) {
                if (StringUtils.hasText(optionName)) {
                    insertOption(remark.getId(), optionName);
                }
            }
        }
        return hydrateOptions(Collections.singletonList(remark)).get(0);
    }

    public void delete(String extCorpId, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        remarkMapper.update(null, new UpdateWrapper<CustomerRemarkPo>()
                .eq("ext_corp_id", extCorpId)
                .in("id", ids)
                .isNull("deleted_at")
                .set("deleted_at", LocalDateTime.now()));
    }

    public CustomerRemarkPo update(CustomerRemarkRequest request) {
        if (request.getId() == null || !StringUtils.hasText(request.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        CustomerRemarkPo existing = remarkMapper.selectById(request.getId());
        if (existing == null || existing.getDeletedAt() != null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        remarkMapper.update(null, new UpdateWrapper<CustomerRemarkPo>()
                .eq("id", request.getId())
                .isNull("deleted_at")
                .set("name", request.getName())
                .set("updated_at", LocalDateTime.now()));
        existing.setName(request.getName());
        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }

    @Transactional
    public void exchangeOrder(CustomerRemarkRequest request) {
        if (request.getId() == null || request.getExchangeOrderId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        CustomerRemarkPo first = remarkMapper.selectById(request.getId());
        CustomerRemarkPo second = remarkMapper.selectById(request.getExchangeOrderId());
        if (first == null || second == null || first.getDeletedAt() != null || second.getDeletedAt() != null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        Integer firstRank = first.getRankNum();
        remarkMapper.update(null, new UpdateWrapper<CustomerRemarkPo>()
                .eq("id", first.getId())
                .set("rank_num", second.getRankNum())
                .set("updated_at", LocalDateTime.now()));
        remarkMapper.update(null, new UpdateWrapper<CustomerRemarkPo>()
                .eq("id", second.getId())
                .set("rank_num", firstRank)
                .set("updated_at", LocalDateTime.now()));
    }

    public void addOption(RemarkOptionRequest request) {
        if (request.getRemarkId() == null || !StringUtils.hasText(request.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        insertOption(request.getRemarkId(), request.getName());
    }

    public void updateOption(RemarkOptionRequest request) {
        if (request.getRemarkOptionId() == null || !StringUtils.hasText(request.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        optionMapper.update(null, new UpdateWrapper<RemarkOptionPo>()
                .eq("id", request.getRemarkOptionId())
                .isNull("deleted_at")
                .set("name", request.getName())
                .set("updated_at", LocalDateTime.now()));
    }

    public void deleteOptions(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        optionMapper.update(null, new UpdateWrapper<RemarkOptionPo>()
                .in("id", ids)
                .isNull("deleted_at")
                .set("deleted_at", LocalDateTime.now()));
    }

    private int nextRankNum(String extCorpId) {
        List<CustomerRemarkPo> rows = remarkMapper.selectList(new QueryWrapper<CustomerRemarkPo>()
                .select("rank_num")
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .orderByDesc("rank_num")
                .last("limit 1"));
        if (rows.isEmpty() || rows.get(0).getRankNum() == null) {
            return 1;
        }
        return rows.get(0).getRankNum() + 1;
    }

    private void insertOption(Long remarkId, String name) {
        RemarkOptionPo option = new RemarkOptionPo();
        option.setId(idGenerator.nextId());
        option.setRemarkId(remarkId);
        option.setName(name);
        option.setCreatedAt(LocalDateTime.now());
        option.setUpdatedAt(option.getCreatedAt());
        optionMapper.insert(option);
    }

    private List<CustomerRemarkResponse> hydrateOptions(List<CustomerRemarkPo> remarks) {
        if (remarks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> remarkIds = new ArrayList<>();
        for (CustomerRemarkPo remark : remarks) {
            remarkIds.add(remark.getId());
        }
        List<RemarkOptionPo> options = optionMapper.selectList(new QueryWrapper<RemarkOptionPo>()
                .in("remark_id", remarkIds)
                .isNull("deleted_at")
                .orderByAsc("created_at"));
        Map<Long, List<RemarkOptionPo>> optionsByRemark = new HashMap<>();
        for (RemarkOptionPo option : options) {
            optionsByRemark.computeIfAbsent(option.getRemarkId(), key -> new ArrayList<>()).add(option);
        }
        List<CustomerRemarkResponse> responses = new ArrayList<>();
        for (CustomerRemarkPo remark : remarks) {
            CustomerRemarkResponse response = new CustomerRemarkResponse();
            response.setId(remark.getId());
            response.setExtCorpId(remark.getExtCorpId());
            response.setExtCreatorId(remark.getExtCreatorId());
            response.setName(remark.getName());
            response.setFieldType(remark.getFieldType());
            response.setHasStaffUsed(remark.getHasStaffUsed());
            response.setRankNum(remark.getRankNum());
            response.setCreatedAt(remark.getCreatedAt());
            response.setUpdatedAt(remark.getUpdatedAt());
            response.setDeletedAt(remark.getDeletedAt());
            response.setInfoOption(optionsByRemark.getOrDefault(remark.getId(), Collections.emptyList()));
            responses.add(response);
        }
        return responses;
    }
}
