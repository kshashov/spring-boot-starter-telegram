package com.github.kshashov.telegram.handler;

import com.pengrad.telegrambot.model.Update;

/**
 * Java 5 enumeration of telegram request methods. All members of the enumeration correspond to the {@link Update} fields.
 *
 * @see Update
 */
public enum MessageType {
    /**
     * Used for all supported telegram requests.
     */
    ANY,

    /**
     * Used when {@link Update#message()} is not null for the current telegram request.
     */
    MESSAGE,

    /**
     * Used when {@link Update#editedMessage()} is not null for the current telegram request.
     */
    EDITED_MESSAGE,

    /**
     * Used when {@link Update#channelPost()} is not null for the current telegram request.
     */
    CHANNEL_POST,

    /**
     * Used when {@link Update#editedChannelPost()} is not null for the current telegram request.
     */
    EDITED_CHANNEL_POST,

    /**
     * Used when {@link Update#inlineQuery()} is not null for the current telegram request.
     */
    INLINE_QUERY,

    /**
     * Used when {@link Update#chosenInlineResult()} is not null for the current telegram request.
     */
    CHOSEN_INLINE_RESULT,

    /**
     * Used when {@link Update#callbackQuery()} is not null for the current telegram request.
     */
    CALLBACK_QUERY,

    /**
     * Used when {@link Update#shippingQuery()} is not null for the current telegram request.
     */
    SHIPPING_QUERY,

    /**
     * Used when {@link Update#preCheckoutQuery()} is not null for the current telegram request.
     */
    PRECHECKOUT_QUERY,

    /**
     * Used when {@link Update#poll()} is not null for the current telegram request.
     */
    POLL,

    /**
     * For new types of telegram requests that are not yet supported.
     */
    UNSUPPORTED
}
