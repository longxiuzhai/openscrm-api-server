package cn.openscrm.api.commonutil.service;

import static org.assertj.core.api.Assertions.assertThat;

import cn.openscrm.api.commonutil.dto.ParseLinkResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

class CommonUtilServiceTest {

    @Test
    void parseHtmlExtractsTitleDescriptionAndResolvesImage() {
        CommonUtilService service = new CommonUtilService(new RestTemplateBuilder());
        String html = "<html><head><title> 标题 </title><meta name=\"description\" content=\"描述\"></head>"
                + "<body><img src=\"/assets/a.png\"></body></html>";

        ParseLinkResponse response = service.parseHtml("https://example.test/page", html);

        assertThat(response.getTitle()).isEqualTo("标题");
        assertThat(response.getDesc()).isEqualTo("描述");
        assertThat(response.getImgUrl()).isEqualTo("https://example.test/assets/a.png");
        assertThat(response.getLinkUrl()).isEqualTo("https://example.test/page");
    }
}
