package com.github.kshashov.telegram.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TestTelegramService {

    @Test
    public void start_registerListenerToExecuteDispatcher() {
        TelegramBot bot = mock(TelegramBot.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        TelegramService service = new TelegramService(bot, dispatcher);
        service.start();

        ArgumentCaptor<UpdatesListener> listenerCaptor = ArgumentCaptor.forClass(UpdatesListener.class);
        verify(bot).setUpdatesListener(listenerCaptor.capture(), any(GetUpdates.class));

        assertNotNull(listenerCaptor.getValue());
        Update update = mock(Update.class);

        listenerCaptor.getValue().process(Collections.singletonList(update));

        verify(dispatcher).execute(update, bot);
    }
}
