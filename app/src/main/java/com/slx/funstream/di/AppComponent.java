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

import com.slx.funstream.App;
import com.slx.funstream.di.modules.AppModule;
import com.slx.funstream.di.modules.BuildersModule;
import com.slx.funstream.di.modules.ChatModule;
import com.slx.funstream.di.modules.FunstreamApiModule;
import com.slx.funstream.di.modules.NetworkModule;
import com.slx.funstream.di.modules.StorageModule;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@PerApp
@Component(
	modules = {
		AndroidSupportInjectionModule.class,
		AppModule.class,
        BuildersModule.class,
		StorageModule.class,
		NetworkModule.class,
        ChatModule.class,
		FunstreamApiModule.class,
	}
)
public interface AppComponent {
//    RxBus rxBus();
//    PrefUtils prefUtils();
//    UserStore userStore();
//    Gson gson();
//    SmileRepo smileRepo();
//    FSRestClient restClient();
//	FunstreamApi funstreamApi();
//    Picasso picasso();
//    Context context();
//    StreamsRepo streamsRepo();
//	ChatServiceController chatServiceController();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(App application);
        Builder appModule(AppModule appModule);
        AppComponent build();
    }
    void inject(App app);

//	void inject(ChatService chatService);
//	void inject(StreamsContainerFragment streamsFragment);
//	void inject(ChannelListFragment channelListFragment);
//	void inject(StreamsActivity streamsActivity);
//	void inject(StreamActivity streamActivity);
//    void inject(LoginActivity loginActivity);
//	void inject(ChatFragment chatFragment);
//	void inject(UserActivity userActivity);
}
