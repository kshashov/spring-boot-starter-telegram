package com.github.kshashov.telegram.api;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

import static com.github.kshashov.telegram.api.MessageType.COMMAND;
import static com.github.kshashov.telegram.api.MessageType.MESSAGE;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BotRequest {

    @AliasFor("path")
    String[] value() default {};

    @AliasFor("value")
    String[] path() default {};

    MessageType[] messageType() default {MESSAGE, COMMAND};
}
