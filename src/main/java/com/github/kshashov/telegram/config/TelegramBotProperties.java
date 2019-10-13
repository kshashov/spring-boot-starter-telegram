package com.github.kshashov.telegram.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.OkHttpClient;

/**
 * Helper entity used for initialization of the new {@link com.pengrad.telegrambot.TelegramBot} instances.
 */
@Getter
@AllArgsConstructor
public class TelegramBotProperties {
    private final String token;
    private final OkHttpClient okHttpClient;
    private final String url;
    private long timeOutMillis = 100L;

    public static Builder builder(String token) {
        return new Builder(token);
    }

    public static class Builder {
        private String token;
        private OkHttpClient okHttpClient;
        private String url;
        private long timeOutMillis;

        Builder(String token) {
            this.token = token;
        }

        public Builder okHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder timeOutMillis(long timeOutMillis) {
            this.timeOutMillis = timeOutMillis;
            return this;
        }

        public TelegramBotProperties build() {
            return new TelegramBotProperties(token, okHttpClient, url, timeOutMillis);
        }
    }
}
