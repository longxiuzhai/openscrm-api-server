package cn.openscrm.api.welcome.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.constant.BooleanFlag;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.persistence.entity.DepartmentPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.entity.WelcomeMsgPo;
import cn.openscrm.api.persistence.mapper.DepartmentPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.persistence.mapper.WelcomeMsgPoMapper;
import cn.openscrm.api.wework.WeWorkClient;
import cn.openscrm.api.welcome.dto.CommonDeleteRequest;
import cn.openscrm.api.welcome.dto.DepartmentMainInfo;
import cn.openscrm.api.welcome.dto.StaffMainInfo;
import cn.openscrm.api.welcome.dto.TimePeriodWelcomeMsgRequest;
import cn.openscrm.api.welcome.dto.WelcomeMsgRequest;
import cn.openscrm.api.welcome.dto.WelcomeMsgResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class WelcomeMsgService {

    private final WelcomeMsgPoMapper welcomeMsgMapper;
    private final DepartmentPoMapper departmentMapper;
    private final StaffPoMapper staffMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final WeWorkClient weWorkClient;
    private final RestTemplate restTemplate;
    private final OpenScrmProperties properties;

    public WelcomeMsgService(WelcomeMsgPoMapper welcomeMsgMapper,
                             DepartmentPoMapper departmentMapper,
                             StaffPoMapper staffMapper,
                             SnowflakeIdGenerator idGenerator,
                             ObjectMapper objectMapper,
                             WeWorkClient weWorkClient,
                             RestTemplate restTemplate,
                             OpenScrmProperties properties) {
        this.welcomeMsgMapper = welcomeMsgMapper;
        this.departmentMapper = departmentMapper;
        this.staffMapper = staffMapper;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
        this.weWorkClient = weWorkClient;
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Transactional(rollbackFor = Exception.class)
    public WelcomeMsgPo create(WelcomeMsgRequest request, String extCorpId, String extCreatorId) {
        if (!StringUtils.hasText(request.getName()) || request.getWelcomeMsg() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        Long id = idGenerator.nextId();
        WelcomeMsgPo item = new WelcomeMsgPo();
        item.setId(id);
        item.setExtCorpId(extCorpId);
        item.setExtCreatorId(extCreatorId);
        item.setName(request.getName());
        item.setWelcomeMsg(toJson(createImageWxUrl(request.getWelcomeMsg(), extCorpId)));
        item.setEnableTimePeriodMsg(request.getEnableTimePeriodMsg() == null
                ? BooleanFlag.FALSE : request.getEnableTimePeriodMsg());
        welcomeMsgMapper.insert(item);
        saveTimePeriodMsgs(id, extCorpId, extCreatorId, request.getTimePeriodMsg());

        List<Integer> deptIds = request.getExtDepartmentIds();
        if (CollectionUtils.isEmpty(deptIds) && CollectionUtils.isEmpty(request.getExtStaffIds())) {
            deptIds = Collections.singletonList(1);
        }
        updateDepartments(extCorpId, id, deptIds, false);
        updateStaffs(extCorpId, id, request.getExtStaffIds(), false);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public WelcomeMsgPo update(Long id, WelcomeMsgRequest request, String extCorpId) {
        WelcomeMsgPo item = getRaw(id, extCorpId);
        if (!StringUtils.hasText(request.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        item.setName(request.getName());
        if (request.getWelcomeMsg() != null) {
            item.setWelcomeMsg(toJson(createImageWxUrl(request.getWelcomeMsg(), extCorpId)));
        }
        if (request.getEnableTimePeriodMsg() != null) {
            item.setEnableTimePeriodMsg(request.getEnableTimePeriodMsg());
        }
        welcomeMsgMapper.updateById(item);

        if (item.getEnableTimePeriodMsg() != null && item.getEnableTimePeriodMsg() == BooleanFlag.FALSE) {
            deleteTimePeriodMsgs(id);
        }
        if (request.getTimePeriodMsg() != null) {
            deleteTimePeriodMsgs(id);
            saveTimePeriodMsgs(id, extCorpId, item.getExtCreatorId(), request.getTimePeriodMsg());
        }
        if (request.getExtDepartmentIds() != null) {
            clearDepartments(extCorpId, id);
            updateDepartments(extCorpId, id, request.getExtDepartmentIds(), false);
        }
        if (request.getExtStaffIds() != null) {
            clearStaffs(id);
            updateStaffs(extCorpId, id, request.getExtStaffIds(), false);
        }
        return item;
    }

    public PageResponse<WelcomeMsgResponse> query(String extCorpId,
                                                  String name,
                                                  String extStaffIds,
                                                  long page,
                                                  long pageSize) {
        List<WelcomeMsgPo> records;
        long total;
        if (StringUtils.hasText(extStaffIds)) {
            List<String> staffIds = splitExtStaffIds(extStaffIds);
            List<Long> msgIds = findMsgIdsByStaffs(extCorpId, staffIds);
            if (msgIds.isEmpty()) {
                return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
            }
            records = welcomeMsgMapper.selectList(new LambdaQueryWrapper<WelcomeMsgPo>()
                    .in(WelcomeMsgPo::getId, msgIds)
                    .eq(WelcomeMsgPo::getExtCorpId, extCorpId)
                    .isNull(WelcomeMsgPo::getMainWelcomeMsgId));
            total = records.size();
        } else {
            IPage<WelcomeMsgPo> result = welcomeMsgMapper.selectPage(new Page<>(page, pageSize),
                    new LambdaQueryWrapper<WelcomeMsgPo>()
                            .eq(WelcomeMsgPo::getExtCorpId, extCorpId)
                            .isNull(WelcomeMsgPo::getMainWelcomeMsgId)
                            .likeRight(StringUtils.hasText(name), WelcomeMsgPo::getName, name)
                            .orderByDesc(WelcomeMsgPo::getCreatedAt));
            records = result.getRecords();
            total = result.getTotal();
        }
        List<WelcomeMsgResponse> responses = new ArrayList<>();
        for (WelcomeMsgPo record : records) {
            responses.add(toResponse(record));
        }
        return new PageResponse<>(responses, total, page, pageSize);
    }

    public WelcomeMsgResponse get(Long id, String extCorpId) {
        return toResponse(getRaw(id, extCorpId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(CommonDeleteRequest request, String extCorpId) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        welcomeMsgMapper.delete(new LambdaQueryWrapper<WelcomeMsgPo>()
                .eq(WelcomeMsgPo::getExtCorpId, extCorpId)
                .in(WelcomeMsgPo::getId, request.getIds()));
    }

    private WelcomeMsgPo getRaw(Long id, String extCorpId) {
        WelcomeMsgPo item = welcomeMsgMapper.selectOne(new LambdaQueryWrapper<WelcomeMsgPo>()
                .eq(WelcomeMsgPo::getId, id)
                .eq(WelcomeMsgPo::getExtCorpId, extCorpId)
                .last("limit 1"));
        if (item == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return item;
    }

    private WelcomeMsgResponse toResponse(WelcomeMsgPo item) {
        WelcomeMsgResponse response = new WelcomeMsgResponse();
        response.setWelcomeMsg(item);
        response.setTimePeriodMsg(welcomeMsgMapper.selectList(new LambdaQueryWrapper<WelcomeMsgPo>()
                .eq(WelcomeMsgPo::getMainWelcomeMsgId, item.getId())
                .orderByAsc(WelcomeMsgPo::getStartTime)));
        response.setDepartment(departmentMapper.selectList(new LambdaQueryWrapper<DepartmentPo>()
                        .eq(DepartmentPo::getWelcomeMsgId, item.getId()))
                .stream()
                .map(dept -> new DepartmentMainInfo(dept.getId(), dept.getExtId(), dept.getName()))
                .collect(Collectors.toList()));
        response.setStaffs(staffMapper.selectList(new LambdaQueryWrapper<StaffPo>()
                        .eq(StaffPo::getWelcomeMsgId, item.getId()))
                .stream()
                .map(staff -> new StaffMainInfo(staff.getId(), staff.getAvatarUrl(), staff.getExtId(), staff.getName()))
                .collect(Collectors.toList()));
        return response;
    }

    private void saveTimePeriodMsgs(Long mainId,
                                    String extCorpId,
                                    String extCreatorId,
                                    List<TimePeriodWelcomeMsgRequest> timePeriodMsg) {
        if (CollectionUtils.isEmpty(timePeriodMsg)) {
            return;
        }
        for (TimePeriodWelcomeMsgRequest request : timePeriodMsg) {
            WelcomeMsgPo item = new WelcomeMsgPo();
            item.setId(request.getId() == null ? idGenerator.nextId() : request.getId());
            item.setExtCorpId(extCorpId);
            item.setExtCreatorId(extCreatorId);
            item.setWelcomeMsg(toJson(createImageWxUrl(request.getAttachments(), extCorpId)));
            item.setMainWelcomeMsgId(mainId);
            item.setEffectiveAt(toJson(request.getEffectiveAt()));
            item.setStartTime(request.getStartTime());
            item.setEndTime(request.getEndTime());
            welcomeMsgMapper.insert(item);
        }
    }

    private void deleteTimePeriodMsgs(Long mainId) {
        welcomeMsgMapper.delete(new LambdaQueryWrapper<WelcomeMsgPo>()
                .eq(WelcomeMsgPo::getMainWelcomeMsgId, mainId));
    }

    private void updateDepartments(String extCorpId, Long welcomeMsgId, List<Integer> extDeptIds, boolean clearFirst) {
        if (clearFirst) {
            clearDepartments(extCorpId, welcomeMsgId);
        }
        if (CollectionUtils.isEmpty(extDeptIds)) {
            return;
        }
        departmentMapper.update(null, new LambdaUpdateWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .in(DepartmentPo::getExtId, extDeptIds)
                .set(DepartmentPo::getWelcomeMsgId, welcomeMsgId));
    }

    private void updateStaffs(String extCorpId, Long welcomeMsgId, List<String> extStaffIds, boolean clearFirst) {
        if (clearFirst) {
            clearStaffs(welcomeMsgId);
        }
        if (CollectionUtils.isEmpty(extStaffIds)) {
            return;
        }
        staffMapper.update(null, new LambdaUpdateWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .in(StaffPo::getExtId, extStaffIds)
                .set(StaffPo::getWelcomeMsgId, welcomeMsgId));
    }

    private void clearDepartments(String extCorpId, Long welcomeMsgId) {
        departmentMapper.update(null, new LambdaUpdateWrapper<DepartmentPo>()
                .eq(DepartmentPo::getExtCorpId, extCorpId)
                .eq(DepartmentPo::getWelcomeMsgId, welcomeMsgId)
                .set(DepartmentPo::getWelcomeMsgId, null));
    }

    private void clearStaffs(Long welcomeMsgId) {
        staffMapper.update(null, new LambdaUpdateWrapper<StaffPo>()
                .eq(StaffPo::getWelcomeMsgId, welcomeMsgId)
                .set(StaffPo::getWelcomeMsgId, null));
    }

    private List<Long> findMsgIdsByStaffs(String extCorpId, List<String> extStaffIds) {
        List<StaffPo> staffs = staffMapper.selectList(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .in(StaffPo::getExtId, extStaffIds));
        List<Long> ids = new ArrayList<>();
        for (StaffPo staff : staffs) {
            if (staff.getWelcomeMsgId() != null) {
                ids.add(staff.getWelcomeMsgId());
            }
        }
        return ids;
    }

    private List<String> splitExtStaffIds(String extStaffIds) {
        String[] parts = extStaffIds.split(",");
        List<String> ids = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                ids.add(part.trim());
            }
        }
        return ids;
    }

    public String uploadImage(InputStream body, String filename, String extCorpId) {
        if (!StringUtils.hasText(filename)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        byte[] data;
        try {
            data = StreamUtils.copyToByteArray(body);
        } catch (IOException e) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        return uploadImage(data, filename, extCorpId);
    }

    public String uploadImage(byte[] data, String filename, String extCorpId) {
        if (data == null || data.length == 0) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        if (!isImage(data)) {
            throw new BizException(ErrorCode.NOT_IMAGE_FILE);
        }
        return weWorkClient.uploadPermanentImage(
                extCorpId,
                properties.getWeWork().getCustomerSecret(),
                filename,
                data);
    }

    private JsonNode createImageWxUrl(JsonNode welcomeMsg, String extCorpId) {
        if (welcomeMsg == null || !welcomeMsg.isObject()) {
            return welcomeMsg;
        }
        ObjectNode copy = welcomeMsg.deepCopy();
        JsonNode attachments = copy.get("attachments");
        if (attachments == null || !attachments.isArray()) {
            return copy;
        }
        for (JsonNode attachment : attachments) {
            if (!attachment.isObject()) {
                continue;
            }
            JsonNode msgType = attachment.get("msgtype");
            if (msgType == null || !"image".equals(msgType.asText())) {
                continue;
            }
            JsonNode image = attachment.get("image");
            if (image == null || !image.isObject()) {
                continue;
            }
            JsonNode picUrl = image.get("pic_url");
            if (picUrl == null || !StringUtils.hasText(picUrl.asText())) {
                continue;
            }
            String url = picUrl.asText();
            byte[] data = download(url);
            String wxUrl = uploadImage(data, filenameFromUrl(url), extCorpId);
            ((ObjectNode) image).put("pic_url", wxUrl);
        }
        return copy;
    }

    private byte[] download(String url) {
        try {
            byte[] data = restTemplate.getForObject(url, byte[].class);
            if (data == null || data.length == 0) {
                throw new BizException(ErrorCode.BAD_REQUEST);
            }
            return data;
        } catch (RuntimeException e) {
            if (e instanceof BizException) {
                throw e;
            }
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
    }

    private boolean isImage(byte[] data) {
        try {
            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
            return contentType != null && contentType.toLowerCase(java.util.Locale.ENGLISH).startsWith("image/");
        } catch (IOException e) {
            return false;
        }
    }

    private String filenameFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            if (StringUtils.hasText(path)) {
                String name = path.substring(path.lastIndexOf('/') + 1);
                if (StringUtils.hasText(name)) {
                    return name;
                }
            }
        } catch (IllegalArgumentException ignored) {
            return "welcome-image";
        }
        return "welcome-image-" + Integer.toHexString(new String(url.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).hashCode());
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JsonNode) {
            return value.toString();
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
