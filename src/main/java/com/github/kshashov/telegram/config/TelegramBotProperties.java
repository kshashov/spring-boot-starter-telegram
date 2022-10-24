package com.github.kshashov.telegram.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

/**
 * Helper entity used for initialization of the new {@link com.pengrad.telegrambot.TelegramBot} instances.
 */
@Getter
@AllArgsConstructor
public class TelegramBotProperties {
    private final @NotNull String token;
    private final @NotNull TelegramBot.Builder botBuilder;
    private final SetWebhook webhook;
    private final boolean keepWebhookRegistration;

    public static Builder builder(String token) {
        return new Builder(token);
    }

    public static class Builder {
        private final TelegramBot.Builder botBuilder;
        private final String token;
        private SetWebhook webhook;
        private boolean keepWebhookRegistration;

        Builder(@NotNull String token) {
            this.token = token;
            this.botBuilder = new TelegramBot.Builder(token);
        }

        /**
         * Specify additional properties to {@link TelegramBot}.
         *
         * @param builderConsumer builder consumer
         * @return current instance
         * @since 0.21
         */
        public Builder configure(Consumer<TelegramBot.Builder> builderConsumer) {
            builderConsumer.accept(botBuilder);
            return this;
        }

        /**
         * Specify webhook that will be used to receive Telegram updates. Webhook will be removed when the bot is shut down.
         *
         * @param webhook configured webhook request. See <a href="https://core.telegram.org/bots/faq">https://core.telegram.org/bots/faq</a>
         * @return current instance
         * @since 0.21
         */
        public Builder useWebhook(@NotNull SetWebhook webhook) {
            return useWebhook(webhook, false);
        }

        /**
         * Specify webhook that will be used to receive Telegram updates.
         *
         * @param webhook                 configured webhook request. See <a href="https://core.telegram.org/bots/faq">https://core.telegram.org/bots/faq</a>
         * @param keepWebhookRegistration true if the webhook should not be removed when the bot is shut down. Сan be useful when using app hostings like heroku
         * @return current instance
         * @since 0.28
         */
        public Builder useWebhook(@NotNull SetWebhook webhook, boolean keepWebhookRegistration) {
            this.webhook = webhook;
            this.keepWebhookRegistration = keepWebhookRegistration;
            return this;
        }

        public TelegramBotProperties build() {
            return new TelegramBotProperties(token, botBuilder, webhook, keepWebhookRegistration);
        }
    }
}
