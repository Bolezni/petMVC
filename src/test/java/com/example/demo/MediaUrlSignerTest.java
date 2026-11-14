package com.example.demo;

import com.example.demo.api.security.MediaUrlSigner;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class MediaUrlSignerTest {

    private static final Pattern URL =
            Pattern.compile("/api/v1/media/(\\d+)/content\\?exp=(\\d+)&sig=([A-Za-z0-9_-]+)");

    private final MediaUrlSigner signer = new MediaUrlSigner(
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes()),
            900_000);

    @Test
    void generatedUrl_isValid() {
        Matcher m = URL.matcher(signer.url(5L));

        assertThat(m.find()).isTrue();
        long id = Long.parseLong(m.group(1));
        long exp = Long.parseLong(m.group(2));
        String sig = m.group(3);

        assertThat(signer.isValid(id, exp, sig)).isTrue();
    }

    @Test
    void signatureForAnotherMedia_isRejected() {
        Matcher m = URL.matcher(signer.url(5L));
        assertThat(m.find()).isTrue();

        long exp = Long.parseLong(m.group(2));
        String sig = m.group(3);

        assertThat(signer.isValid(6L, exp, sig)).isFalse();
    }

    @Test
    void tamperedSignature_isRejected() {
        Matcher m = URL.matcher(signer.url(5L));
        assertThat(m.find()).isTrue();

        long exp = Long.parseLong(m.group(2));

        assertThat(signer.isValid(5L, exp, "AAAA" + m.group(3).substring(4))).isFalse();
    }

    @Test
    void expiredLink_isRejected() {
        long past = System.currentTimeMillis() / 1000 - 10;
        String sig = "any";

        assertThat(signer.isValid(5L, past, sig)).isFalse();
    }

    @Test
    void linkWithFarFutureExpiry_isRejected() {
        long farFuture = System.currentTimeMillis() / 1000 + 86_400;

        assertThat(signer.isValid(5L, farFuture, "any")).isFalse();
    }

    @Test
    void missingParameters_areRejected() {
        assertThat(signer.isValid(5L, null, "sig")).isFalse();
        assertThat(signer.isValid(5L, 1L, null)).isFalse();
        assertThat(signer.isValid(5L, 1L, " ")).isFalse();
    }
}
