package com.slx.funstream.di.stream;

import com.slx.funstream.chat.ChatService;
import com.slx.funstream.ui.streams.ChannelListFragment;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;


@Subcomponent
public interface ChannelsSubComponent extends AndroidInjector<ChannelListFragment> {

    @Subcomponent.Builder
    public abstract class Builder extends AndroidInjector.Builder<ChannelListFragment> {

    }
}
