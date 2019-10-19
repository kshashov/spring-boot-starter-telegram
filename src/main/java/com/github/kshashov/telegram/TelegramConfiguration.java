package com.github.kshashov.telegram;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.TelegramSession;
import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.config.TelegramBotGlobalPropertiesConfiguration;
import com.github.kshashov.telegram.config.TelegramBotProperties;
import com.github.kshashov.telegram.handler.HandlerMethodContainer;
import com.github.kshashov.telegram.handler.RequestDispatcher;
import com.github.kshashov.telegram.handler.TelegramService;
import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.google.common.base.Strings;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            final List<TelegramMvcController> controllers,
            final List<TelegramBotProperties> botProperties,
            final TelegramBotGlobalProperties.Builder defaultBotGlobalPropertiesBuilder,
            final Optional<TelegramBotGlobalPropertiesConfiguration> botGlobalPropertiesConfiguration) {

        botGlobalPropertiesConfiguration.ifPresent(conf -> conf.configure(defaultBotGlobalPropertiesBuilder));
        TelegramBotGlobalProperties botGlobalProperties = defaultBotGlobalPropertiesBuilder.build();

        Map<String, TelegramBotProperties> propertiesByToken = botProperties.stream()
                .filter(p -> !Strings.isNullOrEmpty(p.getToken()))
                .sorted(new AnnotationAwareOrderComparator())
                .collect(Collectors.toMap(
                        TelegramBotProperties::getToken,
                        telegramBotProperties -> telegramBotProperties,
                        (i1, i2) -> i1));

        List<TelegramBotProperties> allBotProperties = createTelegramBotProperties(controllers, propertiesByToken).stream()
                .map(p -> fixTelegramBotProperties(p, botGlobalProperties))
                .collect(Collectors.toList());

        if (allBotProperties.isEmpty()) {
            log.error("No bot configurations found");
            return null;
        }

        RequestDispatcher requestDispatcher = new RequestDispatcher(handlerMethodContainer, botGlobalProperties);
        List<TelegramService> services = createTelegramBotServices(requestDispatcher, allBotProperties);
        beanFactory.registerSingleton("telegramServices", services);

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
                .responseCallback(createDefaultResponseCallback())
                .taskExecutor(createDefaultTaskExecutor());
    }

    @Bean
    @Scope(value = TelegramScope.SCOPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    TelegramSession telegramSession() {
        return new TelegramSession();
    }

    @Bean
    ApplicationListener<ContextRefreshedEvent> runTelegramServices(@Qualifier("telegramServices") List<TelegramService> telegramServices) {
        return event -> telegramServices.forEach(TelegramService::start);
    }

    /**
     * Create telegram services for each {@link TelegramBotProperties} bot configuration.
     */
    private List<TelegramService> createTelegramBotServices(RequestDispatcher requestDispatcher, List<TelegramBotProperties> botConfigurations) {
        return botConfigurations.stream()
                .map(property -> {
                    TelegramBot telegramBot = createTelegramBot(property);
                    return new TelegramService(telegramBot, requestDispatcher);
                }).collect(Collectors.toList());
    }

    /**
     * Return the same {@link TelegramBotProperties} object if it's correct. Otherwise, replace properties with new
     * fixed instance.
     *
     * @param properties       properties to process
     * @param globalProperties global properties
     * @return the same properties instance or new fixed one.
     */
    private TelegramBotProperties fixTelegramBotProperties(@NotNull TelegramBotProperties properties, @NotNull TelegramBotGlobalProperties globalProperties) {
        if (properties.getOkHttpClient() != null) return properties;

        return TelegramBotProperties
                .builder(properties.getToken())
                .url(properties.getUrl())
                .timeOutMillis(properties.getTimeOutMillis())
                .okHttpClient(createDefaultOkHttpClient(globalProperties))
                .build();
    }

    /**
     * Create properties for each unique controller token. Add default properties if it isn't specified by user.
     *
     * @param telegramMvcControllers bot controllers
     * @param propertiesByToken      pre-populated collection of bot specific properties
     * @return properties for each unique token
     */
    private List<TelegramBotProperties> createTelegramBotProperties(List<TelegramMvcController> telegramMvcControllers, Map<String, TelegramBotProperties> propertiesByToken) {
        return telegramMvcControllers.stream()
                .map(TelegramMvcController::getToken)
                .filter(c -> !Strings.isNullOrEmpty(c))
                .distinct() // unique bot tokens
                .map(token ->
                        // Create new property if there is no any
                        propertiesByToken.getOrDefault(token, TelegramBotProperties.builder(token).build())
                ).collect(Collectors.toList());
    }

    private Callback<BaseRequest, BaseResponse> createDefaultResponseCallback() {
        return new Callback<BaseRequest, BaseResponse>() {
            @Override
            public void onResponse(BaseRequest request, BaseResponse response) {
            }

            @Override
            public void onFailure(BaseRequest request, IOException e) {
            }
        };
    }

    private TaskExecutor createDefaultTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(environment.getProperty("telegram.bot.core-pool-size", Integer.class, 15));
        threadPoolTaskExecutor.setMaxPoolSize(environment.getProperty("telegram.bot.max-pool-size", Integer.class, 200));
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    private OkHttpClient createDefaultOkHttpClient(TelegramBotGlobalProperties botGlobalProperties) {
        return new OkHttpClient()
                .newBuilder()
                .dispatcher(new Dispatcher(new ExecutorServiceAdapter(botGlobalProperties.getTaskExecutor())))
                .build();
    }

    private TelegramBot createTelegramBot(TelegramBotProperties telegramBotProperties) {
        return new TelegramBot.Builder(telegramBotProperties.getToken())
                .okHttpClient(telegramBotProperties.getOkHttpClient())
                .updateListenerSleep(telegramBotProperties.getTimeOutMillis())
                .apiUrl(telegramBotProperties.getUrl())
                .build();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
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
