package com.github.kshashov.telegram.config;

import okhttp3.OkHttpClient;
import org.springframework.util.Assert;

public class TelegramBotProperty {
    private final String token;
    private String alias;
    private OkHttpClient okHttpClient;
    private String url;
    private long timeOutMillis = 100L;

    public String getToken() {
        return token;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public String getUrl() {
        return url;
    }

    public long getTimeOutMillis() {
        return timeOutMillis;
    }

    public TelegramBotProperty(String token) {
        this.token = token;
    }

    private TelegramBotProperty(Builder builder) {
        Assert.hasText(builder.token, "Токен должен быть задан");
        token = builder.token;
        alias = builder.alias;
        okHttpClient = builder.okHttpClient;
        url = builder.url;
        timeOutMillis = builder.timeOutMillis;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(TelegramBotProperty copy) {
        Builder builder = new Builder();
        builder.token = copy.getToken();
        builder.alias = copy.getAlias();
        builder.okHttpClient = copy.getOkHttpClient();
        builder.url = copy.getUrl();
        builder.timeOutMillis = copy.getTimeOutMillis();
        return builder;
    }

    public String getAlias() {
        return alias;
    }

    public static final class Builder {
        private String token;
        private String alias;
        private OkHttpClient okHttpClient;
        private String url;
        private long timeOutMillis;

        private Builder() {
        }

        public Builder token(String val) {
            token = val;
            return this;
        }

        public Builder alias(String val) {
            alias = val;
            return this;
        }

        public Builder okHttpClient(OkHttpClient val) {
            okHttpClient = val;
            return this;
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder timeOutMillis(long val) {
            timeOutMillis = val;
            return this;
        }

        public TelegramBotProperty build() {
            return new TelegramBotProperty(this);
        }
    }
}
