package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
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
    private @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers;
    private @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private @NotNull TaskExecutor taskExecutor;
        private @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers;
        private @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers;

        Builder() {
        }

        public Builder taskExecutor(@NotNull TaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
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
            return new TelegramBotGlobalProperties(taskExecutor, argumentResolvers, returnValueHandlers);
        }
    }
}
