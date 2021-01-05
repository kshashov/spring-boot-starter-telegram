# Spring Boot Starter for Telegram
[![Maven Central](https://img.shields.io/maven-central/v/com.github.kshashov/spring-boot-starter-telegram.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.kshashov%22%20AND%20a:%22spring-boot-starter-telegram%22)
[![jitpack](https://jitpack.io/v/kshashov/spring-boot-starter-telegram.svg)](https://jitpack.io/#kshashov/spring-boot-starter-telegram)
[![Build Status](https://travis-ci.org/kshashov/spring-boot-starter-telegram.svg?branch=master)](https://travis-ci.org/kshashov/spring-boot-starter-telegram)
[![CircleCI](https://circleci.com/gh/kshashov/spring-boot-starter-telegram.svg?style=svg)](https://circleci.com/gh/kshashov/spring-boot-starter-telegram)
[![codecov](https://codecov.io/gh/kshashov/spring-boot-starter-telegram/branch/master/graph/badge.svg)](https://codecov.io/gh/kshashov/spring-boot-starter-telegram)

This is a spring boot starter for [Telegram Bot API](https://github.com/pengrad/java-telegram-bot-api/). It's like Spring MVC but for Telegram!

* [Download](#Download)
    * [Maven](#Maven)
    * [Gradle](#Gradle)
* [Example](#Example)
* [@BotController](#BotController)
* [@BotRequest](#BotRequest)
    * [Request binding](#Request-binding)
    * [Supported arguments](#Supported-arguments)
    * [Supported return values](#Supported-return-values)
    * [How to support a new one](#How-to-support-a-new-one)
* [Configurations](#Configurations)
    * [Properties](#Properties)
    * [Java-based configurations](#Java-based-configurations)
        * [Webhooks](#Webhooks)
* [Metrics](#Metrics)
* [License](#License)
* [Thanks](#Thanks)

## Download
### Maven
```xml
<dependency>
  <groupId>com.github.kshashov</groupId>
  <artifactId>spring-boot-starter-telegram</artifactId>
  <version>0.23</version>
</dependency>
```
### Gradle
```groovy
implementation 'com.github.kshashov:spring-boot-starter-telegram:0.23'
```

## Example
The only thing you need to do after adding the dependency is to create a bot controller
```java
@BotController
@SpringBootApplication
public class MyBot implements TelegramMvcController {

    @Value("${bot.token}")
    private String token;

    @Override
    public String getToken() {
        return token;
    }

    @BotRequest(value = "/hello", type = {MessageType.CALLBACK_QUERY, MessageType.MESSAGE})
    public BaseRequest hello(User user, Chat chat) {
        return new SendMessage(chat.id(), "Hello, " + user.firstName() + "!");
    }

    @MessageRequest("/hello {name:[\\S]+}")
    public String helloWithName(@BotPathVariable("name") String userName) {
        // Return a string if you need to reply with a simple message
        return "Hello, " + userName;
    }

    @MessageRequest("/helloCallback")
    public String helloWithCustomCallback(TelegramRequest request, User user) {
        request.setCallback(new Callback<BaseRequest, BaseResponse>() {
            @Override
            public void onResponse(BaseRequest request, BaseResponse response) {
                // TODO
            }

            @Override
            public void onFailure(BaseRequest request, IOException e) {
                // TODO
            }
        });
        return "Hello, " + user.firstName() + "!";
    }

    public static void main(String[] args) {
        SpringApplication.run(MyBot.class);
    }
}
```
The bot will be registered automatically on startup.
## BotController

Telegram requests are handled by the controllers that implemented `TelegramMvcController` interface **and** are marked by the `@BotController` annotation.
It is supposed to use in combination with annotated handler methods based on the `BotRequest` annotation.

## BotRequest
### Request binding
There are two important parameters here:
* `value` or `path`: The request mapping templates (e.g. `/foo`). Ant-style path patterns are supported (e.g. `/foo *`, `/foo {param:[0-9]}`).
    * Values of the path variables can be bound to the method arguments by the `@BotPathVariable` annotation.
* `type`: the telegram request types to map. `MessageType.ANY` by default.

**Aliases**

If you want to handle only one type of telegram request, it is preferred to use one of the telegram method specific variants `@MessageRequest`, `@EditedMessageRequest`, `@ChannelPostRequest`, `@EditedChannelPostRequest`, `@InlineQueryRequest`, `@CallbackQueryRequest`, `@ChosenInlineResultRequest`, `@ShippingQueryRequest`, `@PreCheckoutQueryRequest`, `@PollRequest`.

**Pattern matching**

`org.springframework.util.AntPathMatcher` is used for patterns matching and variables extracting, so any [Ant-style path patterns](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html) are supported (e.g. `/foo *`, `/foo {param:[0-9]}`):
* `?` matches one character
* `*` matches zero or more characters
* `**` matches zero or more directories in a path
* `{spring:[a-z]+}` matches the regexp `[a-z]+` as a path variable named `spring`

An empty patterns list will be replaced with the `**` pattern and matched with any requested text.

**Routes sorting**

 If the telegram request matched with several route mappings at once, the most specific one is selected. By default the routes are sorted by:
 * pattern complexity
 * patterns list size. Be aware that the empty patterns list (= any pattern) has minimal priority.
 * types count. `MessageType.ANY` has minimal priority.

For example, you can define the default handler in the following way:
```java
    @BotRequest(type = {MessageType.ANY})
    public void default() {
        log.info("Default handler method");
    }
```

**Custom behavior**

If you need to override the matcher or process routes in a custom way, you can declare a new `RequestMappingsMatcherStrategy` component or override a [global configuration](#Configurations). You can use `DefaultRequestMappingsMatcherStrategy` as an example.

### Supported arguments

Some parameters may be nullable because they do not exist for all types of telegram requests
* `TelegramRequest` - entity that include all available parameters from the initial request, the path pattern and path variables. Provides an ability to set a custom callback
* `TelegramSession` - current session for the current chat (if any) or user
* `com.pengrad.telegrambot.TelegramBot` - bot instance that received the request
* **Nullable** `String`, `Integer`, `Long`, `Double`, `Float`, `BigInteger`, `BigDecimal` marked with `BotPathVariable` annotation - a value of the template variable from the path pattern
* `com.pengrad.telegrambot.model.Update` - the initial user request which is currently being processed
* **Nullable** `String` - the first non-empty object, if any, among `message.text()`, `inlineQuery.query()`, `chosenInlineResult.query()`, `callbackQuery.data()`, `shippingQuery.invoicePayload()`, `prepreCheckoutQuery.invoicePayload()`
* **Nullable** `com.pengrad.telegrambot.model.User`
* **Nullable** `com.pengrad.telegrambot.model.Chat`
* **Nullable** `com.pengrad.telegrambot.model.Message` - the first non-empty object, if any, among `update.message()`, `update.editedMessage()`, `update.channelPost()`, `update.editedChannelPost()`
* **Nullable** `com.pengrad.telegrambot.model.InlineQuery`, `com.pengrad.telegrambot.model.ChosenInlineResult`, `com.pengrad.telegrambot.model.CallbackQuery`, `com.pengrad.telegrambot.model.ShippingQuery`, `com.pengrad.telegrambot.model.PreCheckoutQuery`, `com.pengrad.telegrambot.model.Poll`

### Supported return values
* `String` - automatically converted into `com.pengrad.telegrambot.request.SendMessage`. Use only if the chat value is not null for the current telegram request
* `com.pengrad.telegrambot.request.BaseRequest`
* `void`

### How to support a new one

If you want to add additional arguments or result values types for your controller methods, you should declare a new component:
* `BotHandlerMethodArgumentResolver` to support an additional type of method argument
* `BotHandlerMethodReturnValueHandler` to support an additional type of method result
* `TelegramBotGlobalPropertiesConfiguration` to manually configure all enabled argument resolvers and result value handlers


## Configurations

### Properties
By default, you can configure only these properties:

| Property | Description | Default value |
| -------- | ----------- | ------- |
| telegram.bot.core-pool-size | Core pool size for default pool executor | 15 |
| telegram.bot.max-pool-size | Max pool size for default pool executor | 50 |
| telegram.bot.session-seconds | Cache expiration time for the all beans inside session scope | 3600 |
| telegram.bot.update-listener-sleep | Timeout between requests to Telegrams API if long polling is enabled (ms) | 300 |
| telegram.bot.server-port | HTTP port that will be used to start embedded web server if webhooks is enabled | 8443 |

### Java-based configurations
If it isn’t enough, you can use Java-based configurations:
* `TelegramBotGlobalPropertiesConfiguration` to configure global and bot specific settings:
    ```java
    @Component
    public class MyBotConfiguration implements TelegramBotGlobalPropertiesConfiguration {
        ...

        @Override
        public void configure(TelegramBotGlobalProperties.Builder builder) {
            OkHttpClient okHttp = new OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .build();

            builder
                .configureBot(token, botBuilder -> {
                    botBuilder
                        .configure(builder1 -> builder1.okHttpClient(okHttp));
                        .withWebhook(new SetWebhook().url(url));
                })
                .configureBot(token2, botBuilder -> {
                    botBuilder
                        .configure(builder1 -> builder1.updateListenerSleep(200L));
                });
        }
    }
    ```

#### Webhooks
If you want to use webhooks instead of long polling, you need to provide webhook url:
```java
                //.setWebserverPort(8443) Here you can customize the port
                .configureBot(token, botBuilder -> {
                    botBuilder
                        .withWebhook(new SetWebhook().url(url));
                })
```
In this case the library
* starts local [Javalin](https://javalin.io/) server on 8443 (by default) port.
* registers `{url}/{random_uuid}` webhook via Telegram API
* adds `/{random_uuid}` endpoint to the local server

## Metrics
You can check the following metrics via jmx in the `bot.metrics` domain:

| Metric | Description |
| ------ | ----------- |
| `updates` | A number of updates received from Telegram |
| `processing.errors` | A number of exceptions thrown during updates processing |
| `no.handlers.errors` | A number of updates for which no suitable handlers were found |
| `handler.{handler_method_name}.errors` | A number of exceptions thrown during handler method execution |
| `handler.{handler_method_name}.successes` | A number of successful executions of handler method |
| `handler.{handler_method_name}.execution.time` | A time spent on successful handler method execution |

## License
```
MIT License

Copyright (c) 2020 Kirill Shashov

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
