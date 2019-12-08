package com.github.kshashov.telegram.handler.config;

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

        /**
         * Specify @{@link OkHttpClient} client to be used by the Telegram bot.
         *
         * @param okHttpClient client
         * @return current instance
         */
        public Builder okHttpClient(@NotNull OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        /**
         * Specify endpoint url for Telegram bot.
         *
         * @param url url path
         * @return current instance
         */
        public Builder url(@NotNull String url) {
            this.url = url;
            return this;
        }

        /**
         * Specify timeout between requests to Telegrams API.
         *
         * @param listenerSleep sleep interval in milliseconds
         * @return current instance
         */
        public Builder sleepTimeoutMilliseconds(long listenerSleep) {
            this.listenerSleep = listenerSleep;
            return this;
        }

        public TelegramBotProperties build() {
            return new TelegramBotProperties(token, okHttpClient, url, listenerSleep);
        }
    }
}
