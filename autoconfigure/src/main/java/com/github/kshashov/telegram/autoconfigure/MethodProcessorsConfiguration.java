package com.github.kshashov.telegram.autoconfigure;

import com.github.kshashov.telegram.autoconfigure.processors.arguments.BotRequestMethodArgumentResolver;
import com.github.kshashov.telegram.autoconfigure.processors.arguments.BotRequestMethodPathArgumentResolver;
import com.github.kshashov.telegram.autoconfigure.processors.response.BotBaseRequestMethodProcessor;
import com.github.kshashov.telegram.autoconfigure.processors.response.BotResponseBodyMethodProcessor;
import com.github.kshashov.telegram.handler.processor.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.BotHandlerMethodReturnValueHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

@Configuration
public class MethodProcessorsConfiguration {

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
