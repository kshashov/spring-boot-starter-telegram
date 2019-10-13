package com.github.kshashov.telegram;

import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.Data;

@Data
public class TelegramRequestResult {

    /**
     * Request that we send in a telegram.
     */
    private BaseRequest baseRequest;

    /**
     * Telegram response for {@link #baseRequest}.
     */
    private BaseResponse baseResponse;

    /**
     * Contain exception object if any error occurred after {@link #baseRequest}
     */
    private Throwable error;

    public TelegramRequestResult() {
    }
}
