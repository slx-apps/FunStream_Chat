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


import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.slx.funstream.di.ApplicationComponent;
import com.slx.funstream.di.ApplicationModule;
import com.slx.funstream.di.ChatComponent;
import com.slx.funstream.di.ChatModule;
import com.slx.funstream.di.DaggerApplicationComponent;
import com.slx.funstream.di.DaggerChatComponent;
import com.slx.funstream.di.StorageModule;
import com.slx.funstream.di.FunstreamApiModule;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
	private static ApplicationComponent applicationComponent;
    private static ChatComponent chatComponent;
	private RefWatcher refWatcher;

	@Override
	public void onCreate() {
		super.onCreate();
        Fabric.with(this, new Crashlytics());
        Stetho.initializeWithDefaults(this);
		initializeApplicationComponent(this);
		if (BuildConfig.DEBUG) refWatcher = LeakCanary.install(this);
	}

	public static RefWatcher getRefWatcher(Context context) {
		App application = (App) context.getApplicationContext();
		return application.refWatcher;
	}

    public static void initializeApplicationComponent(App app) {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(app))
                .storageModule(new StorageModule())
                .funstreamApiModule(new FunstreamApiModule())
                .build();
    }
    public static ApplicationComponent applicationComponent() {
        return applicationComponent;
    }

    public static ChatComponent chatComponent() {
        return chatComponent;
    }

    public static ChatComponent initializeChatComponent() {
        return chatComponent = DaggerChatComponent.builder()
                .chatModule(new ChatModule())
                .applicationComponent(applicationComponent)
                .build();
    }

}
