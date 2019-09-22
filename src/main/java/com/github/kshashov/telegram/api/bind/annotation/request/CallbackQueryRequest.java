package com.github.kshashov.telegram.api.bind.annotation.request;


import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.github.kshashov.telegram.api.MessageType.CALLBACK_QUERY;

/**
 * Annotation for mapping callback query requests onto specific handler methods. Specifically, {@link
 * CallbackQueryRequest} is a composed annotation that acts as a shortcut for {@code BotRequest(method =
 * RequestMethod.CALLBACK_QUERY)}.
 *
 * @see BotRequest
 * @see MessageRequest
 * @see EditedMessageRequest
 * @see ChannelPostRequest
 * @see EditedChannelPostRequest
 * @see InlineQueryRequest
 * @see ChosenInlineResultRequest
 * @see ShippingQueryRequest
 * @see PreCheckoutQueryRequest
 * @see PollRequest
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BotRequest(type = CALLBACK_QUERY)
public @interface CallbackQueryRequest {

    /**
     * Alias for {@link BotRequest#value()}.
     * @return Request mapping templates.
     */
    @AliasFor(annotation = BotRequest.class)
    String[] value() default {};

    /**
     * Alias for {@link BotRequest#path()}.
     * @return Request mapping templates.
     */
    @AliasFor(annotation = BotRequest.class)
    String[] path() default {};
}
