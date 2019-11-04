package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.TestUtils;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.handler.processor.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.response.BotHandlerMethodReturnValueHandler;
import com.pengrad.telegrambot.request.BaseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TelegramInvocableHandlerMethodTest {
    private TelegramRequest telegramRequest;
    private List<BotHandlerMethodArgumentResolver> argumentResolvers;
    private List<BotHandlerMethodReturnValueHandler> returnValueHandlers;
    private TelegramSession telegramSession;

    @BeforeEach
    void init() {
        telegramRequest = mock(TelegramRequest.class);
        telegramSession = mock(TelegramSession.class);
        argumentResolvers = new ArrayList<>();
        returnValueHandlers = new ArrayList<>();
    }

    @Test
    void invokeAndHandle_ExceptionInHandler_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testExceptionResponseMethod");

        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);
        assertThrows(IllegalStateException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInArgumentResolverResolveArgument_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testCorrectMethod");

        BotHandlerMethodArgumentResolver resolver = mock(BotHandlerMethodArgumentResolver.class);
        when(resolver.supportsParameter(any())).thenReturn(true);
        when(resolver.resolveArgument(any(), any(), any())).thenThrow(IllegalArgumentException.class);
        argumentResolvers.add(resolver);

        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        assertThrows(IllegalArgumentException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInArgumentResolverSupportsParameter_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testCorrectMethod");

        BotHandlerMethodArgumentResolver resolver = mock(BotHandlerMethodArgumentResolver.class);
        when(resolver.supportsParameter(any())).thenThrow(NullPointerException.class);
        when(resolver.resolveArgument(any(), any(), any())).thenReturn("test");
        argumentResolvers.add(resolver);

        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        assertThrows(NullPointerException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInReturnValueHandlerHandleReturnValue_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testWithoutArgumentsMethod");

        BotHandlerMethodReturnValueHandler handler = mock(BotHandlerMethodReturnValueHandler.class);
        when(handler.supportsReturnType(any())).thenReturn(true);
        when(handler.handleReturnValue(any(), any(), any())).thenThrow(IllegalArgumentException.class);
        returnValueHandlers.add(handler);

        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        assertThrows(IllegalArgumentException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInReturnValueHandlerSupportsReturnType_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testWithArgumentMethod");

        BotHandlerMethodReturnValueHandler handler = mock(BotHandlerMethodReturnValueHandler.class);
        when(handler.supportsReturnType(any())).thenThrow(NullPointerException.class);
        when(handler.handleReturnValue(any(), any(), any())).thenReturn(null);
        returnValueHandlers.add(handler);

        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        assertThrows(NullPointerException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_UnsupportedParameter_NullParameter() throws Exception {
        HandlerMethod handlerMethod = handlerMethod("testNullParameterMethod");
        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        invocable.invokeAndHandle(telegramRequest, telegramSession);
    }

    @Test
    void invokeAndHandle_NullParameter_NullParameter() throws Exception {
        HandlerMethod handlerMethod = handlerMethod("testNullParameterMethod");
        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        BotHandlerMethodArgumentResolver resolver = mock(BotHandlerMethodArgumentResolver.class);
        when(resolver.supportsParameter(any())).thenReturn(true);
        when(resolver.resolveArgument(any(), any(), any())).thenReturn(null);
        argumentResolvers.add(resolver);

        invocable.invokeAndHandle(telegramRequest, telegramSession);
    }

    @Test
    void invokeAndHandle_UnsupportedReturnValue_ReturnNull() throws Exception {
        HandlerMethod handlerMethod = handlerMethod("testWithoutArgumentsMethod");
        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        assertNull(invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_NullReturnValue_ReturnNull() throws Exception {
        HandlerMethod handlerMethod = handlerMethod("testWithoutArgumentsMethod");

        BotHandlerMethodReturnValueHandler handler = mock(BotHandlerMethodReturnValueHandler.class);
        when(handler.supportsReturnType(any())).thenReturn(true);
        when(handler.handleReturnValue(any(), any(), any())).thenReturn(null);
        returnValueHandlers.add(handler);

        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        assertNull(invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle() throws Exception {
        HandlerMethod handlerMethod = handlerMethod("testCorrectMethod");

        BotHandlerMethodArgumentResolver resolver = mock(BotHandlerMethodArgumentResolver.class);
        when(resolver.supportsParameter(any())).thenReturn(true);
        when(resolver.resolveArgument(any(), any(), any())).thenReturn("resolved");
        argumentResolvers.add(resolver);

        BaseRequest handled = mock(BaseRequest.class);
        BotHandlerMethodReturnValueHandler handler = mock(BotHandlerMethodReturnValueHandler.class);
        when(handler.supportsReturnType(any())).thenReturn(true);
        when(handler.handleReturnValue(any(), any(), any())).then((value) -> {
            // check that argument was passed correctly
            assertEquals("test", value.getArgument(0));
            return handled;
        });
        returnValueHandlers.add(handler);

        TelegramInvocableHandlerMethod invocable = new TelegramInvocableHandlerMethod(handlerMethod, argumentResolvers, returnValueHandlers);

        assertEquals(handled, invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    String testCorrectMethod(String text) {
        assertEquals("resolved", text);
        return "test";
    }

    String testWithArgumentMethod(String text) {
        return "test";
    }

    String testWithoutArgumentsMethod() {
        return "test";
    }

    String testNullParameterMethod(String text) {
        assertNull(text);
        return null;
    }

    String testExceptionResponseMethod() {
        throw new IllegalArgumentException("test");
    }

    HandlerMethod handlerMethod(String methodName) {
        Method method = TestUtils.findMethod(this, methodName);
        return new HandlerMethod(this, method);
    }
}
