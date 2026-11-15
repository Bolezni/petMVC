package com.example.demo.api.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Уведомления в Telegram.
 *
 * Работает, только если заданы TELEGRAM_BOT_TOKEN и TELEGRAM_CHAT_ID.
 * Отправка идёт в отдельном потоке и никогда не ломает основной запрос:
 * пользователь не должен видеть ошибку из-за недоступного Telegram.
 *
 * JSON собирается вручную: в Spring Boot 4 Jackson переехал в другие пакеты,
 * а тянуть зависимость ради одного запроса не нужно.
 */
@Slf4j
@Component
public class TelegramNotifier {

    private static final int MAX_TEXT = 3500;

    private final String botToken;
    private final String chatId;
    private final String baseUrl;
    private final boolean enabled;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "telegram-notifier");
        thread.setDaemon(true);
        return thread;
    });

    public TelegramNotifier(@Value("${app.telegram.bot-token:}") String botToken,
                            @Value("${app.telegram.chat-id:}") String chatId,
                            @Value("${app.telegram.enabled:true}") boolean enabled,
                            @Value("${app.base-url:}") String baseUrl) {
        this.botToken = botToken == null ? "" : botToken.trim();
        this.chatId = chatId == null ? "" : chatId.trim();
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.enabled = enabled;
    }

    public boolean isConfigured() {
        return enabled && !botToken.isBlank() && !chatId.isBlank();
    }

    /** Ссылка на страницу проекта — чтобы сообщение было кликабельным в Telegram. */
    public String pageUrl(String path) {
        if (baseUrl.isBlank()) {
            return path;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) + path : baseUrl + path;
    }

    public void notifySpecialist(String text) {
        if (!isConfigured()) {
            log.debug("Telegram не настроен, уведомление пропущено");
            return;
        }

        String message = text.length() > MAX_TEXT ? text.substring(0, MAX_TEXT) + "…" : text;

        executor.submit(() -> {
            try {
                String body = "{\"chat_id\":\"" + escape(chatId) + "\","
                        + "\"text\":\"" + escape(message) + "\","
                        + "\"disable_web_page_preview\":true}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json; charset=utf-8")
                        .POST(HttpRequest.BodyPublishers.ofString(body, java.nio.charset.StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.warn("Telegram ответил {}: {}", response.statusCode(), response.body());
                }
            } catch (Exception ex) {
                log.warn("Не удалось отправить уведомление в Telegram: {}", ex.getMessage());
            }
        });
    }

    private String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(' ');
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
