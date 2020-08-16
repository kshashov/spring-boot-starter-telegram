package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.TelegramSessionResolver;
import com.github.kshashov.telegram.TestUtils;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.processor.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.arguments.BotRequestMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.response.BotBaseRequestMethodProcessor;
import com.github.kshashov.telegram.handler.processor.response.BotHandlerMethodReturnValueHandler;
import com.github.kshashov.telegram.metrics.MetricsService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestDispatcherTest {
    private HandlerMethodContainer handlerMethodContainer;
    private BotHandlerMethodArgumentResolver argumentResolver;
    private BotHandlerMethodReturnValueHandler returnValueHandler;
    private TelegramSessionResolver sessionResolver;
    private TelegramEvent telegramEvent;
    private TelegramSessionResolver.TelegramSessionHolder sessionHolder;
    private SendMessage sendMessage = new SendMessage(12, "text");
    private MetricsService metricsService;

    @BeforeEach
    void init() {
        handlerMethodContainer = mock(HandlerMethodContainer.class);
        sessionResolver = mock(TelegramSessionResolver.class);
        metricsService = mock(MetricsService.class);
        argumentResolver = new BotRequestMethodArgumentResolver();
        returnValueHandler = new BotBaseRequestMethodProcessor();

        TelegramSession session = mock(TelegramSession.class);
        sessionHolder = mock(TelegramSessionResolver.TelegramSessionHolder.class);
        when(sessionHolder.getSession()).thenReturn(session);
        when(sessionResolver.resolveTelegramSession(any())).thenReturn(sessionHolder);

        String token = "";
        TelegramBot bot = mock(TelegramBot.class);
        Update update = mock(Update.class);
        telegramEvent = new TelegramEvent(token, update, bot);
    }

    @Test
    void execute_HandlerNotFound_ReturnNull() throws Exception {
        when(handlerMethodContainer.lookupHandlerMethod(any())).thenReturn(new HandlerMethodContainer.HandlerLookupResult());
        BaseRequest result = doExecute();

        assertNull(result);
        verify(sessionResolver).resolveTelegramSession(any());
        verify(sessionHolder).releaseSessionId();
    }

    @Test
    void execute_ExceptionInHandler_ThrowException() {
        HandlerMethodContainer.HandlerLookupResult lookupResult = new HandlerMethodContainer.HandlerLookupResult(
                new HandlerMethod(this, TestUtils.findMethodByTitle(this, "methodThrows")),
                "pattern",
                new HashMap<>()
        );
        when(handlerMethodContainer.lookupHandlerMethod(any())).thenReturn(lookupResult);
        assertThrows(Exception.class, this::doExecute);

        verify(sessionResolver).resolveTelegramSession(any());
        verify(sessionHolder).releaseSessionId();
    }

    @Test
    void execute_HandlerReturnNull_ReturnNull() throws Exception {
        HandlerMethodContainer.HandlerLookupResult lookupResult = new HandlerMethodContainer.HandlerLookupResult(
                new HandlerMethod(this, TestUtils.findMethodByTitle(this, "methodNull")),
                "pattern",
                new HashMap<>()
        );
        when(handlerMethodContainer.lookupHandlerMethod(any())).thenReturn(lookupResult);
        BaseRequest result = doExecute();

        assertNull(result);

        verify(sessionResolver).resolveTelegramSession(any());
        verify(sessionHolder).releaseSessionId();
    }

    @Test
    void execute() throws Exception {
        HandlerMethodContainer.HandlerLookupResult lookupResult = new HandlerMethodContainer.HandlerLookupResult(
                new HandlerMethod(this, TestUtils.findMethodByTitle(this, "method")),
                "pattern",
                new HashMap<>()
        );
        when(handlerMethodContainer.lookupHandlerMethod(any())).thenReturn(lookupResult);
        BaseRequest result = doExecute();

        assertNotNull(result);
        assertEquals(sendMessage, result);

        verify(sessionResolver).resolveTelegramSession(any());
        verify(sessionHolder).releaseSessionId();
    }

    BaseRequest doExecute() throws Exception {
        RequestDispatcher dispatcher = new RequestDispatcher(
                handlerMethodContainer,
                sessionResolver,
                argumentResolver,
                returnValueHandler,
                metricsService);
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
