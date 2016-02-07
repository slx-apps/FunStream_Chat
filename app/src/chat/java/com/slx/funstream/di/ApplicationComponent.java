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
import com.slx.funstream.adapters.ChatAdapter;
import com.slx.funstream.adapters.SmileAdapter;
import com.slx.funstream.chat.ChatService;
import com.slx.funstream.rest.FunstreamApiModule;
import com.slx.funstream.ui.chat.ChatFragment;
import com.slx.funstream.ui.login.LoginFragment;
import com.slx.funstream.ui.streams.ChatListFragment;
import com.slx.funstream.ui.streams.StreamActivity;
import com.slx.funstream.ui.streams.StreamsActivity;
import com.slx.funstream.ui.streams.StreamsContainerFragment;

import dagger.Component;

@PerApp
@Component(
	modules = {
		ApplicationModule.class,
		StorageModule.class,
		NetworkModule.class,
		FunstreamApiModule.class
	}
)
public interface ApplicationComponent {
	Context applicationContext();
	App application();

	void inject(ChatService chatService);
	void inject(ChatAdapter chatAdapter);
	void inject(StreamsContainerFragment streamsFragment);
	void inject(ChatListFragment chatListFragment);
	void inject(StreamsActivity streamsActivity);
	void inject(StreamActivity streamActivity);
	void inject(LoginFragment loginFragment);
	void inject(ChatFragment chatFragment);
	void inject(SmileAdapter smileAdapter);
}
