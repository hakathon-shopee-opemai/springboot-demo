package parallax.vn.springbootdemo.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Shared HMAC helper for service-to-service auth. The canonical string is:
 * METHOD \n PATH \n TS \n sha256hex(body). Signature = hex(HMAC-SHA256(secret, canonical)).
 * Must stay byte-for-byte identical to the FastAPI peer implementation.
 */
@Component
public class HmacSigner {

    private static final HexFormat HEX = HexFormat.of();

    public String sha256Hex(byte[] body) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HEX.formatHex(md.digest(body == null ? new byte[0] : body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public String canonical(String method, String path, String ts, byte[] body) {
        return method + "\n" + path + "\n" + ts + "\n" + sha256Hex(body);
    }

    public String sign(String secret, String canonical) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HEX.formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute HMAC", e);
        }
    }

    public boolean verify(String secret, String canonical, String provided) {
        if (provided == null) {
            return false;
        }
        String expected = sign(secret, canonical);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }
}
