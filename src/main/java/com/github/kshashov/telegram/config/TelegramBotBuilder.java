package com.github.kshashov.telegram.config;

import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Билдер конфигурации бота
 */
public class TelegramBotBuilder {

    private List<TelegramBotProperty.Builder> telegramBotProperties = new ArrayList<>();
    private TelegramBotProperty.Builder lastBuilder;

    public TelegramBotBuilderExt token(String val) {
        if (lastBuilder != null) {
            telegramBotProperties.add(lastBuilder);
        }
        lastBuilder = TelegramBotProperty.newBuilder().token(val);
        return new TelegramBotBuilderExt();
    }

    public class TelegramBotBuilderExt extends TelegramBotBuilder {
        public TelegramBotBuilderExt() {
        }

        public TelegramBotBuilderExt alias(String val) {
            lastBuilder.alias(val);
            return this;
        }

        public TelegramBotBuilderExt okHttpClient(OkHttpClient val) {
            lastBuilder.okHttpClient(val);
            return this;
        }

        public TelegramBotBuilderExt url(String val) {
            lastBuilder.url(val);
            return this;
        }

        public TelegramBotBuilderExt timeOutMillis(long val) {
            lastBuilder.timeOutMillis(val);
            return this;
        }
    }

    public TelegramBotProperties build() {
        if (lastBuilder != null) {
            telegramBotProperties.add(lastBuilder);
        }
        TelegramBotProperties result = new TelegramBotProperties();
        for (TelegramBotProperty.Builder builder : telegramBotProperties) {
            result.addTelegramProperty(builder.build());
        }
        return result;
    }

}
