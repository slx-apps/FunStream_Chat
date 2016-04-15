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

package com.slx.funstream.di;

import android.content.Context;

import com.slx.funstream.App;
import com.slx.funstream.utils.RxBus;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
	private final App application;

	public ApplicationModule(App application) {
		this.application = application;
	}

	@Provides
	@PerApp
	public App application() {
		return this.application;
	}

	@Provides
	@PerApp
	public Context applicationContext() {
		return this.application;
	}


	@Provides
	@PerApp
	public RxBus provideRxBus() {
		return new RxBus();
	}

}
