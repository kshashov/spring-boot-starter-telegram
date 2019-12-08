package com.github.kshashov.telegram.autoconfigure.api;

import com.github.kshashov.telegram.autoconfigure.TelegramControllerBeanPostProcessor;
import com.github.kshashov.telegram.autoconfigure.annotation.BotRequest;

/**
 * An interface from which all classes marked with annotation {@link BotRequest} must.
 * inherit
 *
 * @see BotRequest
 * @see TelegramControllerBeanPostProcessor
 */
public interface TelegramMvcController {
    String getToken();
}
