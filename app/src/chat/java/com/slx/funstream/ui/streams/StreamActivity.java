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

package com.slx.funstream.ui.streams;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.di.DiProvider;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.ui.settings.AppSettingsActivity;
import com.slx.funstream.ui.chat.ChatFragment;
import com.slx.funstream.ui.user.LoginActivity;
import com.slx.funstream.ui.user.LoginFragment;
import com.slx.funstream.ui.user.UserActivity;
import com.slx.funstream.utils.PrefUtils;
import com.slx.funstream.utils.Toaster;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasDispatchingSupportFragmentInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subscribers.DisposableSubscriber;

import static android.text.TextUtils.isEmpty;

public class StreamActivity extends RxAppCompatActivity implements DiProvider, HasDispatchingSupportFragmentInjector {
    private static final String TAG = "StreamActivity";

    public static final String STREAMER_NAME = "streamer_name";
    public static final String STREAMER_ID = "streamer_id";
    public static final String STREAM = "stream";

    public static final int RC_LOGIN = 101;
    public static final int DEFAULT_NON_EXISTING_VALUE = -999;

    @BindView(R.id.chat_container)
    FrameLayout chatContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.stream_root)
    LinearLayout streamRoot;

    @Inject
    PrefUtils prefUtils;
    @Inject
    UserStore userStore;
    @Inject
    FunstreamApi api;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    private long streamerId;
    private String streamerName;

    private ChatFragment chatFragment;
    private FragmentManager fm;
    private CurrentUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_layout);

        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            streamerName = savedInstanceState.getString(STREAMER_NAME);
            streamerId = savedInstanceState.getLong(STREAMER_ID, DEFAULT_NON_EXISTING_VALUE);
        } else {
            Intent startIntent = getIntent();
            if (startIntent.hasExtra(STREAMER_ID) && startIntent.hasExtra(STREAMER_NAME)) {
                streamerName = startIntent.getStringExtra(STREAMER_NAME);
                streamerId = startIntent.getLongExtra(STREAMER_ID, DEFAULT_NON_EXISTING_VALUE);
            }
        }

        chatFragment = createChatFragment();

        userStore.userObservable()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<CurrentUser>() {
                    @Override
                    public void onNext(CurrentUser currentUser) {
                        Log.d(TAG, "UserStore->fetchUser->onNext " + currentUser);
                        user = currentUser;
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "UserStore->fetchUser->onComplete");
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stream, menu);

        MenuItem logInItem = menu.findItem(R.id.menu_log_in);
        MenuItem userAccItem = menu.findItem(R.id.menu_user_account);
        // Hide login button if user is not logged in
        if (user == null || isEmpty(user.getToken())) {
            logInItem.setVisible(true);
            userAccItem.setVisible(false);
        } else {
            logInItem.setVisible(false);
            userAccItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_log_in) {
            startActivityForResult(new Intent(this, LoginActivity.class), RC_LOGIN);
            return true;
        } else if (id == R.id.menu_user_account) {
            startActivity(new Intent(this, UserActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, AppSettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (streamerName != null) {
            outState.putString(STREAMER_NAME, streamerName);
        }
        if (streamerId > DEFAULT_NON_EXISTING_VALUE) {
            outState.putLong(STREAMER_ID, streamerId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefUtils.isScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        userStore.fetchUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_LOGIN) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra(LoginFragment.FIELD_TOKEN)
                        && data.hasExtra(LoginFragment.FIELD_USERID)) {
                    CurrentUser user = new CurrentUser();
                    Long userId = data.getLongExtra(LoginFragment.FIELD_USERID, DEFAULT_NON_EXISTING_VALUE);
                    // Check if something went wrong
                    if (userId == DEFAULT_NON_EXISTING_VALUE) {
                        Toaster.makeLongToast(this, getString(R.string.error_login));
                        return;
                    }
                    user.setId(userId);
                    user.setName(data.getStringExtra(LoginFragment.FIELD_USERNAME));
                    user.setToken(data.getStringExtra(LoginFragment.FIELD_TOKEN));
                    prefUtils.saveUser(user);
                    userStore.fetchUser();
                    invalidateOptionsMenu();
                }
            }
        }
    }
    @CheckResult
    private ChatFragment createChatFragment() {
        chatFragment = ChatFragment.newInstance(streamerId, streamerName);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.chat_container, chatFragment);
        ft.commit();
        return chatFragment;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = fm.findFragmentById(R.id.chat_container);

        if (fragment instanceof ChatFragment) {
            ((ChatFragment) fragment).showSmileKeyboard();
        }
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

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }
}
