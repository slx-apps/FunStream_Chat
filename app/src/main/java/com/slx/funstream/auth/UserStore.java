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

package com.slx.funstream.auth;


import android.util.Log;

import com.google.gson.Gson;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.slx.funstream.model.SimpleToken;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.utils.LogUtils;
import com.slx.funstream.utils.PrefUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

public class UserStore {
	private FSRestClient restClient;
	private Gson mGson;
	private PrefUtils prefUtils;
    private CurrentUser currentUser;

	private boolean userTokenValid = false;

	private List<UserStoreListener> listeners = new CopyOnWriteArrayList<>();

	public UserStore(FSRestClient fsRestClient, Gson gson, PrefUtils prefUtils) {
		this.mGson = gson;
		this.prefUtils = prefUtils;
        this.restClient = fsRestClient;
		fetchUser();
	}

	public void registerUserStoreListener(UserStoreListener listener){
		if(!this.listeners.contains(listener)){
			this.listeners.add(listener);
		}
	}

	public boolean unregisterUserStoreListener(UserStoreListener listener){
		return this.listeners.remove(listener);
	}


	/**
	 * Retrieve {@link com.slx.funstream.rest.model.CurrentUser} from Shared Preferences
	 *
	 */
	@DebugLog
	public void fetchUser(){
		currentUser = prefUtils.getUser();
		isUserLoggedIn();
		notifyListenersUserChanged();
	}

	public CurrentUser getCurrentUser() {
		return currentUser;
	}


	/**
	 * Check if user's token has not been expired
	 * @return True if user token is valid
	 */
	@DebugLog
	public boolean isUserLoggedIn(){
		if(currentUser == null || isEmpty(currentUser.getToken())) return false;
		SimpleToken token = parseToken(currentUser.getToken());
		return isTokenExpire(token);
	}

	@DebugLog
	public SimpleToken parseToken(String token){
		try {
			SignedJWT signedJWT = (SignedJWT)JWTParser.parse(token);
			Payload payload = signedJWT.getPayload();
			return mGson.fromJson(payload.toString(), SimpleToken.class);
		} catch (java.text.ParseException e) {
			Log.e(LogUtils.TAG, e.toString());
		}
		return null;
	}

//	private boolean checkIpMatch(SimpleToken token){
//		if(token == null) return true;
//		return token.getIp().equals(NetworkUtils.getIPAddress(true));
//	}

	/**
	 * Check user token expiration
	 * @param token JWT Token
	 * @return False if token expired
	 */
	@DebugLog
	private boolean isTokenExpire(SimpleToken token){
		if(token == null) {
			Log.e(LogUtils.TAG, "User token is null");
			return true;
		}

		return token.getExp() < System.currentTimeMillis();
	}

	@DebugLog
	public boolean isUserTokenValid() {
		return userTokenValid;
	}

	private void notifyListenersUserChanged(){
		for(UserStoreListener listener : listeners){
			listener.OnUserChanged();
		}
	}

	public interface UserStoreListener {
		void OnUserChanged();
	}

    public Observable<CurrentUser> loadCurrentUser() {
        return restClient.getApiService().getCurrentUser(getCurrentUser().getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
                //.compose(bindToLifecycle())
    }

}
