package com.github.kshashov.telegram.api.bind.annotation;

import com.github.kshashov.telegram.handler.arguments.BotRequestMethodPathArgumentResolver;

import java.lang.annotation.*;

/**
 * Annotation which indicates that a method parameter should be bound to a request template variable. Supported for
 * {@link BotRequest} annotated handler methods.
 *
 * <p><strong>Note:</strong> Works only if and when the method parameter is {@link String}.
 *
 * @see BotRequest
 * @see BotRequestMethodPathArgumentResolver
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BotPathVariable {

    /**
     * Name of the template variable that should be bound to a method parameter
     */
    String value() default "";
}
