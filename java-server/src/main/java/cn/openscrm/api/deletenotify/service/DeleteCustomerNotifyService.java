package cn.openscrm.api.deletenotify.service;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.deletenotify.dto.DeleteCustomerNotifyRuleRequest;
import cn.openscrm.api.deletenotify.dto.StaffDeleteCustomerResponse;
import cn.openscrm.api.persistence.entity.EventNotifyPo;
import cn.openscrm.api.persistence.mapper.EventNotifyPoMapper;
import cn.openscrm.api.persistence.mapper.StaffDeleteCustomerQueryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import org.springframework.util.StringUtils;

@Service
public class DeleteCustomerNotifyService {

    private static final int NOTIFY_ON = 1;
    private static final int NOTIFY_OFF = 2;
    private static final int NOTIFY_REAL_TIME = 1;
    private static final String STAFF_DELETE_CUSTOMER_EVENT = "staff_delete_customer";
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventNotifyPoMapper eventNotifyMapper;
    private final StaffDeleteCustomerQueryMapper queryMapper;
    private final ObjectMapper objectMapper;

    public DeleteCustomerNotifyService(EventNotifyPoMapper eventNotifyMapper,
                                       StaffDeleteCustomerQueryMapper queryMapper,
                                       ObjectMapper objectMapper) {
        this.eventNotifyMapper = eventNotifyMapper;
        this.queryMapper = queryMapper;
        this.objectMapper = objectMapper;
    }

    public EventNotifyPo getRule(String extCorpId) {
        EventNotifyPo rule = eventNotifyMapper.selectOne(new QueryWrapper<EventNotifyPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .last("limit 1"));
        return rule == null ? defaultRule(extCorpId) : normalize(rule);
    }

    public void upsertRule(String extCorpId, DeleteCustomerNotifyRuleRequest request) {
        EventNotifyPo existing = eventNotifyMapper.selectOne(new QueryWrapper<EventNotifyPo>()
                .eq("ext_corp_id", extCorpId)
                .isNull("deleted_at")
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        Integer notifyStatus = normalizeStatus(request.getIsNotifyStaff());
        Integer notifyType = normalizeNotifyType(request.getNotifyType());
        String extStaffIds = serializeStaffIds(request.getExtStaffIds());
        if (existing == null) {
            EventNotifyPo rule = new EventNotifyPo();
            rule.setExtCorpId(extCorpId);
            rule.setEventName(STAFF_DELETE_CUSTOMER_EVENT);
            rule.setIsNotifyAdmins(notifyStatus);
            rule.setIsNotifyStaff(notifyStatus);
            rule.setNotifyType(notifyType);
            rule.setExtStaffIds(extStaffIds);
            rule.setCreatedAt(now);
            rule.setUpdatedAt(now);
            eventNotifyMapper.insert(rule);
            return;
        }
        eventNotifyMapper.update(null, new UpdateWrapper<EventNotifyPo>()
                .eq("id", existing.getId())
                .set("event_name", STAFF_DELETE_CUSTOMER_EVENT)
                .set("is_notify_admins", notifyStatus)
                .set("is_notify_staff", notifyStatus)
                .set("notify_type", notifyType)
                .set("ext_staff_ids", extStaffIds)
                .set("updated_at", now));
    }

    public PageResponse<StaffDeleteCustomerResponse> queryRecords(String extCorpId,
                                                                  Long extDepartmentId,
                                                                  List<String> extStaffIds,
                                                                  LocalDate connectionCreateStart,
                                                                  LocalDate connectionCreateEnd,
                                                                  LocalDate deleteCustomerStart,
                                                                  LocalDate deleteCustomerEnd,
                                                                  String sortField,
                                                                  String sortType,
                                                                  long page,
                                                                  long pageSize) {
        long total = queryMapper.countStaffDeleteCustomers(
                extCorpId, extDepartmentId, extStaffIds, connectionCreateStart, connectionCreateEnd,
                deleteCustomerStart, deleteCustomerEnd);
        if (total == 0) {
            return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
        }
        List<StaffDeleteCustomerResponse> items = queryMapper.selectStaffDeleteCustomers(
                extCorpId, extDepartmentId, extStaffIds, connectionCreateStart, connectionCreateEnd,
                deleteCustomerStart, deleteCustomerEnd, orderBy(sortField, sortType), pageSize, (page - 1) * pageSize);
        return new PageResponse<>(items, total, page, pageSize);
    }

    public byte[] exportXlsx(String extCorpId,
                             Long extDepartmentId,
                             List<String> extStaffIds,
                             LocalDate connectionCreateStart,
                             LocalDate connectionCreateEnd,
                             LocalDate deleteCustomerStart,
                             LocalDate deleteCustomerEnd,
                             String sortField,
                             String sortType) {
        List<StaffDeleteCustomerResponse> items = queryRecords(
                extCorpId, extDepartmentId, extStaffIds, connectionCreateStart, connectionCreateEnd,
                deleteCustomerStart, deleteCustomerEnd, sortField, sortType, 1, 100000).getItems();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("删人提醒列表");
            String[] titles = {"删除客户", "操作人", "删除时间", "添加好友时间", "unionid"};
            writeExportHeader(workbook, sheet, titles);
            int rowIndex = 2;
            for (StaffDeleteCustomerResponse item : items) {
                Row row = sheet.createRow(rowIndex++);
                setCell(row, 0, item.getExtCustomerName());
                setCell(row, 1, item.getStaffName());
                setCell(row, 2, formatTime(item.getRelationDeleteAt()));
                setCell(row, 3, formatTime(item.getRelationCreateAt()));
                setCell(row, 4, "");
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "导出删人提醒列表失败");
        }
    }

    public List<String> parseExtStaffIds(EventNotifyPo rule) {
        if (rule == null || !StringUtils.hasText(rule.getExtStaffIds())) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(rule.getExtStaffIds(), new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private EventNotifyPo defaultRule(String extCorpId) {
        EventNotifyPo rule = new EventNotifyPo();
        rule.setExtCorpId(extCorpId);
        rule.setEventName(STAFF_DELETE_CUSTOMER_EVENT);
        rule.setIsNotifyAdmins(NOTIFY_OFF);
        rule.setIsNotifyStaff(NOTIFY_OFF);
        rule.setNotifyType(NOTIFY_REAL_TIME);
        rule.setExtStaffIds("[]");
        return rule;
    }

    private EventNotifyPo normalize(EventNotifyPo rule) {
        rule.setIsNotifyAdmins(NOTIFY_ON == normalizeStatus(rule.getIsNotifyAdmins()) ? NOTIFY_ON : NOTIFY_OFF);
        rule.setIsNotifyStaff(NOTIFY_ON == normalizeStatus(rule.getIsNotifyStaff()) ? NOTIFY_ON : NOTIFY_OFF);
        rule.setNotifyType(normalizeNotifyType(rule.getNotifyType()));
        if (!StringUtils.hasText(rule.getExtStaffIds())) {
            rule.setExtStaffIds("[]");
        }
        return rule;
    }

    private Integer normalizeStatus(Integer value) {
        return Integer.valueOf(NOTIFY_ON).equals(value) ? NOTIFY_ON : NOTIFY_OFF;
    }

    private Integer normalizeNotifyType(Integer value) {
        return Integer.valueOf(2).equals(value) ? 2 : NOTIFY_REAL_TIME;
    }

    private String serializeStaffIds(List<String> extStaffIds) {
        try {
            return objectMapper.writeValueAsString(extStaffIds == null ? Collections.emptyList() : extStaffIds);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INVALID_PARAMS, "管理员列表格式错误");
        }
    }

    private String orderBy(String sortField, String sortType) {
        String column = sortColumn(sortField);
        String direction = "asc".equalsIgnoreCase(sortType) ? "asc" : "desc";
        return column + " " + direction;
    }

    private String sortColumn(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return "customer_staff_relation_history.staff_delete_customer_at";
        }
        switch (sortField.toLowerCase(Locale.ROOT)) {
            case "relation_delete_at":
            case "staff_delete_customer_at":
            case "delete_customer_at":
                return "customer_staff_relation_history.staff_delete_customer_at";
            case "relation_create_at":
            case "createtime":
                return "customer_staff_relation_history.createtime";
            case "staff_name":
                return "staff.name";
            case "ext_customer_name":
            case "customer_name":
                return "customer.name";
            default:
                return "customer_staff_relation_history.staff_delete_customer_at";
        }
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(EXPORT_TIME_FORMATTER);
    }

    private void writeExportHeader(XSSFWorkbook workbook, Sheet sheet, String[] titles) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, titles.length - 1));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("删人提醒列表(导出时间: " + LocalDateTime.now().format(EXPORT_TIME_FORMATTER) + ")");
        titleCell.setCellStyle(centerStyle(workbook, true));
        Row headerRow = sheet.createRow(1);
        CellStyle headerStyle = centerStyle(workbook, true);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 20 * 256);
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
}
