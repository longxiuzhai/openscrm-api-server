package cn.openscrm.api.wework.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WeWorkCallbackController {

    private final WeWorkCallbackService callbackService;

    @GetMapping(value = {"/callback", "/wework/callback", "/customer/action/notify-verify"},
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String echo(@RequestParam("msg_signature") String msgSignature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("echostr") String echoStr) {
        return callbackService.echo(msgSignature, timestamp, nonce, echoStr);
    }

    @PostMapping(value = {"/callback", "/wework/callback", "/customer/action/notify-verify"},
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String callback(@RequestParam("msg_signature") String msgSignature,
                           @RequestParam("timestamp") String timestamp,
                           @RequestParam("nonce") String nonce,
                           @RequestBody String body) {
        callbackService.handle(msgSignature, timestamp, nonce, body);
        return "success";
    }
}
