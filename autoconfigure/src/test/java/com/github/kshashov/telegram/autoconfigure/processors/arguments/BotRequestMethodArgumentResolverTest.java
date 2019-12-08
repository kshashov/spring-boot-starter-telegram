package com.github.kshashov.telegram.autoconfigure.processors.arguments;

import com.github.kshashov.telegram.autoconfigure.TestUtils;
import com.github.kshashov.telegram.autoconfigure.annotation.BotPathVariable;
import com.github.kshashov.telegram.handler.TelegramRequest;
import com.github.kshashov.telegram.handler.processor.TelegramSession;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;

import java.util.stream.Stream;

public class BotRequestMethodArgumentResolverTest {

    private BotRequestMethodArgumentResolver processor;
    private MethodParameter[] values;
    private TelegramRequest telegramRequest;
    private TelegramSession telegramSession;
    private Update update;

    @BeforeEach
    void prepare() {
        this.processor = new BotRequestMethodArgumentResolver();
        this.values = Stream.of(TestUtils.findMethodByTitle(this, "method").getParameters())
                .map(MethodParameter::forParameter)
                .toArray(MethodParameter[]::new);

        this.update = Mockito.mock(Update.class);
        Mockito.when(update.inlineQuery()).thenReturn(Mockito.mock(InlineQuery.class));
        Mockito.when(update.chosenInlineResult()).thenReturn(Mockito.mock(ChosenInlineResult.class));
        Mockito.when(update.callbackQuery()).thenReturn(Mockito.mock(CallbackQuery.class));
        Mockito.when(update.shippingQuery()).thenReturn(Mockito.mock(ShippingQuery.class));
        Mockito.when(update.preCheckoutQuery()).thenReturn(Mockito.mock(PreCheckoutQuery.class));
        Mockito.when(update.poll()).thenReturn(Mockito.mock(Poll.class));

        this.telegramSession = Mockito.mock(TelegramSession.class);
        this.telegramRequest = Mockito.mock(TelegramRequest.class);
        Mockito.when(telegramRequest.getUpdate()).thenReturn(update);
        Mockito.when(telegramRequest.getChat()).thenReturn(Mockito.mock(Chat.class));
        Mockito.when(telegramRequest.getUser()).thenReturn(Mockito.mock(User.class));
        Mockito.when(telegramRequest.getText()).thenReturn("text");
        Mockito.when(telegramRequest.getMessage()).thenReturn(Mockito.mock(Message.class));
        Mockito.when(telegramRequest.getTelegramBot()).thenReturn(Mockito.mock(TelegramBot.class));
    }

    @Test
    void supportsParameter() {
        Assertions.assertFalse(processor.supportsParameter(values[0]));
        Assertions.assertFalse(processor.supportsParameter(values[1]));

        Assertions.assertTrue(processor.supportsParameter(values[2]));
        Assertions.assertTrue(processor.supportsParameter(values[3]));
        Assertions.assertTrue(processor.supportsParameter(values[4]));
        Assertions.assertTrue(processor.supportsParameter(values[5]));
        Assertions.assertTrue(processor.supportsParameter(values[6]));
        Assertions.assertTrue(processor.supportsParameter(values[7]));
        Assertions.assertTrue(processor.supportsParameter(values[8]));
        Assertions.assertTrue(processor.supportsParameter(values[9]));
        Assertions.assertTrue(processor.supportsParameter(values[10]));
        Assertions.assertTrue(processor.supportsParameter(values[11]));
        Assertions.assertTrue(processor.supportsParameter(values[12]));
        Assertions.assertTrue(processor.supportsParameter(values[13]));
        Assertions.assertTrue(processor.supportsParameter(values[14]));
        Assertions.assertTrue(processor.supportsParameter(values[15]));
    }

    @Test
    void resolveArgument_UnsupportedTypes_ReturnNull() {
        Assertions.assertNull(processor.resolveArgument(values[0], telegramRequest, telegramSession));
    }

    @Test
    void resolveArgument_ReturnAsIs() {
        Assertions.assertEquals(telegramRequest, processor.resolveArgument(values[2], telegramRequest, telegramSession));
        Assertions.assertEquals(telegramSession, processor.resolveArgument(values[3], telegramRequest, telegramSession));
        Assertions.assertEquals(telegramRequest.getTelegramBot(), processor.resolveArgument(values[4], telegramRequest, telegramSession));
        Assertions.assertEquals(telegramRequest.getText(), processor.resolveArgument(values[5], telegramRequest, telegramSession));
        Assertions.assertEquals(telegramRequest.getUpdate(), processor.resolveArgument(values[6], telegramRequest, telegramSession));

        Assertions.assertEquals(telegramRequest.getMessage(), processor.resolveArgument(values[7], telegramRequest, telegramSession));
        Assertions.assertEquals(update.inlineQuery(), processor.resolveArgument(values[8], telegramRequest, telegramSession));
        Assertions.assertEquals(update.chosenInlineResult(), processor.resolveArgument(values[9], telegramRequest, telegramSession));
        Assertions.assertEquals(update.callbackQuery(), processor.resolveArgument(values[10], telegramRequest, telegramSession));
        Assertions.assertEquals(update.shippingQuery(), processor.resolveArgument(values[11], telegramRequest, telegramSession));

        Assertions.assertEquals(update.preCheckoutQuery(), processor.resolveArgument(values[12], telegramRequest, telegramSession));
        Assertions.assertEquals(update.poll(), processor.resolveArgument(values[13], telegramRequest, telegramSession));
        Assertions.assertEquals(telegramRequest.getChat(), processor.resolveArgument(values[14], telegramRequest, telegramSession));
        Assertions.assertEquals(telegramRequest.getUser(), processor.resolveArgument(values[15], telegramRequest, telegramSession));
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
