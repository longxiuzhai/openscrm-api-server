package cn.openscrm.api.stafffrontend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.openscrm.api.config.OpenScrmProperties;
import cn.openscrm.api.stafffrontend.dto.JsConfigResponse;
import cn.openscrm.api.stafffrontend.dto.UploadMediaRequest;
import cn.openscrm.api.stafffrontend.dto.UploadMediaResponse;
import cn.openscrm.api.wework.JsApiTicketResponse;
import cn.openscrm.api.wework.MediaUploadResponse;
import cn.openscrm.api.wework.WeWorkClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class StaffFrontendUtilityServiceTest {

    @Test
    void getJsConfigSignsUrlWithoutFragment() {
        OpenScrmProperties properties = properties();
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        JsApiTicketResponse ticket = new JsApiTicketResponse();
        ticket.setTicket("ticket-a");
        when(weWorkClient.getJsApiTicket(eq("ww-corp"), eq("customer-secret"))).thenReturn(ticket);
        StaffFrontendUtilityService service = service(properties, weWorkClient, mock(RestTemplate.class), redis(null));

        JsConfigResponse response = service.getJsConfig("ww-corp", "https://example.test/page#a");

        assertThat(response.getAppId()).isEqualTo("ww-corp");
        assertThat(response.getUrl()).isEqualTo("https://example.test/page");
        assertThat(response.getNonceStr()).hasSize(20);
        assertThat(response.getSignature()).hasSize(40);
    }

    @Test
    void uploadMediaReturnsCachedResponseWhenPresent() throws Exception {
        OpenScrmProperties properties = properties();
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        UploadMediaResponse cached = new UploadMediaResponse();
        cached.setType("image");
        cached.setMediaId("media-cached");
        String cachedJson = objectMapper.writeValueAsString(cached);
        StringRedisTemplate redisTemplate = redis(cachedJson);
        RestTemplate restTemplate = mock(RestTemplate.class);
        StaffFrontendUtilityService service = service(properties, weWorkClient, restTemplate, redisTemplate);

        UploadMediaRequest request = new UploadMediaRequest();
        request.setType("image");
        request.setUrl("https://example.test/a.png");
        UploadMediaResponse response = service.uploadMedia("ww-corp", request);

        assertThat(response.getMediaId()).isEqualTo("media-cached");
        verify(restTemplate, never()).getForEntity(anyString(), eq(byte[].class));
        verify(weWorkClient, never()).uploadTemporaryMedia(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void uploadMediaDownloadsAndUploadsWhenCacheMiss() {
        OpenScrmProperties properties = properties();
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        MediaUploadResponse uploaded = new MediaUploadResponse();
        uploaded.setType("image");
        uploaded.setMediaId("media-a");
        uploaded.setCreatedAt(1780000000L);
        when(weWorkClient.uploadTemporaryMedia(eq("ww-corp"), eq("customer-secret"), eq("image"), anyString(), any()))
                .thenReturn(uploaded);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(eq("https://example.test/a.png"), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(new byte[]{1, 2, 3}));
        StaffFrontendUtilityService service = service(properties, weWorkClient, restTemplate, redis(null));
        UploadMediaRequest request = new UploadMediaRequest();
        request.setType("image");
        request.setUrl("https://example.test/a.png");

        UploadMediaResponse response = service.uploadMedia("ww-corp", request);

        assertThat(response.getMediaId()).isEqualTo("media-a");
        verify(weWorkClient).uploadTemporaryMedia(eq("ww-corp"), eq("customer-secret"), eq("image"), anyString(), any());
    }

    @Test
    void uploadMaterialTempUploadsAsImageLikeGoTempMaterial() {
        OpenScrmProperties properties = properties();
        WeWorkClient weWorkClient = mock(WeWorkClient.class);
        MediaUploadResponse uploaded = new MediaUploadResponse();
        uploaded.setType("image");
        uploaded.setMediaId("media-temp");
        uploaded.setCreatedAt(1780000000L);
        when(weWorkClient.uploadTemporaryMedia(eq("ww-corp"), eq("customer-secret"), eq("image"), anyString(), any()))
                .thenReturn(uploaded);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(eq("https://example.test/temp.png"), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(new byte[]{7, 8, 9}));
        StaffFrontendUtilityService service = service(properties, weWorkClient, restTemplate, redis(null));

        UploadMediaResponse response = service.uploadMaterialTemp("ww-corp", "https://example.test/temp.png");

        assertThat(response.getMediaId()).isEqualTo("media-temp");
        verify(weWorkClient).uploadTemporaryMedia(eq("ww-corp"), eq("customer-secret"), eq("image"), anyString(), any());
    }

    private StaffFrontendUtilityService service(OpenScrmProperties properties,
                                                WeWorkClient weWorkClient,
                                                RestTemplate restTemplate,
                                                StringRedisTemplate redisTemplate) {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(restTemplate);
        return new StaffFrontendUtilityService(
                properties, weWorkClient, builder, redisTemplate, new ObjectMapper().findAndRegisterModules());
    }

    @SuppressWarnings("unchecked")
    private StringRedisTemplate redis(String value) {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn(value);
        return redisTemplate;
    }

    private OpenScrmProperties properties() {
        OpenScrmProperties properties = new OpenScrmProperties();
        properties.getWeWork().setExtCorpId("ww-corp");
        properties.getWeWork().setCustomerSecret("customer-secret");
        properties.getWeWork().setMainAgentSecret("main-secret");
        properties.getWeWork().setMainAgentId(100001L);
        return properties;
    }
}
