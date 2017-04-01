package com.slx.funstream.di.chat;

import com.slx.funstream.ui.chat.ChatFragment;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;


@Subcomponent
public interface ChatFragmentSubComponent extends AndroidInjector<ChatFragment> {

    @Subcomponent.Builder
    public abstract class Builder extends AndroidInjector.Builder<ChatFragment> {

    }
}
