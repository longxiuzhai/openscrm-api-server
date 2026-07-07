package cn.openscrm.api.auth.service;

import cn.openscrm.api.auth.session.SessionKeys;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.persistence.entity.CustomerPo;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CurrentCustomerService {

    private final CustomerPoMapper customerMapper;

    public CustomerPo requireCustomer(HttpSession session) {
        if (session == null) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        Object current = session.getAttribute(SessionKeys.CUSTOMER_INFO);
        if (!(current instanceof CustomerPo)) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        CustomerPo sessionCustomer = (CustomerPo) current;
        if (!StringUtils.hasText(sessionCustomer.getExtId())) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        CustomerPo customer = customerMapper.selectOne(new LambdaQueryWrapper<CustomerPo>()
                .eq(CustomerPo::getExtCorpId, sessionCustomer.getExtCorpId())
                .eq(CustomerPo::getExtId, sessionCustomer.getExtId())
                .last("limit 1"));
        if (customer == null) {
            throw new BizException(ErrorCode.INVALID_SESSION);
        }
        return customer;
    }
}
