package com.github.kshashov.telegram;

import com.github.kshashov.telegram.handler.processor.arguments.BotHandlerMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.arguments.BotRequestMethodArgumentResolver;
import com.github.kshashov.telegram.handler.processor.arguments.BotRequestMethodPathArgumentResolver;
import com.github.kshashov.telegram.handler.processor.response.BotBaseRequestMethodProcessor;
import com.github.kshashov.telegram.handler.processor.response.BotHandlerMethodReturnValueHandler;
import com.github.kshashov.telegram.handler.processor.response.BotResponseBodyMethodProcessor;
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
