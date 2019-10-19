package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.task.TaskExecutor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Provides global configurations for all telegram bots.
 */
@Getter
@AllArgsConstructor
public class TelegramBotGlobalProperties {
    private @NotNull TaskExecutor taskExecutor;
    private @NotNull Callback<BaseRequest, BaseResponse> responseCallback;
    private @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers;
    private @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private @NotNull TaskExecutor taskExecutor;
        private @NotNull Callback<BaseRequest, BaseResponse> responseCallback;
        private @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers;
        private @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers;

        Builder() {
        }

        public Builder taskExecutor(@NotNull TaskExecutor taskExecutor) {
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

        public TelegramBotGlobalProperties build() {
            return new TelegramBotGlobalProperties(taskExecutor, responseCallback, argumentResolvers, returnValueHandlers);
        }
    }
}
