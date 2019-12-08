package com.github.kshashov.telegram.autoconfigure.annotation.request;


import com.github.kshashov.telegram.autoconfigure.annotation.BotRequest;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.github.kshashov.telegram.handler.MessageType.EDITED_MESSAGE;

/**
 * Annotation for mapping edited message requests onto specific handler methods. Specifically, {@link
 * EditedMessageRequest} is a composed annotation that acts as a shortcut for {@code BotRequest(method =
 * RequestMethod.EDITED_MESSAGE)}.
 *
 * @see BotRequest
 * @see MessageRequest
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
@BotRequest(type = EDITED_MESSAGE)
public @interface EditedMessageRequest {

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
