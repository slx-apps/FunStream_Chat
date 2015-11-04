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

import com.slx.funstream.dagger.Injector;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class CustomApplication extends Application {
	private RefWatcher refWatcher;

	public static RefWatcher getRefWatcher(Context context) {
		CustomApplication application = (CustomApplication) context.getApplicationContext();
		return application.refWatcher;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Injector.INSTANCE.initializeApplicationComponent(this);
		if(BuildConfig.DEBUG) refWatcher = LeakCanary.install(this);
	}

}
