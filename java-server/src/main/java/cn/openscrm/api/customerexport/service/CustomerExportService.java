package cn.openscrm.api.customerexport.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.customerexport.dto.CustomerExportRow;
import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import cn.openscrm.api.persistence.mapper.CustomerExportQueryMapper;
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
public class CustomerExportService {

    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<Long, String> ADD_WAY_NAMES = addWayNames();

    private final CustomerExportQueryMapper exportQueryMapper;
    private final CustomerStaffTagPoMapper tagMapper;

    public CustomerExportService(CustomerExportQueryMapper exportQueryMapper, CustomerStaffTagPoMapper tagMapper) {
        this.exportQueryMapper = exportQueryMapper;
        this.tagMapper = tagMapper;
    }

    public byte[] exportXlsx(String extCorpId,
                             String name,
                             List<String> extStaffIds,
                             List<String> extTagIds,
                             Integer channelType,
                             Integer gender,
                             Integer type,
                             LocalDate startTime,
                             LocalDate endTime,
                             String sortField,
                             String sortType) {
        List<CustomerExportRow> rows = queryRows(
                extCorpId, name, extStaffIds, extTagIds, channelType, gender, type,
                startTime, endTime, sortField, sortType, 1, 100000);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("客户列表");
            String[] titles = {
                    "客户名称", "客户备注", "客户描述", "流失状态", "企业名称", "添加人", "添加时间",
                    "企业标签", "添加渠道", "性别", "电话", "年龄", "生日"
            };
            writeExportHeader(workbook, sheet, titles);
            int rowIndex = 2;
            for (CustomerExportRow item : rows) {
                Row row = sheet.createRow(rowIndex++);
                setCell(row, 0, item.getCustomerName());
                setCell(row, 1, item.getRemark());
                setCell(row, 2, item.getDescription());
                setCell(row, 3, item.getStatus());
                setCell(row, 4, item.getCustomerCorpName());
                setCell(row, 5, item.getStaffName());
                setCell(row, 6, formatTime(item.getCreatetime()));
                setCell(row, 7, item.getTags().stream()
                        .map(CustomerStaffTagPo::getTagName)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining(",")));
                setCell(row, 8, ADD_WAY_NAMES.getOrDefault(item.getAddWay(), stringValue(item.getAddWay())));
                setCell(row, 9, stringValue(item.getGender()));
                setCell(row, 10, item.getPhoneNumber());
                setCell(row, 11, stringValue(item.getAge()));
                setCell(row, 12, item.getBirthday());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "导出客户列表失败");
        }
    }

    List<CustomerExportRow> queryRows(String extCorpId,
                                      String name,
                                      List<String> extStaffIds,
                                      List<String> extTagIds,
                                      Integer channelType,
                                      Integer gender,
                                      Integer type,
                                      LocalDate startTime,
                                      LocalDate endTime,
                                      String sortField,
                                      String sortType,
                                      long page,
                                      long pageSize) {
        List<CustomerExportRow> rows = exportQueryMapper.selectRows(
                extCorpId, name, extStaffIds, extTagIds, channelType, gender, type, startTime, endTime,
                orderBy(sortField, sortType), pageSize, (page - 1) * pageSize);
        attachTags(rows);
        return rows;
    }

    private void attachTags(List<CustomerExportRow> rows) {
        List<Long> ids = rows.stream()
                .map(CustomerExportRow::getCustomerStaffId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        List<CustomerStaffTagPo> tags = tagMapper.selectList(new QueryWrapper<CustomerStaffTagPo>()
                .in("customer_staff_id", ids)
                .isNull("deleted_at"));
        Map<Long, List<CustomerStaffTagPo>> tagsByRelation = new HashMap<>();
        for (CustomerStaffTagPo tag : tags) {
            tagsByRelation.computeIfAbsent(tag.getCustomerStaffId(), key -> new ArrayList<>()).add(tag);
        }
        for (CustomerExportRow row : rows) {
            row.setTags(tagsByRelation.getOrDefault(row.getCustomerStaffId(), Collections.emptyList()));
        }
    }

    private String orderBy(String sortField, String sortType) {
        String direction = "asc".equalsIgnoreCase(sortType) ? "asc" : "desc";
        return sortColumn(sortField) + " " + direction;
    }

    private String sortColumn(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return "customer_staff.createtime";
        }
        switch (sortField.toLowerCase(Locale.ROOT)) {
            case "name":
            case "customer_name":
                return "customer.name";
            case "staff_name":
                return "s.name";
            case "createtime":
            case "created_at":
                return "customer_staff.createtime";
            case "add_way":
                return "customer_staff.add_way";
            default:
                return "customer_staff.createtime";
        }
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(EXPORT_TIME_FORMATTER);
    }

    private String stringValue(Long value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void writeExportHeader(XSSFWorkbook workbook, Sheet sheet, String[] titles) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, titles.length - 1));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("客户列表(导出时间: " + LocalDateTime.now().format(EXPORT_TIME_FORMATTER) + ")");
        titleCell.setCellStyle(centerStyle(workbook, true));
        Row headerRow = sheet.createRow(1);
        CellStyle headerStyle = centerStyle(workbook, true);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 18 * 256);
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

    private static Map<Long, String> addWayNames() {
        Map<Long, String> values = new HashMap<>();
        values.put(0L, "未知来源");
        values.put(1L, "扫描二维码");
        values.put(2L, "搜索手机号");
        values.put(3L, "名片分享");
        values.put(4L, "群聊");
        values.put(5L, "手机通讯录");
        values.put(6L, "微信联系人");
        values.put(7L, "来自微信的添加好友申请");
        values.put(8L, "安装第三方应用时自动添加的客服人员");
        values.put(9L, "搜索邮箱");
        values.put(201L, "内部成员共享");
        values.put(202L, "管理员/负责人分配");
        return Collections.unmodifiableMap(values);
    }
}
