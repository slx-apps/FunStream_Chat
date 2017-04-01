package com.slx.funstream.di.chat;

import com.slx.funstream.chat.ChatService;
import com.slx.funstream.ui.chat.ChatFragment;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;


@Subcomponent
public interface ChatServiceSubComponent extends AndroidInjector<ChatService> {

    @Subcomponent.Builder
    public abstract class Builder extends AndroidInjector.Builder<ChatService> {

    }
}
