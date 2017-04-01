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
import com.slx.funstream.rest.model.Stream;
import com.slx.funstream.rest.APIUtils;
import com.slx.funstream.rest.model.AuthRequest;
import com.slx.funstream.rest.model.AuthResponse;
import com.slx.funstream.rest.model.Category;
import com.slx.funstream.rest.model.CategoryRequest;
import com.slx.funstream.rest.model.ChatListRequest;
import com.slx.funstream.rest.model.ContentRequest;
import com.slx.funstream.rest.model.ContentResponse;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.rest.model.OAuthRequest;
import com.slx.funstream.rest.model.OAuthResponse;
import com.slx.funstream.rest.model.Smile;
import com.slx.funstream.rest.model.StreamRequest;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FunstreamApi {

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_CONTENT)
	Call<ContentResponse> getContent(@Body ContentRequest contentRequest);
	//{"content":"stream","type":"all","category":{"slug":"top"}}

	// Categories
	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_CATEGORY)
	Single<Category> getCategoriesWithSubs(@Body CategoryRequest categoryRequest);

	//
	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_CONTENT)
	Single<List<Stream>> getStreams(@Body ContentRequest contentRequest);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_AUTH_LOGIN)
	Call<AuthResponse> login(@Body AuthRequest authRequest);

//	@Headers(APIUtils.HEADER_API_VERSION)
//	@GET(APIUtils.API_SMILE)
//	Observable<List<Map<String, Smile>>> getSmiles();

	@Headers(APIUtils.HEADER_API_VERSION)
	@GET(APIUtils.API_SMILE)
	Call<Map<String, Smile>> getSmiles();

	@Headers(APIUtils.HEADER_API_VERSION)
	@GET(APIUtils.API_SMILE)
	Single<List<Smile>> smileyObservable();

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_USER)
	Call<ChatUser> getUser(@Body ChatUser user);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.OAUTH_REQUEST)
	Call<OAuthResponse> getPermissionCode(@Body OAuthRequest oAuthRequest);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.OAUTH_REQUEST)
	Single<OAuthResponse> getPermissionCodeObs(@Body OAuthRequest oAuthRequest);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.OAUTH_EXCHANGE)
	Call<OAuthResponse> getToken(@Body OAuthResponse response);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.OAUTH_EXCHANGE)
	Single<OAuthResponse> getTokenObs(@Body OAuthResponse response);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_IDS_TO_LIST)
	Single<ChatUser[]> getChatUsers(@Body ChatListRequest chatListRequest);

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_CURRENT_USER)
	Single<CurrentUser> getCurrentUser(@Header(APIUtils.HEADER_API_TOKEN) String token);


	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_STREAM)
	void getStream(@Body StreamRequest streamRequest, Callback<Stream> callback);
	//{streamer: "Clever.Alex007", options: {players: true}}

	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_STREAM)
	Stream getStream(@Body StreamRequest streamRequest);
	//{streamer: "Clever.Alex007", options: {players: true}}


	@Headers(APIUtils.HEADER_API_VERSION)
	@POST(APIUtils.API_STREAM)
	Single<Stream> streamObservable(@Body StreamRequest streamRequest);
}
