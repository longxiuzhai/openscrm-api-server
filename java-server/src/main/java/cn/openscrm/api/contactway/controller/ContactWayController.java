package cn.openscrm.api.contactway.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.contactway.dto.ContactWayBatchUpdateRequest;
import cn.openscrm.api.contactway.dto.ContactWayDeleteRequest;
import cn.openscrm.api.contactway.dto.ContactWayRequest;
import cn.openscrm.api.contactway.dto.ContactWayResponse;
import cn.openscrm.api.contactway.service.ContactWayService;
import cn.openscrm.api.persistence.entity.ContactWayPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ContactWayController {

    private final ContactWayService contactWayService;
    private final CurrentStaffService currentStaffService;

    @GetMapping({
            "/staff_admin/contact_ways", "/staff-admin/contact_ways",
            "/staff_admin/contact-ways", "/staff-admin/contact-ways"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.READ)
    public ApiResponse<PageResponse<ContactWayResponse>> query(
            @RequestParam(required = false) Long id,
            @RequestParam(value = "ext_staff_ids", required = false) List<String> extStaffIds,
            @RequestParam(required = false) String name,
            @RequestParam(value = "config_id", required = false) String configId,
            @RequestParam(value = "group_id", required = false) Long groupId,
            @RequestParam(value = "created_at_start", required = false) String createdAtStart,
            @RequestParam(value = "created_at_end", required = false) String createdAtEnd,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long pageSize,
            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayService.query(
                current.getExtCorpId(),
                id,
                normalize(extStaffIds),
                name,
                configId,
                groupId,
                parseStart(createdAtStart),
                parseEnd(createdAtEnd),
                page,
                pageSize));
    }

    @GetMapping({
            "/staff_admin/contact_way/{id}", "/staff-admin/contact_way/{id}",
            "/staff_admin/contact-way/{id}", "/staff-admin/contact-way/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.READ)
    public ApiResponse<ContactWayResponse> get(@PathVariable Long id, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayService.get(id, current.getExtCorpId()));
    }

    @PostMapping({
            "/staff_admin/contact_way", "/staff-admin/contact_way",
            "/staff_admin/contact-way", "/staff-admin/contact-way"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.FULL)
    public ApiResponse<ContactWayPo> create(@RequestBody ContactWayRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PutMapping({
            "/staff_admin/contact_way/{id}", "/staff-admin/contact_way/{id}",
            "/staff_admin/contact-way/{id}", "/staff-admin/contact-way/{id}"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.FULL)
    public ApiResponse<ContactWayPo> update(@PathVariable Long id,
                                            @RequestBody ContactWayRequest request,
                                            HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayService.update(id, request, current.getExtCorpId()));
    }

    @PostMapping({
            "/staff_admin/contact_way/action/delete", "/staff-admin/contact_way/action/delete",
            "/staff_admin/contact-way/action/delete", "/staff-admin/contact-way/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@RequestBody ContactWayDeleteRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayService.delete(request, current.getExtCorpId()));
    }

    @PostMapping({
            "/staff_admin/contact_way/action/batch-update", "/staff-admin/contact_way/action/batch-update",
            "/staff_admin/contact-way/action/batch-update", "/staff-admin/contact-way/action/batch-update"})
    @RequirePermission(biz = BizIdentity.BIZ_CONTACT_WAY, operation = OperationType.FULL)
    public ApiResponse<Long> batchUpdate(@RequestBody ContactWayBatchUpdateRequest request, HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(contactWayService.batchUpdate(request, current.getExtCorpId()));
    }

    private List<String> normalize(List<String> raw) {
        List<String> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        for (String item : raw) {
            if (!StringUtils.hasText(item)) {
                continue;
            }
            String[] parts = item.split(",");
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    result.add(part.trim());
                }
            }
        }
        return result;
    }

    private LocalDateTime parseStart(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value).atStartOfDay();
    }

    private LocalDateTime parseEnd(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value).plusDays(1).atStartOfDay();
    }
}
