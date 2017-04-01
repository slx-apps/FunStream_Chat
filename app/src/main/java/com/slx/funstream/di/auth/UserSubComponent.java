package com.slx.funstream.di.auth;

import com.slx.funstream.ui.user.LoginActivity;
import com.slx.funstream.ui.user.UserActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface UserSubComponent extends AndroidInjector<UserActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<UserActivity> {

    }
}
