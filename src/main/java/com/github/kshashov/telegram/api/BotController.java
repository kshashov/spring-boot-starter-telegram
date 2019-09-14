package com.github.kshashov.telegram.api;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Анотация помечаем контроллер который будет обрабатывать команды
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BotController {
}
