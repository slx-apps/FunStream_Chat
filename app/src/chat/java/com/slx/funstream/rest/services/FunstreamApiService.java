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

package com.slx.funstream.rest.services;


import com.slx.funstream.model.ChatUser;
import com.slx.funstream.model.Stream;
import com.slx.funstream.rest.APIUtils;
import com.slx.funstream.rest.model.AuthRequest;
import com.slx.funstream.rest.model.AuthResponse;
import com.slx.funstream.rest.model.ContentRequest;
import com.slx.funstream.rest.model.OAuthRequest;
import com.slx.funstream.rest.model.OAuthResponse;
import com.slx.funstream.rest.model.Smile;

import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;

public interface FunstreamApiService {

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_CONTENT)
	Call<List<Stream>> getContent(@Body ContentRequest contentRequest);
	//{"content":"stream","type":"all","category":{"slug":"top"}}

	//
//	@Headers(APIUtils.HEADER_API_VERSION)
//	@POST(APIUtils.API_CONTENT)
//	Observable<List<Stream>> getContent(@Body ContentRequest contentRequest);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_AUTH_LOGIN)
	Call<AuthResponse> login(@Body AuthRequest authRequest);

//	@Headers(APIUtils.HEADER_API_VERSION)
//	@GET(APIUtils.API_SMILE)
//	Observable<List<Map<String, Smile>>> getSmiles();

	@Headers(APIUtils.HEADER_API_VERSION)
	@GET(APIUtils.API_SMILE)
	Call<List<Map<String, Smile>>> getSmiles();

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_USER)
	Call<ChatUser> getUser(@Body ChatUser user);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.OAUTH_REQUEST)
	Call<OAuthResponse> getPermissionCode(@Body OAuthRequest oAuthRequest);

//	@Headers(APIUtils.HEADER_API_VERSION)
//	@POST(APIUtils.OAUTH_REQUEST)
//	Observable<OAuthResponse> getPermissionCode(@Body OAuthRequest oAuthRequest);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.OAUTH_EXCHANGE)
	Call<OAuthResponse> getToken(@Body OAuthResponse response);
}
