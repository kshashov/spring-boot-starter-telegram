package com.github.kshashov.telegram.api.bind.annotation.request;


import com.github.kshashov.telegram.api.bind.annotation.BotRequest;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.github.kshashov.telegram.api.MessageType.CHANNEL_POST;

/**
 * Annotation for mapping channel post requests onto specific handler methods. Specifically, {@link ChannelPostRequest}
 * is a composed annotation that acts as a shortcut for {@code BotRequest(method = RequestMethod.CHANNEL_POST)}.
 *
 * @see BotRequest
 * @see MessageRequest
 * @see EditedMessageRequest
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
@BotRequest(type = CHANNEL_POST)
public @interface ChannelPostRequest {

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
