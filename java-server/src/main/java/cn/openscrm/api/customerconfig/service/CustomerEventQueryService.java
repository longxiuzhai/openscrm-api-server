package cn.openscrm.api.customerconfig.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.persistence.entity.CustomerEventPo;
import cn.openscrm.api.persistence.mapper.CustomerEventPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CustomerEventQueryService {

    private final CustomerEventPoMapper customerEventMapper;

    public CustomerEventQueryService(CustomerEventPoMapper customerEventMapper) {
        this.customerEventMapper = customerEventMapper;
    }

    public PageResponse<CustomerEventPo> query(String extCorpId,
                                               String extStaffId,
                                               String extCustomerId,
                                               String eventType,
                                               String sortField,
                                               String sortType,
                                               long page,
                                               long pageSize) {
        QueryWrapper<CustomerEventPo> countQuery = baseQuery(extCorpId, extStaffId, extCustomerId, eventType);
        long total = customerEventMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
        }
        QueryWrapper<CustomerEventPo> query = baseQuery(extCorpId, extStaffId, extCustomerId, eventType)
                .last("order by " + orderBy(sortField, sortType)
                        + " limit " + pageSize + " offset " + ((page - 1) * pageSize));
        List<CustomerEventPo> items = customerEventMapper.selectList(query);
        return new PageResponse<>(items, total, page, pageSize);
    }

    private QueryWrapper<CustomerEventPo> baseQuery(String extCorpId,
                                                   String extStaffId,
                                                   String extCustomerId,
                                                   String eventType) {
        QueryWrapper<CustomerEventPo> query = new QueryWrapper<CustomerEventPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at");
        if (StringUtils.hasText(extStaffId)) {
            query.eq("ext_staff_id", extStaffId);
        }
        if (StringUtils.hasText(extCustomerId)) {
            query.eq("ext_customer_id", extCustomerId);
        }
        if (StringUtils.hasText(eventType)) {
            query.eq("event_type", eventType);
        }
        return query;
    }

    private String orderBy(String sortField, String sortType) {
        String direction = "asc".equalsIgnoreCase(sortType) ? "asc" : "desc";
        return sortColumn(sortField) + " " + direction;
    }

    private String sortColumn(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return "created_at";
        }
        switch (sortField.toLowerCase(Locale.ROOT)) {
            case "created_at":
            case "updated_at":
            case "send_at":
            case "event_type":
            case "event_name":
                return sortField.toLowerCase(Locale.ROOT);
            default:
                return "created_at";
        }
    }
}
