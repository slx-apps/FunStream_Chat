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


import com.slx.funstream.rest.services.FunstreamApi;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;


public class FSRestClient {
	private FunstreamApi apiService;


	public FSRestClient(OkHttpClient client,
	                    Converter.Factory convFactory,
	                    CallAdapter.Factory callAdapterFactory,
	                    String base){
		// 2.0
		Retrofit retrofit = new Retrofit.Builder()
				//.addConverterFactory(MoshiConverterFactory.create())
				.addConverterFactory(convFactory)
				.addCallAdapterFactory(callAdapterFactory)
				.client(client)
				.baseUrl(base)
				.build();
		// 1.9
		//RestAdapter restAdapter = new Retrofit.Builder()
//				.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
//				.setEndpoint(twitchEndpoint)
//				.build();

		apiService = retrofit.create(FunstreamApi.class);
	}

	public FunstreamApi getApiService(){
		return apiService;
	}
}
