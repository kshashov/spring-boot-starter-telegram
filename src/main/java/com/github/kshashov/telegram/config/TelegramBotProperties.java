package com.github.kshashov.telegram.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.OkHttpClient;

import javax.validation.constraints.NotNull;

/**
 * Helper entity used for initialization of the new {@link com.pengrad.telegrambot.TelegramBot} instances.
 */
@Getter
@AllArgsConstructor
public class TelegramBotProperties {
    private final @NotNull String token;
    private final @NotNull OkHttpClient okHttpClient;
    private final @NotNull String url;
    private final @NotNull long listenerSleep;

    public static Builder builder(String token) {
        return new Builder(token);
    }

    public static class Builder {
        private final String token;
        private OkHttpClient okHttpClient;
        private String url;
        private long listenerSleep;

        Builder(@NotNull String token) {
            this.token = token;
        }

        public Builder okHttpClient(@NotNull OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder url(@NotNull String url) {
            this.url = url;
            return this;
        }

        public Builder listenerSleepMilliseconds(long listenerSleep) {
            this.listenerSleep = listenerSleep;
            return this;
        }

        public TelegramBotProperties build() {
            return new TelegramBotProperties(token, okHttpClient, url, listenerSleep);
        }
    }
}
