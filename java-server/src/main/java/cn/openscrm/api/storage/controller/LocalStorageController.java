package cn.openscrm.api.storage.controller;

import cn.openscrm.api.storage.service.LocalStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storage/local")
public class LocalStorageController {

    private final LocalStorageService localStorageService;

    public LocalStorageController(LocalStorageService localStorageService) {
        this.localStorageService = localStorageService;
    }

    @PutMapping
    public ResponseEntity<Void> put(@RequestParam("key") String key,
                                    @RequestParam("method") String method,
                                    @RequestParam("expires_at") Long expiresAt,
                                    @RequestParam("token") String token,
                                    @RequestBody(required = false) byte[] body) {
        localStorageService.put(key, method, expiresAt, token, body);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<byte[]> get(@RequestParam("key") String key,
                                      @RequestParam("method") String method,
                                      @RequestParam("expires_at") Long expiresAt,
                                      @RequestParam("token") String token) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(localStorageService.get(key, method, expiresAt, token));
    }
}
