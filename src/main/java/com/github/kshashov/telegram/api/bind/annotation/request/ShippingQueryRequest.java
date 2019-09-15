package com.github.kshashov.telegram.api.bind.annotation.request;


import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.github.kshashov.telegram.api.MessageType.SHIPPING_QUERY;

/**
 * Annotation for mapping shipping query requests onto specific handler methods. Specifically, {@code
 * @ShippingQueryRequest} is a composed annotation that acts as a shortcut for @BotRequest(method =
 * RequestMethod.SHIPPING_QUERY).
 *
 * @see BotRequest
 * @see MessageRequest
 * @see EditedMessageRequest
 * @see ChannelPostRequest
 * @see EditedChannelPostRequest
 * @see InlineQueryRequest
 * @see CallbackQueryRequest
 * @see ChosenInlineResultRequest
 * @see PreCheckoutQueryRequest
 * @see PollRequest
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BotRequest(type = SHIPPING_QUERY)
public @interface ShippingQueryRequest {

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
