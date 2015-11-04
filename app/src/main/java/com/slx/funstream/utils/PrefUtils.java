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

package com.slx.funstream.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.slx.funstream.rest.model.CurrentUser;

public class PrefUtils {
	public static final String PREFS_FILE = "user.sp";
	private static final String PREFS_USERID = "prefs_userid";
	private static final String PREFS_USERNAME = "prefs_username";
//	private static final String PREFS_LOGIN = "prefs_login";
//	private static final String PREFS_PASSWORD = "prefs_password";
	private static final String PREFS_TOKEN = "prefs_token";

	public static final String PREFS_AWAKE = "chat.awake";
	public static final String PREFS_SCROLL = "chat.scroll";
	public static final String PREFS_FIRST = "first_time";

	public static final String PREFS_SHOW_CHAT = "chat.show";
	public static final String PREFS_SHOW_STREAM = "stream.show";
	public static final String PREFS_SHOW_IMAGES = "chat.images";

	public static final String PREFS_OAUTH_CODE = "user.code";

	private final SharedPreferences prefs;


	public PrefUtils(Context context) {
		prefs = context.getApplicationContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
	}


	public boolean isLoggedIn() {
		return getUser() != null;
	}

	public void saveUser(CurrentUser user) {

		prefs.edit()
			.putLong(PREFS_USERID, user.getId())
			.putString(PREFS_USERNAME, user.getName())
	//		.putString(PREFS_LOGIN, user.getLogin())
	//		.putString(PREFS_PASSWORD, user.getPassword())
			.putString(PREFS_TOKEN, user.getToken())
			.apply();
	}

	public CurrentUser getUser() {
		if (prefs.contains(PREFS_TOKEN)) {
			CurrentUser user = new CurrentUser();
			user.setId(prefs.getLong(PREFS_USERID, -999));
//			user.setLogin(prefs.getString(PREFS_LOGIN, null));
//			user.setPassword(prefs.getString(PREFS_PASSWORD, null));
			user.setName(prefs.getString(PREFS_USERNAME, null));
			user.setToken(prefs.getString(PREFS_TOKEN, null));
			return user;
		}
		return null;
	}

	public boolean isScreenOn(){
		return prefs.getBoolean(PREFS_AWAKE, true);
	}

	public boolean isScroll(){
		return prefs.getBoolean(PREFS_SCROLL, true);
	}

	public boolean isFirst(){
		return prefs.getBoolean(PREFS_FIRST, true);
	}

	public void setFirst(){
		prefs.edit()
				.putBoolean(PREFS_FIRST, false)
				.apply();
	}

	public boolean isShowStream(){
		return prefs.getBoolean(PREFS_SHOW_STREAM, true);
	}

	public boolean isShowChat(){
		return prefs.getBoolean(PREFS_SHOW_CHAT, true);
	}

	public String getUserCode(){
		return prefs.getString(PREFS_OAUTH_CODE, null);

	}

	public void setUserCode(String code){
		prefs.edit()
				.putString(PREFS_OAUTH_CODE, code)
				.apply();
	}
	public void clearUserCode() {
		prefs.edit()
				.remove(PREFS_OAUTH_CODE)
				.apply();
	}

	public void clearUser() {
		prefs.edit()
		.remove(PREFS_USERNAME)
		.remove(PREFS_USERID)
//		.remove(PREFS_LOGIN)
//		.remove(PREFS_PASSWORD)
		.remove(PREFS_TOKEN)
		.apply();
	}

	public SharedPreferences getPrefs(){
		return prefs;
	}

	public boolean isShowImages() {
		return prefs.getBoolean(PREFS_SHOW_IMAGES, false);
	}
}
