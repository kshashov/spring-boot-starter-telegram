package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.handler.RequestMappingsMatcherStrategy;
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
    private final @NotNull Integer webserverPort;
    private final @NotNull ThreadPoolExecutor taskExecutor;
    private final @NotNull RequestMappingsMatcherStrategy matcherStrategy;
    private final @NotNull Callback<BaseRequest, BaseResponse> responseCallback;
    private final @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers;
    private final @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers;
    private final @NotNull Map<String, Consumer<TelegramBotProperties.Builder>> botProperties;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, Consumer<TelegramBotProperties.Builder>> botProperties = new HashMap<>();
        private ThreadPoolExecutor taskExecutor;
        private RequestMappingsMatcherStrategy matcherStrategy;
        private Callback<BaseRequest, BaseResponse> responseCallback;
        private List<BotHandlerMethodArgumentResolver> argumentResolvers;
        private List<BotHandlerMethodReturnValueHandler> returnValueHandlers;
        private int webserverPort;

        public Builder taskExecutor(@NotNull ThreadPoolExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
            return this;
        }

        /**
         * Specify custom matcher strategy to override matcher behavior and routes post processing.
         *
         * @param matcherStrategy matcher strategy
         * @return current instance
         */
        public Builder matcherStrategy(RequestMappingsMatcherStrategy matcherStrategy) {
            this.matcherStrategy = matcherStrategy;
            return this;
        }

        /**
         * Specify callback to process result of the telegram request that was submitted by the handler method.
         *
         * @param responseCallback response callback
         * @return current instance
         */
        public Builder responseCallback(@NotNull Callback<BaseRequest, BaseResponse> responseCallback) {
            this.responseCallback = responseCallback;
            return this;
        }

        /**
         * Specify resolvers to process handler method arguments.
         *
         * @param argumentResolvers resolvers list
         * @return current instance
         */
        public Builder argumentResolvers(@NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers) {
            this.argumentResolvers = argumentResolvers;
            return this;
        }

        /**
         * Specify handlers to process result value.
         *
         * @param returnValueHandlers handlers list
         * @return current instance
         */
        public Builder returnValueHandlers(@NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers) {
            this.returnValueHandlers = returnValueHandlers;
            return this;
        }

        /**
         * Provide configuration for specified bot.
         *
         * @param token              bot token
         * @param propertiesConsumer configuration for specified bot
         * @return current instance
         */
        public Builder configureBot(@NotNull String token, @NotNull Consumer<TelegramBotProperties.Builder> propertiesConsumer) {
            botProperties.put(token, propertiesConsumer);
            return this;
        }

        /**
         * @param webserverPort HTTP port that will be used to start embedded web server if webhooks is enabled. Default value is 8443.
         * @return current instance
         */
        public Builder setWebserverPort(int webserverPort) {
            this.webserverPort = webserverPort;
            return this;
        }

        public TelegramBotGlobalProperties build() {
            return new TelegramBotGlobalProperties(webserverPort, taskExecutor, matcherStrategy, responseCallback, argumentResolvers, returnValueHandlers, botProperties);
        }
    }
}
