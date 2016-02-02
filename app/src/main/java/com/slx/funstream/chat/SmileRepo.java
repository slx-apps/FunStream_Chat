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

import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.model.Smile;
import com.slx.funstream.utils.LogUtils;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static android.text.TextUtils.isEmpty;

public class SmileRepo {
	public static final String SMILE_REGEX = "(:[a-z0-9-_]+:)";
	public static Pattern SMILE_PATTERN = Pattern.compile(SMILE_REGEX, Pattern.CASE_INSENSITIVE);

	private Map<String, Smile> smiles;
	private final FSRestClient restClient;
	private final Picasso picasso;
	private boolean isSmilesInitialazed = false;


	WeakReference<OnSmileLoaded> callback;
	public void setSmileRepoListener(OnSmileLoaded callback) {
		this.callback = new WeakReference<>(callback);
	}
	private OnSmileLoaded getCallback() {
		return callback.get();
	}


	@Inject
	public SmileRepo(FSRestClient restClient, Picasso picasso) {
		this.restClient = restClient;
		this.picasso = picasso;
	}

	public void loadSmiles(){
		if(isSmilesInitialazed) return;
		Call<Map<String, Smile>> call = restClient.getApiService().getSmiles();
		call.enqueue(new Callback<Map<String, Smile>>() {
			@Override
			public void onResponse(Response<Map<String, Smile>> response, Retrofit retrofit) {
				smiles = response.body();
				Log.d(LogUtils.TAG, "Smiles loaded. Size=" + response.body().size());
				//warmSmileImages(smiles);
				isSmilesInitialazed = true;
				if(getCallback() != null) getCallback().onSmileLoaded();
			}

			@Override
			public void onFailure(Throwable t) {
				Log.e(LogUtils.TAG, "Can't load smiles " + t.toString());
				// mark init anyway
				isSmilesInitialazed = true;
			}
		});
	}

	public boolean isSmilesInitialazed(){
		return isSmilesInitialazed;
	}

	public Map<String, Smile> getSmiles(){
		return smiles;
	}

	public Smile getSmile(String pattern){
		Smile smile = null;
		if (!isSmilesInitialazed) return null;
		if(smiles.containsKey(pattern)){
			smile = smiles.get(pattern);
		}
		return smile;
	}

	private void warmSmileImages(List<Map<String, Smile>> smiles){
		Log.d(LogUtils.TAG, "warmSmileImages");
		for (int i = 0; i < smiles.size(); i++) {
			if(i>=2) break;
			List<Smile> smilesList = Collections.list(Collections.enumeration(smiles.get(i).values()));

			for(Smile smiley : smilesList){
				if (!isEmpty(smiley.getUrl())) {
					picasso
							.load(smiley.getUrl())
							.fetch(new com.squareup.picasso.Callback() {
								@Override
								public void onSuccess() {
									isSmilesInitialazed = true;
									if(getCallback() != null){
										getCallback().onSmileLoaded();
									}
								}

								@Override
								public void onError() {
									isSmilesInitialazed = true;
									if(getCallback() != null){
										getCallback().onSmileLoaded();
									}
								}
							});
				}
			}
		}
	}

	public interface OnSmileLoaded {
		void onSmileLoaded();
	}
}
