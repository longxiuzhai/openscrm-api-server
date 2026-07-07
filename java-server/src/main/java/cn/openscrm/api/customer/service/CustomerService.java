package cn.openscrm.api.customer.service;

import cn.openscrm.api.customer.entity.CustomerEntity;
import cn.openscrm.api.customer.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;

    public IPage<CustomerEntity> page(long page, long pageSize) {
        return customerMapper.selectPage(Page.of(page, pageSize), null);
    }

    public CustomerEntity getById(Long id) {
        return customerMapper.selectById(id);
    }
}
