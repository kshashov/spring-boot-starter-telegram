package com.github.kshashov.telegram.handler.response;

import com.github.kshashov.telegram.api.TelegramRequest;
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

public class TestBotHandlerMethodReturnValueHandlerComposite {

    private MethodParameter[] values;
    private List<BotHandlerMethodReturnValueHandler> handlers = new ArrayList<>();
    private TelegramRequest telegramRequest;

    @BeforeEach
    public void prepare() throws NoSuchMethodException {
        this.values = Stream.of(this.getClass().getMethod("method", String.class, int.class, Integer.class, BaseRequest.class, SendMessage.class).getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        for (int i = 0; i < 10; i++) {
            BotHandlerMethodReturnValueHandler handler = mock(BotHandlerMethodReturnValueHandler.class);
            when(handler.supportsReturnType(any())).thenReturn(false);
            handlers.add(handler);
        }

        this.telegramRequest = mock(TelegramRequest.class);
    }

    @Test
    public void supportsReturnType_WithoutResolvers_ReturnFalse() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(new ArrayList<>());

        assertFalse(processor.supportsReturnType(values[0]));
        assertFalse(processor.supportsReturnType(values[1]));
        assertFalse(processor.supportsReturnType(values[2]));
        assertFalse(processor.supportsReturnType(values[3]));
        assertFalse(processor.supportsReturnType(values[4]));
    }

    @Test
    public void supportsReturnType_UnsupportedTypes_ReturnFalse() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        assertFalse(processor.supportsReturnType(values[0]));
        assertFalse(processor.supportsReturnType(values[1]));
        assertFalse(processor.supportsReturnType(values[2]));
        assertFalse(processor.supportsReturnType(values[3]));
        assertFalse(processor.supportsReturnType(values[4]));

        handlers.forEach(h -> {
            verify(h, times(5)).supportsReturnType(any());
            verifyNoMoreInteractions(h);
        });
    }

    @Test
    public void supportsReturnType() {
        BotHandlerMethodReturnValueHandler handler = mock(BotHandlerMethodReturnValueHandler.class);
        when(handler.supportsReturnType(any(MethodParameter.class))).thenAnswer(any ->
                BaseRequest.class.isAssignableFrom(any.<MethodParameter>getArgument(0).getParameterType()));
        handlers.add(4, handler);

        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        assertTrue(processor.supportsReturnType(values[3]));
        assertTrue(processor.supportsReturnType(values[4]));

        handlers.stream()
                .limit(5)
                .forEach(h -> {
                    verify(h, times(2)).supportsReturnType(any());
                    verifyNoMoreInteractions(h);
                });

        handlers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    @Test
    public void handleReturnValue_WithoutResolvers_ReturnNull() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(new ArrayList<>());
        assertNull(processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest));
    }

    @Test
    public void handleReturnValue_UnsupportedTypes_ReturnNull() {
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        assertNull(processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest));
    }

    @Test
    public void testHandleReturnValue() {
        BotHandlerMethodReturnValueHandler handler = mock(BotHandlerMethodReturnValueHandler.class);
        when(handler.supportsReturnType(any(MethodParameter.class))).thenReturn(true);
        when(handler.handleReturnValue(any(), any(), any())).thenReturn(new SendMessage(12L, "text"));

        handlers.add(4, handler);
        BotHandlerMethodReturnValueHandlerComposite processor =
                new BotHandlerMethodReturnValueHandlerComposite(handlers);

        BaseRequest result = processor.handleReturnValue(new SendMessage(12L, "text"), values[4], telegramRequest);
        assertNotNull(result);
        assertTrue(result instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) result;
        assertEquals(sendMessage.getParameters().get("chat_id"), 12L);
        assertEquals(sendMessage.getParameters().get("text"), "text");

        verify(handler).supportsReturnType(any());
        verify(handler).handleReturnValue(any(), any(), any());
        verifyNoMoreInteractions(handler);

        handlers.stream()
                .limit(4)
                .forEach(h -> {
                    verify(h).supportsReturnType(any());
                    verifyNoMoreInteractions(h);
                });

        handlers.stream()
                .skip(5)
                .forEach(Mockito::verifyNoMoreInteractions);
    }

    public void method(String unsupported, int unSupported1Primitive, Integer unSupportedClass, BaseRequest supported, SendMessage supportedInherit) {
    }
}
