package com.slx.funstream.di.modules;

import android.app.Activity;
import android.app.Service;
import android.support.v4.app.Fragment;

import com.slx.funstream.chat.ChatService;
import com.slx.funstream.di.auth.AuthSubComponent;
import com.slx.funstream.di.auth.UserSubComponent;
import com.slx.funstream.di.chat.ChatFragmentSubComponent;
import com.slx.funstream.di.chat.ChatServiceSubComponent;
import com.slx.funstream.di.stream.ChannelsSubComponent;
import com.slx.funstream.di.stream.StreamSubComponent;
import com.slx.funstream.di.stream.StreamsSubComponent;
import com.slx.funstream.ui.chat.ChatFragment;
import com.slx.funstream.ui.streams.ChannelListFragment;
import com.slx.funstream.ui.streams.StreamActivity;
import com.slx.funstream.ui.streams.StreamsActivity;
import com.slx.funstream.ui.user.LoginActivity;
import com.slx.funstream.ui.user.UserActivity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.ServiceKey;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

/**
 * This module contains all the binding to the sub component builders in the app
 */
@Module
public abstract class BuildersModule {

    @Binds
    @IntoMap
    @ActivityKey(LoginActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindLoginActivityInjectorFactory(AuthSubComponent.Builder builder);


    @Binds
    @IntoMap
    @ActivityKey(StreamsActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindStreamsActivityInjectorFactory(StreamsSubComponent.Builder builder);

    @Binds
    @IntoMap
    @ActivityKey(StreamActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindStreamActivityInjectorFactory(StreamSubComponent.Builder builder);

    @Binds
    @IntoMap
    @ActivityKey(UserActivity.class)
    abstract AndroidInjector.Factory<? extends Activity>
    bindUserActivityInjectorFactory(UserSubComponent.Builder builder);


    @Binds
    @IntoMap
    @FragmentKey(ChannelListFragment.class)
    abstract AndroidInjector.Factory<? extends Fragment>
    bindChatListFragmentInjectorFactory(ChannelsSubComponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ChatFragment.class)
    abstract AndroidInjector.Factory<? extends Fragment>
    bindChatFragmentInjectorFactory(ChatFragmentSubComponent.Builder builder);

    @Binds
    @IntoMap
    @ServiceKey(ChatService.class)
    abstract AndroidInjector.Factory<? extends Service>
    bindChatServiceInjectorFactory(ChatServiceSubComponent.Builder builder);



}
