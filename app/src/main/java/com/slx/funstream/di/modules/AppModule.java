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

package com.slx.funstream.di.modules;

import android.content.Context;

import com.slx.funstream.App;
import com.slx.funstream.di.PerApp;
import com.slx.funstream.di.auth.AuthSubComponent;
import com.slx.funstream.di.auth.UserSubComponent;
import com.slx.funstream.di.chat.ChatFragmentSubComponent;
import com.slx.funstream.di.chat.ChatServiceSubComponent;
import com.slx.funstream.di.stream.ChannelsSubComponent;
import com.slx.funstream.di.stream.StreamSubComponent;
import com.slx.funstream.di.stream.StreamsSubComponent;
import com.slx.funstream.utils.RxBus;

import dagger.Module;
import dagger.Provides;

@Module(subcomponents = { AuthSubComponent.class, ChatFragmentSubComponent.class, StreamsSubComponent.class,
		StreamSubComponent.class, UserSubComponent.class, ChatServiceSubComponent.class, ChannelsSubComponent.class })
public class AppModule {
	private final App application;

	public AppModule(App application) {
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
