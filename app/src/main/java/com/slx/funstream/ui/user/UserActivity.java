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
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jakewharton.rxbinding.view.RxView;
import com.slx.funstream.App;
import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.model.SimpleToken;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.utils.PrefUtils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserActivity extends RxAppCompatActivity {
    private static final String TAG = "UserActivity";

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        App.applicationComponent().inject(this);
        ButterKnife.bind(this);


        RxView.clicks(btLogOut)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    prefUtils.clearUser();
                    prefUtils.clearUserCode();
                    finish();
                });

        userStore
                .fetchUser()
                .subscribeOn(Schedulers.io())
//                .map(user -> {
//                    api.getUser(new ChatUser(user.getId()));
//                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(new Subscriber<CurrentUser>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "UserStore->fetchUser->onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "UserStore->fetchUser->onError");
                        e.printStackTrace();

                    }

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
                });

    }


}
