package cn.openscrm.api.customer.service;

import cn.openscrm.api.customer.dto.CustomerTrendResponse;
import cn.openscrm.api.persistence.entity.CustomerStatisticPo;
import cn.openscrm.api.persistence.mapper.CustomerStatisticPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CustomerStatisticService {

    public static final String TOTAL = "total";
    public static final String INCREASE = "increase";
    public static final String DECREASE = "decrease";
    public static final String NET_INCREASE = "net_increase";

    private final CustomerStatisticPoMapper mapper;

    public CustomerStatisticService(CustomerStatisticPoMapper mapper) {
        this.mapper = mapper;
    }

    public List<CustomerTrendResponse> query(String extCorpId,
                                             String statisticType,
                                             List<String> extStaffIds,
                                             LocalDate startTime,
                                             LocalDate endTime) {
        if (!isSupported(statisticType)) {
            throw new IllegalArgumentException("unsupported statistic_type");
        }
        if (startTime == null || endTime == null || endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("invalid date range");
        }
        QueryWrapper<CustomerStatisticPo> query = new QueryWrapper<CustomerStatisticPo>()
                .eq("ext_corp_id", extCorpId)
                .between("date", startTime, endTime)
                .isNull("deleted_at")
                .orderByAsc("date");
        if (extStaffIds != null && !extStaffIds.isEmpty()) {
            query.in("ext_staff_id", extStaffIds);
        }
        List<CustomerStatisticPo> rows = mapper.selectList(query);
        Map<LocalDate, Long> values = aggregate(rows, statisticType);
        return fillRange(values, statisticType, startTime, endTime);
    }

    private Map<LocalDate, Long> aggregate(List<CustomerStatisticPo> rows, String statisticType) {
        Map<LocalDate, Long> values = new LinkedHashMap<>();
        for (CustomerStatisticPo row : rows) {
            if (row.getDate() == null) {
                continue;
            }
            values.merge(row.getDate(), valueOf(row, statisticType), Long::sum);
        }
        return values;
    }

    private List<CustomerTrendResponse> fillRange(Map<LocalDate, Long> values,
                                                  String statisticType,
                                                  LocalDate startTime,
                                                  LocalDate endTime) {
        List<CustomerTrendResponse> result = new ArrayList<>();
        long lastTotal = 0;
        boolean hasLastTotal = false;
        for (LocalDate date = startTime; !date.isAfter(endTime); date = date.plusDays(1)) {
            Long number = values.get(date);
            if (TOTAL.equals(statisticType)) {
                if (number != null) {
                    lastTotal = number;
                    hasLastTotal = true;
                } else if (!hasLastTotal) {
                    LocalDate firstDate = values.keySet().stream().findFirst().orElse(null);
                    if (firstDate != null && date.isBefore(firstDate)) {
                        number = values.get(firstDate);
                    }
                }
                result.add(new CustomerTrendResponse(number == null ? lastTotal : number, date.toString()));
            } else {
                result.add(new CustomerTrendResponse(number == null ? 0 : number, date.toString()));
            }
        }
        return result;
    }

    private long valueOf(CustomerStatisticPo row, String statisticType) {
        switch (statisticType) {
            case TOTAL:
                return zero(row.getTotalCustomerNum());
            case INCREASE:
                return zero(row.getIncreaseCustomerNum());
            case DECREASE:
                return zero(row.getDecreaseCustomerNum());
            case NET_INCREASE:
                return zero(row.getIncreaseCustomerNum()) - zero(row.getDecreaseCustomerNum());
            default:
                throw new IllegalArgumentException("unsupported statistic_type");
        }
    }

    private boolean isSupported(String statisticType) {
        return StringUtils.hasText(statisticType)
                && (TOTAL.equals(statisticType)
                || INCREASE.equals(statisticType)
                || DECREASE.equals(statisticType)
                || NET_INCREASE.equals(statisticType));
    }

    private long zero(Long value) {
        return value == null ? 0 : value;
    }
}
