package com.github.kshashov.telegram.config;

import com.pengrad.telegrambot.TelegramBot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import okhttp3.OkHttpClient;

/**
 * Helper entity used for initialization of new {@link TelegramBot} instances
 */
@Builder
@Getter
@AllArgsConstructor
public class TelegramBotProperty {
    private final String token;
    private OkHttpClient okHttpClient;
    private String url;
    private long timeOutMillis = 100L;

    public TelegramBotProperty(String token) {
        this.token = token;
    }
}
