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
            return telegramRequest;
        } else if (TelegramSession.class.isAssignableFrom(paramType)) {
            return telegramSession;
        } else if (Update.class.isAssignableFrom(paramType)) {
            return telegramRequest.getUpdate();
        } else if (Message.class.isAssignableFrom(paramType)) {
            if (telegramRequest.getMessage() != null && !paramType.isInstance(telegramRequest.getMessage())) {
                throw new IllegalStateException(
                        "Current request is not of type [" + paramType.getName() + "]: " + telegramRequest.getMessage());
            }
            return telegramRequest.getMessage();
        } else if (User.class.isAssignableFrom(paramType)) {
            if (telegramRequest.getUser() != null && !paramType.isInstance(telegramRequest.getUser())) {
                throw new IllegalStateException(
                        "Current request is not of type [" + paramType.getName() + "]: " + telegramRequest.getUser());
            }
            return telegramRequest.getUser();
        } else if (Chat.class.isAssignableFrom(paramType)) {
            if (telegramRequest.getChat() != null && !paramType.isInstance(telegramRequest.getChat())) {
                throw new IllegalStateException(
                        "Current request is not of type [" + paramType.getName() + "]: " + telegramRequest.getChat());
            }
            return telegramRequest.getChat();
        } else if (String.class.isAssignableFrom(paramType)) {
            if (telegramRequest.getText() != null && !paramType.isInstance(telegramRequest.getText())) {
                throw new IllegalStateException(
                        "Current request is not of type [" + paramType.getName() + "]: " + telegramRequest.getText());
            }
            return telegramRequest.getText();
        } else {
            Update update = telegramRequest.getUpdate();
            if (InlineQuery.class.isAssignableFrom(paramType)) {
                if (update.callbackQuery() != null && !paramType.isInstance(update.callbackQuery())) {
                    throw new IllegalStateException(
                            "Current request is not of type [" + paramType.getName() + "]: " + update.callbackQuery());
                }
                return update.callbackQuery();
            } else if (ChosenInlineResult.class.isAssignableFrom(paramType)) {
                if (update.chosenInlineResult() != null && !paramType.isInstance(update.chosenInlineResult())) {
                    throw new IllegalStateException(
                            "Current request is not of type [" + paramType.getName() + "]: " + update.chosenInlineResult());
                }
                return update.chosenInlineResult();
            } else if (ShippingQuery.class.isAssignableFrom(paramType)) {
                if (update.shippingQuery() != null && !paramType.isInstance(update.shippingQuery())) {
                    throw new IllegalStateException(
                            "Current request is not of type [" + paramType.getName() + "]: " + update.shippingQuery());
                }
                return update.shippingQuery();
            } else if (PreCheckoutQuery.class.isAssignableFrom(paramType)) {
                if (update.preCheckoutQuery() != null && !paramType.isInstance(update.preCheckoutQuery())) {
                    throw new IllegalStateException(
                            "Current request is not of type [" + paramType.getName() + "]: " + update.preCheckoutQuery());
                }
                return update.preCheckoutQuery();
            } else if (Poll.class.isAssignableFrom(paramType)) {
                if (update.poll() != null && !paramType.isInstance(update.poll())) {
                    throw new IllegalStateException(
                            "Current request is not of type [" + paramType.getName() + "]: " + update.poll());
                }
                return update.preCheckoutQuery();
            }
        }
        return null;
    }
}
