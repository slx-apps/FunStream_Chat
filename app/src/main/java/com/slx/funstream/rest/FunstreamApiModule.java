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

package com.slx.funstream.rest;

import com.google.gson.Gson;
import com.slx.funstream.chat.SmileRepo;
import com.slx.funstream.dagger.PerApp;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

import dagger.Module;
import dagger.Provides;
import retrofit.Converter;
import retrofit.GsonConverterFactory;

@Module
public class FunstreamApiModule {
	@Provides
	@PerApp
	SmileRepo provideSmileRepo(FSRestClient restclient, Picasso picasso) {
		return new SmileRepo(restclient, picasso);
	}

	@Provides
	@PerApp
	Converter.Factory provideConverter(Gson gson) {
		return GsonConverterFactory.create(gson);
	}

//	@Provides
//	@PerApp
//	CallAdapter.Factory provideCallAdapterFactory() {
//		return RxJavaCallAdapterFactory.create();
//	}

//  1.9
//	@Provides
//	@PerApp
//	RestAdapter provideRestAdapter(Endpoint endpoint, Converter converter, Client client){
//		return new RestAdapter.Builder()
//		 .setEndpoint(endpoint)
//		 .setConverter(converter)
//		 .setClient(client)
//		 .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
//		 .build();
//	}

	@Provides
	@PerApp
	FSRestClient provideFunstreamApi(OkHttpClient client,
	                                 Converter.Factory convFactory
	                                 //CallAdapter.Factory callAdapterFactory
	) {
		return new FSRestClient(client,
				convFactory,
				//callAdapterFactory,
				APIUtils.FUNSTREAM_API_ENDPOINT);
	}

}
