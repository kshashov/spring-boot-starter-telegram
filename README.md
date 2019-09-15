# Spring-boot-starter for Telegram
[![Build Status](https://travis-ci.org/kshashov/spring-boot-starter-telegram.svg?branch=master)](https://travis-ci.org/kshashov/spring-boot-starter-telegram)
[![CircleCI](https://circleci.com/gh/kshashov/spring-boot-starter-telegram.svg?style=svg)](https://circleci.com/gh/kshashov/spring-boot-starter-telegram)
[![codecov](https://codecov.io/gh/kshashov/spring-boot-starter-telegram/branch/master/graph/badge.svg)](https://codecov.io/gh/kshashov/spring-boot-starter-telegram)

It's like Spring REST but for Telegram!

This is a starter for a spring boot application with the [Telegram Bot API](https://github.com/pengrad/java-telegram-bot-api/).

## Download
### Maven
```soon```
### Gradle
```soon```
### Jar
Check [releases page](https://github.com/kshashov/spring-boot-starter-telegram/releases)

## Getting Started
The only thing you need to do after adding the dependency is to create a controller for your bot
```java
@SpringBootApplication
@BotController
public class MyBot implements TelegramMvcController {

    @Value("${bot.token}")
    private String token;

    @Override
    public String getToken() {
        return token;
    }

    // Indicate what type of request you want to handle
    @BotRequest(value = "/click", messageType = {MessageType.CALLBACK_QUERY, MessageType.MESSAGE})
    public BaseRequest divide(User user, Chat chat) {
        return new SendMessage(chat.id(), "Hello, " + user.firstName() + "!");
    }

    // Use useful arguments including variables from the request pattern 
    @MessageRequest(value = "/divide {first:[0-9]} {second:[0-9]}")
    public BaseRequest divide(
        @BotPathVariable("first") String first, 
        @BotPathVariable("second") String second
    ) {
        // Return a string if you need to reply with a simple message
        return String.valueOf(Integer.parseInt(first) / ((double) Integer.parseInt(second)));
    }

    public static void main(String[] args) {
        SpringApplication.run(MyBot.class);
    }
}
```
The bot will be registered automatically on startup.
## @BotController

Telegram requests are handled by the controllers that implemented `TelegramMvcController` interface **and** are marked by the `@BotController` annotation.
It is supposed to use in combination with annotated handler methods based on the `BotRequest` annotation.
## @BotRequest
### Request binding
There are two important parameters here:
* `value` or `path`: The request mapping templates (e.g. `/foo`). Ant-style path patterns are supported (e.g. `/foo *`, `/foo param:[0-9]`). If the telegram request matched with several patterns at once, the result pattern will be selected randomly. Use `org.springframework.util.AntPathMatcher`. An empty pattern is matched for any request.
    * A `String` value of a path variable can be bound to a method argument by the `@BotPathVariable` annotation.
* `type`: the telegram request types to map. `MessageType.ANY` by default.

**Aliases**

If you want to handle only one type of telegram request, it is preferred to use one of the telegram method specific variants `@MessageRequest`, `@EditedMessageRequest`, `@ChannelPostRequest`, `@EditedChannelPostRequest`, `@InlineQueryRequest`, `@CallbackQueryRequest`, `@ChosenInlineResultRequest`, `@ShippingQueryRequest`, `@PreCheckoutQueryRequest`, `@PollRequest`.


### Supported arguments

Some parameters may be nullable because they do not exist for all types of telegram requests
* `TelegramRequest` - entity that inlude all available parameters from the initial request, the path pattern and path variables
* `TelegramSession` - current session for the current chat (if any) or user
* (Nullable) `String` marked with `BotPathVariable` annotation - value of the template variable from the path pattern

`com.pengrad.telegrambot.model.`
* `Update` - the initial user request which is currently being processed
* (Nullable) `String` - the first non-empty object, if any, among `message.text()`, `inlineQuery.query()`, `chosenInlineResult.query()`, `callbackQuery.data()`, `shippingQuery.invoicePayload()`, `prepreCheckoutQuery.invoicePayload()`
* (Nullable) `User`
* (Nullable) `Chat`
* (Nullable) `Message` - the first non-empty object, if any, among `update.message()`, `update.editedMessage()`, `update.channelPost()`, `update.editedChannelPost()`
* (Nullable) `InlineQuery`, `ChosenInlineResult`, `CallbackQuery`, `ShippingQuery`, `PreCheckoutQuery`, `Poll`

**Supported return values**
* `String` - automatically converted into `com.pengrad.telegrambot.request.SendMessage`. Use only if the chat value is not null for the current telegram request
* `com.pengrad.telegrambot.request.BaseRequest`

**How to support a new one**

If you want to add additional arguments or result values types for your controller methods, you should declare a new component:
* `BotHandlerMethodArgumentResolver` to support an additional type of method argument 
* `BotHandlerMethodReturnValueHandler` to support an additional type of method result
* `HandlerAdapter` to manually configure all enabled argument resolvers and result value handlers


## Configurations
By default, you can configure only these properties:

| property | description |
| -------- | ----------- |
| telegram.bot.corePoolSize | Core pool size for default pool executor |
| telegram.bot.maxPoolSize | Max pool size for default pool executor |
| telegram.bot.sessionSeconds | Cache expiration time for the all beans inside session scope |

If it isn’t enough, you can declare the following components with your implementations:
* `org.springframework.core.task.TaskExecutor` to configure default TaskExecutor instance
* `java.util.function.Consumer<okhttp3.OkHttpClient.Builder>` to configure default OkHttpClient instance
* `TelegramBotProperty` to manage the bot specific settings:
    ```java    
           @Bean
           public TelegramBotProperty botProperty2() {
               // Do not forget about token!
               // Other fields are optional
               
               return TelegramBotProperty
                       .builder()
                       .token(token)
                       .okHttpClient(new OkHttpClient.Builder()
                               .connectTimeout(30, TimeUnit.SECONDS)
                               .build())
                       .build();
           }
    ```

## License
```
MIT License

Copyright (c) 2019 Kirill Shashov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Thanks
* @OlegNyr for his [java-telegram-bot-mvc](https://github.com/OlegNyr/java-telegram-bot-mvc) repo