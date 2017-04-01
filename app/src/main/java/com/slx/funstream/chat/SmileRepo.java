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

package com.slx.funstream.chat;


import android.util.Log;

import com.slx.funstream.chat.events.SmileLoadEvent;
import com.slx.funstream.rest.model.Smile;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.utils.RxBus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class SmileRepo {
    private static final String TAG = "SmileRepo";
    public static final String SMILE_REGEX = "(:[a-z0-9-_]+:)";
	public static Pattern SMILE_PATTERN = Pattern.compile(SMILE_REGEX, Pattern.CASE_INSENSITIVE);

    private Map<String, Smile> map;
	private FunstreamApi funstreamApi;
	private RxBus rxBus;


	public SmileRepo(FunstreamApi funstreamApi, RxBus rxBus) {
		this.funstreamApi = funstreamApi;
		this.rxBus = rxBus;
	}

	public void loadSmiles() {
		funstreamApi.smileyObservable()
                .map(list -> {
                    Map<String, Smile> map = new HashMap<>();
                    for (Smile smile : list) {
                        map.put(smile.getCode(), smile);
                    }
                    return map;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Map<String, Smile>>() {
                    @Override
                    public void onSuccess(Map<String, Smile> smileMap) {
                        Log.d(TAG, "smile loaded");
                        map = smileMap;
                        rxBus.send(new SmileLoadEvent(smileMap));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
	}

	public Smile getSmile(String pattern) {
        if (map != null) {
            return map.get(pattern);
        }
        return null;
	}

    public Map<String, Smile> getSmiles() {
        return map;
    }
}
