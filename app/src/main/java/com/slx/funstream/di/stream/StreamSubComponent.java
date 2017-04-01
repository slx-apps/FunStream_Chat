package com.slx.funstream.di.stream;

import com.slx.funstream.di.modules.BuildersModule;
import com.slx.funstream.ui.streams.StreamActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent(modules = { BuildersModule.class })
public interface StreamSubComponent extends AndroidInjector<StreamActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<StreamActivity> {

    }
}
