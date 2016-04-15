package com.slx.funstream.di;

import com.google.gson.Gson;
import com.slx.funstream.App;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.ChatServicePresenter;
import com.slx.funstream.utils.PrefUtils;
import com.slx.funstream.utils.RxBus;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatModule {

    @Provides
    @PerChatService
    public ChatServicePresenter provideChatServicePresenter(RxBus rxBus, PrefUtils prefUtils,
                                                            UserStore userStore,
                                                            Gson gson) {
        return new ChatServicePresenter(rxBus, prefUtils, userStore, gson);
    }
}
