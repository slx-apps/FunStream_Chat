package com.slx.funstream.di.modules;

import com.google.gson.Gson;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.ChatServiceController;
import com.slx.funstream.di.PerApp;
import com.slx.funstream.utils.PrefUtils;
import com.slx.funstream.utils.RxBus;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatModule {

    @Provides
    @PerApp
    public ChatServiceController provideChatServicePresenter(RxBus rxBus, PrefUtils prefUtils,
                                                             UserStore userStore,
                                                             Gson gson) {
        return new ChatServiceController(rxBus, prefUtils, userStore, gson);
    }
}
