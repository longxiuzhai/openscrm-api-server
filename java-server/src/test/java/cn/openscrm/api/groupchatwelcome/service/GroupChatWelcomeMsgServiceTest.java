package cn.openscrm.api.groupchatwelcome.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.common.api.PageResponse;
import cn.openscrm.api.common.id.SnowflakeIdGenerator;
import cn.openscrm.api.groupchatwelcome.dto.CommonIdsRequest;
import cn.openscrm.api.groupchatwelcome.dto.GroupChatWelcomeMsgRequest;
import cn.openscrm.api.persistence.entity.GroupChatWelcomeMsgPo;
import cn.openscrm.api.persistence.mapper.GroupChatWelcomeMsgPoMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GroupChatWelcomeMsgServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createStoresJsonAttachment() throws Exception {
        GroupChatWelcomeMsgPoMapper mapper = mock(GroupChatWelcomeMsgPoMapper.class);
        GroupChatWelcomeMsgService service = service(mapper);
        GroupChatWelcomeMsgRequest request = new GroupChatWelcomeMsgRequest();
        request.setContent("欢迎入群");
        request.setAttachmentType("image");
        request.setAttachment(objectMapper.readTree("{\"image\":{\"pic_url\":\"https://example.test/a.png\"}}"));

        GroupChatWelcomeMsgPo msg = service.create(request, "ww-corp", "admin-a");

        assertThat(msg.getId()).isEqualTo(10001L);
        assertThat(msg.getExtCorpId()).isEqualTo("ww-corp");
        assertThat(msg.getExtCreatorId()).isEqualTo("admin-a");
        assertThat(msg.getAttachment()).contains("pic_url");
        verify(mapper).insert(msg);
    }

    @Test
    void updateUsesCorpScopedWrapper() throws Exception {
        GroupChatWelcomeMsgPoMapper mapper = mock(GroupChatWelcomeMsgPoMapper.class);
        GroupChatWelcomeMsgPo stored = new GroupChatWelcomeMsgPo();
        stored.setId(10001L);
        stored.setContent("更新");
        when(mapper.selectById(10001L)).thenReturn(stored);
        GroupChatWelcomeMsgService service = service(mapper);
        GroupChatWelcomeMsgRequest request = new GroupChatWelcomeMsgRequest();
        request.setContent("更新");
        request.setAttachmentType("link");
        request.setAttachment(objectMapper.readTree("{\"link\":{\"title\":\"官网\"}}"));

        GroupChatWelcomeMsgPo msg = service.update(10001L, request, "ww-corp");

        assertThat(msg).isSameAs(stored);
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Wrapper<GroupChatWelcomeMsgPo>> wrapperCaptor = ArgumentCaptor.forClass((Class) Wrapper.class);
        verify(mapper).update(any(), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    @Test
    void queryWrapsPagedResult() {
        GroupChatWelcomeMsgPoMapper mapper = mock(GroupChatWelcomeMsgPoMapper.class);
        IPage<GroupChatWelcomeMsgPo> page = Page.of(2, 10, 1);
        GroupChatWelcomeMsgPo msg = new GroupChatWelcomeMsgPo();
        msg.setContent("欢迎");
        page.setRecords(Collections.singletonList(msg));
        when(mapper.selectPage(any(), any())).thenReturn(page);
        GroupChatWelcomeMsgService service = service(mapper);

        PageResponse<GroupChatWelcomeMsgPo> response = service.query("ww-corp", "欢", 2, 10);

        assertThat(response.getItems()).containsExactly(msg);
        assertThat(response.getTotalRows()).isEqualTo(1L);
        assertThat(response.getPage()).isEqualTo(2L);
        assertThat(response.getPageSize()).isEqualTo(10L);
    }

    @Test
    void deleteUsesBatchIds() {
        GroupChatWelcomeMsgPoMapper mapper = mock(GroupChatWelcomeMsgPoMapper.class);
        when(mapper.deleteBatchIds(Arrays.asList(1L, 2L))).thenReturn(2);
        GroupChatWelcomeMsgService service = service(mapper);
        CommonIdsRequest request = new CommonIdsRequest();
        request.setIds(Arrays.asList(1L, 2L));

        long rows = service.delete(request);

        assertThat(rows).isEqualTo(2L);
        verify(mapper).deleteBatchIds(Arrays.asList(1L, 2L));
    }

    private GroupChatWelcomeMsgService service(GroupChatWelcomeMsgPoMapper mapper) {
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        when(idGenerator.nextId()).thenReturn(10001L);
        return new GroupChatWelcomeMsgService(mapper, idGenerator, objectMapper);
    }
}
