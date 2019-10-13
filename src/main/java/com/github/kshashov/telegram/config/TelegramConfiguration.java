package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.HandlerMethodContainer;
import com.github.kshashov.telegram.RequestDispatcher;
import com.github.kshashov.telegram.ResolversContainer;
import com.github.kshashov.telegram.TelegramService;
import com.github.kshashov.telegram.api.TelegramSession;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pengrad.telegrambot.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Suppliers.memoize;

/**
 * Main configuration for telegram mvc
 */
@Slf4j
@Configuration
@Import(MethodProcessorsConfiguration.class)
@EnableConfigurationProperties(TelegramConfigurationProperties.class)
public class TelegramConfiguration implements BeanFactoryPostProcessor, EnvironmentAware {
    private ConfigurableListableBeanFactory beanFactory;
    private Environment environment;

    @Bean
    RequestDispatcher requestDispatcher(
            final List<TelegramMvcController> telegramMvcControllers,
            final Consumer<OkHttpClient.Builder> okHttpClientBuilderConsumer,
            final TaskExecutor taskExecutor,
            final List<TelegramBotProperty> botProperties,
            final ResolversContainer resolversContainer) {

        Supplier<OkHttpClient> defaultOkHttpClientSupplier = () -> {
            OkHttpClient.Builder builder = new OkHttpClient()
                    .newBuilder()
                    .dispatcher(new Dispatcher(new ExecutorServiceAdapter(taskExecutor)));
            okHttpClientBuilderConsumer.accept(builder);
            return builder.build();
        };

        RequestDispatcher requestDispatcher = new RequestDispatcher(handlerMethodContainer(), resolversContainer, taskExecutor);
        registerTelegramBotServices(requestDispatcher, telegramMvcControllers, botProperties, defaultOkHttpClientSupplier);
        return requestDispatcher;
    }

    @Bean
    @ConditionalOnMissingBean
    TaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(environment.getProperty("telegram.bot.getCorePoolSize", Integer.class, 15));
        threadPoolTaskExecutor.setMaxPoolSize(environment.getProperty("telegram.bot.getMaxPoolSize", Integer.class, 200));
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean
    @ConditionalOnMissingBean
    Consumer<OkHttpClient.Builder> getOkHttpClientSupplier() {
        return (builder) -> {
        };
    }

    @Bean
    @Scope(value = TelegramScope.SCOPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    TelegramSession telegramSession() {
        return new TelegramSession();
    }

    private void registerTelegramBotServices(RequestDispatcher requestDispatcher, List<TelegramMvcController> telegramMvcControllers, List<TelegramBotProperty> botProperties, Supplier<OkHttpClient> httpClientSupplier) {
        List<TelegramBotProperty> mergedBotProperties = getTelegramBotProperties(telegramMvcControllers, botProperties);
        if (!mergedBotProperties.iterator().hasNext()) {
            log.warn("No bot configurations found");
        } else {
            List<TelegramService> services = new ArrayList<>();
            for (TelegramBotProperty property : mergedBotProperties) {

                // If OkHttpClient is not set, create one that will use our TaskExecutor
                if (property.getOkHttpClient() == null) {
                    property = TelegramBotProperty
                            .builder()
                            .token(property.getToken())
                            .url(property.getUrl())
                            .timeOutMillis(property.getTimeOutMillis())
                            .okHttpClient(memoize(httpClientSupplier::get).get())
                            .build();
                }

                TelegramBot telegramBot = createTelegramBot(property);
                TelegramService telegramService = new TelegramService(telegramBot, requestDispatcher);
                services.add(telegramService);
            }

            beanFactory.registerSingleton("telegramServices", services);
            runnerTelegramService(services);
        }
    }

    @Bean
    @Order()
    ApplicationListener<ContextRefreshedEvent> runnerTelegramService(@Qualifier("telegramServices") List<TelegramService> telegramServices) {
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

    private List<TelegramBotProperty> getTelegramBotProperties(List<TelegramMvcController> telegramMvcControllers, List<TelegramBotProperty> botProperties) {
        List<TelegramMvcController> telegramMvcConfigurationsList = Lists.newArrayList(telegramMvcControllers);
        AnnotationAwareOrderComparator.sort(telegramMvcConfigurationsList);

        List<String> tokens = telegramMvcControllers.stream()
                .filter(c -> !Strings.isNullOrEmpty(c.getToken()))
                .map(TelegramMvcController::getToken).distinct()
                .collect(Collectors.toList());
        Map<String, List<TelegramBotProperty>> propertiesByToken = botProperties.stream()
                .filter(p -> !Strings.isNullOrEmpty(p.getToken()))
                .collect(Collectors.groupingBy(TelegramBotProperty::getToken));

        List<TelegramBotProperty> merged = new ArrayList<>();
        for (String token : tokens) {
            List<TelegramBotProperty> properties = propertiesByToken.get(token);

            // Create new property if there is no any
            // Get first in other case
            TelegramBotProperty property = (properties == null || properties.isEmpty())
                    ? TelegramBotProperty.builder().token(token).build()
                    : properties.get(0);

            merged.add(property);
        }

        return merged;
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
        beanFactory.registerScope(TelegramScope.SCOPE,
                new TelegramScope(beanFactory, environment.getProperty("telegram.bot.getSessionSeconds", Integer.class, 3600)));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
