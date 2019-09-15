package com.github.kshashov.telegram;

import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

public class TelegramRequestResult {

    /**
     * Request that we send in a telegram.
     */
    @Getter
    @Setter
    private BaseRequest baseRequest;

    /**
     * Telegram response for {@link #baseRequest}.
     */
    @Getter
    @Setter
    private BaseResponse baseResponse;

    /**
     * Contain exception object if any error occurred after {@link #baseRequest}
     */
    @Getter
    @Setter
    private Throwable error;

    public TelegramRequestResult() {
    }
}
