package com.github.kshashov.telegram.config;

import com.pengrad.telegrambot.TelegramBot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.springframework.util.Assert;

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

    private TelegramBotProperty(TelegramBotPropertyBuilder builder) {
        Assert.hasText(builder.token, "Токен должен быть задан");
        token = builder.token;
        okHttpClient = builder.okHttpClient;
        url = builder.url;
        timeOutMillis = builder.timeOutMillis;
    }

    public static TelegramBotPropertyBuilder newBuilder(TelegramBotProperty copy) {
        TelegramBotPropertyBuilder builder = new TelegramBotPropertyBuilder();
        builder.token = copy.getToken();
        builder.okHttpClient = copy.getOkHttpClient();
        builder.url = copy.getUrl();
        builder.timeOutMillis = copy.getTimeOutMillis();
        return builder;
    }
}
