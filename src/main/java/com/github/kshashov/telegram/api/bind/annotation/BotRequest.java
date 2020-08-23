package com.github.kshashov.telegram.api.bind.annotation;


import com.github.kshashov.telegram.api.MessageType;
import com.github.kshashov.telegram.api.bind.annotation.request.*;
import com.github.kshashov.telegram.handler.DefaultRequestMappingsMatcherStrategy;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation for mapping telegram requests onto methods in request-handling classes with flexible method signatures. In
 * most cases, it is preferred to use one of the telegram method specific variants {@link MessageRequest}, {@link
 * EditedMessageRequest}, {@link ChannelPostRequest}, {@link EditedChannelPostRequest}, {@link InlineQueryRequest},
 * {@link CallbackQueryRequest}, {@link ChosenInlineResultRequest}, {@link ShippingQueryRequest}, {@link PollRequest},
 * {@link PreCheckoutQueryRequest}.
 *
 * <p><strong>Note:</strong> works only with methods in the class marked with {@link BotController} annotation</p>.
 * <p><strong>Note:</strong> if the telegram request matched with several patterns at once, the result pattern will be
 * selected randomly</p>.
 *
 * @see BotController
 * @see MessageRequest
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
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BotRequest {

    /**
     * The primary mapping expressed by this annotation.
     * <p>This is an alias for {@link #path}. For example
     * {@code @BotRequest("/foo")} is equivalent to {@code @BotRequest(path="/foo")}.
     *
     * @return Request mapping templates.
     */
    @AliasFor("path")
    String[] value() default {};

    /**
     * @return Request mapping templates (e.g. "/foo"). Ant-style path patterns are also supported (e.g. "/foo *", "/foo
     * {param:[0-9]}"). An empty patterns list (= any request) has the lowest priority and will be matched only if there
     * is no match in the other methods. See {@link DefaultRequestMappingsMatcherStrategy} default comparator for route priority details.
     * @see org.springframework.util.AntPathMatcher
     */
    @AliasFor("value")
    String[] path() default {};

    /**
     * @return Telegram request types to map.
     */
    MessageType[] type() default {MessageType.ANY};
}
