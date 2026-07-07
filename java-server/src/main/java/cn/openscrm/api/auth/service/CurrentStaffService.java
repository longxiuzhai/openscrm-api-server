package cn.openscrm.api.auth.service;

import cn.openscrm.api.auth.session.SessionKeys;
import cn.openscrm.api.auth.session.StaffSession;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CurrentStaffService {

    private final StaffPoMapper staffMapper;

    public CurrentStaffService(StaffPoMapper staffMapper) {
        this.staffMapper = staffMapper;
    }

    public StaffPo requireStaffAdmin(HttpSession session) {
        if (session == null) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        StaffSession current = (StaffSession) session.getAttribute(SessionKeys.STAFF_ADMIN_INFO);
        return load(current);
    }

    public StaffPo requireStaff(HttpSession session) {
        if (session == null) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        StaffSession current = (StaffSession) session.getAttribute(SessionKeys.STAFF_INFO);
        return load(current);
    }

    public StaffSession toSession(StaffPo staff) {
        StaffSession session = new StaffSession();
        session.setId(staff.getId());
        session.setExtCorpId(staff.getExtCorpId());
        session.setExtStaffId(staff.getExtId());
        session.setRoleId(staff.getRoleId());
        session.setRoleType(staff.getRoleType());
        session.setName(staff.getName());
        return session;
    }

    private StaffPo load(StaffSession current) {
        if (current == null || current.getExtStaffId() == null) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        StaffPo staff = staffMapper.selectOne(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtId, current.getExtStaffId())
                .last("limit 1"));
        if (staff == null || staff.getId() == null) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        return staff;
    }
}
