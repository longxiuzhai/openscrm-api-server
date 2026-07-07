package cn.openscrm.api.customerexport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.customerexport.dto.CustomerExportRow;
import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import cn.openscrm.api.persistence.mapper.CustomerExportQueryMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffTagPoMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CustomerExportServiceTest {

    @Test
    void queryRowsHydratesTagsAndWhitelistsSort() {
        CustomerExportQueryMapper exportMapper = mock(CustomerExportQueryMapper.class);
        CustomerStaffTagPoMapper tagMapper = mock(CustomerStaffTagPoMapper.class);
        CustomerExportRow row = row();
        when(exportMapper.selectRows(eq("ww-corp"), eq("张"), any(), any(), any(), any(), any(), any(), any(),
                eq("s.name asc"), eq(20L), eq(20L))).thenReturn(Collections.singletonList(row));
        CustomerStaffTagPo tag = new CustomerStaffTagPo();
        tag.setCustomerStaffId(10001L);
        tag.setTagName("高意向");
        when(tagMapper.selectList(any())).thenReturn(Collections.singletonList(tag));
        CustomerExportService service = new CustomerExportService(exportMapper, tagMapper);

        java.util.List<CustomerExportRow> rows = service.queryRows(
                "ww-corp", "张", null, null, null, null, null,
                null, null, "staff_name", "asc", 2, 20);

        assertThat(rows).containsExactly(row);
        assertThat(rows.get(0).getTags()).containsExactly(tag);
        verify(exportMapper).selectRows(eq("ww-corp"), eq("张"), any(), any(), any(), any(), any(), any(), any(),
                eq("s.name asc"), eq(20L), eq(20L));
    }

    @Test
    void exportXlsxReturnsWorkbookBytes() {
        CustomerExportQueryMapper exportMapper = mock(CustomerExportQueryMapper.class);
        CustomerStaffTagPoMapper tagMapper = mock(CustomerStaffTagPoMapper.class);
        CustomerExportRow row = row();
        when(exportMapper.selectRows(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Long.class), any(Long.class)))
                .thenReturn(Collections.singletonList(row));
        CustomerStaffTagPo tag = new CustomerStaffTagPo();
        tag.setCustomerStaffId(10001L);
        tag.setTagName("高意向");
        when(tagMapper.selectList(any())).thenReturn(Collections.singletonList(tag));
        CustomerExportService service = new CustomerExportService(exportMapper, tagMapper);

        byte[] bytes = service.exportXlsx("ww-corp", null, null, null, null, null, null, null, null, null, null);

        assertThat(bytes).isNotEmpty();
        assertThat(bytes[0]).isEqualTo((byte) 'P');
        assertThat(bytes[1]).isEqualTo((byte) 'K');
    }

    private CustomerExportRow row() {
        CustomerExportRow row = new CustomerExportRow();
        row.setCustomerStaffId(10001L);
        row.setCustomerName("张三");
        row.setRemark("备注");
        row.setDescription("描述");
        row.setStatus("未流失");
        row.setCustomerCorpName("客户公司");
        row.setStaffName("客服A");
        row.setCreatetime(LocalDateTime.parse("2026-07-05T10:00:00"));
        row.setAddWay(1L);
        row.setGender(1L);
        row.setPhoneNumber("13800000000");
        row.setAge(30L);
        row.setBirthday("1996-01-01");
        return row;
    }
}
