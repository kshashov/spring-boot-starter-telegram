package com.github.kshashov.telegram.handler.processor;

import com.github.kshashov.telegram.TelegramSessionResolver;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//@RunWith(PowerMockRunner.class)
@PrepareForTest(RequestDispatcher.class)
public class RequestDispatcherTest {
    private RequestDispatcher dispatcher;
    private HandlerMethodContainer methodContainer;
    private TelegramBotGlobalProperties properties;
    private TelegramSessionResolver sessionResolver;
    private ThreadPoolExecutor threadPoolExecutor;
    private TelegramBot bot;
    private Update update;
    private TelegramSessionResolver.TelegramSessionHolder sessionHolder;
    private TelegramSession session;

    @BeforeEach
    public void init() {
        properties = mock(TelegramBotGlobalProperties.class);
        threadPoolExecutor = mock(ThreadPoolExecutor.class);
        doAnswer((Answer<Void>) args -> {
            ((Runnable) args.getArgument(0)).run();
            return null;
        }).when(threadPoolExecutor).execute(any(Runnable.class));
        when(properties.getTaskExecutor()).thenReturn(threadPoolExecutor);

        update = mock(Update.class);
        bot = mock(TelegramBot.class);

        session = mock(TelegramSession.class);
        sessionHolder = mock(TelegramSessionResolver.TelegramSessionHolder.class);
        when(sessionHolder.getSession()).thenReturn(session);
        sessionResolver = mock(TelegramSessionResolver.class);
        when(sessionResolver.resolveTelegramSession(any())).thenReturn(sessionHolder);
        doNothing().when(sessionHolder).releaseSessionId();
        methodContainer = mock(HandlerMethodContainer.class);
        dispatcher = spy(new RequestDispatcher(methodContainer, properties, sessionResolver));
    }

    /*@Test
    public void execute_MethodNotFound_DoNothing() throws Exception {
        PowerMockito.doNothing().when(dispatcher, "postExecute", any(), any());
        when(methodContainer.lookupHandlerMethod(any())).thenReturn(new HandlerMethodContainer.HandlerLookupResult());
        dispatcher.execute(update, bot);

        verifyPrivate(dispatcher, never()).invoke("postExecute", any(), any());
        assertTrue(true);
        //verify(dispatcher, times(0), )
    }*/
}
