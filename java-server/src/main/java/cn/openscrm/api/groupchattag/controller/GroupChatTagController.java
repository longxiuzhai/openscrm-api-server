package cn.openscrm.api.groupchattag.controller;

import cn.openscrm.api.auth.annotation.RequirePermission;
import cn.openscrm.api.auth.service.CurrentStaffService;
import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.common.constant.BizIdentity;
import cn.openscrm.api.common.constant.OperationType;
import cn.openscrm.api.groupchattag.dto.CommonIdsRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagCreateRequest;
import cn.openscrm.api.groupchattag.dto.GroupChatTagUpdateRequest;
import cn.openscrm.api.groupchattag.service.GroupChatTagService;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/staff-admin", "/api/v1/staff_admin"})
public class GroupChatTagController {

    private final CurrentStaffService currentStaffService;
    private final GroupChatTagService tagService;

    @PostMapping({"/group-chat/tag", "/customer-group/tag"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<List<GroupChatTagPo>> create(@Valid @RequestBody GroupChatTagCreateRequest request,
                                                    HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagService.create(request, current.getExtCorpId(), current.getExtId()));
    }

    @PutMapping({"/group-chat/tag", "/customer-group/tag"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<GroupChatTagPo> update(@Valid @RequestBody GroupChatTagUpdateRequest request,
                                              HttpSession session) {
        StaffPo current = currentStaffService.requireStaffAdmin(session);
        return ApiResponse.ok(tagService.update(request, current.getExtCorpId()));
    }

    @PostMapping({"/group-chat/tag/action/delete", "/customer-group/tag/action/delete"})
    @RequirePermission(biz = BizIdentity.BIZ_CUSTOMER_GROUP_CHAT, operation = OperationType.FULL)
    public ApiResponse<Long> delete(@Valid @RequestBody CommonIdsRequest request) {
        return ApiResponse.ok(tagService.delete(request));
    }
}
