package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.handler.TelegramRequest;
import com.github.kshashov.telegram.handler.TestUtils;
import com.pengrad.telegrambot.request.BaseRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;

public class TelegramInvocableHandlerMethodTest {
    private TelegramRequest telegramRequest;
    private BotHandlerMethodArgumentResolver argumentResolver;
    private BotHandlerMethodReturnValueHandler returnValueHandler;
    private TelegramSession telegramSession;

    @BeforeEach
    void init() {
        telegramRequest = Mockito.mock(TelegramRequest.class);
        telegramSession = Mockito.mock(TelegramSession.class);
        argumentResolver = new BotHandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(@NotNull MethodParameter methodParameter) {
                return false;
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
                return false;
            }

            @Nullable
            @Override
            public BaseRequest handleReturnValue(@Nullable Object returnValue, @NotNull MethodParameter returnType, @NotNull TelegramRequest telegramRequest) {
                return null;
            }
        };
    }

    @Test
    void invokeAndHandle_ExceptionInHandler_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testExceptionResponseMethod");

        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, argumentResolver, returnValueHandler);
        Assertions.assertThrows(IllegalStateException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInArgumentResolverResolveArgument_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testCorrectMethod");

        BotHandlerMethodArgumentResolver resolver = Mockito.mock(BotHandlerMethodArgumentResolver.class);
        Mockito.when(resolver.supportsParameter(ArgumentMatchers.any())).thenReturn(true);
        Mockito.when(resolver.resolveArgument(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenThrow(IllegalArgumentException.class);
        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, resolver, returnValueHandler);

        Assertions.assertThrows(IllegalArgumentException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInArgumentResolverSupportsParameter_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testCorrectMethod");

        BotHandlerMethodArgumentResolver resolver = Mockito.mock(BotHandlerMethodArgumentResolver.class);
        Mockito.when(resolver.supportsParameter(ArgumentMatchers.any())).thenThrow(NullPointerException.class);
        Mockito.when(resolver.resolveArgument(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn("test");

        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, resolver, returnValueHandler);

        Assertions.assertThrows(NullPointerException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInReturnValueHandlerHandleReturnValue_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testWithoutArgumentsMethod");

        BotHandlerMethodReturnValueHandler handler = Mockito.mock(BotHandlerMethodReturnValueHandler.class);
        Mockito.when(handler.supportsReturnType(ArgumentMatchers.any())).thenReturn(true);
        Mockito.when(handler.handleReturnValue(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenThrow(IllegalArgumentException.class);

        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, argumentResolver, handler);

        Assertions.assertThrows(IllegalArgumentException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_ExceptionInReturnValueHandlerSupportsReturnType_ThrowException() {
        HandlerMethod handlerMethod = handlerMethod("testWithArgumentMethod");

        BotHandlerMethodReturnValueHandler handler = Mockito.mock(BotHandlerMethodReturnValueHandler.class);
        Mockito.when(handler.supportsReturnType(ArgumentMatchers.any())).thenThrow(NullPointerException.class);
        Mockito.when(handler.handleReturnValue(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);

        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, argumentResolver, handler);

        Assertions.assertThrows(NullPointerException.class, () -> invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_UnsupportedParameter_NullParameter() {
        HandlerMethod handlerMethod = handlerMethod("testNullParameterMethod");
        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, argumentResolver, returnValueHandler);

        invocable.invokeAndHandle(telegramRequest, telegramSession);
    }

    @Test
    void invokeAndHandle_NullParameter_NullParameter() {
        HandlerMethod handlerMethod = handlerMethod("testNullParameterMethod");

        BotHandlerMethodArgumentResolver resolver = Mockito.mock(BotHandlerMethodArgumentResolver.class);
        Mockito.when(resolver.supportsParameter(ArgumentMatchers.any())).thenReturn(true);
        Mockito.when(resolver.resolveArgument(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);

        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, resolver, returnValueHandler);


        invocable.invokeAndHandle(telegramRequest, telegramSession);
    }

    @Test
    void invokeAndHandle_UnsupportedReturnValue_ReturnNull() {
        HandlerMethod handlerMethod = handlerMethod("testWithoutArgumentsMethod");
        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, argumentResolver, returnValueHandler);

        Assertions.assertNull(invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle_NullReturnValue_ReturnNull() {
        HandlerMethod handlerMethod = handlerMethod("testWithoutArgumentsMethod");

        BotHandlerMethodReturnValueHandler handler = Mockito.mock(BotHandlerMethodReturnValueHandler.class);
        Mockito.when(handler.supportsReturnType(ArgumentMatchers.any())).thenReturn(true);
        Mockito.when(handler.handleReturnValue(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);

        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, argumentResolver, handler);

        Assertions.assertNull(invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    @Test
    void invokeAndHandle() {
        HandlerMethod handlerMethod = handlerMethod("testCorrectMethod");

        BotHandlerMethodArgumentResolver resolver = Mockito.mock(BotHandlerMethodArgumentResolver.class);
        Mockito.when(resolver.supportsParameter(ArgumentMatchers.any())).thenReturn(true);
        Mockito.when(resolver.resolveArgument(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn("resolved");

        BaseRequest handled = Mockito.mock(BaseRequest.class);
        BotHandlerMethodReturnValueHandler handler = Mockito.mock(BotHandlerMethodReturnValueHandler.class);
        Mockito.when(handler.supportsReturnType(ArgumentMatchers.any())).thenReturn(true);
        Mockito.when(handler.handleReturnValue(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).then((value) -> {
            // check that argument was passed correctly
            Assertions.assertEquals("test", value.getArgument(0));
            return handled;
        });

        TelegramInvocableHandlerMethod invocable = invocable(handlerMethod, resolver, handler);

        Assertions.assertEquals(handled, invocable.invokeAndHandle(telegramRequest, telegramSession));
    }

    String testCorrectMethod(String text) {
        Assertions.assertEquals("resolved", text);
        return "test";
    }

    String testWithArgumentMethod(String text) {
        return "test";
    }

    String testWithoutArgumentsMethod() {
        return "test";
    }

    String testNullParameterMethod(String text) {
        Assertions.assertNull(text);
        return null;
    }

    String testExceptionResponseMethod() {
        throw new IllegalArgumentException("test");
    }

    HandlerMethod handlerMethod(String methodName) {
        Method method = TestUtils.findMethodByTitle(this, methodName);
        return new HandlerMethod(this, method);
    }

    TelegramInvocableHandlerMethod invocable(HandlerMethod handlerMethod, BotHandlerMethodArgumentResolver argumentResolver, BotHandlerMethodReturnValueHandler returnValueHandler) {
        return new TelegramInvocableHandlerMethod(
                handlerMethod,
                argumentResolver,
                returnValueHandler);
    }
}
