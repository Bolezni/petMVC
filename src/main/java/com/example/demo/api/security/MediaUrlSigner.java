package com.example.demo.api.security;

import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Подписанные ссылки на медиафайлы.
 *
 * Зачем: <img> и <video> не умеют отправлять заголовок Authorization, поэтому
 * при фронтенде на другом домене с bearer-токенами файл иначе не загрузить.
 * Ссылка живёт коротко и подписана ключом сервера, подделать exp нельзя.
 *
 * Ключ — тот же секрет, что у JWT, но с отдельным контекстом в сообщении,
 * чтобы подпись нельзя было переиспользовать как токен.
 */
@Component
public class MediaUrlSigner {

    private static final String HMAC = "HmacSHA256";
    private static final String CONTEXT = "media-url-v1";

    private final byte[] key;
    private final long ttlSeconds;

    public MediaUrlSigner(@Value("${jwt.secret}") String secret,
                          @Value("${app.media.url-ttl-ms:900000}") long ttlMs) {
        this.key = Decoders.BASE64.decode(secret);
        this.ttlSeconds = Math.max(60, ttlMs / 1000);
    }

    /** Относительная ссылка на файл, действительная ttl. */
    public String url(Long mediaId) {
        long expiresAt = nowSeconds() + ttlSeconds;
        return "/api/v1/media/" + mediaId + "/content?exp=" + expiresAt + "&sig=" + sign(mediaId, expiresAt);
    }

    public boolean isValid(Long mediaId, Long expiresAt, String signature) {
        if (mediaId == null || expiresAt == null || signature == null || signature.isBlank()) {
            return false;
        }

        long now = nowSeconds();
        if (expiresAt < now) {
            return false;                              // просрочена
        }
        if (expiresAt > now + ttlSeconds + 60) {
            return false;                              // «вечную» ссылку выпустить нельзя
        }

        byte[] expected = sign(mediaId, expiresAt).getBytes(StandardCharsets.UTF_8);
        byte[] actual = signature.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);   // сравнение за постоянное время
    }

    private String sign(Long mediaId, long expiresAt) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            byte[] raw = mac.doFinal((CONTEXT + ":" + mediaId + ":" + expiresAt)
                    .getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось подписать ссылку", ex);
        }
    }

    private long nowSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
