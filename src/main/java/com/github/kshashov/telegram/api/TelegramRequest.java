package com.github.kshashov.telegram.api;

import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Accumulates all available parameters from the initial request, the path pattern and path variables.
 *
 * @see BaseRequest
 */
@Getter
@RequiredArgsConstructor
public class TelegramRequest {
    /**
     * Bot instance that received the current telegram event.
     */
    private final TelegramBot telegramBot;

    /**
     * The initial user request which is currently being processed.
     */
    private final Update update;

    /**
     * Type of the current telegram request.
     */
    private final MessageType messageType;

    /**
     * A path pattern from {@link BotRequest} annotation that matches the current request.
     */
    private final String basePattern;

    /**
     * All path variables parsed from the {@link #basePattern} field.
     */
    private final Map<String, String> templateVariables;

    /**
     * The first non-empty object, if any, among:
     * <ul>
     *     <li>telegram message</li>
     *     <li>telegram edited message</li>
     *     <li>telegram channel post</li>
     *     <li>telegram edited channel post</li>
     * </ul>
     */
    private final Message message;

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
    private final String text;

    /**
     * Сhat instance if it present in the current telegram request.
     */
    private final Chat chat;

    /**
     * User instance if it present in the current telegram request.
     */
    private final User user;

    /**
     * Callback
     */
    @Nullable
    @Setter
    private Callback<BaseRequest, BaseResponse> callback;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TelegramRequest{");
        sb.append("chat=").append(chat);
        sb.append(", user=").append(user);
        sb.append(", text='").append(text).append('\'');
        sb.append(", messageType=").append(messageType);
        sb.append('}');
        return sb.toString();
    }
}
