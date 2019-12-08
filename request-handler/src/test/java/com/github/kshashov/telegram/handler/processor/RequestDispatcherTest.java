package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.TelegramRequest;
import com.github.kshashov.telegram.handler.TestUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;

public class RequestDispatcherTest {
    private HandlerMethodContainer handlerMethodContainer;
    private BotHandlerMethodArgumentResolver argumentResolver;
    private BotHandlerMethodReturnValueHandler returnValueHandler;
    private SessionResolver sessionResolver;
    private TelegramEvent telegramEvent;
    private SessionResolver.SessionHolder sessionHolder;
    private SendMessage sendMessage = new SendMessage(12, "text");

    @BeforeEach
    void init() {
        handlerMethodContainer = Mockito.mock(HandlerMethodContainer.class);
        sessionResolver = Mockito.mock(SessionResolver.class);
        argumentResolver = new BotHandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(@NotNull MethodParameter methodParameter) {
                return true;
            }

            @Nullable
            @Override
            public Object resolveArgument(@NotNull MethodParameter parameter, @NotNull TelegramRequest telegramRequest, @NotNull TelegramSession telegramSession) {
                return null;
            }
        };
        returnValueHandler = new BotHandlerMethodReturnValueHandler() {
            @Override
            public boolean supportsReturnType(@NotNull MethodParameter returnType) {
                return true;
            }

            @Nullable
            @Override
            public BaseRequest handleReturnValue(@Nullable Object returnValue, @NotNull MethodParameter returnType, @NotNull TelegramRequest telegramRequest) {
                return (returnValue instanceof SendMessage) ? (SendMessage) returnValue : null;
            }
        };

        TelegramSession session = Mockito.mock(TelegramSession.class);
        sessionHolder = Mockito.mock(SessionResolver.SessionHolder.class);
        Mockito.when(sessionHolder.getSession()).thenReturn(session);
        Mockito.when(sessionResolver.resolveTelegramSession(ArgumentMatchers.any())).thenReturn(sessionHolder);

        String token = "";
        TelegramBot bot = Mockito.mock(TelegramBot.class);
        Update update = Mockito.mock(Update.class);
        telegramEvent = new TelegramEvent(token, update, bot);
    }

    @Test
    void execute_HandlerNotFound_ReturnNull() throws Exception {
        Mockito.when(handlerMethodContainer.lookupHandlerMethod(ArgumentMatchers.any())).thenReturn(new HandlerMethodContainer.HandlerLookupResult());
        BaseRequest result = doExecute();

        Assertions.assertNull(result);
        Mockito.verify(sessionResolver).resolveTelegramSession(ArgumentMatchers.any());
        Mockito.verify(sessionHolder).releaseSession();
    }

    @Test
    void execute_ExceptionInHandler_ThrowException() {
        HandlerMethodContainer.HandlerLookupResult lookupResult = new HandlerMethodContainer.HandlerLookupResult(
                new HandlerMethod(this, TestUtils.findMethodByTitle(this, "methodThrows")),
                "pattern",
                new HashMap<>()
        );
        Mockito.when(handlerMethodContainer.lookupHandlerMethod(ArgumentMatchers.any())).thenReturn(lookupResult);
        Assertions.assertThrows(Exception.class, this::doExecute);

        Mockito.verify(sessionResolver).resolveTelegramSession(ArgumentMatchers.any());
        Mockito.verify(sessionHolder).releaseSession();
    }

    @Test
    void execute_HandlerReturnNull_ReturnNull() throws Exception {
        HandlerMethodContainer.HandlerLookupResult lookupResult = new HandlerMethodContainer.HandlerLookupResult(
                new HandlerMethod(this, TestUtils.findMethodByTitle(this, "methodNull")),
                "pattern",
                new HashMap<>()
        );
        Mockito.when(handlerMethodContainer.lookupHandlerMethod(ArgumentMatchers.any())).thenReturn(lookupResult);
        BaseRequest result = doExecute();

        Assertions.assertNull(result);

        Mockito.verify(sessionResolver).resolveTelegramSession(ArgumentMatchers.any());
        Mockito.verify(sessionHolder).releaseSession();
    }

    @Test
    void execute() throws Exception {
        HandlerMethodContainer.HandlerLookupResult lookupResult = new HandlerMethodContainer.HandlerLookupResult(
                new HandlerMethod(this, TestUtils.findMethodByTitle(this, "method")),
                "pattern",
                new HashMap<>()
        );
        Mockito.when(handlerMethodContainer.lookupHandlerMethod(ArgumentMatchers.any())).thenReturn(lookupResult);
        BaseRequest result = doExecute();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(sendMessage, result);

        Mockito.verify(sessionResolver).resolveTelegramSession(ArgumentMatchers.any());
        Mockito.verify(sessionHolder).releaseSession();
    }

    BaseRequest doExecute() throws Exception {
        RequestDispatcher dispatcher = new RequestDispatcher(
                handlerMethodContainer,
                sessionResolver,
                argumentResolver,
                returnValueHandler);
        return dispatcher.execute(telegramEvent);
    }

    BaseRequest method() {
        return sendMessage;
    }

    BaseRequest methodNull() {
        return null;
    }

    BaseRequest methodThrows() {
        throw new IndexOutOfBoundsException();
    }
}
