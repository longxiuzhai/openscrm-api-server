package cn.openscrm.api.msgarch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.msgarch.dto.MsgArchSyncResponse;
import cn.openscrm.api.persistence.entity.ChatMsgPo;
import cn.openscrm.api.persistence.mapper.ChatMsgContentPoMapper;
import cn.openscrm.api.persistence.mapper.ChatMsgPoMapper;
import cn.openscrm.api.persistence.mapper.CustomerPoMapper;
import cn.openscrm.api.persistence.mapper.GroupChatPoMapper;
import cn.openscrm.api.persistence.mapper.StaffPoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class MsgArchServiceTest {

    @Test
    void syncProxiesToConfiguredMsgArchServiceWithGoCompatibleSignature() throws Exception {
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getMsgArch().setServerUrl("http://msg-archive-server:8080");
        properties.getMsgArch().setAppCode("inner-app-code");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        String extCorpId = "ww2d3e2957190c6e4c";
        String expectedSignature = hmacSha256Hex("inner-app-code", extCorpId);

        server.expect(requestTo("http://msg-archive-server:8080/api/v1/chat-msg/sync"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"ext_corp_id\":\"" + extCorpId
                        + "\",\"signature\":\"" + expectedSignature + "\"}"))
                .andRespond(withSuccess("{\"code\":0,\"message\":\"ok\",\"data\":null}", APPLICATION_JSON));

        MsgArchService service = service(properties, restTemplate, mock(ChatMsgPoMapper.class));

        MsgArchSyncResponse response = service.sync(extCorpId);

        assertThat(response.isAccepted()).isTrue();
        assertThat(response.getMessage()).isEqualTo("msg-archive sync proxied");
        server.verify();
    }

    @Test
    void syncFallsBackToLocalLatestSeqWhenProxyIsNotConfigured() {
        ChatMsgPo latest = new ChatMsgPo();
        latest.setSeq(42L);
        ChatMsgPoMapper chatMsgMapper = mock(ChatMsgPoMapper.class);
        when(chatMsgMapper.selectList(any())).thenReturn(Collections.singletonList(latest));

        MsgArchService service = service(new OpenScrmProperties(), new RestTemplate(), chatMsgMapper);

        MsgArchSyncResponse response = service.sync("ww2d3e2957190c6e4c");

        assertThat(response.isAccepted()).isTrue();
        assertThat(response.getMessage()).isEqualTo("local sync fallback, latest_seq=42");
    }

    @Test
    void syncReportsFailureWhenConfiguredProxyReturnsNonOkStatus() {
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getMsgArch().setServerUrl("http://msg-archive-server:8080");
        properties.getMsgArch().setAppCode("inner-app-code");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://msg-archive-server:8080/api/v1/chat-msg/sync"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        MsgArchService service = service(properties, restTemplate, mock(ChatMsgPoMapper.class));

        MsgArchSyncResponse response = service.sync("ww2d3e2957190c6e4c");

        assertThat(response.isAccepted()).isFalse();
        assertThat(response.getMessage()).isEqualTo("msg-archive sync failed, status=502");
        server.verify();
    }

    private static MsgArchService service(OpenScrmProperties properties,
                                          RestTemplate restTemplate,
                                          ChatMsgPoMapper chatMsgMapper) {
        return new MsgArchService(
                chatMsgMapper,
                mock(ChatMsgContentPoMapper.class),
                mock(StaffPoMapper.class),
                mock(CustomerPoMapper.class),
                mock(GroupChatPoMapper.class),
                new ObjectMapper(),
                properties,
                restTemplate);
    }

    private static String hmacSha256Hex(String secret, String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
