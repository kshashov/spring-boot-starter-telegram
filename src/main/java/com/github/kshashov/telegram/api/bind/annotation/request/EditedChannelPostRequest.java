package com.github.kshashov.telegram.api.bind.annotation.request;


import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.github.kshashov.telegram.api.MessageType.EDITED_CHANNEL_POST;

/**
 * Annotation for edited channel post requests onto specific handler methods. Specifically, {@link
 * EditedChannelPostRequest} is a composed annotation that acts as a shortcut for @BotRequest(method =
 * RequestMethod.EDITED_CHANNEL_POST).
 *
 * @see BotRequest
 * @see MessageRequest
 * @see EditedMessageRequest
 * @see ChannelPostRequest
 * @see InlineQueryRequest
 * @see CallbackQueryRequest
 * @see ChosenInlineResultRequest
 * @see ShippingQueryRequest
 * @see PreCheckoutQueryRequest
 * @see PollRequest
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BotRequest(type = EDITED_CHANNEL_POST)
public @interface EditedChannelPostRequest {

    /**
     * Alias for {@link BotRequest#value()}.
     */
    @AliasFor(annotation = BotRequest.class)
    String[] value() default {};

    /**
     * Alias for {@link BotRequest#path()}.
     */
    @AliasFor(annotation = BotRequest.class)
    String[] path() default {};
}
