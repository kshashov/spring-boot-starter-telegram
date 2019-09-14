package com.github.kshashov.telegram.api;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;

import java.util.Map;

public class TelegramRequest {
    /**
     * Оригинальный запрос пользователя
     */
    private final Update update;
    /**
     * Сообщение пользователя
     */
    private final Message message;
    /**
     * Идентификатор беседы
     */
    private final Long chatId;
    /**
     * Ссылка на сущьность Chat
     */
    private final Chat chat;
    /**
     * Ссылка на сущьность User
     */

    private final User user;
    /**
     * строка ввода пользователя
     */
    private final String text;
    /**
     * Сервис бота
     */
    private TelegramBot telegramBot;

    /**
     * Сессия диалога
     */
    private TelegramSession session;
    /**
     * Тип запроса
     */
    private MessageType messageType = MessageType.MESSAGE;

    /**
     * переменные распарсенные из запроса
     */
    private Map<String, String> templateVariables;
    /**
     * Паттрен для поиска запроса
     */
    private String basePattern;
    /**
     * Запрос который отправим в телеграмм
     */
    private BaseRequest baseRequest;
    private BaseResponse baseResponse;
    /**
     * Ответ который ответил телеграмм
     */
    private boolean responseOk;
    private String responseText;

    public TelegramRequest(Update update, TelegramBot telegramBot) {
        this.update = update;
        this.message = firstNonNull(update.message(),
                update.editedMessage(),
                update.channelPost(),
                update.editedChannelPost());

        this.telegramBot = telegramBot;
        if (message != null) {
            this.user = firstNonNull(message.from(), message.leftChatMember(), message.forwardFrom());
            this.chat = firstNonNull(message.chat(), message.forwardFromChat());
            this.text = message.text();
            if (text != null && text.startsWith("/")) {
                this.messageType = MessageType.COMMAND;
            }
        } else {
            InlineQuery inlineQuery = update.inlineQuery();
            if (inlineQuery != null) {
                this.user = inlineQuery.from();
                this.text = inlineQuery.query();
                this.chat = null;
                this.messageType = MessageType.INLINE_QUERY;
            } else {
                this.chat = null;
                ChosenInlineResult chosenInlineResult = update.chosenInlineResult();
                if (chosenInlineResult != null) {
                    this.user = chosenInlineResult.from();
                    this.text = chosenInlineResult.query();
                    this.messageType = MessageType.INLINE_CHOSEN;
                } else {
                    CallbackQuery callbackQuery = update.callbackQuery();
                    if (callbackQuery != null) {
                        this.user = callbackQuery.from();
                        this.text = callbackQuery.data();
                        this.messageType = MessageType.INLINE_CALLBACK;
                    } else {
                        this.messageType = MessageType.MESSAGE;
                        this.user = null;
                        this.text = null;
                    }
                }
            }
        }
        if (chat != null) {
            chatId = chat.id();
        } else if (user != null) {
            chatId = Long.valueOf(user.id());
        } else {
            chatId = Long.valueOf(update.updateId());
        }

    }

    public void setBaseRequest(BaseRequest baseRequest) {
        this.baseRequest = baseRequest;
    }

    public void setTemplateVariables(Map<String, String> templateVariables) {
        this.templateVariables = templateVariables;
    }

    public void setBasePattern(String basePattern) {
        this.basePattern = basePattern;
    }

    public void setSession(TelegramSession session) {
        this.session = session;
    }

    public void complete(BaseResponse baseResponse) {
        if (baseResponse != null) {
            this.responseOk = baseResponse.isOk();
            this.baseResponse = baseResponse;
        } else {
            this.responseOk = true;
        }
    }

    public void error(Exception e) {
        this.responseOk = false;
        this.responseText = e.getMessage();
    }

    public long chatId() {
        return chatId;
    }

    private static <T> T firstNonNull(T... messages) {
        for (T message : messages) {
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    public Update getUpdate() {
        return update;
    }

    public Message getMessage() {
        return message;
    }

    public Long getChatId() {
        return chatId;
    }

    public Chat getChat() {
        return chat;
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }

    public TelegramSession getSession() {
        return session;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Map<String, String> getTemplateVariables() {
        return templateVariables;
    }

    public String getBasePattern() {
        return basePattern;
    }

    public BaseRequest getBaseRequest() {
        return baseRequest;
    }

    public BaseResponse getBaseResponse() {
        return baseResponse;
    }

    public boolean isResponseOk() {
        return responseOk;
    }

    public String getResponseText() {
        return responseText;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TelegramRequest{");
        sb.append("chatId=").append(chatId);
        sb.append(", chat=").append(chat);
        sb.append(", user=").append(user);
        sb.append(", text='").append(text).append('\'');
        sb.append(", messageType=").append(messageType);
        sb.append('}');
        return sb.toString();
    }
}
