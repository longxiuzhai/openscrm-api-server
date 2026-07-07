package cn.openscrm.api.customer.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.customer.dto.CustomerEventsResponse;
import cn.openscrm.api.customer.dto.CustomerStaffRelationResponse;
import cn.openscrm.api.customer.dto.FullCustomerInfoResponse;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.entity.CustomerInfoPo;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.CustomerStaffPo;
import cn.openscrm.api.persistence.entity.InternalTagPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerInfoPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffTagPoMapper;
import cn.openscrm.api.persistence.mapper.InternalTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomerFrontendService {

    private final CustomerPoMapper customerMapper;
    private final CustomerInfoPoMapper customerInfoMapper;
    private final CustomerStaffPoMapper customerStaffMapper;
    private final CustomerStaffTagPoMapper customerStaffTagMapper;
    private final InternalTagPoMapper internalTagMapper;
    private final CustomerEventPoMapper customerEventMapper;
    private final ObjectMapper objectMapper;

    public FullCustomerInfoResponse getFullCustomerInfo(String extCustomerId, StaffPo staff) {
        if (!StringUtils.hasText(extCustomerId) || staff == null || !StringUtils.hasText(staff.getExtId())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        CustomerPo customer = customerMapper.selectOne(new LambdaQueryWrapper<CustomerPo>()
                .eq(CustomerPo::getExtCorpId, staff.getExtCorpId())
                .eq(CustomerPo::getExtId, extCustomerId)
                .last("limit 1"));
        if (customer == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }

        FullCustomerInfoResponse response = new FullCustomerInfoResponse();
        response.setCustomer(customer);
        response.setCustomerInfo(findCustomerInfo(staff.getExtCorpId(), extCustomerId, staff.getExtId()));
        response.setStaffRelations(findRelations(staff.getExtCorpId(), extCustomerId, staff.getExtId()));
        response.setCustomerEvents(findEvents(staff.getExtCorpId(), extCustomerId));
        return response;
    }

    private CustomerInfoPo findCustomerInfo(String extCorpId, String extCustomerId, String extStaffId) {
        return customerInfoMapper.selectOne(new LambdaQueryWrapper<CustomerInfoPo>()
                .eq(CustomerInfoPo::getExtCorpId, extCorpId)
                .eq(CustomerInfoPo::getExtCustomerId, extCustomerId)
                .eq(CustomerInfoPo::getExtStaffId, extStaffId)
                .last("limit 1"));
    }

    private List<CustomerStaffRelationResponse> findRelations(String extCorpId, String extCustomerId, String extStaffId) {
        List<CustomerStaffPo> relations = customerStaffMapper.selectList(new LambdaQueryWrapper<CustomerStaffPo>()
                .eq(CustomerStaffPo::getExtCorpId, extCorpId)
                .eq(CustomerStaffPo::getExtCustomerId, extCustomerId)
                .eq(CustomerStaffPo::getExtStaffId, extStaffId)
                .isNull(CustomerStaffPo::getDeletedAt));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        return relations.stream().map(this::toRelationResponse).collect(java.util.stream.Collectors.toList());
    }

    private CustomerStaffRelationResponse toRelationResponse(CustomerStaffPo relation) {
        CustomerStaffRelationResponse response = new CustomerStaffRelationResponse();
        response.setRelation(relation);
        response.setCustomerStaffTags(customerStaffTagMapper.selectList(
                new LambdaQueryWrapper<cn.openscrm.api.persistence.entity.CustomerStaffTagPo>()
                        .eq(cn.openscrm.api.persistence.entity.CustomerStaffTagPo::getCustomerStaffId, relation.getId())
                        .isNull(cn.openscrm.api.persistence.entity.CustomerStaffTagPo::getDeletedAt)));
        List<Long> internalTagIds = readLongList(relation.getInternalTagIds());
        if (!internalTagIds.isEmpty()) {
            response.setInternalTags(internalTagMapper.selectList(new LambdaQueryWrapper<InternalTagPo>()
                    .in(InternalTagPo::getId, internalTagIds)
                    .isNull(InternalTagPo::getDeletedAt)));
        }
        return response;
    }

    private CustomerEventsResponse findEvents(String extCorpId, String extCustomerId) {
        LambdaQueryWrapper<CustomerEventPo> query = new LambdaQueryWrapper<CustomerEventPo>()
                .eq(CustomerEventPo::getExtCorpId, extCorpId)
                .eq(CustomerEventPo::getExtCustomerId, extCustomerId)
                .isNull(CustomerEventPo::getDeletedAt)
                .orderByDesc(CustomerEventPo::getCreatedAt);
        CustomerEventsResponse response = new CustomerEventsResponse();
        response.setTotal(customerEventMapper.selectCount(query));
        response.setEvents(customerEventMapper.selectList(query));
        return response;
    }

    private List<Long> readLongList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
