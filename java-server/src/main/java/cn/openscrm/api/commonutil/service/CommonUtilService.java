package cn.openscrm.api.commonutil.service;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.commonutil.dto.ParseLinkResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CommonUtilService {

    private static final Pattern TITLE = Pattern.compile("(?is)<title[^>]*>(.*?)</title>");
    private static final Pattern DESC = Pattern.compile("(?is)<meta[^>]+name=[\"']description[\"'][^>]+content=[\"']([^\"']*)[\"'][^>]*>");
    private static final Pattern IMG = Pattern.compile("(?is)<img[^>]+src=[\"']([^\"']*)[\"'][^>]*>");

    private final RestTemplate restTemplate;

    public CommonUtilService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/92 Safari/537.36")
                .build();
    }

    public ParseLinkResponse parseLink(String url) {
        URI uri = parseUri(url);
        String html;
        try {
            html = restTemplate.getForObject(uri, String.class);
        } catch (RestClientException e) {
            throw new BizException(ErrorCode.ILLEGAL_URL);
        }
        ParseLinkResponse response = parseHtml(url, html == null ? "" : html);
        response.setLinkUrl(url);
        return response;
    }

    ParseLinkResponse parseHtml(String baseUrl, String html) {
        ParseLinkResponse response = new ParseLinkResponse();
        response.setTitle(clean(match(TITLE, html)));
        response.setDesc(clean(match(DESC, html)));
        String image = clean(match(IMG, html));
        response.setImgUrl(resolve(baseUrl, image));
        response.setLinkUrl(baseUrl);
        return response;
    }

    private URI parseUri(String url) {
        try {
            URI uri = new URI(url);
            if (!StringUtils.hasText(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                throw new BizException(ErrorCode.ILLEGAL_URL);
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new BizException(ErrorCode.ILLEGAL_URL);
        }
    }

    private String match(Pattern pattern, String html) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private String resolve(String baseUrl, String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        try {
            return new URI(baseUrl).resolve(value).toString();
        } catch (URISyntaxException e) {
            return value;
        }
    }
}
