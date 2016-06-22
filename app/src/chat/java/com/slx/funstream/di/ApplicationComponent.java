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

import com.google.gson.Gson;
import com.slx.funstream.adapters.ChatAdapter;
import com.slx.funstream.adapters.SmileAdapter;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.ChatService;
import com.slx.funstream.chat.SmileRepo;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.StreamsRepo;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.ui.chat.ChatFragment;
import com.slx.funstream.ui.user.LoginActivity;
import com.slx.funstream.ui.user.LoginFragment;
import com.slx.funstream.ui.user.LoginWebviewFragment;
import com.slx.funstream.ui.streams.ChannelListFragment;
import com.slx.funstream.ui.streams.StreamActivity;
import com.slx.funstream.ui.streams.StreamsActivity;
import com.slx.funstream.ui.streams.StreamsContainerFragment;
import com.slx.funstream.ui.user.UserActivity;
import com.slx.funstream.utils.PrefUtils;
import com.slx.funstream.utils.RxBus;
import com.squareup.picasso.Picasso;

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
    RxBus rxBus();
    PrefUtils prefUtils();
    UserStore userStore();
    Gson gson();
    SmileRepo smileRepo();
    FSRestClient restClient();
	FunstreamApi funstreamApi();
    Picasso picasso();
    Context context();
    StreamsRepo streamsRepo();

	void inject(ChatService chatService);
	void inject(ChatAdapter chatAdapter);
	void inject(StreamsContainerFragment streamsFragment);
	void inject(ChannelListFragment channelListFragment);
	void inject(StreamsActivity streamsActivity);
	void inject(StreamActivity streamActivity);
	void inject(LoginFragment loginFragment);
	void inject(LoginWebviewFragment loginWebviewFragment);
    void inject(LoginActivity loginActivity);
	void inject(ChatFragment chatFragment);
	void inject(SmileAdapter smileAdapter);
	void inject(UserActivity userActivity);

}
