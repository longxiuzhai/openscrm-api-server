package cn.openscrm.api.customerconfig.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.customerconfig.dto.CustomerInfoDisplayRuleRequest;
import cn.openscrm.api.customerconfig.dto.CustomerInfoRequest;
import cn.openscrm.api.persistence.entity.CustomerInfoDisplayRulePo;
import cn.openscrm.api.persistence.entity.CustomerInfoPo;
import cn.openscrm.api.persistence.mapper.CustomerInfoDisplayRulePoMapper;
import cn.openscrm.api.persistence.mapper.CustomerInfoPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CustomerInfoConfigService {

    private static final int TRUE = 1;
    private static final int FALSE = 2;
    private static final Map<String, String> DISPLAY_COLUMNS = new HashMap<>();

    static {
        DISPLAY_COLUMNS.put("age", "age");
        DISPLAY_COLUMNS.put("description", "description");
        DISPLAY_COLUMNS.put("email", "email");
        DISPLAY_COLUMNS.put("phone_number", "phone_number");
        DISPLAY_COLUMNS.put("qq", "qq");
        DISPLAY_COLUMNS.put("address", "address");
        DISPLAY_COLUMNS.put("birthday", "birthday");
        DISPLAY_COLUMNS.put("weibo", "weibo");
    }

    private final CustomerInfoPoMapper customerInfoMapper;
    private final CustomerInfoDisplayRulePoMapper displayRuleMapper;

    public CustomerInfoConfigService(CustomerInfoPoMapper customerInfoMapper,
                                     CustomerInfoDisplayRulePoMapper displayRuleMapper) {
        this.customerInfoMapper = customerInfoMapper;
        this.displayRuleMapper = displayRuleMapper;
    }

    public CustomerInfoPo getCustomerInfo(String extCorpId, String extCustomerId, String extStaffId) {
        return customerInfoMapper.selectOne(new QueryWrapper<CustomerInfoPo>()
                .eq("ext_corp_id", extCorpId)
                .eq("ext_customer_id", extCustomerId)
                .eq("ext_staff_id", extStaffId)
                .isNull("deleted_at")
                .last("limit 1"));
    }

    public void updateCustomerInfo(String extCorpId, CustomerInfoRequest request) {
        if (!StringUtils.hasText(request.getExtCustomerId()) || !StringUtils.hasText(request.getExtStaffId())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        UpdateWrapper<CustomerInfoPo> update = new UpdateWrapper<CustomerInfoPo>()
                .eq("ext_corp_id", extCorpId)
                .eq("ext_customer_id", request.getExtCustomerId())
                .eq("ext_staff_id", request.getExtStaffId())
                .isNull("deleted_at")
                .set("updated_at", LocalDateTime.now());
        setIfPresent(update, "age", request.getAge());
        setIfPresent(update, "description", request.getDescription());
        setIfPresent(update, "email", request.getEmail());
        setIfPresent(update, "phone_number", request.getPhoneNumber());
        setIfPresent(update, "qq", request.getQq());
        setIfPresent(update, "address", request.getAddress());
        setIfPresent(update, "birthday", request.getBirthday());
        setIfPresent(update, "weibo", request.getWeibo());
        setIfPresent(update, "remark_field", StringUtils.hasText(request.getRemarkField())
                ? request.getRemarkField() : request.getRemarkValues());
        customerInfoMapper.update(null, update);
    }

    public CustomerInfoDisplayRulePo getDisplayRule(String extCorpId) {
        CustomerInfoDisplayRulePo rule = displayRuleMapper.selectOne(new QueryWrapper<CustomerInfoDisplayRulePo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .last("limit 1"));
        return rule == null ? defaultDisplayRule(extCorpId) : rule;
    }

    public void updateDisplayRule(String extCorpId, CustomerInfoDisplayRuleRequest request) {
        Set<String> displayFields = normalizedFields(request.getDisplayFieldList());
        Set<String> cancelFields = normalizedFields(request.getCancelDisplayFieldList());
        Set<String> intersection = new HashSet<>(displayFields);
        intersection.retainAll(cancelFields);
        if (!intersection.isEmpty()) {
            throw new BizException(ErrorCode.INFO_FIELD_DUPLICATE);
        }
        CustomerInfoDisplayRulePo existing = displayRuleMapper.selectOne(new QueryWrapper<CustomerInfoDisplayRulePo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .last("limit 1"));
        if (existing == null) {
            CustomerInfoDisplayRulePo rule = defaultDisplayRule(extCorpId);
            displayRuleMapper.insert(rule);
        }
        UpdateWrapper<CustomerInfoDisplayRulePo> update = new UpdateWrapper<CustomerInfoDisplayRulePo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .set("updated_at", LocalDateTime.now());
        for (String field : displayFields) {
            update.set(DISPLAY_COLUMNS.get(field), TRUE);
        }
        for (String field : cancelFields) {
            update.set(DISPLAY_COLUMNS.get(field), FALSE);
        }
        displayRuleMapper.update(null, update);
    }

    private CustomerInfoDisplayRulePo defaultDisplayRule(String extCorpId) {
        CustomerInfoDisplayRulePo rule = new CustomerInfoDisplayRulePo();
        rule.setExtCorpId(extCorpId);
        rule.setAge(TRUE);
        rule.setDescription(TRUE);
        rule.setEmail(TRUE);
        rule.setPhoneNumber(TRUE);
        rule.setQq(TRUE);
        rule.setAddress(TRUE);
        rule.setBirthday(TRUE);
        rule.setWeibo(TRUE);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(rule.getCreatedAt());
        return rule;
    }

    private Set<String> normalizedFields(java.util.List<String> fields) {
        Set<String> result = new HashSet<>();
        if (fields == null) {
            return result;
        }
        for (String field : fields) {
            if (!StringUtils.hasText(field)) {
                continue;
            }
            String normalized = field.trim().toLowerCase();
            if (!DISPLAY_COLUMNS.containsKey(normalized)) {
                throw new BizException(ErrorCode.BAD_REQUEST, "不支持的展示字段: " + field);
            }
            result.add(normalized);
        }
        return result;
    }

    private void setIfPresent(UpdateWrapper<CustomerInfoPo> update, String column, Object value) {
        if (value instanceof String && !StringUtils.hasText((String) value)) {
            return;
        }
        if (value != null) {
            update.set(column, value);
        }
    }
}
