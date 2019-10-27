package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.TestUtils;
import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestBotHandlerMethodArgumentResolverComposite {

    private MethodParameter[] values;
    private List<BotHandlerMethodArgumentResolver> resolvers = new ArrayList<>();
    private TelegramRequest telegramRequest;
    private TelegramSession telegramSession;

    @BeforeEach
    public void prepare() {
        this.values = Stream.of(TestUtils.findMethod(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        for (int i = 0; i < 10; i++) {
            BotHandlerMethodArgumentResolver handler = mock(BotHandlerMethodArgumentResolver.class);
            when(handler.supportsParameter(any())).thenReturn(false);
            resolvers.add(handler);
        }

        this.telegramSession = mock(TelegramSession.class);
        this.telegramRequest = mock(TelegramRequest.class);
    }

    @Test
    public void supportsParameter_WithoutResolvers_ReturnFalse() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(new ArrayList<>());

        assertFalse(processor.supportsParameter(values[0]));
        assertFalse(processor.supportsParameter(values[1]));
        assertFalse(processor.supportsParameter(values[2]));
        assertFalse(processor.supportsParameter(values[3]));
        assertFalse(processor.supportsParameter(values[4]));
    }

    @Test
    public void supportsParameter_UnsupportedArguments_ReturnFalse() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        assertFalse(processor.supportsParameter(values[0]));
        assertFalse(processor.supportsParameter(values[1]));
        assertFalse(processor.supportsParameter(values[2]));
        assertFalse(processor.supportsParameter(values[3]));
        assertFalse(processor.supportsParameter(values[4]));

        resolvers.forEach(h -> {
            verify(h, times(5)).supportsParameter(any());
            verifyNoMoreInteractions(h);
        });
    }

    @Test
    public void supportsParameter() {
        BotHandlerMethodArgumentResolver handler = mock(BotHandlerMethodArgumentResolver.class);
        when(handler.supportsParameter(any(MethodParameter.class))).thenAnswer(any ->
                SendMessage.class.isAssignableFrom(any.<MethodParameter>getArgument(0).getParameterType()));
        resolvers.add(4, handler);

        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        assertTrue(processor.supportsParameter(values[4]));

        resolvers.stream()
                .limit(5)
                .forEach(h -> {
                    verify(h, times(1)).supportsParameter(any());
                    verifyNoMoreInteractions(h);
                });

        resolvers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    @Test
    public void resolveArgument_WithoutResolvers_ReturnNull() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(new ArrayList<>());

        assertNull(processor.resolveArgument(values[4], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_UnsupportedArgument_ReturnNull() {
        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        assertNull(processor.resolveArgument(values[4], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument() {
        BotHandlerMethodArgumentResolver handler = mock(BotHandlerMethodArgumentResolver.class);
        when(handler.supportsParameter(any(MethodParameter.class))).thenReturn(true);
        when(handler.resolveArgument(any(), any(), any())).thenReturn(new SendMessage(12L, "text"));

        resolvers.add(4, handler);

        BotHandlerMethodArgumentResolverComposite processor =
                new BotHandlerMethodArgumentResolverComposite(resolvers);

        Object result = processor.resolveArgument(values[4], telegramRequest, telegramSession);
        assertNotNull(result);
        assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        assertEquals(sendMessage.getParameters().get("text"), "text");

        verify(handler).supportsParameter(any());
        verify(handler).resolveArgument(any(), any(), any());
        verifyNoMoreInteractions(handler);

        resolvers.stream()
                .limit(4)
                .forEach(h -> {
                    verify(h).supportsParameter(any());
                    verifyNoMoreInteractions(h);
                });

        resolvers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
