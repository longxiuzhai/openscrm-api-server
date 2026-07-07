package cn.openscrm.api.customerexport.dto;

import cn.openscrm.api.persistence.entity.CustomerStaffTagPo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerExportRow {

    private Long customerStaffId;

    private String customerName;

    private String remark;

    private String description;

    private String status;

    private String customerCorpName;

    private String staffName;

    private LocalDateTime createtime;

    private Long addWay;

    private Long gender;

    private String phoneNumber;

    private Long age;

    private String birthday;

    private List<CustomerStaffTagPo> tags = new ArrayList<>();
}
