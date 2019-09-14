package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import org.springframework.core.MethodParameter;

/**
 * Имплементация интрефейса должна уметь обрабатывать аргументы метода который обрабатывает запрос
 */
public interface BotHandlerMethodArgumentResolver {
    boolean supportsParameter(MethodParameter parameter);

    /**
     * Праметры метода
     * @param parameter Метаданные параметра
     * @param telegramRequest Описание запроса
     * @return Значение параметра передается
     * @throws Exception Общие ошибки возникают
     */
    Object resolveArgument(MethodParameter parameter, TelegramRequest telegramRequest) throws Exception;
}
