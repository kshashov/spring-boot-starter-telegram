package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.handler.MessageType;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TelegramEventTest {
    private Update update;
    private TelegramBot bot;
    private User user;
    private Chat chat;
    private String token = "";

    @BeforeEach
    void init() {
        bot = Mockito.mock(TelegramBot.class);
        update = Mockito.mock(Update.class);
        chat = Mockito.mock(Chat.class);
        user = Mockito.mock(User.class);
    }

    @Test
    void unsupported() {
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.UNSUPPORTED, event.getMessageType());
        Assertions.assertNull(event.getChat());
        Assertions.assertNull(event.getUser());
        Assertions.assertNull(event.getMessage());
        Assertions.assertNull(event.getText());
    }

    @Test
    void message() {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.chat()).thenReturn(chat);
        Mockito.when(message.from()).thenReturn(user);
        Mockito.when(message.text()).thenReturn("test");

        Mockito.when(update.message()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.MESSAGE, event.getMessageType());
        Assertions.assertEquals(chat, event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals(message, event.getMessage());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void editedMessage() {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.chat()).thenReturn(chat);
        Mockito.when(message.from()).thenReturn(user);
        Mockito.when(message.text()).thenReturn("test");

        Mockito.when(update.editedMessage()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.EDITED_MESSAGE, event.getMessageType());
        Assertions.assertEquals(chat, event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals(message, event.getMessage());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void channelPost() {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.chat()).thenReturn(chat);
        Mockito.when(message.from()).thenReturn(user);
        Mockito.when(message.text()).thenReturn("test");

        Mockito.when(update.channelPost()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.CHANNEL_POST, event.getMessageType());
        Assertions.assertEquals(chat, event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals(message, event.getMessage());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void editedChannelPost() {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.chat()).thenReturn(chat);
        Mockito.when(message.from()).thenReturn(user);
        Mockito.when(message.text()).thenReturn("test");

        Mockito.when(update.editedChannelPost()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.EDITED_CHANNEL_POST, event.getMessageType());
        Assertions.assertEquals(chat, event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals(message, event.getMessage());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void callbackQuery() {
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.chat()).thenReturn(chat);
        CallbackQuery callbackQuery = Mockito.mock(CallbackQuery.class);
        Mockito.when(callbackQuery.message()).thenReturn(message);
        Mockito.when(callbackQuery.from()).thenReturn(user);
        Mockito.when(callbackQuery.data()).thenReturn("test");

        Mockito.when(update.callbackQuery()).thenReturn(callbackQuery);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.CALLBACK_QUERY, event.getMessageType());
        Assertions.assertEquals(chat, event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void inlineQuery() {
        InlineQuery inlineQuery = Mockito.mock(InlineQuery.class);
        Mockito.when(inlineQuery.query()).thenReturn("test");
        Mockito.when(inlineQuery.from()).thenReturn(user);

        Mockito.when(update.inlineQuery()).thenReturn(inlineQuery);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.INLINE_QUERY, event.getMessageType());
        Assertions.assertNull(event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void chosenInlineResult() {
        ChosenInlineResult chosenInlineResult = Mockito.mock(ChosenInlineResult.class);
        Mockito.when(chosenInlineResult.query()).thenReturn("test");
        Mockito.when(chosenInlineResult.from()).thenReturn(user);

        Mockito.when(update.chosenInlineResult()).thenReturn(chosenInlineResult);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.CHOSEN_INLINE_RESULT, event.getMessageType());
        Assertions.assertNull(event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void shippingQuery() {
        ShippingQuery shippingQuery = Mockito.mock(ShippingQuery.class);
        Mockito.when(shippingQuery.invoicePayload()).thenReturn("test");
        Mockito.when(shippingQuery.from()).thenReturn(user);

        Mockito.when(update.shippingQuery()).thenReturn(shippingQuery);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.SHIPPING_QUERY, event.getMessageType());
        Assertions.assertNull(event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void preCheckoutQuery() {
        PreCheckoutQuery preCheckoutQuery = Mockito.mock(PreCheckoutQuery.class);
        Mockito.when(preCheckoutQuery.invoicePayload()).thenReturn("test");
        Mockito.when(preCheckoutQuery.from()).thenReturn(user);

        Mockito.when(update.preCheckoutQuery()).thenReturn(preCheckoutQuery);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.PRECHECKOUT_QUERY, event.getMessageType());
        Assertions.assertNull(event.getChat());
        Assertions.assertEquals(user, event.getUser());
        Assertions.assertEquals("test", event.getText());
    }

    @Test
    void poll() {
        Poll poll = Mockito.mock(Poll.class);

        Mockito.when(update.poll()).thenReturn(poll);
        TelegramEvent event = new TelegramEvent(token, update, bot);

        Assertions.assertEquals(update, event.getUpdate());
        Assertions.assertEquals(bot, event.getTelegramBot());
        Assertions.assertEquals(MessageType.POLL, event.getMessageType());
        Assertions.assertNull(event.getChat());
        Assertions.assertNull(event.getUser());
        Assertions.assertNull(event.getText());
    }
}
