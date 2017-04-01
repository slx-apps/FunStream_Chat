package com.slx.funstream.di.auth;

import com.slx.funstream.di.modules.BuildersModule;
import com.slx.funstream.ui.user.LoginActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface AuthSubComponent extends AndroidInjector<LoginActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<LoginActivity> {

    }
}
