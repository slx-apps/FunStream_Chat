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

package com.slx.funstream.ui.user;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.slx.funstream.App;
import com.slx.funstream.R;
import com.slx.funstream.rest.model.OAuthResponse;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.utils.PrefUtils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends RxAppCompatActivity implements LoginFragment.LoginHandler {
	private static final String TAG = "LoginActivity";

    public static final String FIELD_USERID = "id";
    public static final String FIELD_USERNAME = "name";
    public static final String FIELD_TOKEN = "token";

	@BindView(R.id.toolbar)
	Toolbar toolbar;

    @Inject
    FunstreamApi api;
    @Inject
    PrefUtils prefUtils;

	private FragmentManager fm;
	private String oAuthCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.LoginActivityStyle);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);
        App.applicationComponent().inject(this);

		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(R.string.log_in);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}

		toolbar.setNavigationOnClickListener(view -> onBackPressed());


		fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.container);

		if (fragment == null) {
			fragment = LoginFragment.newInstance();
			fm.beginTransaction()
					.add(R.id.container, fragment)
					.commit();
		}

        //showMessage();
	}

    @Override
    protected void onStart() {
        super.onStart();
        oAuthCode = prefUtils.getUserCode();

//        Observable<OAuthResponse> tokenObservable = api.getTokenObs(new OAuthResponse(oAuthCode))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .compose(bindToLifecycle());
//                .subscribe(new Subscriber<OAuthResponse>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(OAuthResponse oAuthResponse) {
//
//                    }
//                });

        Observable
                .interval(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(aLong -> {
                    oAuthCode = prefUtils.getUserCode();
                    Log.d(TAG, "interval->map: " + oAuthCode);
                    if (oAuthCode != null) {
                        try {
                            OAuthResponse oAuthResponse = api.getToken(new OAuthResponse(oAuthCode)).execute().body();
                            Log.d(TAG, "interval->map " + oAuthResponse);
                            return oAuthResponse;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                    }
                )
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<OAuthResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(OAuthResponse oAuthResponse) {
                        Log.d(TAG, "interval->onNext: "  + oAuthResponse);
                        if (oAuthResponse != null) {
                            String token = oAuthResponse.getToken();
                            sendResult(oAuthResponse.getUser().getId(), oAuthResponse.getUser().getName(), token);
                        }
                    }
                });

    }

    @Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_CANCELED);
		finish();
	}

	private void showWebViewFragment(Uri uri) {
		fm.beginTransaction()
				.replace(R.id.container, LoginWebviewFragment.newInstance(uri))
				.commit();
	}

	@Override
	public void onFunstreamLoginClicked(Uri uri) {
		showWebViewFragment(uri);
	}

	@Override
	public void onCredsEntered() {
		fm.beginTransaction()
				.replace(R.id.container, LoginFragment.newInstance())
				.commit();
	}

    private void sendResult(long userId, String username, String token) {
        Intent intent = new Intent();
        intent.putExtra(FIELD_USERID, userId);
        intent.putExtra(FIELD_USERNAME, username);
        intent.putExtra(FIELD_TOKEN, token);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void showMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle(R.string.auth_dialog_help_title);
        builder.setMessage(R.string.auth_dialog_help_desc);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
