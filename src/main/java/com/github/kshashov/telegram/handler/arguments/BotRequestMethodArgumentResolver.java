package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;

@Slf4j
public class BotRequestMethodArgumentResolver implements BotHandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(BotPathVariable.class)) {
            return false;
        }

        Class<?> paramType = parameter.getParameterType();
        return TelegramRequest.class.isAssignableFrom(paramType) ||
                TelegramSession.class.isAssignableFrom(paramType) ||
                TelegramBot.class.isAssignableFrom(paramType) ||
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
        } else if (TelegramBot.class.isAssignableFrom(paramType)) {
            return validateValue(paramType, telegramRequest.getTelegramBot());
        }else if (Update.class.isAssignableFrom(paramType)) {
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
            if (CallbackQuery.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, update.callbackQuery());
            } else if (InlineQuery.class.isAssignableFrom(paramType)) {
                return validateValue(paramType, update.inlineQuery());
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
            log.error("Current request is not of type [" + paramType.getName() + "]: " + value + "");
            return null;
        }

        return value;
    }
}
