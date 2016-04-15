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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.model.ChatResponse;
import com.slx.funstream.model.Stream;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.rest.model.Smile;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.utils.ChatResponseDeserializer;
import com.slx.funstream.utils.FunstreamDeserializer;
import com.slx.funstream.utils.PrefUtils;
import com.slx.funstream.utils.SmileDeserializer;
import com.slx.funstream.utils.UserSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import dagger.Module;
import dagger.Provides;

@Module
public class StorageModule {

	@Provides
	@PerApp
	public PrefUtils prefUtils(Context applicationContext){
		return new PrefUtils(applicationContext.getApplicationContext());
	}

	@Provides
	@PerApp
	public UserStore provideUserStore(FunstreamApi funstreamApi, Gson gson, PrefUtils prefUtils){
		return new UserStore(funstreamApi, gson, prefUtils);
	}

	@Provides
	@PerApp
	Gson provideGson() {
		Type listStreamType = new TypeToken<List<Stream>>() {}.getType();
		Type smilesType = new TypeToken<List<Smile>>() {}.getType();
		return new GsonBuilder()
			//.serializeNulls()
			.registerTypeAdapter(listStreamType, new FunstreamDeserializer<List<Stream>>())
			.registerTypeAdapter(smilesType, new SmileDeserializer())
			.registerTypeAdapter(ChatResponse.class, new ChatResponseDeserializer())
			.registerTypeAdapter(CurrentUser.class, new UserSerializer())
			.create();
	}

}
