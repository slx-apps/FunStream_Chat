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


public final class APIUtils {

	// Chat responses
	public static final String OK_STATUS = "ok";
	public static final String ERROR_STATUS = "error";

	public static final String CONTENT_STREAM = "stream";
	public static final String CONTENT_SUPPORT = "support";

	// Login response ok status  => 430[{"status":"ok","result":{}}]


	public static final String HEADER_API_VERSION = "Accept: application/json; version=1.0";
	public static final String HEADER_API_TOKEN = "Token";
//	public static final String FUNSTREAM_SMILES = "https://funstream.tv/build/images/smiles/";
	public static final String FUNSTREAM_API_ENDPOINT = "http://funstream.tv";
	public static final String API_CONTENT = "/api/content";

	public static final String API_AUTH_LOGIN = "/api/user/login";
	public static final String API_AUTH_LOGOUT = "/api/user/logout";

	public static final String API_STREAM = "/api/stream";
	public static final String API_CATEGORY = "/api/category";
	public static final String API_SMILE = "/api/smile";
	public static final String API_USER = "/api/user";
	public static final String API_CURRENT_USER = "/api/user/current";


	//OAUTH
	public static final String OAUTH_REQUEST = "/api/oauth/request";
	public static final String OAUTH_EXCHANGE = "/api/oauth/exchange";
	public static final String OAUTH_CHECK = "/api/oauth/check";
	public static final String OAUTH_GRANT = "/api/oauth/grant";
	public static final String OAUTH_BROWSER_LINK = "http://funstream.tv/oauth/";

	public static final String API_IDS_TO_LIST = "/api/user/list";

	//Twitch
	public static final String TWITCH_KRAKEN_API_ENDPOINT = "https://api.twitch.tv/kraken";
	public static final String TWITCH_USHER_API_ENDPOINT = "http://usher.twitch.tv/api";
	public static final String TWITCH_KRAKEN_BASE = "https://api.twitch.tv/kraken";
	public static final String TWITCH_BASE = "https://api.twitch.tv/api/";


	public static final String TWITCH_API_STREAM = "/streams/{channel}";
	public static final String TWITCH_API_ACCESS_TOKEN = "/channels/{channel}/access_token";

//	public static final String TWITCH_USHER_STREAM_PLAYLIST = "/channel/hls/{channel}.m3u8?player=twitchweb&token={token}&sig={sig}&allow_audio_only=true&allow_source=true&type=any&p={random}";.
	public static final String TWITCH_USHER_STREAM_PLAYLIST = "/channel/hls/{channel}.m3u8";

	// Goodgame
	public static final String GOODGAME_HLS_ENDPOINT = "http://hls.goodgame.ru";
	public static final String GOODGAME_STREAM = "/hls/{channel}.smil";

	// Cybergame
	public static final String CYBERGAME_API_ENDPOINT = "http://api.cybergame.tv";
	public static final String CYBERGAME_STREAM = "/p/playlist.smil?channel={channel}";


	private APIUtils(){

	}

}
