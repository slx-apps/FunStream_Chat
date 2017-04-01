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
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.di.DiProvider;
import com.slx.funstream.rest.model.OAuthResponse;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.utils.PrefUtils;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class LoginActivity extends RxAppCompatActivity implements LoginFragment.LoginHandler,
        DiProvider {
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
    @Inject
    UserStore userStore;

	private FragmentManager fm;
	private String oAuthCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
		super.onCreate(savedInstanceState);
		setTheme(R.style.LoginActivityStyle);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);

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

	}

    @Override
    protected void onStart() {
        super.onStart();
        oAuthCode = prefUtils.getUserCode();

        Flowable.interval(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(aLong -> {
                    oAuthCode = prefUtils.getUserCode();
                    Log.d(TAG, "interval->map: " + oAuthCode);
                    OAuthResponse oAuthResponse = new OAuthResponse(oAuthCode);
                    if (oAuthCode != null) {
                        try {
                            OAuthResponse res = api.getToken(new OAuthResponse(oAuthCode)).execute().body();
                            Log.d(TAG, "interval->map " + oAuthResponse);
                            if (res != null) {
                                oAuthResponse = res;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return oAuthResponse;
                })
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<OAuthResponse>() {

                    @Override
                    public void onNext(OAuthResponse oAuthResponse) {
                        Log.d(TAG, "interval->onNext: "  + oAuthResponse);
                        String token = oAuthResponse.getToken();
                        if (oAuthResponse.getToken() != null) {
                            sendResult(oAuthResponse.getUser().getId(), oAuthResponse.getUser().getName(), token);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
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

    @Override
    public FunstreamApi getFunstreamApi() {
        return api;
    }

    @Override
    public UserStore getUserStore() {
        return userStore;
    }

    @Override
    public PrefUtils getPrefUtils() {
        return prefUtils;
    }
}
