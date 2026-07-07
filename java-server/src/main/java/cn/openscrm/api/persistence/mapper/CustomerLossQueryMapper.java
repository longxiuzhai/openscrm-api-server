package cn.openscrm.api.persistence.mapper;

import cn.openscrm.api.customerloss.dto.CustomerLossResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CustomerLossQueryMapper {

    @Select({
            "<script>",
            "select customer_staff.id as id,",
            "customer.ext_id as extCustomerId,",
            "customer.avatar as customerAvatar,",
            "customer.name as extCustomerName,",
            "customer.type as customerType,",
            "customer.corp_name as customerCorpName,",
            "customer_staff_relation_history.createtime as relationCreateAt,",
            "customer_staff_relation_history.customer_delete_staff_at as customerDeleteStaffAt,",
            "s.name as staffName,",
            "s.ext_id as extStaffId,",
            "s.id as staffId,",
            "s.avatar_url as staffAvatar,",
            "timestampdiff(day, customer_staff_relation_history.createtime, customer_staff_relation_history.customer_delete_staff_at) as inConnectionTimeRange",
            "from customer_staff_relation_history",
            "join customer_staff on customer_staff.ext_staff_id = customer_staff_relation_history.ext_staff_id",
            "and customer_staff.ext_customer_id = customer_staff_relation_history.ext_customer_id",
            "join customer on customer.ext_id = customer_staff.ext_customer_id",
            "join staff s on customer_staff.ext_staff_id = s.ext_id",
            "where customer_staff_relation_history.customer_delete_staff_at is not null",
            "<if test='extCorpId != null and extCorpId != \"\"'>and s.ext_corp_id = #{extCorpId}</if>",
            "<if test='extStaffIds != null and extStaffIds.size() > 0'>",
            "and s.ext_id in",
            "<foreach collection='extStaffIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='lossStart != null and lossEnd != null'>",
            "and date(customer_staff_relation_history.customer_delete_staff_at) between #{lossStart} and #{lossEnd}",
            "</if>",
            "<if test='connectionCreateStart != null and connectionCreateEnd != null'>",
            "and date(customer_staff.createtime) between #{connectionCreateStart} and #{connectionCreateEnd}",
            "</if>",
            "<if test='timeSpanLowerLimit != null and timeSpanLowerLimit > 0'>",
            "and timestampdiff(day, customer_staff_relation_history.createtime, customer_staff_relation_history.customer_delete_staff_at) &gt; #{timeSpanLowerLimit}",
            "</if>",
            "<if test='timeSpanUpperLimit != null and timeSpanUpperLimit > 0'>",
            "and timestampdiff(day, customer_staff_relation_history.createtime, customer_staff_relation_history.customer_delete_staff_at) &lt; #{timeSpanUpperLimit}",
            "</if>",
            "order by ${orderBy}",
            "limit #{limit} offset #{offset}",
            "</script>"
    })
    List<CustomerLossResponse> selectLosses(@Param("extCorpId") String extCorpId,
                                            @Param("extStaffIds") List<String> extStaffIds,
                                            @Param("lossStart") LocalDate lossStart,
                                            @Param("lossEnd") LocalDate lossEnd,
                                            @Param("connectionCreateStart") LocalDate connectionCreateStart,
                                            @Param("connectionCreateEnd") LocalDate connectionCreateEnd,
                                            @Param("timeSpanLowerLimit") Long timeSpanLowerLimit,
                                            @Param("timeSpanUpperLimit") Long timeSpanUpperLimit,
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
            "join staff s on customer_staff.ext_staff_id = s.ext_id",
            "where customer_staff_relation_history.customer_delete_staff_at is not null",
            "<if test='extCorpId != null and extCorpId != \"\"'>and s.ext_corp_id = #{extCorpId}</if>",
            "<if test='extStaffIds != null and extStaffIds.size() > 0'>",
            "and s.ext_id in",
            "<foreach collection='extStaffIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "<if test='lossStart != null and lossEnd != null'>",
            "and date(customer_staff_relation_history.customer_delete_staff_at) between #{lossStart} and #{lossEnd}",
            "</if>",
            "<if test='connectionCreateStart != null and connectionCreateEnd != null'>",
            "and date(customer_staff.createtime) between #{connectionCreateStart} and #{connectionCreateEnd}",
            "</if>",
            "<if test='timeSpanLowerLimit != null and timeSpanLowerLimit > 0'>",
            "and timestampdiff(day, customer_staff_relation_history.createtime, customer_staff_relation_history.customer_delete_staff_at) &gt; #{timeSpanLowerLimit}",
            "</if>",
            "<if test='timeSpanUpperLimit != null and timeSpanUpperLimit > 0'>",
            "and timestampdiff(day, customer_staff_relation_history.createtime, customer_staff_relation_history.customer_delete_staff_at) &lt; #{timeSpanUpperLimit}",
            "</if>",
            "</script>"
    })
    long countLosses(@Param("extCorpId") String extCorpId,
                     @Param("extStaffIds") List<String> extStaffIds,
                     @Param("lossStart") LocalDate lossStart,
                     @Param("lossEnd") LocalDate lossEnd,
                     @Param("connectionCreateStart") LocalDate connectionCreateStart,
                     @Param("connectionCreateEnd") LocalDate connectionCreateEnd,
                     @Param("timeSpanLowerLimit") Long timeSpanLowerLimit,
                     @Param("timeSpanUpperLimit") Long timeSpanUpperLimit);
}
