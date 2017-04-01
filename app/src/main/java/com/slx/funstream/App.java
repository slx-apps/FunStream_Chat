/*
 *   Copyright (C) 2015 Alex Neeky
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.slx.funstream;


import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.slx.funstream.di.modules.AppModule;
import com.slx.funstream.di.DaggerAppComponent;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasDispatchingActivityInjector;
import dagger.android.HasDispatchingServiceInjector;
import dagger.android.support.HasDispatchingSupportFragmentInjector;
import io.fabric.sdk.android.Fabric;

public class App extends Application implements HasDispatchingActivityInjector,
        HasDispatchingSupportFragmentInjector,
        HasDispatchingServiceInjector {

	@Inject DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
    @Inject DispatchingAndroidInjector<Service> dispatchingServiceFragmentAndroidInjector;
	@Inject DispatchingAndroidInjector<Fragment> dispatchingFragmentAndroidInjector;

	private RefWatcher refWatcher;

	@Override
	public void onCreate() {
		super.onCreate();
        Fabric.with(this, new Crashlytics());
        Stetho.initializeWithDefaults(this);

       DaggerAppComponent.builder()
			   .application(this)
			   .appModule(new AppModule(this))
               .build()
               .inject(this);

		if (BuildConfig.DEBUG) refWatcher = LeakCanary.install(this);
	}

	public static RefWatcher getRefWatcher(Context context) {
		App application = (App) context.getApplicationContext();
		return application.refWatcher;
	}

	@Override
	public DispatchingAndroidInjector<Activity> activityInjector() {
		return dispatchingAndroidInjector;
	}

    @Override
    public DispatchingAndroidInjector<Service> serviceInjector() {
        return dispatchingServiceFragmentAndroidInjector;
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingFragmentAndroidInjector;
    }
}
