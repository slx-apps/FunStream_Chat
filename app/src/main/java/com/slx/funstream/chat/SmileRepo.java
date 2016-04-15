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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
		funstreamApi
                .smileyObservable()
                .map(list -> {
                    Map<String, Smile> map = new HashMap<>();
                    for (Smile smile : list) {
                        map.put(smile.getCode(), smile);
                    }
                    return map;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Smile>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "loadSmiles onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: "+e);
                    }

                    @Override
                    public void onNext(Map<String, Smile> smileMap) {
                        Log.d(TAG, "smile loaded");
                        map = smileMap;
                        rxBus.send(new SmileLoadEvent(smileMap));
                    }
                });
		// +
//		smiles = response.body();
//		Log.d(TAG, "Smiles loaded. Size=" + response.body().size());
//		//warmSmileImages(smiles);
//		isSmilesInitialized = true;
//		if (getCallback() != null) getCallback().onSmileLoaded();
		// -
//		Log.e(TAG, "Can't load smiles " + t.toString());
//		// mark init anyway
//		isSmilesInitialized = true;
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

    //	private void warmSmileImages(List<Map<String, Smile>> smiles) {
//		Log.d(TAG, "warmSmileImages");
//		for (int i = 0; i < smiles.size(); i++) {
//			if (i>=2) break;
//			List<Smile> smilesList = Collections.list(Collections.enumeration(smiles.get(i).values()));
//
//			for (Smile smiley : smilesList) {
//				if (!isEmpty(smiley.getUrl())) {
//					picasso
//							.load(smiley.getUrl())
//							.fetch(new com.squareup.picasso.Callback() {
//								@Override
//								public void onSuccess() {
//									isSmilesInitialized = true;
//									if (getCallback() != null) {
//										getCallback().onSmileLoaded();
//									}
//								}
//
//								@Override
//								public void onError() {
//									isSmilesInitialized = true;
//									if (getCallback() != null) {
//										getCallback().onSmileLoaded();
//									}
//								}
//							});
//				}
//			}
//		}
//	}
}
