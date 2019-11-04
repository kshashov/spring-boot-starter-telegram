package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.handler.processor.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.response.BotHandlerMethodReturnValueHandler;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * Provides global configurations for all telegram bots.
 */
@Getter
@AllArgsConstructor
public class TelegramBotGlobalProperties {
    private final @NotNull ThreadPoolExecutor taskExecutor;
    private final @NotNull Callback<BaseRequest, BaseResponse> responseCallback;
    private final @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers;
    private final @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers;
    private final @NotNull Map<String, Consumer<TelegramBotProperties.Builder>> botProperties;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ThreadPoolExecutor taskExecutor;
        private Callback<BaseRequest, BaseResponse> responseCallback;
        private List<BotHandlerMethodArgumentResolver> argumentResolvers;
        private List<BotHandlerMethodReturnValueHandler> returnValueHandlers;
        private Map<String, Consumer<TelegramBotProperties.Builder>> botProperties = new HashMap<>();

        public Builder taskExecutor(@NotNull ThreadPoolExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
            return this;
        }

        public Builder responseCallback(@NotNull Callback<BaseRequest, BaseResponse> responseCallback) {
            this.responseCallback = responseCallback;
            return this;
        }

        public Builder argumentResolvers(@NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers) {
            this.argumentResolvers = argumentResolvers;
            return this;
        }

        public Builder returnValueHandlers(@NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers) {
            this.returnValueHandlers = returnValueHandlers;
            return this;
        }

        public Builder configureBot(@NotNull String token, @NotNull Consumer<TelegramBotProperties.Builder> propertiesConsumer) {
            botProperties.put(token, propertiesConsumer);
            return this;
        }

        public TelegramBotGlobalProperties build() {
            return new TelegramBotGlobalProperties(taskExecutor, responseCallback, argumentResolvers, returnValueHandlers, botProperties);
        }
    }
}
