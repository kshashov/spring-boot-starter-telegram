package com.github.kshashov.telegram.handler;

import com.github.kshashov.telegram.api.MessageType;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestTelegramEvent {
    private Update update;
    private TelegramBot bot;
    private User user;
    private Chat chat;

    @BeforeEach
    public void init() {
        bot = mock(TelegramBot.class);
        update = mock(Update.class);
        chat = mock(Chat.class);
        user = mock(User.class);
    }

    @Test
    public void unsupported() {
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.UNSUPPORTED, event.getMessageType());
        assertNull(event.getChat());
        assertNull(event.getUser());
        assertNull(event.getMessage());
        assertNull(event.getText());
    }

    @Test
    public void message() {
        Message message = mock(Message.class);
        when(message.chat()).thenReturn(chat);
        when(message.from()).thenReturn(user);
        when(message.text()).thenReturn("test");

        when(update.message()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.MESSAGE, event.getMessageType());
        assertEquals(chat, event.getChat());
        assertEquals(user, event.getUser());
        assertEquals(message, event.getMessage());
        assertEquals("test", event.getText());
    }

    @Test
    public void editedMessage() {
        Message message = mock(Message.class);
        when(message.chat()).thenReturn(chat);
        when(message.from()).thenReturn(user);
        when(message.text()).thenReturn("test");

        when(update.editedMessage()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.EDITED_MESSAGE, event.getMessageType());
        assertEquals(chat, event.getChat());
        assertEquals(user, event.getUser());
        assertEquals(message, event.getMessage());
        assertEquals("test", event.getText());
    }

    @Test
    public void channelPost() {
        Message message = mock(Message.class);
        when(message.chat()).thenReturn(chat);
        when(message.from()).thenReturn(user);
        when(message.text()).thenReturn("test");

        when(update.channelPost()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.CHANNEL_POST, event.getMessageType());
        assertEquals(chat, event.getChat());
        assertEquals(user, event.getUser());
        assertEquals(message, event.getMessage());
        assertEquals("test", event.getText());
    }

    @Test
    public void editedChannelPost() {
        Message message = mock(Message.class);
        when(message.chat()).thenReturn(chat);
        when(message.from()).thenReturn(user);
        when(message.text()).thenReturn("test");

        when(update.editedChannelPost()).thenReturn(message);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.EDITED_CHANNEL_POST, event.getMessageType());
        assertEquals(chat, event.getChat());
        assertEquals(user, event.getUser());
        assertEquals(message, event.getMessage());
        assertEquals("test", event.getText());
    }

    @Test
    public void callbackQuery() {
        Message message = mock(Message.class);
        when(message.chat()).thenReturn(chat);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        when(callbackQuery.message()).thenReturn(message);
        when(callbackQuery.from()).thenReturn(user);
        when(callbackQuery.data()).thenReturn("test");

        when(update.callbackQuery()).thenReturn(callbackQuery);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.CALLBACK_QUERY, event.getMessageType());
        assertEquals(chat, event.getChat());
        assertEquals(user, event.getUser());
        assertEquals("test", event.getText());
    }

    @Test
    public void inlineQuery() {
        InlineQuery inlineQuery = mock(InlineQuery.class);
        when(inlineQuery.query()).thenReturn("test");
        when(inlineQuery.from()).thenReturn(user);

        when(update.inlineQuery()).thenReturn(inlineQuery);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.INLINE_QUERY, event.getMessageType());
        assertNull(event.getChat());
        assertEquals(user, event.getUser());
        assertEquals("test", event.getText());
    }

    @Test
    public void chosenInlineResult() {
        ChosenInlineResult chosenInlineResult = mock(ChosenInlineResult.class);
        when(chosenInlineResult.query()).thenReturn("test");
        when(chosenInlineResult.from()).thenReturn(user);

        when(update.chosenInlineResult()).thenReturn(chosenInlineResult);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.CHOSEN_INLINE_RESULT, event.getMessageType());
        assertNull(event.getChat());
        assertEquals(user, event.getUser());
        assertEquals("test", event.getText());
    }

    @Test
    public void shippingQuery() {
        ShippingQuery shippingQuery = mock(ShippingQuery.class);
        when(shippingQuery.invoicePayload()).thenReturn("test");
        when(shippingQuery.from()).thenReturn(user);

        when(update.shippingQuery()).thenReturn(shippingQuery);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.SHIPPING_QUERY, event.getMessageType());
        assertNull(event.getChat());
        assertEquals(user, event.getUser());
        assertEquals("test", event.getText());
    }

    @Test
    public void preCheckoutQuery() {
        PreCheckoutQuery preCheckoutQuery = mock(PreCheckoutQuery.class);
        when(preCheckoutQuery.invoicePayload()).thenReturn("test");
        when(preCheckoutQuery.from()).thenReturn(user);

        when(update.preCheckoutQuery()).thenReturn(preCheckoutQuery);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.PRECHECKOUT_QUERY, event.getMessageType());
        assertNull(event.getChat());
        assertEquals(user, event.getUser());
        assertEquals("test", event.getText());
    }

    @Test
    public void poll() {
        Poll poll = mock(Poll.class);

        when(update.poll()).thenReturn(poll);
        TelegramEvent event = new TelegramEvent(update, bot);

        assertEquals(update, event.getUpdate());
        assertEquals(bot, event.getTelegramBot());
        assertEquals(MessageType.POLL, event.getMessageType());
        assertNull(event.getChat());
        assertNull(event.getUser());
        assertNull(event.getText());
    }
}
