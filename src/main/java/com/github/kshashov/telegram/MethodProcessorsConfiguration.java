package com.github.kshashov.telegram;

import com.github.kshashov.telegram.handler.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.arguments.BotRequestMethodArgumentResolver;
import com.github.kshashov.telegram.handler.arguments.BotRequestMethodPathArgumentResolver;
import com.github.kshashov.telegram.handler.response.BotBaseRequestMethodProcessor;
import com.github.kshashov.telegram.handler.response.BotHandlerMethodReturnValueHandler;
import com.github.kshashov.telegram.handler.response.BotResponseBodyMethodProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

import java.util.List;

@Configuration
public class MethodProcessorsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HandlerAdapter handlerAdapter(List<BotHandlerMethodArgumentResolver> resolvers, List<BotHandlerMethodReturnValueHandler> handlers) {
        return new HandlerAdapter(resolvers, handlers);
    }

    @Bean
    public BotHandlerMethodArgumentResolver botRequestMethodArgumentResolver() {
        return new BotRequestMethodArgumentResolver();
    }

    @Bean
    public BotHandlerMethodArgumentResolver botRequestMethodPathArgumentResolver() {
        return new BotRequestMethodPathArgumentResolver();
    }

    @Bean
    public BotHandlerMethodReturnValueHandler botBaseRequestMethodProcessor() {
        return new BotBaseRequestMethodProcessor();
    }

    @Bean
    public BotHandlerMethodReturnValueHandler botResponseBodyMethodProcessor(ConversionService conversionService) {
        return new BotResponseBodyMethodProcessor(conversionService);
    }

    @Bean
    public ConversionService conversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();
        return bean.getObject();
    }
}
