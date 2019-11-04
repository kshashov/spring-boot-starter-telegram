package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.config.TelegramBotGlobalPropertiesConfiguration;
import com.github.kshashov.telegram.config.TelegramBotProperties;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.TelegramService;
import com.github.kshashov.telegram.handler.processor.RequestDispatcher;
import com.github.kshashov.telegram.handler.processor.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.response.BotHandlerMethodReturnValueHandler;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Main configuration for telegram mvc.
 */
@Slf4j
@Configuration
@Import(MethodProcessorsConfiguration.class)
@EnableConfigurationProperties(TelegramConfigurationProperties.class)
public class TelegramConfiguration implements BeanFactoryPostProcessor, EnvironmentAware {
    private Environment environment;

    @Bean
    @Qualifier("telegramServicesList")
    List<TelegramService> telegramServices(@Qualifier("telegramBotPropertiesList") List<TelegramBotProperties> botProperties, RequestDispatcher requestDispatcher) {
        List<TelegramService> services = botProperties.stream()
                .map(p -> new TelegramService(p, requestDispatcher))
                .collect(Collectors.toList());

        if (services.isEmpty()) {
            log.error("No bot configurations found");
        } else {
            log.info("Finished Telegram controllers scanning. Found {} bots", services.size());
        }

        return services;
    }

    @Bean
    @Qualifier("telegramBotPropertiesList")
    List<TelegramBotProperties> telegramBotPropertiesList(List<TelegramMvcController> controllers, TelegramBotGlobalProperties globalProperties) {
        return controllers.stream()
                .map(TelegramMvcController::getToken)
                .distinct()
                .map(token -> {
                    TelegramBotProperties.Builder defaultBuilder = createDefaultBotPropertiesBuilder(token, globalProperties);

                    if (globalProperties.getBotProperties().containsKey(token)) {
                        globalProperties.getBotProperties().get(token).accept(defaultBuilder);
                    }
                    return defaultBuilder.build();
                }).collect(Collectors.toList());
    }

    @Bean
    RequestDispatcher requestDispatcher(
            HandlerMethodContainer handlerMethodContainer,
            TelegramSessionResolver sessionResolver,
            TelegramBotGlobalProperties botGlobalProperties) {
        return new RequestDispatcher(handlerMethodContainer, botGlobalProperties, sessionResolver);
    }

    @Bean
    TelegramBotGlobalProperties telegramBotGlobalProperties(
            TelegramBotGlobalPropertiesConfiguration botGlobalPropertiesConfiguration,
            List<BotHandlerMethodArgumentResolver> argumentResolvers,
            List<BotHandlerMethodReturnValueHandler> returnValueHandlers) {
        TelegramBotGlobalProperties.Builder defaultBuilder = createDefaultBotGlobalPropertiesBuilder(argumentResolvers, returnValueHandlers);
        botGlobalPropertiesConfiguration.configure(defaultBuilder);
        return defaultBuilder.build();
    }

    @Bean
    TelegramSessionResolver telegramSessionResolver(ApplicationContext context) {
        return new TelegramSessionResolver(context);
    }

    @Bean
    @ConditionalOnMissingBean(TelegramBotGlobalPropertiesConfiguration.class)
    TelegramBotGlobalPropertiesConfiguration telegramBotGlobalPropertiesConfiguration() {
        return builder -> {
        };
    }

    @Bean
    @Scope(value = TelegramScope.SCOPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    TelegramSession telegramSession() {
        return new TelegramSession();
    }

    @Bean
    HandlerMethodContainer handlerMethodContainer() {
        return new HandlerMethodContainer();
    }

    @Bean
    TelegramControllerBeanPostProcessor telegramControllerBeanPostProcessor(HandlerMethodContainer handlerMethodContainer) {
        return new TelegramControllerBeanPostProcessor(handlerMethodContainer);
    }

    @Bean
    ApplicationListener<ContextRefreshedEvent> onContextRefreshed(@Qualifier("telegramServicesList") List<TelegramService> telegramServices) {
        return event -> telegramServices.forEach(TelegramService::start);
    }

    @Bean
    ApplicationListener<ContextClosedEvent> onContextClosed(RequestDispatcher requestDispatcher) {
        return event -> {
            if (requestDispatcher.getBotGlobalProperties().getTaskExecutor() != null) {
                log.info("Shutting down ThreadPoolExecutor");
                requestDispatcher.getBotGlobalProperties().getTaskExecutor().shutdown();
                log.info("ThreadPoolExecutor has been shut down");
            }
        };
    }

    private TelegramBotGlobalProperties.Builder createDefaultBotGlobalPropertiesBuilder(@NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers, @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers) {
        return TelegramBotGlobalProperties
                .builder()
                .argumentResolvers(argumentResolvers)
                .returnValueHandlers(returnValueHandlers)
                .taskExecutor(new ThreadPoolExecutor(
                        environment.getProperty("telegram.bot.core-pool-size", Integer.class, 15),
                        environment.getProperty("telegram.bot.max-pool-size", Integer.class, 200),
                        0L, TimeUnit.SECONDS, new SynchronousQueue<>()))
                .responseCallback(new Callback<BaseRequest, BaseResponse>() {
                    @Override
                    public void onResponse(BaseRequest request, BaseResponse response) {
                    }

                    @Override
                    public void onFailure(BaseRequest request, IOException e) {
                    }
                });
    }

    private TelegramBotProperties.Builder createDefaultBotPropertiesBuilder(@NotNull String token, @NotNull TelegramBotGlobalProperties globalProperties) {
        return TelegramBotProperties
                .builder(token)
                .url("https://api.telegram.org/bot")
                .listenerSleepMilliseconds(200L)
                .okHttpClient(new OkHttpClient.Builder()
                        .dispatcher(new Dispatcher(globalProperties.getTaskExecutor()))
                        .build());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope(TelegramScope.SCOPE,
                new TelegramScope(beanFactory, environment.getProperty("telegram.bot.session-seconds", Integer.class, 3600)));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
