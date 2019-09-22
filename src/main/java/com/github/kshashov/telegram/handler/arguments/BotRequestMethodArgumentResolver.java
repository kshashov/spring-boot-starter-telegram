package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.pengrad.telegrambot.model.*;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

@Component
public class BotRequestMethodArgumentResolver implements BotHandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        if (parameter.hasParameterAnnotation(BotPathVariable.class)) {
            return false;
        }

        return TelegramRequest.class.isAssignableFrom(paramType) ||
                TelegramSession.class.isAssignableFrom(paramType) ||
                String.class.isAssignableFrom(paramType) ||
                Update.class.isAssignableFrom(paramType) ||
                Message.class.isAssignableFrom(paramType) ||
                InlineQuery.class.isAssignableFrom(paramType) ||
                ChosenInlineResult.class.isAssignableFrom(paramType) ||
                CallbackQuery.class.isAssignableFrom(paramType) ||
                ShippingQuery.class.isAssignableFrom(paramType) ||
                PreCheckoutQuery.class.isAssignableFrom(paramType) ||
                Poll.class.isAssignableFrom(paramType) ||
                Chat.class.isAssignableFrom(paramType) ||
                User.class.isAssignableFrom(paramType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, TelegramRequest telegramRequest, TelegramSession telegramSession) {

        Class<?> paramType = parameter.getParameterType();

        if (TelegramRequest.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramRequest);
        } else if (TelegramSession.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramSession);
        } else if (Update.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramRequest.getUpdate());
        } else if (Message.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramRequest.getMessage());
        } else if (User.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramRequest.getUser());
        } else if (Chat.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramRequest.getChat());
        } else if (String.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramRequest.getText());
        } else {
            Update update = telegramRequest.getUpdate();
            if (update == null) {
                return null;
            }

            if (InlineQuery.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, update.callbackQuery());
            } else if (ChosenInlineResult.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, update.chosenInlineResult());
            } else if (ShippingQuery.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, update.shippingQuery());
            } else if (PreCheckoutQuery.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, update.preCheckoutQuery());
            } else if (Poll.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, update.poll());
            }
        }
        return null;
    }

    private Object validateValue(Class<?> paramType, Object value) {
        if (value != null && !paramType.isInstance(value)) {
            throw new IllegalStateException(
                    "Current request is not of type [" + paramType.getName() + "]: " + value + "");
        }

        return value;
    }
}
