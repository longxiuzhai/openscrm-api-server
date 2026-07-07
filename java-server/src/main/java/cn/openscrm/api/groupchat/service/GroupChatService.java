package cn.openscrm.api.groupchat.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.groupchat.dto.GroupChatMainInfo;
import cn.openscrm.api.groupchat.dto.GroupChatResponse;
import cn.openscrm.api.groupchat.dto.GroupChatTagUpdateRequest;
import cn.openscrm.api.persistence.entity.GroupChatMemberPo;
import cn.openscrm.api.persistence.entity.GroupChatPo;
import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import cn.openscrm.api.persistence.entity.StaffPo;
import cn.openscrm.api.persistence.mapper.GroupChatMemberPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatTagRelationMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import cn.openscrm.api.wework.GroupChatListRequest;
import cn.openscrm.api.wework.GroupChatListResponse;
import cn.openscrm.api.wework.WeWorkClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GroupChatPoMapper groupChatMapper;
    private final GroupChatMemberPoMapper groupChatMemberMapper;
    private final GroupChatTagRelationMapper groupChatTagRelationMapper;
    private final StaffPoMapper staffMapper;
    private final GroupChatSyncService groupChatSyncService;
    private final WeWorkClient weWorkClient;
    private final OpenScrmProperties properties;

    public PageResponse<GroupChatResponse> query(String extCorpId,
                                                 List<String> owners,
                                                 String name,
                                                 Integer status,
                                                 LocalDate createTimeStart,
                                                 LocalDate createTimeEnd,
                                                 List<Long> groupTagIds,
                                                 String tagsUnionType,
                                                 long page,
                                                 long pageSize) {
        List<Long> tagFilteredIds = findChatIdsByTags(groupTagIds, tagsUnionType);
        if (!CollectionUtils.isEmpty(groupTagIds) && tagFilteredIds.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
        }
        LambdaQueryWrapper<GroupChatPo> wrapper = baseQuery(extCorpId)
                .in(!CollectionUtils.isEmpty(owners), GroupChatPo::getOwner, owners)
                .likeRight(StringUtils.hasText(name), GroupChatPo::getName, name)
                .eq(status != null, GroupChatPo::getStatus, status)
                .in(!tagFilteredIds.isEmpty(), GroupChatPo::getId, tagFilteredIds)
                .ge(createTimeStart != null, GroupChatPo::getCreateTime,
                        createTimeStart == null ? null : createTimeStart.atStartOfDay())
                .lt(createTimeEnd != null, GroupChatPo::getCreateTime,
                        createTimeEnd == null ? null : createTimeEnd.plusDays(1).atStartOfDay())
                .orderByDesc(GroupChatPo::getCreatedAt);
        IPage<GroupChatPo> result = groupChatMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<GroupChatResponse> items = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(items, result.getTotal(), page, pageSize);
    }

    public byte[] exportXlsx(String extCorpId,
                             List<String> owners,
                             String name,
                             Integer status,
                             LocalDate createTimeStart,
                             LocalDate createTimeEnd,
                             List<Long> groupTagIds,
                             String tagsUnionType) {
        List<GroupChatResponse> items = query(extCorpId, owners, name, status, createTimeStart, createTimeEnd,
                groupTagIds, tagsUnionType, 1, 100000).getItems();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("客户群列表");
            String[] titles = {"客户群名称", "群主", "群标签", "群人数", "当日入群", "当日退群", "创群时间", "群ID"};
            writeExportHeader(workbook, sheet, titles);
            int rowIndex = 2;
            for (GroupChatResponse item : items) {
                GroupChatPo chat = item.getGroupChat();
                Row row = sheet.createRow(rowIndex++);
                setCell(row, 0, chat.getName());
                setCell(row, 1, chat.getOwnerName());
                setCell(row, 2, item.getTags().stream().map(GroupChatTagPo::getName).collect(Collectors.joining("|")));
                setCell(row, 3, defaultInt(chat.getTotal()));
                setCell(row, 4, defaultInt(chat.getTodayJoinMemberNum()));
                setCell(row, 5, defaultInt(chat.getTodayQuitMemberNum()));
                setCell(row, 6, chat.getCreateTime() == null ? "" : chat.getCreateTime().format(EXPORT_TIME_FORMATTER));
                setCell(row, 7, chat.getExtChatId());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "导出客户群失败");
        }
    }

    public GroupChatResponse get(String extCorpId, String extChatId) {
        GroupChatPo chat = groupChatMapper.selectOne(baseQuery(extCorpId)
                .eq(GroupChatPo::getExtChatId, extChatId)
                .last("limit 1"));
        if (chat == null) {
            throw new BizException(ErrorCode.ITEM_NOT_FOUND);
        }
        return toResponse(chat);
    }

    public PageResponse<GroupChatMainInfo> getAll(String extCorpId, long page, long pageSize) {
        IPage<GroupChatPo> result = groupChatMapper.selectPage(new Page<>(page, pageSize),
                baseQuery(extCorpId).orderByDesc(GroupChatPo::getCreatedAt));
        List<GroupChatMainInfo> items = result.getRecords().stream()
                .map(chat -> new GroupChatMainInfo(chat.getExtChatId(), chat.getName(), chat.getOwnerName()))
                .collect(Collectors.toList());
        return new PageResponse<>(items, result.getTotal(), page, pageSize);
    }

    public List<StaffPo> getAllOwners(String extCorpId) {
        List<GroupChatPo> chats = groupChatMapper.selectList(baseQuery(extCorpId));
        Set<String> ownerIds = new LinkedHashSet<>();
        for (GroupChatPo chat : chats) {
            if (StringUtils.hasText(chat.getOwner())) {
                ownerIds.add(chat.getOwner());
            }
        }
        if (ownerIds.isEmpty()) {
            return Collections.emptyList();
        }
        return staffMapper.selectList(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .in(StaffPo::getExtId, ownerIds)
                .isNull(StaffPo::getDeletedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTags(GroupChatTagUpdateRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getGroupChatIds())) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        for (String groupChatId : request.getGroupChatIds()) {
            Long id = parseLong(groupChatId);
            if (id == null) {
                continue;
            }
            if (!CollectionUtils.isEmpty(request.getAddTagIds())) {
                for (Long tagId : request.getAddTagIds()) {
                    groupChatTagRelationMapper.addTag(id, tagId);
                }
            }
            if (!CollectionUtils.isEmpty(request.getRemoveTagIds())) {
                groupChatTagRelationMapper.removeTags(id, request.getRemoveTagIds());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncOne(String extCorpId, String extChatId) {
        if (!StringUtils.hasText(extChatId)) {
            throw new BizException(ErrorCode.BAD_REQUEST);
        }
        groupChatSyncService.syncOne(extCorpId, extChatId, null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncAll(String extCorpId) {
        List<StaffPo> staffs = staffMapper.selectList(new LambdaQueryWrapper<StaffPo>()
                .eq(StaffPo::getExtCorpId, extCorpId)
                .isNull(StaffPo::getDeletedAt));
        for (StaffPo staff : staffs) {
            if (!StringUtils.hasText(staff.getExtId())) {
                continue;
            }
            syncByOwner(extCorpId, staff.getExtId());
        }
    }

    private void syncByOwner(String extCorpId, String extStaffId) {
        String cursor = null;
        do {
            GroupChatListRequest request = new GroupChatListRequest();
            request.setLimit(100);
            request.setCursor(cursor);
            GroupChatListRequest.OwnerFilter ownerFilter = new GroupChatListRequest.OwnerFilter();
            ownerFilter.setUserIdList(Collections.singletonList(extStaffId));
            request.setOwnerFilter(ownerFilter);
            GroupChatListResponse response = weWorkClient.listGroupChats(
                    extCorpId, properties.getWeWork().getCustomerSecret(), request);
            if (response.getGroupChatList() != null) {
                for (GroupChatListResponse.Item item : response.getGroupChatList()) {
                    if (StringUtils.hasText(item.getChatId())) {
                        groupChatSyncService.syncOne(extCorpId, item.getChatId(), null, null);
                    }
                }
            }
            cursor = response.getNextCursor();
        } while (StringUtils.hasText(cursor));
    }

    private GroupChatResponse toResponse(GroupChatPo chat) {
        GroupChatResponse response = new GroupChatResponse();
        response.setGroupChat(chat);
        response.setMemberList(groupChatMemberMapper.selectList(new LambdaQueryWrapper<GroupChatMemberPo>()
                .eq(GroupChatMemberPo::getExtCorpId, chat.getExtCorpId())
                .eq(GroupChatMemberPo::getExtChatId, chat.getExtChatId())));
        List<GroupChatTagPo> tags = groupChatTagRelationMapper.selectTagsByGroupChatId(chat.getId());
        response.setTags(tags == null ? Collections.emptyList() : tags);
        return response;
    }

    private LambdaQueryWrapper<GroupChatPo> baseQuery(String extCorpId) {
        return new LambdaQueryWrapper<GroupChatPo>()
                .eq(GroupChatPo::getExtCorpId, extCorpId)
                .isNull(GroupChatPo::getDeletedAt);
    }

    private List<Long> findChatIdsByTags(List<Long> tagIds, String unionType) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        if ("and".equalsIgnoreCase(unionType)) {
            return groupChatTagRelationMapper.selectChatIdsByAllTags(tagIds, tagIds.size());
        }
        return groupChatTagRelationMapper.selectChatIdsByAnyTag(tagIds);
    }

    public List<String> split(String csv) {
        if (!StringUtils.hasText(csv)) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    public List<Long> splitLong(String csv) {
        if (!StringUtils.hasText(csv)) {
            return Collections.emptyList();
        }
        List<Long> values = new ArrayList<>();
        for (String item : csv.split(",")) {
            Long value = parseLong(item.trim());
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private Long parseLong(String value) {
        try {
            return StringUtils.hasText(value) ? Long.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void writeExportHeader(XSSFWorkbook workbook, Sheet sheet, String[] titles) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, titles.length - 1));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("客户群列表(导出时间: " + LocalDateTime.now().format(EXPORT_TIME_FORMATTER) + ")");
        titleCell.setCellStyle(centerStyle(workbook, true));
        Row headerRow = sheet.createRow(1);
        CellStyle headerStyle = centerStyle(workbook, true);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, i == 7 ? 24 * 256 : 18 * 256);
        }
    }

    private CellStyle centerStyle(XSSFWorkbook workbook, boolean bold) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(bold);
        style.setFont(font);
        return style;
    }

    private void setCell(Row row, int index, String value) {
        row.createCell(index).setCellValue(value == null ? "" : value);
    }

    private void setCell(Row row, int index, int value) {
        row.createCell(index).setCellValue(value);
    }
}
