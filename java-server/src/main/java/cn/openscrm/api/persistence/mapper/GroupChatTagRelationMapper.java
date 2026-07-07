package cn.openscrm.api.persistence.mapper;

import cn.openscrm.api.persistence.entity.GroupChatTagPo;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface GroupChatTagRelationMapper {

    @Insert("replace into group_chat_tags (group_chat_id, group_chat_tag_id) values (#{groupChatId}, #{groupChatTagId})")
    void addTag(@Param("groupChatId") Long groupChatId, @Param("groupChatTagId") Long groupChatTagId);

    @Delete({
            "<script>",
            "delete from group_chat_tags",
            "where group_chat_id = #{groupChatId}",
            "and group_chat_tag_id in",
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>#{tagId}</foreach>",
            "</script>"
    })
    void removeTags(@Param("groupChatId") Long groupChatId, @Param("tagIds") List<Long> tagIds);

    @Select({
            "<script>",
            "select t.* from group_chat_tag t",
            "join group_chat_tags r on r.group_chat_tag_id = t.id",
            "where r.group_chat_id = #{groupChatId}",
            "and t.deleted_at is null",
            "</script>"
    })
    List<GroupChatTagPo> selectTagsByGroupChatId(@Param("groupChatId") Long groupChatId);

    @Select({
            "<script>",
            "select distinct group_chat_id from group_chat_tags",
            "where group_chat_tag_id in",
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>#{tagId}</foreach>",
            "</script>"
    })
    List<Long> selectChatIdsByAnyTag(@Param("tagIds") List<Long> tagIds);

    @Select({
            "<script>",
            "select group_chat_id from group_chat_tags",
            "where group_chat_tag_id in",
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>#{tagId}</foreach>",
            "group by group_chat_id",
            "having count(distinct group_chat_tag_id) = #{tagCount}",
            "</script>"
    })
    List<Long> selectChatIdsByAllTags(@Param("tagIds") List<Long> tagIds, @Param("tagCount") int tagCount);
}
