package cn.openscrm.api.customerloss.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.customerloss.dto.CustomerLossResponse;
import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import cn.openscrm.api.persistence.mapper.CustomerLossQueryMapper;
import cn.openscrm.api.persistence.mapper.CustomerStaffTagPoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class CustomerLossService {

    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CustomerLossQueryMapper lossQueryMapper;
    private final CustomerStaffTagPoMapper tagMapper;

    public CustomerLossService(CustomerLossQueryMapper lossQueryMapper, CustomerStaffTagPoMapper tagMapper) {
        this.lossQueryMapper = lossQueryMapper;
        this.tagMapper = tagMapper;
    }

    public PageResponse<CustomerLossResponse> query(String extCorpId,
                                                    List<String> extStaffIds,
                                                    LocalDate lossStart,
                                                    LocalDate lossEnd,
                                                    LocalDate connectionCreateStart,
                                                    LocalDate connectionCreateEnd,
                                                    Long timeSpanLowerLimit,
                                                    Long timeSpanUpperLimit,
                                                    String sortField,
                                                    String sortType,
                                                    long page,
                                                    long pageSize) {
        String orderBy = orderBy(sortField, sortType);
        long total = lossQueryMapper.countLosses(
                extCorpId, extStaffIds, lossStart, lossEnd, connectionCreateStart, connectionCreateEnd,
                timeSpanLowerLimit, timeSpanUpperLimit);
        if (total == 0) {
            return new PageResponse<>(Collections.emptyList(), 0, page, pageSize);
        }
        List<CustomerLossResponse> items = lossQueryMapper.selectLosses(
                extCorpId, extStaffIds, lossStart, lossEnd, connectionCreateStart, connectionCreateEnd,
                timeSpanLowerLimit, timeSpanUpperLimit, orderBy, pageSize, (page - 1) * pageSize);
        attachTags(items);
        return new PageResponse<>(items, total, page, pageSize);
    }

    public byte[] exportXlsx(String extCorpId,
                             List<String> extStaffIds,
                             LocalDate lossStart,
                             LocalDate lossEnd,
                             LocalDate connectionCreateStart,
                             LocalDate connectionCreateEnd,
                             Long timeSpanLowerLimit,
                             Long timeSpanUpperLimit,
                             String sortField,
                             String sortType) {
        List<CustomerLossResponse> items = query(
                extCorpId,
                extStaffIds,
                lossStart,
                lossEnd,
                connectionCreateStart,
                connectionCreateEnd,
                timeSpanLowerLimit,
                timeSpanUpperLimit,
                sortField,
                sortType,
                1,
                100000).getItems();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("流失提醒列表");
            String[] titles = {"流失客户", "所属客服", "标签", "流失时间", "添加时间", "添加员工时长/天"};
            writeExportHeader(workbook, sheet, titles);
            int rowIndex = 2;
            for (CustomerLossResponse item : items) {
                Row row = sheet.createRow(rowIndex++);
                setCell(row, 0, item.getExtCustomerName());
                setCell(row, 1, item.getStaffName());
                setCell(row, 2, item.getTags().stream()
                        .map(CustomerStaffTagPo::getTagName)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining(",")));
                setCell(row, 3, formatTime(item.getCustomerDeleteStaffAt()));
                setCell(row, 4, formatTime(item.getRelationCreateAt()));
                setCell(row, 5, item.getInConnectionTimeRange() == null ? "" : String.valueOf(item.getInConnectionTimeRange()));
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "导出流失提醒列表失败");
        }
    }

    private void attachTags(List<CustomerLossResponse> items) {
        List<Long> customerStaffIds = items.stream()
                .map(CustomerLossResponse::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (customerStaffIds.isEmpty()) {
            return;
        }
        List<CustomerStaffTagPo> tags = tagMapper.selectList(new QueryWrapper<CustomerStaffTagPo>()
                .in("customer_staff_id", customerStaffIds)
                .isNull("deleted_at"));
        Map<Long, List<CustomerStaffTagPo>> tagsByRelation = new HashMap<>();
        for (CustomerStaffTagPo tag : tags) {
            tagsByRelation.computeIfAbsent(tag.getCustomerStaffId(), key -> new ArrayList<>()).add(tag);
        }
        for (CustomerLossResponse item : items) {
            item.setTags(tagsByRelation.getOrDefault(item.getId(), Collections.emptyList()));
        }
    }

    private String orderBy(String sortField, String sortType) {
        String column = sortColumn(sortField);
        String direction = "asc".equalsIgnoreCase(sortType) ? "asc" : "desc";
        return column + " " + direction;
    }

    private String sortColumn(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return "customer_staff_relation_history.customer_delete_staff_at";
        }
        switch (sortField.toLowerCase(Locale.ROOT)) {
            case "customer_delete_staff_at":
            case "loss_at":
                return "customer_staff_relation_history.customer_delete_staff_at";
            case "relation_create_at":
            case "createtime":
                return "customer_staff_relation_history.createtime";
            case "staff_name":
                return "s.name";
            case "ext_customer_name":
            case "customer_name":
                return "customer.name";
            case "in_connection_time_range":
                return "inConnectionTimeRange";
            default:
                return "customer_staff_relation_history.customer_delete_staff_at";
        }
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(EXPORT_TIME_FORMATTER);
    }

    private void writeExportHeader(XSSFWorkbook workbook, Sheet sheet, String[] titles) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, titles.length - 1));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("流失提醒列表(导出时间: " + LocalDateTime.now().format(EXPORT_TIME_FORMATTER) + ")");
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
