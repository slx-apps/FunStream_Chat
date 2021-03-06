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
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.slx.funstream.model.SimpleToken;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.utils.PrefUtils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static android.text.TextUtils.isEmpty;

public class UserStore {
	private static final String TAG = "UserStore";
	private FunstreamApi funstreamApi;
	private Gson mGson;
	private PrefUtils prefUtils;
    private CurrentUser currentUser;
	private BehaviorRelay<CurrentUser> userRelay;

	private boolean userTokenValid = false;

	public UserStore(FunstreamApi funstreamApi, Gson gson, PrefUtils prefUtils) {
		this.mGson = gson;
		this.prefUtils = prefUtils;
        this.funstreamApi = funstreamApi;

		userRelay = BehaviorRelay.create();

		userRelay.doOnNext(user -> {
            Log.d(TAG, "UserStore: new user" + user);
            currentUser = user;
        });
	}

	public void fetchUser() {
//        user.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<CurrentUser>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "UserStore->fetchUser->onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(TAG, "UserStore->fetchUser->onError " + e);
//                    }
//
//                    @Override
//                    public void onNext(CurrentUser user) {
//                        Log.d(TAG, "UserStore->fetchUser->onNext");
//                        currentUser = user;
//                    }
//        });
//		return userRelay.toFlowable(BackpressureStrategy.LATEST);

		Single.just(prefUtils.getUser())
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
                //.toObservable();
				.subscribeWith(new DisposableSingleObserver<CurrentUser>() {
					@Override
					public void onSuccess(CurrentUser currentUser) {
                        userRelay.accept(currentUser);
					}

					@Override
					public void onError(Throwable e) {
                        e.printStackTrace();
					}
				});
	}

//    public void updateUser() {
//		userRelay.accept(prefUtils.getUser());
//    }

	/**
	 * Check if user's token has not been expired
	 * @return True if user token is valid
	 */
	public static boolean isUserLoggedIn(CurrentUser user) {
		if (user == null || isEmpty(user.getToken())) return false;
        return true;
		//SimpleToken token = parseToken(user.getToken());
		//return isTokenExpire(token);
	}

    public static SimpleToken parseToken(String token, Gson gson) {
		try {
			SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
			Payload payload = signedJWT.getPayload();
            SimpleToken simpleToken = gson.fromJson(payload.toString(), SimpleToken.class);
            Log.d(TAG, "parseToken: " + simpleToken);
            return simpleToken;
		} catch (java.text.ParseException e) {
			Log.e(TAG, e.toString());
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
	private static boolean isTokenExpire(SimpleToken token) {
		if (token == null) {
			Log.e(TAG, "User token is null");
			return true;
		}

		return token.getExp() < System.currentTimeMillis();
	}

//	@DebugLog
//	public boolean isUserTokenValid() {
//		return userTokenValid;
//	}


	public CurrentUser getCurrentUser() {
		return currentUser;
	}

	public Flowable<CurrentUser> userObservable() {
		return userRelay.toFlowable(BackpressureStrategy.LATEST);
	}
}
