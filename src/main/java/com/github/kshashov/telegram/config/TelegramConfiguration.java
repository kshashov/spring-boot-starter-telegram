package com.github.kshashov.telegram.config;

import com.github.kshashov.telegram.HandlerMethodContainer;
import com.github.kshashov.telegram.RequestDispatcher;
import com.github.kshashov.telegram.TelegramService;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
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

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Suppliers.memoize;

/**
 * Main configuration for telegram mvc.
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
            final HandlerMethodContainer handlerMethodContainer,
            final List<TelegramMvcController> telegramMvcControllers,
            final List<TelegramBotProperties> botProperties,
            final TelegramBotGlobalProperties.Builder defaultBotGlobalPropertiesBuilder,
            final Optional<TelegramBotGlobalPropertiesConfiguration> botGlobalPropertiesConfiguration) {

        botGlobalPropertiesConfiguration.ifPresent(conf -> conf.configure(defaultBotGlobalPropertiesBuilder));
        TelegramBotGlobalProperties botGlobalProperties = defaultBotGlobalPropertiesBuilder.build();

        Supplier<OkHttpClient> defaultOkHttpClientSupplier = () -> new OkHttpClient()
                .newBuilder()
                .dispatcher(new Dispatcher(new ExecutorServiceAdapter(botGlobalProperties.getTaskExecutor())))
                .build();

        RequestDispatcher requestDispatcher = new RequestDispatcher(handlerMethodContainer, botGlobalProperties);
        List<TelegramBotProperties> mergedBotProperties = getTelegramBotProperties(telegramMvcControllers, botProperties);
        registerTelegramBotServices(requestDispatcher, mergedBotProperties, defaultOkHttpClientSupplier);
        return requestDispatcher;
    }

    /**
     * Create preconfigured default builder for {@link TelegramBotGlobalProperties}
     *
     * @param argumentResolvers   default list of argumentResolvers
     * @param returnValueHandlers default list of returnValueHandlers
     * @return preconfigured default builder
     */
    @Bean
    @ConditionalOnMissingBean(TelegramBotGlobalProperties.Builder.class)
    private TelegramBotGlobalProperties.Builder defaultTelegramBotGlobalPropertiesBuilder(
            @NotNull List<BotHandlerMethodArgumentResolver> argumentResolvers,
            @NotNull List<BotHandlerMethodReturnValueHandler> returnValueHandlers) {

        return TelegramBotGlobalProperties
                .builder()
                .argumentResolvers(argumentResolvers)
                .returnValueHandlers(returnValueHandlers)
                .taskExecutor(defaultTaskExecutor());
    }

    private TaskExecutor defaultTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(environment.getProperty("telegram.bot.core-pool-size", Integer.class, 15));
        threadPoolTaskExecutor.setMaxPoolSize(environment.getProperty("telegram.bot.max-pool-size", Integer.class, 200));
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean
    @Scope(value = TelegramScope.SCOPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    TelegramSession telegramSession() {
        return new TelegramSession();
    }

    /**
     * Create and add telegram service to context for each {@link TelegramBotProperties} bot configuration.
     *
     * @param httpClientSupplier uses if {@link OkHttpClient} object is not set for the current bot.
     */
    private void registerTelegramBotServices(RequestDispatcher requestDispatcher, List<TelegramBotProperties> botConfigurations, Supplier<OkHttpClient> httpClientSupplier) {
        if (botConfigurations.isEmpty()) {
            log.error("No bot configurations found");
            return;
        }

        List<TelegramService> services = new ArrayList<>();
        for (TelegramBotProperties property : botConfigurations) {

            // If OkHttpClient is not set, create one that will use our TaskExecutor
            if (property.getOkHttpClient() == null) {
                property = TelegramBotProperties
                        .builder(property.getToken())
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
        //runTelegramServices(services);
    }

    @Bean
    @Order()
    ApplicationListener<ContextRefreshedEvent> runTelegramServices(@Qualifier("telegramServices") List<TelegramService> telegramServices) {
        return event -> {
            for (TelegramService telegramService : telegramServices) {
                telegramService.start();
            }
        };
    }

    private TelegramBot createTelegramBot(TelegramBotProperties telegramBotProperties) {
        return new TelegramBot.Builder(telegramBotProperties.getToken())
                .okHttpClient(telegramBotProperties.getOkHttpClient())
                .updateListenerSleep(telegramBotProperties.getTimeOutMillis())
                .apiUrl(telegramBotProperties.getUrl())
                .build();
    }

    /**
     * Create properties for each unique token. Create default properties if it isn't specified by user.
     *
     * @param telegramMvcControllers bot controllers
     * @param botProperties          bot specific properties
     * @return properties for each unique token
     */
    private List<TelegramBotProperties> getTelegramBotProperties(List<TelegramMvcController> telegramMvcControllers, List<TelegramBotProperties> botProperties) {
        List<TelegramMvcController> telegramMvcConfigurationsList = Lists.newArrayList(telegramMvcControllers);
        AnnotationAwareOrderComparator.sort(telegramMvcConfigurationsList);

        List<String> uniqueTokens = telegramMvcControllers.stream()
                .map(TelegramMvcController::getToken).distinct()
                .filter(c -> !Strings.isNullOrEmpty(c))
                .collect(Collectors.toList());

        Map<String, List<TelegramBotProperties>> propertiesByToken = botProperties.stream()
                .filter(p -> !Strings.isNullOrEmpty(p.getToken()))
                .collect(Collectors.groupingBy(TelegramBotProperties::getToken));

        List<TelegramBotProperties> merged = new ArrayList<>();
        for (String token : uniqueTokens) {
            List<TelegramBotProperties> properties = propertiesByToken.get(token);

            // Create new property if there is no any
            // Get first in other case
            TelegramBotProperties property = (properties == null || properties.isEmpty())
                    ? TelegramBotProperties.builder(token).build()
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
    TelegramControllerBeanPostProcessor telegramControllerBeanPostProcessor(HandlerMethodContainer handlerMethodContainer) {
        return new TelegramControllerBeanPostProcessor(handlerMethodContainer);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        beanFactory.registerScope(TelegramScope.SCOPE,
                new TelegramScope(beanFactory, environment.getProperty("telegram.bot.session-seconds", Integer.class, 3600)));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
