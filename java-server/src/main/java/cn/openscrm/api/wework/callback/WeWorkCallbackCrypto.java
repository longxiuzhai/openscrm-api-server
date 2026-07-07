package cn.openscrm.api.wework.callback;

import cn.openscrm.api.common.exception.BizException;
import cn.openscrm.api.common.exception.ErrorCode;
import cn.openscrm.api.config.OpenScrmProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WeWorkCallbackCrypto {

    private final OpenScrmProperties properties;

    public WeWorkCallbackCrypto(OpenScrmProperties properties) {
        this.properties = properties;
    }

    public boolean verify(String timestamp, String nonce, String encrypted, String signature) {
        if (!StringUtils.hasText(signature)) {
            return false;
        }
        return signature.equals(sign(properties.getWeWork().getCallbackToken(), timestamp, nonce, encrypted));
    }

    public String decrypt(String encrypted) {
        try {
            byte[] aesKey = aesKey();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(aesKey, 0, 16));
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            int msgLength = ByteBuffer.wrap(plain, 16, 4).getInt();
            return new String(plain, 20, msgLength, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BizException(ErrorCode.INVALID_CIPHER);
        }
    }

    private byte[] aesKey() {
        String key = properties.getWeWork().getCallbackAesKey();
        if (!StringUtils.hasText(key)) {
            throw new BizException(ErrorCode.INVALID_CIPHER);
        }
        byte[] decoded = Base64.getDecoder().decode(key + "=");
        if (decoded.length != 32) {
            throw new BizException(ErrorCode.INVALID_CIPHER);
        }
        return decoded;
    }

    private String sign(String token, String timestamp, String nonce, String encrypted) {
        try {
            List<String> values = Arrays.asList(token, timestamp, nonce, encrypted);
            values.sort(String::compareTo);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            for (String value : values) {
                if (value != null) {
                    digest.update(value.getBytes(StandardCharsets.UTF_8));
                }
            }
            byte[] bytes = digest.digest();
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
