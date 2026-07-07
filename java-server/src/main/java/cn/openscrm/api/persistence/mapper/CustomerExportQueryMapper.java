package cn.openscrm.api.persistence.mapper;

import cn.openscrm.api.customerexport.dto.CustomerExportRow;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CustomerExportQueryMapper {

    @Select({
            "<script>",
            "select customer_staff.id as customerStaffId,",
            "customer.name as customerName,",
            "customer.corp_name as customerCorpName,",
            "s.name as staffName,",
            "customer_staff.remark as remark,",
            "customer_staff.description as description,",
            "if(customer_staff.deleted_at is null, '未流失', '已流失') as status,",
            "customer_staff.createtime as createtime,",
            "customer_staff.add_way as addWay,",
            "customer.gender as gender,",
            "customer_info.age as age,",
            "customer_info.birthday as birthday,",
            "customer_info.phone_number as phoneNumber",
            "from customer",
            "left join customer_staff on customer.ext_id = customer_staff.ext_customer_id",
            "left join customer_staff_tag cst on cst.customer_staff_id = customer_staff.id",
            "join staff s on s.ext_id = customer_staff.ext_staff_id",
            "left join customer_info on customer.ext_id = customer_info.ext_customer_id",
            "where customer_staff.ext_corp_id = #{extCorpId}",
            "<if test='name != null and name != \"\"'>and customer.name like concat(#{name}, '%')</if>",
            "<if test='gender != null and gender != 0'>and customer.gender = #{gender}</if>",
            "<if test='type != null and type != 0'>and customer.type = #{type}</if>",
            "<if test='extStaffIds != null and extStaffIds.size() > 0'>",
            "and customer_staff.ext_staff_id in",
            "<foreach collection='extStaffIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='startTime != null and endTime != null'>",
            "and date(customer_staff.createtime) between #{startTime} and #{endTime}",
            "</if>",
            "<if test='extTagIds != null and extTagIds.size() > 0'>",
            "and cst.ext_tag_id in",
            "<foreach collection='extTagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='channelType != null and channelType > 0'>and customer_staff.add_way = #{channelType}</if>",
            "group by customer_staff.id",
            "order by ${orderBy}",
            "limit #{limit} offset #{offset}",
            "</script>"
    })
    List<CustomerExportRow> selectRows(@Param("extCorpId") String extCorpId,
                                       @Param("name") String name,
                                       @Param("extStaffIds") List<String> extStaffIds,
                                       @Param("extTagIds") List<String> extTagIds,
                                       @Param("channelType") Integer channelType,
                                       @Param("gender") Integer gender,
                                       @Param("type") Integer type,
                                       @Param("startTime") LocalDate startTime,
                                       @Param("endTime") LocalDate endTime,
                                       @Param("orderBy") String orderBy,
                                       @Param("limit") long limit,
                                       @Param("offset") long offset);

    @Select({
            "<script>",
            "select count(distinct customer.id)",
            "from customer",
            "left join customer_staff on customer.ext_id = customer_staff.ext_customer_id",
            "left join customer_staff_tag cst on cst.customer_staff_id = customer_staff.id",
            "join staff s on s.ext_id = customer_staff.ext_staff_id",
            "left join customer_info on customer.ext_id = customer_info.ext_customer_id",
            "where customer_staff.ext_corp_id = #{extCorpId}",
            "<if test='name != null and name != \"\"'>and customer.name like concat(#{name}, '%')</if>",
            "<if test='gender != null and gender != 0'>and customer.gender = #{gender}</if>",
            "<if test='type != null and type != 0'>and customer.type = #{type}</if>",
            "<if test='extStaffIds != null and extStaffIds.size() > 0'>",
            "and customer_staff.ext_staff_id in",
            "<foreach collection='extStaffIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='startTime != null and endTime != null'>",
            "and date(customer_staff.createtime) between #{startTime} and #{endTime}",
            "</if>",
            "<if test='extTagIds != null and extTagIds.size() > 0'>",
            "and cst.ext_tag_id in",
            "<foreach collection='extTagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='channelType != null and channelType > 0'>and customer_staff.add_way = #{channelType}</if>",
            "</script>"
    })
    long countRows(@Param("extCorpId") String extCorpId,
                   @Param("name") String name,
                   @Param("extStaffIds") List<String> extStaffIds,
                   @Param("extTagIds") List<String> extTagIds,
                   @Param("channelType") Integer channelType,
                   @Param("gender") Integer gender,
                   @Param("type") Integer type,
                   @Param("startTime") LocalDate startTime,
                   @Param("endTime") LocalDate endTime);
}
