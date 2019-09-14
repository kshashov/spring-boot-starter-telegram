package com.github.kshashov.telegram;

import com.google.common.collect.Lists;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.*;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.pengrad.telegrambot.TelegramBot;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.memoize;

/**
 * Конфигурирование бинов
 */
@Configuration
public class TelegramConfiguration implements BeanFactoryPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TelegramConfiguration.class);
    private ConfigurableListableBeanFactory beanFactory;

    @Bean
    RequestDispatcher requestDispatcher(ConversionService conversionService,
                                        List<TelegramMvcConfiguration> telegramMvcConfigurations,
                                        final Consumer<OkHttpClient.Builder> okHttpClientBuilderConsumer,
                                        final TaskExecutor taskExecutor,
                                        List<BotHandlerMethodArgumentResolver> resolvers,
                                        List<BotHandlerMethodReturnValueHandler> handlers) {
        Supplier<OkHttpClient> okHttpClientSupplier = () -> {
            OkHttpClient.Builder builder = new OkHttpClient()
                    .newBuilder()
                    .dispatcher(new Dispatcher(new ExecutorServiceAdapter(taskExecutor)));
            okHttpClientBuilderConsumer.accept(builder);
            return builder.build();
        };

        RequestDispatcher requestDispatcher = new RequestDispatcher(handlerMethodContainer(), new HandlerAdapter(resolvers, handlers), taskExecutor);
        registerTelegramBotService(requestDispatcher, telegramMvcConfigurations, okHttpClientSupplier);
        return requestDispatcher;
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(15);
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean
    @ConditionalOnMissingBean
    public Consumer<OkHttpClient.Builder> getOkHttpClientSupplier() {
        return (builder) -> {
        };
    }

    @Bean
    public ConversionService getConversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    @Scope(value = TelegramScope.SCOPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    TelegramSession telegramSession() {
        return new TelegramSession();
    }

    private void registerTelegramBotService(RequestDispatcher requestDispatcher, List<TelegramMvcConfiguration> telegramMvcConfigurations, Supplier<OkHttpClient> httpClientSupplier) {
        TelegramBotProperties telegramBotProperties = getTelegramBotProperties(telegramMvcConfigurations);
        if (!telegramBotProperties.iterator().hasNext()) {
            logger.warn("Не найдено не одной настройки бота");
        } else {
            for (TelegramBotProperty telegramBotProperty : telegramBotProperties) {
                //Если не задан OkHttpClient создадим свой который будет юзать наш TaskExecutor
                if (telegramBotProperty.getOkHttpClient() == null) {
                    telegramBotProperty =
                            TelegramBotProperty
                                    .newBuilder(telegramBotProperty)
                                    .okHttpClient(memoize(httpClientSupplier::get).get())
                                    .build();
                }

                TelegramBot telegramBot = createTelegramBot(telegramBotProperty);
                TelegramService telegramService = new TelegramService(telegramBot, requestDispatcher);
                beanFactory.registerSingleton("telegramService" + telegramBotProperty.getAlias(), telegramService);
            }
        }
    }

    @Bean
    @Order()
    ApplicationListener<ContextRefreshedEvent> runnerTelegramService(List<TelegramService> telegramServices) {
        return event -> {
            for (TelegramService telegramService : telegramServices) {
                telegramService.start();
            }
        };
    }

    private TelegramBot createTelegramBot(TelegramBotProperty telegramBotProperty) {
        return new TelegramBot.Builder(telegramBotProperty.getToken())
                .okHttpClient(telegramBotProperty.getOkHttpClient())
                .updateListenerSleep(telegramBotProperty.getTimeOutMillis())
                .apiUrl(telegramBotProperty.getUrl())
                .build();
    }

    private TelegramBotProperties getTelegramBotProperties(List<TelegramMvcConfiguration> telegramMvcConfigurations) {
        TelegramBotProperties telegramBotProperties = new TelegramBotProperties();
        ArrayList<TelegramMvcConfiguration> telegramMvcConfigurationsList = Lists.newArrayList(telegramMvcConfigurations);
        AnnotationAwareOrderComparator.sort(telegramMvcConfigurationsList);
        for (TelegramMvcConfiguration telegramMvcConfiguration : telegramMvcConfigurationsList) {
            TelegramBotBuilder telegramBotBuilder = new TelegramBotBuilder();
            telegramMvcConfiguration.configuration(telegramBotBuilder);
            telegramBotProperties.addAll(telegramBotBuilder.build());
        }
        return telegramBotProperties;
    }

    @Bean
    HandlerMethodContainer handlerMethodContainer() {
        return new HandlerMethodContainer();
    }

    @Bean
    TelegramControllerBeanPostProcessor telegramControllerBeanPostProcessor() {
        return new TelegramControllerBeanPostProcessor(handlerMethodContainer());
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        beanFactory.registerScope(TelegramScope.SCOPE, new TelegramScope(beanFactory, TimeUnit.HOURS.toMillis(1)));
    }
}
