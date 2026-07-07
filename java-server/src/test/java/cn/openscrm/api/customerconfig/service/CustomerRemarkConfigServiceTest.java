package cn.openscrm.api.customerconfig.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.customerconfig.dto.CustomerRemarkRequest;
import cn.openscrm.api.customerconfig.dto.InfoRemarkResponse;
import cn.openscrm.api.customerconfig.dto.RemarkOptionRequest;
import cn.openscrm.api.persistence.entity.CustomerInfoDisplayRulePo;
import cn.openscrm.api.persistence.entity.CustomerRemarkPo;
import cn.openscrm.api.persistence.entity.RemarkOptionPo;
import cn.openscrm.api.persistence.mapper.CustomerRemarkPoMapper;
import cn.openscrm.api.persistence.mapper.RemarkOptionPoMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CustomerRemarkConfigServiceTest {

    @Test
    void getHydratesRemarkOptionsAndDisplayRules() {
        CustomerRemarkPoMapper remarkMapper = mock(CustomerRemarkPoMapper.class);
        RemarkOptionPoMapper optionMapper = mock(RemarkOptionPoMapper.class);
        CustomerInfoConfigService infoService = mock(CustomerInfoConfigService.class);
        CustomerInfoDisplayRulePo rule = new CustomerInfoDisplayRulePo();
        rule.setExtCorpId("ww-corp");
        when(infoService.getDisplayRule("ww-corp")).thenReturn(rule);
        CustomerRemarkPo remark = new CustomerRemarkPo();
        remark.setId(100L);
        remark.setName("爱好");
        when(remarkMapper.selectList(any())).thenReturn(Collections.singletonList(remark));
        RemarkOptionPo option = new RemarkOptionPo();
        option.setRemarkId(100L);
        option.setName("读书");
        when(optionMapper.selectList(any())).thenReturn(Collections.singletonList(option));
        CustomerRemarkConfigService service = new CustomerRemarkConfigService(
                remarkMapper, optionMapper, infoService, new SnowflakeIdGenerator());

        InfoRemarkResponse response = service.get("ww-corp");

        assertThat(response.getDisplayRules()).isEqualTo(rule);
        assertThat(response.getRemark()).hasSize(1);
        assertThat(response.getRemark().get(0).getInfoOption()).containsExactly(option);
    }

    @Test
    void createOptionTextRemarkCreatesOptions() {
        CustomerRemarkPoMapper remarkMapper = mock(CustomerRemarkPoMapper.class);
        RemarkOptionPoMapper optionMapper = mock(RemarkOptionPoMapper.class);
        when(remarkMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(optionMapper.selectList(any())).thenReturn(Collections.emptyList());
        CustomerRemarkConfigService service = new CustomerRemarkConfigService(
                remarkMapper, optionMapper, mock(CustomerInfoConfigService.class), new SnowflakeIdGenerator());
        CustomerRemarkRequest request = new CustomerRemarkRequest();
        request.setFieldType("option_text");
        request.setFieldName("爱好");
        request.setOptionNameList(Arrays.asList("读书", "跑步"));

        service.create("ww-corp", request);

        verify(remarkMapper).insert(any(CustomerRemarkPo.class));
        verify(optionMapper, times(2)).insert(any(RemarkOptionPo.class));
    }

    @Test
    void exchangeOrderSwapsRankNumbers() {
        CustomerRemarkPoMapper remarkMapper = mock(CustomerRemarkPoMapper.class);
        CustomerRemarkPo first = new CustomerRemarkPo();
        first.setId(1L);
        first.setRankNum(10);
        CustomerRemarkPo second = new CustomerRemarkPo();
        second.setId(2L);
        second.setRankNum(20);
        when(remarkMapper.selectById(1L)).thenReturn(first);
        when(remarkMapper.selectById(2L)).thenReturn(second);
        CustomerRemarkConfigService service = new CustomerRemarkConfigService(
                remarkMapper, mock(RemarkOptionPoMapper.class), mock(CustomerInfoConfigService.class),
                new SnowflakeIdGenerator());
        CustomerRemarkRequest request = new CustomerRemarkRequest();
        request.setId(1L);
        request.setExchangeOrderId(2L);

        service.exchangeOrder(request);

        verify(remarkMapper, times(2)).update(any(), any());
    }

    @Test
    void optionUpdateAndDeleteUseSoftDeleteStyleUpdates() {
        RemarkOptionPoMapper optionMapper = mock(RemarkOptionPoMapper.class);
        CustomerRemarkConfigService service = new CustomerRemarkConfigService(
                mock(CustomerRemarkPoMapper.class), optionMapper, mock(CustomerInfoConfigService.class),
                new SnowflakeIdGenerator());
        RemarkOptionRequest update = new RemarkOptionRequest();
        update.setRemarkOptionId(10L);
        update.setName("新选项");

        service.updateOption(update);
        service.deleteOptions(Collections.singletonList(10L));

        verify(optionMapper, times(2)).update(any(), any());
    }
}
