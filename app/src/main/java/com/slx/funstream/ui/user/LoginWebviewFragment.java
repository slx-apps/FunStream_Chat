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


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.slx.funstream.App;
import com.slx.funstream.R;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.ui.user.LoginFragment.LoginHandler;
import com.trello.rxlifecycle.components.support.RxFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginWebviewFragment extends RxFragment {
	private static final String TAG = "LoginWebviewFragment";
	private static final String KEY_URI = "login.uri";


	@BindView(R.id.fragment_login_progress_bar)
	ProgressBar mProgressBar;
	@BindView(R.id.fragment_login_web_view)
	WebView mWebView;

    @Inject
    FunstreamApi api;

	private Uri mUri;
	private LoginHandler callback;
	private Unbinder unbinder;

	public static LoginWebviewFragment newInstance(Uri uri) {

		Bundle args = new Bundle();
		args.putParcelable(KEY_URI, uri);
		LoginWebviewFragment fragment = new LoginWebviewFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		callback = (LoginHandler) context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUri = getArguments().getParcelable(KEY_URI);
        App.applicationComponent().inject(this);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_login_webview, container, false);
        unbinder = ButterKnife.bind(this, v);
		return v;
	}

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        rx.Observable
//                .interval(5000, TimeUnit.SECONDS)
//                .compose(bindToLifecycle())
//                .subscribe(new Subscriber<Long>() {
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
//                    public void onNext(Long aLong) {
//
//                    }
//                });
//
//        api.getTokenObs(new OAuthResponse(oAuthCode))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .compose(bindToLifecycle())
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
//    }

    @SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
//		settings.setBuiltInZoomControls(true);
//		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
//		settings.setDatabaseEnabled(true);
		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView webView, int newProgress) {
				if (newProgress == 100) {
					if(mProgressBar != null) {
						mProgressBar.setVisibility(View.GONE);
					}
				} else {
					if(mProgressBar != null){
						mProgressBar.setVisibility(View.VISIBLE);
						mProgressBar.setProgress(newProgress);
					}
				}
			}

			public void onReceivedTitle(WebView webView, String title) {
//				AppCompatActivity activity = (AppCompatActivity) getActivity();
//				activity.getSupportActionBar().setSubtitle(title);
			}

		});
		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

		});

		mWebView.loadUrl(mUri.toString());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();
	}

	@OnClick(R.id.btNext)
	void onCredsEntered(){
		if (callback != null) callback.onCredsEntered();
	}
}
