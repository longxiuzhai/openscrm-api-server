package cn.openscrm.api.customerconfig.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.customerconfig.dto.CustomerInfoDisplayRuleRequest;
import cn.openscrm.api.customerconfig.dto.CustomerInfoRequest;
import cn.openscrm.api.persistence.entity.CustomerInfoDisplayRulePo;
import cn.openscrm.api.persistence.mapper.CustomerInfoDisplayRulePoMapper;
import cn.openscrm.api.persistence.mapper.CustomerInfoPoMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CustomerInfoConfigServiceTest {

    @Test
    void getDisplayRuleReturnsDefaultAllVisibleWhenMissing() {
        CustomerInfoPoMapper infoMapper = mock(CustomerInfoPoMapper.class);
        CustomerInfoDisplayRulePoMapper ruleMapper = mock(CustomerInfoDisplayRulePoMapper.class);
        when(ruleMapper.selectOne(any())).thenReturn(null);
        CustomerInfoConfigService service = new CustomerInfoConfigService(infoMapper, ruleMapper);

        CustomerInfoDisplayRulePo rule = service.getDisplayRule("ww-corp");

        assertThat(rule.getExtCorpId()).isEqualTo("ww-corp");
        assertThat(rule.getAge()).isEqualTo(1);
        assertThat(rule.getPhoneNumber()).isEqualTo(1);
        assertThat(rule.getWeibo()).isEqualTo(1);
    }

    @Test
    void updateDisplayRuleRejectsDuplicateDisplayAndCancelFields() {
        CustomerInfoConfigService service = new CustomerInfoConfigService(
                mock(CustomerInfoPoMapper.class), mock(CustomerInfoDisplayRulePoMapper.class));
        CustomerInfoDisplayRuleRequest request = new CustomerInfoDisplayRuleRequest();
        request.setDisplayFieldList(Collections.singletonList("email"));
        request.setCancelDisplayFieldList(Collections.singletonList("email"));

        assertThatThrownBy(() -> service.updateDisplayRule("ww-corp", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INFO_FIELD_DUPLICATE);
    }

    @Test
    void updateDisplayRuleInsertsDefaultWhenMissingThenAppliesChanges() {
        CustomerInfoDisplayRulePoMapper ruleMapper = mock(CustomerInfoDisplayRulePoMapper.class);
        when(ruleMapper.selectOne(any())).thenReturn(null);
        CustomerInfoConfigService service = new CustomerInfoConfigService(mock(CustomerInfoPoMapper.class), ruleMapper);
        CustomerInfoDisplayRuleRequest request = new CustomerInfoDisplayRuleRequest();
        request.setDisplayFieldList(Collections.singletonList("email"));
        request.setCancelDisplayFieldList(Arrays.asList("phone_number", "weibo"));

        service.updateDisplayRule("ww-corp", request);

        verify(ruleMapper).insert(any(CustomerInfoDisplayRulePo.class));
        verify(ruleMapper).update(any(), any());
    }

    @Test
    void updateCustomerInfoRequiresCustomerAndStaffIds() {
        CustomerInfoConfigService service = new CustomerInfoConfigService(
                mock(CustomerInfoPoMapper.class), mock(CustomerInfoDisplayRulePoMapper.class));
        CustomerInfoRequest request = new CustomerInfoRequest();
        request.setExtCustomerId("customer-a");

        assertThatThrownBy(() -> service.updateCustomerInfo("ww-corp", request))
                .isInstanceOf(BizException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }
}
