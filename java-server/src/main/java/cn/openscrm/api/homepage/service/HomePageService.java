package cn.openscrm.api.homepage.service;

import cn.openscrm.api.homepage.dto.CustomerSummaryResponse;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.entity.CustomerStaffRelationHistoryPo;
import cn.openscrm.api.persistence.entity.GroupChatPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffRelationHistoryPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HomePageService {

    private final StaffPoMapper staffMapper;
    private final CustomerPoMapper customerMapper;
    private final CustomerStaffRelationHistoryPoMapper relationHistoryMapper;
    private final GroupChatPoMapper groupChatMapper;

    public HomePageService(StaffPoMapper staffMapper,
                           CustomerPoMapper customerMapper,
                           CustomerStaffRelationHistoryPoMapper relationHistoryMapper,
                           GroupChatPoMapper groupChatMapper) {
        this.staffMapper = staffMapper;
        this.customerMapper = customerMapper;
        this.relationHistoryMapper = relationHistoryMapper;
        this.groupChatMapper = groupChatMapper;
    }

    public CustomerSummaryResponse summary(String extCorpId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        CustomerSummaryResponse response = new CustomerSummaryResponse();
        response.setTotalStaffsNum(staffMapper.selectCount(new QueryWrapper<StaffPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")));
        response.setTotalCustomersNum(customerMapper.selectCount(new QueryWrapper<CustomerPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")));
        response.setTodayCustomersIncrease(customerMapper.selectCount(new QueryWrapper<CustomerPo>()
                .eq("ext_corp_id", extCorpId)
                .ge("created_at", todayStart)
                .lt("created_at", tomorrowStart)
                .isNull("deleted_at")));
        response.setTodayCustomersDecrease(relationHistoryMapper.selectCount(new QueryWrapper<CustomerStaffRelationHistoryPo>()
                .eq("ext_corp_id", extCorpId)
                .ge("customer_delete_staff_at", todayStart)
                .lt("customer_delete_staff_at", tomorrowStart)
                .isNull("deleted_at")));
        response.setTotalGroupsNum(groupChatMapper.selectCount(new QueryWrapper<GroupChatPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")));
        response.setTodayGroupsIncrease(sumGroupChat(extCorpId, "today_join_member_num"));
        response.setTodayGroupsDecrease(sumGroupChat(extCorpId, "today_quit_member_num"));
        return response;
    }

    private long sumGroupChat(String extCorpId, String column) {
        List<Object> values = groupChatMapper.selectObjs(new QueryWrapper<GroupChatPo>()
                .select("coalesce(sum(" + column + "), 0)")
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at"));
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return 0;
        }
        Object value = values.get(0);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
