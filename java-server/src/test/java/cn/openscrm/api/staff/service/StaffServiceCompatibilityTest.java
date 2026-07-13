package cn.openscrm.api.staff.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.RolePo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.DepartmentPoMapper;
import cn.openscrm.api.persistence.mapper.RolePoMapper;
import cn.openscrm.api.persistence.mapper.StaffDepartmentPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.staff.dto.CurrentStaffResponse;
import cn.openscrm.api.staff.dto.SimpleStaffResponse;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaffServiceCompatibilityTest {

    private final StaffPoMapper staffMapper = mock(StaffPoMapper.class);
    private final StaffDepartmentPoMapper staffDepartmentMapper = mock(StaffDepartmentPoMapper.class);
    private final DepartmentPoMapper departmentMapper = mock(DepartmentPoMapper.class);
    private final RolePoMapper roleMapper = mock(RolePoMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private StaffService service;

    @BeforeEach
    void setUp() {
        service = new StaffService(
                staffMapper,
                staffDepartmentMapper,
                departmentMapper,
                roleMapper,
                mock(WeWorkClient.class),
                new OpenScrmProperties(),
                mock(SnowflakeIdGenerator.class),
                objectMapper);
    }

    @Test
    void currentStaffUsesDashboardFieldNamesAndPermissionArray() throws Exception {
        StaffPo staff = staff();
        RolePo role = new RolePo();
        role.setId(99L);
        role.setExtCorpId("ww-corp");
        role.setName("超级管理员");
        role.setType("superAdmin");
        role.setPermissionIds("[\"BizRole_Read\",\"BizRole_Full\"]");
        when(roleMapper.selectById(99L)).thenReturn(role);

        CurrentStaffResponse response = service.getCurrent(staff);
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.has("extStaffId")).isFalse();
        assertThat(json.get("ext_staff_id").asText()).isEqualTo("admin");
        assertThat(json.get("ext_corp_id").asText()).isEqualTo("ww-corp");
        assertThat(json.get("dept_ids").isArray()).isTrue();
        assertThat(json.get("role").get("name").asText()).isEqualTo("超级管理员");
        assertThat(json.get("role").get("permission_ids").isArray()).isTrue();
        assertThat(json.get("role").get("permission_ids").get(1).asText()).isEqualTo("BizRole_Full");
    }

    @Test
    void mainInfoReturnsItemsUsingCompactStaffContract() {
        StaffPo staff = staff();
        Page<StaffPo> dbPage = Page.of(1, 20);
        dbPage.setRecords(Collections.singletonList(staff));
        dbPage.setTotal(1);
        when(staffMapper.selectPage(any(), any())).thenReturn(dbPage);
        when(staffDepartmentMapper.selectList(any())).thenReturn(Collections.emptyList());

        PageResponse<SimpleStaffResponse> response = service.queryMainInfo(
                "ww-corp", null, null, 1, 20);
        JsonNode json = objectMapper.valueToTree(response);

        assertThat(response.getItems()).hasSize(1);
        assertThat(json.get("items").get(0).get("ext_id").asText()).isEqualTo("admin");
        assertThat(json.get("items").get(0).get("role_type").asText()).isEqualTo("superAdmin");
        assertThat(json.get("items").get(0).get("departments").isArray()).isTrue();
    }

    private StaffPo staff() {
        StaffPo staff = new StaffPo();
        staff.setId(1L);
        staff.setExtCorpId("ww-corp");
        staff.setExtId("admin");
        staff.setRoleId(99L);
        staff.setRoleType("superAdmin");
        staff.setName("本地管理员");
        staff.setAvatarUrl("https://example.test/avatar.png");
        staff.setDeptIds("[1]");
        return staff;
    }
}
