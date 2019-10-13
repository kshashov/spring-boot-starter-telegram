package com.github.kshashov.telegram.api.bind.annotation;


import com.github.kshashov.telegram.config.TelegramBotProperties;
import com.github.kshashov.telegram.config.TelegramControllerBeanPostProcessor;
import com.github.kshashov.telegram.config.TelegramMvcController;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a "Controller" (e.g. a web controller).
 *
 * <p>This annotation serves as a specialization of {@link Component @Component},
 * allowing for implementation classes to be autodetected through classpath scanning. It is supposed to used in
 * combination with annotated handler methods based on the {@link BotRequest} annotation.
 *
 * <p><strong>Note:</strong> all classes marked with annotation {@link BotController}
 * must inherit {@link TelegramMvcController}.</p>
 *
 * @see Component
 * @see BotRequest
 * @see TelegramMvcController
 * @see TelegramControllerBeanPostProcessor
 * @see TelegramBotProperties
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BotController {
}
