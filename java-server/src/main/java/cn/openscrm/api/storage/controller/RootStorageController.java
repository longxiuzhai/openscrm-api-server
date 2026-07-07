package cn.openscrm.api.storage.controller;

import cn.openscrm.api.common.api.ApiResponse;
import cn.openscrm.api.storage.dto.RawSignedUrlRequest;
import cn.openscrm.api.storage.service.FileStorageService;
import cn.openscrm.api.storage.service.LocalStorageService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
public class RootStorageController {

    private final LocalStorageService localStorageService;
    private final FileStorageService fileStorageService;

    public RootStorageController(LocalStorageService localStorageService, FileStorageService fileStorageService) {
        this.localStorageService = localStorageService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/public/**")
    public ResponseEntity<byte[]> getPublic(@RequestParam("expire_at") Long expireAt,
                                            @RequestParam("signature") String signature,
                                            HttpServletRequest request) {
        String objectKey = objectKey(request);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(localStorageService.getGoStyle(objectKey, request.getMethod(), expireAt, signature));
    }

    @PutMapping("/public/**")
    public ResponseEntity<Void> putPublic(@RequestParam("expire_at") Long expireAt,
                                          @RequestParam("signature") String signature,
                                          @RequestBody(required = false) byte[] body,
                                          HttpServletRequest request) {
        localStorageService.putGoStyle(objectKey(request), request.getMethod(), expireAt, signature, body);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/action/get-signed-url", "/action/get_signed_url"})
    public ApiResponse<String> signedUrl(@RequestBody RawSignedUrlRequest request) {
        long ttl = request.getExpiredInSec() == null ? fileStorageService.signedUrlTtlSeconds() : request.getExpiredInSec();
        return ApiResponse.ok(fileStorageService.signedUrl(request.getObjectKey(), request.getMethod(), ttl));
    }

    private String objectKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = "/storage/public/";
        if (!StringUtils.hasText(uri) || !uri.startsWith(prefix)) {
            return "";
        }
        return uri.substring(prefix.length());
    }
}
