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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jakewharton.rxbinding2.view.RxView;
import com.slx.funstream.App;
import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.model.SimpleToken;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.utils.PrefUtils;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class UserActivity extends RxAppCompatActivity {
    private static final String TAG = "UserActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvUserName)
    TextView tvUserName;
    @BindView(R.id.tvExpire)
    TextView tvExpire;

    @BindView(R.id.btLogOut)
    Button btLogOut;

    @Inject
    UserStore userStore;
    @Inject
    PrefUtils prefUtils;
    @Inject
    Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        RxView.clicks(btLogOut)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    prefUtils.clearUser();
                    prefUtils.clearUserCode();
                    finish();
                });

        userStore.userObservable()
                .subscribeOn(Schedulers.io())
//                .map(user -> {
//                    api.userObservable(new ChatUser(user.getId()));
//                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(new DisposableSubscriber<CurrentUser>() {
                    @Override
                    public void onNext(CurrentUser currentUser) {
                        Log.d(TAG, "UserStore->fetchUser->onNext " + currentUser);
                        tvUserName.setText(currentUser.getName());
                        SimpleToken simpleToken = UserStore.parseToken(currentUser.getToken(), gson);
                        if (simpleToken != null) {
                            Locale current = getResources().getConfiguration().locale;
                            SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy", current);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(simpleToken.getExp() * 1000);
                            tvExpire.setText(formatter.format(calendar.getTime()));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
