package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.api.MessageType;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.BaseRequest;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Accumulates all available parameters from the initial telegram request.
 *
 * @see BaseRequest
 */
@Getter
public class TelegramEvent {
    /**
     * The initial user request which is currently being processed.
     */
    private final @NotNull Update update;

    /**
     * The first non-empty object, if any, among:
     * <ul>
     *     <li>telegram message</li>
     *     <li>telegram edited message</li>
     *     <li>telegram channel post</li>
     *     <li>telegram edited channel post</li>
     * </ul>
     */
    @Nullable
    private final Message message;

    /**
     * Сhat instance if it present in the current telegram request.
     */
    @Nullable
    private final Chat chat;

    /**
     * User instance if it present in the current telegram request.
     */
    @Nullable
    private final User user;

    /**
     * The first non-empty object, if any, among:
     * <ul>
     *     <li>{@code message.text()}</li>
     *     <li>{@code update.inlineQuery.query()}</li>
     *     <li>{@code update.chosenInlineResult.query()}</li>
     *     <li>{@code update.callbackQuery.data()}</li>
     *     <li>{@code update.shippingQuery.invoicePayload()</li>
     *     <li>{@code update.preCheckoutQuery.invoicePayload()</li>
     * </ul>
     */
    @Nullable
    private final String text;

    /**
     * Bot instance that received the current telegram event.
     */

    private final @NotNull TelegramBot telegramBot;

    /**
     * Type of the current telegram request.
     */
    private final @NotNull MessageType messageType;

    private final String token;

    public TelegramEvent(@NotNull String token, @NotNull Update update, @NotNull TelegramBot telegramBot) {
        this.token = token;
        this.telegramBot = telegramBot;
        this.update = update;
        this.message = firstNonNull(update.message(),
                update.editedMessage(),
                update.channelPost(),
                update.editedChannelPost());

        if (message != null) {
            this.user = firstNonNull(message.from(), message.leftChatMember(), message.forwardFrom());
            this.chat = firstNonNull(message.chat(), message.forwardFromChat());
            this.text = message.text();
            if (update.editedMessage() != null) {
                this.messageType = MessageType.EDITED_MESSAGE;
            } else if (update.channelPost() != null) {
                this.messageType = MessageType.CHANNEL_POST;
            } else if (update.editedChannelPost() != null) {
                this.messageType = MessageType.EDITED_CHANNEL_POST;
            } else {
                this.messageType = MessageType.MESSAGE;
            }
        } else if (update.inlineQuery() != null) {
            InlineQuery inlineQuery = update.inlineQuery();
            this.user = inlineQuery.from();
            this.text = inlineQuery.query();
            this.chat = null;
            this.messageType = MessageType.INLINE_QUERY;
        } else if (update.chosenInlineResult() != null) {
            ChosenInlineResult chosenInlineResult = update.chosenInlineResult();
            this.user = chosenInlineResult.from();
            this.text = chosenInlineResult.query();
            this.chat = null;
            this.messageType = MessageType.CHOSEN_INLINE_RESULT;
        } else if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            this.user = callbackQuery.from();
            this.text = callbackQuery.data();
            this.chat = callbackQuery.message().chat();
            this.messageType = MessageType.CALLBACK_QUERY;
        } else if (update.shippingQuery() != null) {
            ShippingQuery shippingQuery = update.shippingQuery();
            this.user = shippingQuery.from();
            this.text = shippingQuery.invoicePayload();
            this.chat = null;
            this.messageType = MessageType.SHIPPING_QUERY;
        } else if (update.preCheckoutQuery() != null) {
            PreCheckoutQuery preCheckoutQuery = update.preCheckoutQuery();
            this.user = preCheckoutQuery.from();
            this.text = preCheckoutQuery.invoicePayload();
            this.chat = null;
            this.messageType = MessageType.PRECHECKOUT_QUERY;
        } else if (update.poll() != null) {
            this.user = null;
            this.text = update.poll().question();
            this.chat = null;
            this.messageType = MessageType.POLL;
        } else {
            this.user = null;
            this.text = null;
            this.chat = null;
            this.messageType = MessageType.UNSUPPORTED;
        }
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... messages) {
        for (T message : messages) {
            if (message != null) {
                return message;
            }
        }
        return null;
    }
}
