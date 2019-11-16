package com.github.kshashov.telegram.handler.processor.response;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

/**
 * Add support for {@link String} return type.
 */
@Component
public class BotResponseBodyMethodProcessor implements BotHandlerMethodReturnValueHandler {
    final private ConversionService conversionService;

    public BotResponseBodyMethodProcessor(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public BaseRequest handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) {
        String outputValue = null;
        Class<?> valueType;

        if (returnValue instanceof CharSequence) {
            outputValue = returnValue.toString();
        } else {
            valueType = returnValue != null
                    ? returnValue.getClass()
                    : returnType.getParameterType();

            if (conversionService.canConvert(valueType, String.class)) {
                outputValue = conversionService.convert(returnValue, String.class);
            }
        }

        if (outputValue != null) {
            if (telegramRequest.getChat() != null) {
                return new SendMessage(telegramRequest.getChat().id(), outputValue);
            }
        }

        return null;
    }
}
