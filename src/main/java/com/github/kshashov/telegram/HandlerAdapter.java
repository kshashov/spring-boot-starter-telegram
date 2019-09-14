package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolverComposite;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandlerComposite;
import com.pengrad.telegrambot.request.BaseRequest;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

/**
 * Вызывает обработчик запроса, подготавливает параметры метода для выполнения
 */
public class HandlerAdapter {

    private final BotHandlerMethodArgumentResolverComposite argumentResolvers;
    private final BotHandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public HandlerAdapter(List<BotHandlerMethodArgumentResolver> resolvers, List<BotHandlerMethodReturnValueHandler> handlers) {
        this.argumentResolvers = new BotHandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        this.returnValueHandlers = new BotHandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
    }

    /**
     * Вызывает медот представленный в handlerMethod
     *
     * @param telegramRequest описание сообщение
     * @param handlerMethod   описание медода который нужно вызвать
     * @return Возвращает ответ который нужно передать пользователь
     * @throws Exception пробрасывает все ошибки
     */
    public BaseRequest handle(TelegramRequest telegramRequest, HandlerMethod handlerMethod) throws Exception {
        TelegramInvocableHandlerMethod invocableMethod = new TelegramInvocableHandlerMethod(handlerMethod);
        invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
        invocableMethod.invokeAndHandle(telegramRequest);
        return telegramRequest.getBaseRequest();
    }
}
