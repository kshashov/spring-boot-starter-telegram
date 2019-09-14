package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class BotResponseBodyMethodProcessor implements BotHandlerMethodReturnValueHandler {
    private ConversionService conversionService;

    public BotResponseBodyMethodProcessor(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, TelegramRequest telegramRequest) throws Exception {
        String outputValue = null;
        Class<?> valueType;

        if (returnValue instanceof CharSequence) {
            outputValue = returnValue.toString();
        } else {
            valueType = getReturnValueType(returnValue, returnType);
            if (conversionService.canConvert(valueType, String.class)) {
                outputValue = conversionService.convert(returnValue, String.class);
            } else if (conversionService.canConvert(returnType.getParameterType(), String.class)) {
                outputValue = conversionService.convert(returnType.getParameterType(), String.class);
            }
        }

        if (outputValue != null) {
            telegramRequest.setBaseRequest(new SendMessage(telegramRequest.chatId(), outputValue));
        }
    }

    private Class<?> getReturnValueType(Object value, MethodParameter returnType) {
        return (value != null ? value.getClass() : returnType.getParameterType());
    }

}
