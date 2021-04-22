package com.github.kshashov.telegram.handler.processor;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * Result of the Telegram request processing. Includes callback methods to handle Telegram response.
 */
public class TelegramCallback implements Callback {
    private final BaseRequest request;
    private final Callback nestedCallback;

    public TelegramCallback(@NotNull BaseRequest request, @Nullable Callback nestedCallback) {
        this.request = request;
        this.nestedCallback = nestedCallback;
    }

    public BaseRequest getRequest() {
        return request;
    }

    @Override
    public void onResponse(BaseRequest request, BaseResponse response) {
        if (nestedCallback == null) return;
        nestedCallback.onResponse(request, response);
    }

    @Override
    public void onFailure(BaseRequest request, IOException e) {
        if (nestedCallback == null) return;
        nestedCallback.onFailure(request, e);
    }
}
