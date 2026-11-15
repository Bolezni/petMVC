package com.example.demo;

import com.example.demo.api.notify.TelegramNotifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TelegramNotifierTest {

    @Test
    void withoutToken_notConfiguredAndDoesNotThrow() {
        TelegramNotifier notifier = new TelegramNotifier("", "", true, "https://example.com");

        assertThat(notifier.isConfigured()).isFalse();
        assertThatCode(() -> notifier.notifySpecialist("тест"))
                .doesNotThrowAnyException();
    }

    @Test
    void disabledByFlag_isNotConfigured() {
        TelegramNotifier notifier = new TelegramNotifier("token", "123", false, "https://example.com");

        assertThat(notifier.isConfigured()).isFalse();
    }

    @Test
    void configured_buildsAbsoluteLinks() {
        TelegramNotifier notifier = new TelegramNotifier("token", "123", true, "https://pet.example.com");

        assertThat(notifier.isConfigured()).isTrue();
        assertThat(notifier.pageUrl("/specialist/users/5"))
                .isEqualTo("https://pet.example.com/specialist/users/5");
    }

    @Test
    void baseUrlWithTrailingSlash_doesNotProduceDoubleSlash() {
        TelegramNotifier notifier = new TelegramNotifier("token", "123", true, "https://pet.example.com/");

        assertThat(notifier.pageUrl("/app")).isEqualTo("https://pet.example.com/app");
    }

    @Test
    void withoutBaseUrl_returnsRelativePath() {
        TelegramNotifier notifier = new TelegramNotifier("token", "123", true, "");

        assertThat(notifier.pageUrl("/app")).isEqualTo("/app");
    }
}
