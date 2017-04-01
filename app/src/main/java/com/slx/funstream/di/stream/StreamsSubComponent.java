package com.slx.funstream.di.stream;

import com.slx.funstream.di.modules.BuildersModule;
import com.slx.funstream.ui.streams.StreamsActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface StreamsSubComponent extends AndroidInjector<StreamsActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<StreamsActivity> {

    }
}
