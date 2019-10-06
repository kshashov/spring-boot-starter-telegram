package com.github.kshashov.telegram.handler.arguments;

import com.github.kshashov.telegram.api.TelegramRequest;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.api.bind.annotation.BotPathVariable;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBotRequestMethodArgumentResolver {

    private BotRequestMethodArgumentResolver processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;
    private TelegramSession telegramSession;
    private Update update;

    @BeforeEach
    public void prepare() throws NoSuchMethodException {
        this.processor = new BotRequestMethodArgumentResolver();
        this.values = Stream.of(this.getClass().getMethod("method", Integer.class, String.class, TelegramRequest.class, TelegramSession.class, TelegramBot.class, String.class, Update.class,
                Message.class, InlineQuery.class, ChosenInlineResult.class, CallbackQuery.class, ShippingQuery.class,
                PreCheckoutQuery.class, Poll.class, Chat.class, User.class).getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.update = mock(Update.class);
        when(update.inlineQuery()).thenReturn(mock(InlineQuery.class));
        when(update.chosenInlineResult()).thenReturn(mock(ChosenInlineResult.class));
        when(update.callbackQuery()).thenReturn(mock(CallbackQuery.class));
        when(update.shippingQuery()).thenReturn(mock(ShippingQuery.class));
        when(update.preCheckoutQuery()).thenReturn(mock(PreCheckoutQuery.class));
        when(update.poll()).thenReturn(mock(Poll.class));

        this.telegramSession = mock(TelegramSession.class);
        this.telegramRequest = mock(TelegramRequest.class);
        when(telegramRequest.getUpdate()).thenReturn(update);
        when(telegramRequest.getChat()).thenReturn(mock(Chat.class));
        when(telegramRequest.getUser()).thenReturn(mock(User.class));
        when(telegramRequest.getText()).thenReturn("text");
        when(telegramRequest.getMessage()).thenReturn(mock(Message.class));
        when(telegramRequest.getTelegramBot()).thenReturn(mock(TelegramBot.class));
    }

    @Test
    public void supportsParameter() {
        assertFalse(processor.supportsParameter(values[0]));
        assertFalse(processor.supportsParameter(values[1]));

        assertTrue(processor.supportsParameter(values[2]));
        assertTrue(processor.supportsParameter(values[3]));
        assertTrue(processor.supportsParameter(values[4]));
        assertTrue(processor.supportsParameter(values[5]));
        assertTrue(processor.supportsParameter(values[6]));
        assertTrue(processor.supportsParameter(values[7]));
        assertTrue(processor.supportsParameter(values[8]));
        assertTrue(processor.supportsParameter(values[9]));
        assertTrue(processor.supportsParameter(values[10]));
        assertTrue(processor.supportsParameter(values[11]));
        assertTrue(processor.supportsParameter(values[12]));
        assertTrue(processor.supportsParameter(values[13]));
        assertTrue(processor.supportsParameter(values[14]));
        assertTrue(processor.supportsParameter(values[15]));
    }

    @Test
    public void resolveArgument_UnsupportedTypes_ReturnNull() {
        assertNull(processor.resolveArgument(values[0], telegramRequest, telegramSession));
    }

    @Test
    public void resolveArgument_ReturnAsIs() {
        assertEquals(telegramRequest, processor.resolveArgument(values[2], telegramRequest, telegramSession));
        assertEquals(telegramSession, processor.resolveArgument(values[3], telegramRequest, telegramSession));
        assertEquals(telegramRequest.getTelegramBot(), processor.resolveArgument(values[4], telegramRequest, telegramSession));
        assertEquals(telegramRequest.getText(), processor.resolveArgument(values[5], telegramRequest, telegramSession));
        assertEquals(telegramRequest.getUpdate(), processor.resolveArgument(values[6], telegramRequest, telegramSession));

        assertEquals(telegramRequest.getMessage(), processor.resolveArgument(values[7], telegramRequest, telegramSession));
        assertEquals(update.inlineQuery(), processor.resolveArgument(values[8], telegramRequest, telegramSession));
        assertEquals(update.chosenInlineResult(), processor.resolveArgument(values[9], telegramRequest, telegramSession));
        assertEquals(update.callbackQuery(), processor.resolveArgument(values[10], telegramRequest, telegramSession));
        assertEquals(update.shippingQuery(), processor.resolveArgument(values[11], telegramRequest, telegramSession));

        assertEquals(update.preCheckoutQuery(), processor.resolveArgument(values[12], telegramRequest, telegramSession));
        assertEquals(update.poll(), processor.resolveArgument(values[13], telegramRequest, telegramSession));
        assertEquals(telegramRequest.getChat(), processor.resolveArgument(values[14], telegramRequest, telegramSession));
        assertEquals(telegramRequest.getUser(), processor.resolveArgument(values[15], telegramRequest, telegramSession));
    }

    public void method(
            Integer unsupportedInt,
            @BotPathVariable() String unsupportedPathVariable,

            TelegramRequest request,
            TelegramSession session,
            TelegramBot bot,
            String text,
            Update update,

            Message message,
            InlineQuery inlineQuery,
            ChosenInlineResult chosenInlineResult,
            CallbackQuery callbackQuery,
            ShippingQuery shippingQuery,

            PreCheckoutQuery preCheckoutQuery,
            Poll poll,
            Chat chat,
            User user) {
    }

}
