package cn.openscrm.api.persistence.mapper;

import cn.openscrm.api.deletenotify.dto.StaffDeleteCustomerResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StaffDeleteCustomerQueryMapper {

    @Select({
            "<script>",
            "select customer_staff.id as id,",
            "customer.ext_id as extCustomerId,",
            "customer.avatar as extCustomerAvatar,",
            "customer.name as extCustomerName,",
            "customer.type as customerType,",
            "customer.corp_name as customerCorpName,",
            "customer_staff_relation_history.createtime as relationCreateAt,",
            "customer_staff_relation_history.staff_delete_customer_at as relationDeleteAt,",
            "staff.name as staffName,",
            "staff.ext_id as extStaffId,",
            "staff.id as staffId,",
            "staff.avatar_url as extStaffAvatar",
            "from customer_staff_relation_history",
            "join customer_staff on customer_staff.ext_staff_id = customer_staff_relation_history.ext_staff_id",
            "and customer_staff.ext_customer_id = customer_staff_relation_history.ext_customer_id",
            "join customer on customer.ext_id = customer_staff.ext_customer_id",
            "join staff on customer_staff.ext_staff_id = staff.ext_id",
            "where customer_staff_relation_history.staff_delete_customer_at is not null",
            "<if test='extCorpId != null and extCorpId != \"\"'>and staff.ext_corp_id = #{extCorpId}</if>",
            "<if test='extDepartmentId != null and extDepartmentId &gt; 0'>",
            "and json_contains(staff.dept_ids, json_array(#{extDepartmentId}))",
            "</if>",
            "<if test='extStaffIds != null and extStaffIds.size() > 0'>",
            "and staff.ext_id in",
            "<foreach collection='extStaffIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='connectionCreateStart != null and connectionCreateEnd != null'>",
            "and date(customer_staff.createtime) between #{connectionCreateStart} and #{connectionCreateEnd}",
            "</if>",
            "<if test='deleteCustomerStart != null and deleteCustomerEnd != null'>",
            "and date(customer_staff_relation_history.staff_delete_customer_at) between #{deleteCustomerStart} and #{deleteCustomerEnd}",
            "</if>",
            "order by ${orderBy}",
            "limit #{limit} offset #{offset}",
            "</script>"
    })
    List<StaffDeleteCustomerResponse> selectStaffDeleteCustomers(@Param("extCorpId") String extCorpId,
                                                                 @Param("extDepartmentId") Long extDepartmentId,
                                                                 @Param("extStaffIds") List<String> extStaffIds,
                                                                 @Param("connectionCreateStart") LocalDate connectionCreateStart,
                                                                 @Param("connectionCreateEnd") LocalDate connectionCreateEnd,
                                                                 @Param("deleteCustomerStart") LocalDate deleteCustomerStart,
                                                                 @Param("deleteCustomerEnd") LocalDate deleteCustomerEnd,
                                                                 @Param("orderBy") String orderBy,
                                                                 @Param("limit") long limit,
                                                                 @Param("offset") long offset);

    @Select({
            "<script>",
            "select count(*)",
            "from customer_staff_relation_history",
            "join customer_staff on customer_staff.ext_staff_id = customer_staff_relation_history.ext_staff_id",
            "and customer_staff.ext_customer_id = customer_staff_relation_history.ext_customer_id",
            "join customer on customer.ext_id = customer_staff.ext_customer_id",
            "join staff on customer_staff.ext_staff_id = staff.ext_id",
            "where customer_staff_relation_history.staff_delete_customer_at is not null",
            "<if test='extCorpId != null and extCorpId != \"\"'>and staff.ext_corp_id = #{extCorpId}</if>",
            "<if test='extDepartmentId != null and extDepartmentId &gt; 0'>",
            "and json_contains(staff.dept_ids, json_array(#{extDepartmentId}))",
            "</if>",
            "<if test='extStaffIds != null and extStaffIds.size() > 0'>",
            "and staff.ext_id in",
            "<foreach collection='extStaffIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='connectionCreateStart != null and connectionCreateEnd != null'>",
            "and date(customer_staff.createtime) between #{connectionCreateStart} and #{connectionCreateEnd}",
            "</if>",
            "<if test='deleteCustomerStart != null and deleteCustomerEnd != null'>",
            "and date(customer_staff_relation_history.staff_delete_customer_at) between #{deleteCustomerStart} and #{deleteCustomerEnd}",
            "</if>",
            "</script>"
    })
    long countStaffDeleteCustomers(@Param("extCorpId") String extCorpId,
                                   @Param("extDepartmentId") Long extDepartmentId,
                                   @Param("extStaffIds") List<String> extStaffIds,
                                   @Param("connectionCreateStart") LocalDate connectionCreateStart,
                                   @Param("connectionCreateEnd") LocalDate connectionCreateEnd,
                                   @Param("deleteCustomerStart") LocalDate deleteCustomerStart,
                                   @Param("deleteCustomerEnd") LocalDate deleteCustomerEnd);
}
