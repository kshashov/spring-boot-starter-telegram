package com.github.kshashov.telegram.api.bind.annotation.request;


import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.github.kshashov.telegram.api.MessageType.MESSAGE;

/**
 * Annotation for mapping message requests onto specific handler methods. Specifically, {@link MessageRequest} is a
 * composed annotation that acts as a shortcut for {@code BotRequest(method = RequestMethod.MESSAGE)}.
 *
 * @see BotRequest
 * @see EditedMessageRequest
 * @see ChannelPostRequest
 * @see EditedChannelPostRequest
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
@BotRequest(type = MESSAGE)
public @interface MessageRequest {

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
