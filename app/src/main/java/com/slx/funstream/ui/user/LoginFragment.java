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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.slx.funstream.App;
import com.slx.funstream.BuildConfig;
import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.di.DiProvider;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.model.AuthRequest;
import com.slx.funstream.rest.model.AuthResponse;
import com.slx.funstream.rest.model.OAuthRequest;
import com.slx.funstream.rest.model.OAuthResponse;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.utils.LogUtils;
import com.slx.funstream.utils.NetworkUtils;
import com.slx.funstream.utils.PrefUtils;
import com.trello.rxlifecycle2.components.support.RxFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;
import static com.slx.funstream.rest.APIUtils.OAUTH_BROWSER_LINK;


public class LoginFragment extends RxFragment {
    private static final String TAG = "LoginFragment";

    public static final String FIELD_USERID = "id";
	public static final String FIELD_USERNAME = "name";
	public static final String FIELD_TOKEN = "token";

	String login;
	String password;
	String token;

	private String oAuthCode;
	private ProgressDialog progressDialog;


	@BindView(R.id.username_input)
	EditText usernameInput;
	@BindView(R.id.password_input)
	EditText passwordInput;
	@BindView(R.id.bt_log_in)
	Button btLogIn;
	@BindView(R.id.username_text_input_layout)
	TextInputLayout usernameTextInputLayout;
	@BindView(R.id.password_text_input_layout)
	TextInputLayout passwordTextInputLayout;
	@BindView(R.id.login_root)
	RelativeLayout loginRoot;
	@BindView(R.id.btOAuthLogin)
	Button btOAuthLogin;

    private FunstreamApi api;
    private PrefUtils prefUtils;
	private LoginHandler callback;
	private Unbinder unbinder;

	public static LoginFragment newInstance() {

		Bundle args = new Bundle();

		LoginFragment fragment = new LoginFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public LoginFragment() {
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		callback = (LoginHandler) context;

		api = ((DiProvider) context).getFunstreamApi();
		prefUtils = ((DiProvider) context).getPrefUtils();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_login, container, false);
		unbinder = ButterKnife.bind(this, v);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Ensure the size of the layout doesn’t alter when the error message is shown
		usernameTextInputLayout.setErrorEnabled(true);
		passwordTextInputLayout.setErrorEnabled(true);

		passwordInput.setOnEditorActionListener((v, actionId, event) -> {
			boolean handled = false;
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				login();
				handled = true;
			}
			return handled;
		});

        btOAuthLogin.setEnabled(false);

//		loginClick
//				.subscribe()


		//loginObs = PublishSubject.create();


//		RxView
//				.clicks(btOAuthLogin)
//				.subscribe(loginClick);
	}

	@Override
	public void onStart() {
		super.onStart();
//		// Retrieve code from shared preferences
//		oAuthCode = prefUtils.getUserCode();
//		// Only if code exists
//		if(oAuthCode != null){
//			// Disable button
//			btOAuthLogin.setEnabled(false);
//			//showProgressDialog();
//			getToken();
//		}

        // Check if connection is present and working
        if (!NetworkUtils.isNetworkConnectionPresent(getContext())) {
            Snackbar.make(loginRoot, getString(R.string.error_no_connection), Snackbar.LENGTH_LONG)
                    .setAction(R.string.error_loading_action, view -> {
                        getToken();
                    })
                    .show();
            return;
        }

        api.getPermissionCodeObs(new OAuthRequest(BuildConfig.APP_KEY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(new DisposableSingleObserver<OAuthResponse>() {
					@Override
					public void onSuccess(OAuthResponse oAuthResponse) {
						Log.d(TAG, "permissionCode->Next: " + oAuthResponse);

						if (!isEmpty(oAuthResponse.getCode())) {
							btOAuthLogin.setEnabled(true);
							// Retrieve code from response
							oAuthCode = oAuthResponse.getCode();
							// Save code to SP
							prefUtils.setUserCode(oAuthCode);
						} else {
							showErrorLoginMessage();
						}
					}

					@Override
					public void onError(Throwable e) {
						e.printStackTrace();
						showErrorLoginMessage();
					}
				});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unbinder.unbind();
	}

	private void showErrorLoginMessage(){
		Snackbar.make(loginRoot, getString(R.string.error_login), Snackbar.LENGTH_LONG)
				.show();
		prefUtils.clearUserCode();
	}

	private void showLoginError() {
		Snackbar.make(loginRoot, getString(R.string.log_in_error), Snackbar.LENGTH_LONG)
				.show();
	}

	/**
	 * OAUTH login method
	 */
	@OnClick(R.id.btOAuthLogin)
	void loginOAuth() {
        Log.d(TAG, "loginOAuth: " + oAuthCode);
        // Start browser with code
        startBrowser(getActivity(), Uri.parse(OAUTH_BROWSER_LINK + oAuthCode));

//		// Check if connection is present and working
//		if(!NetworkUtils.isNetworkConnectionPresent(this)){
//			Snackbar.make(loginRoot, getString(R.string.error_no_connection), Snackbar.LENGTH_LONG)
//					.setAction(R.string.error_loading_action, view -> {
//						getToken();
//					})
//					.show();
//			return;
//		}
//
//		Call<OAuthResponse> call = restClient.getApiService().getPermissionCode(new OAuthRequest(UserStore.APP_KEY));
//		call.enqueue(new Callback<OAuthResponse>() {
//			@Override
//			public void onResponse(Response<OAuthResponse> response, Retrofit retrofit) {
//				OAuthResponse oAuthResponse = response.body();
//				if (!isEmpty(oAuthResponse.getCode())) {
//					// Retrieve code from response
//					oAuthCode = oAuthResponse.getCode();
//					// Save code to SP
//					prefUtils.setUserCode(oAuthCode);
//					// Start browser with code
//					startBrowser(context, Uri.parse(OAUTH_BROWSER_LINK + oAuthCode));
//				}
//			}
//
//			@Override
//			public void onFailure(Throwable t) {
//				Snackbar.make(loginRoot, getString(R.string.error_login), Snackbar.LENGTH_LONG)
//						.show();
//				prefUtils.clearUserCode();
////				btOAuthLogin.setEnabled(false);
////				pbLogin.setVisibility(View.VISIBLE);
//			}
//		});
	}

	@OnClick(R.id.bt_log_in)
	void login() {

		// Reset errors
		usernameTextInputLayout.setError(null);
		passwordTextInputLayout.setError(null);

		// Get fields
		login = usernameInput.getText().toString().trim();
		password = passwordInput.getText().toString().trim();

		// Check
		if (TextUtils.isEmpty(login)) {
			usernameTextInputLayout.setError(getString(R.string.error_username_required));
			usernameInput.requestFocus();
			showLoginError();
			return;
		}
		if (TextUtils.isEmpty(password)) {
			passwordTextInputLayout.setError(getString(R.string.error_password_required));
			passwordInput.requestFocus();
			showLoginError();
			return;
		}
		//showProgressDialog();
		Call<AuthResponse> call = api.login(new AuthRequest(login, password));
		call.enqueue(new Callback<AuthResponse>() {

			@Override
			public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.body() != null) {
                    AuthResponse authResponse = response.body();
                    token = authResponse.getToken();

                    sendResult(authResponse.getUser().getId(),
                            authResponse.getUser().getName(),
                            token);
                }

                if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
			}

			@Override
			public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Snackbar.make(loginRoot, getString(R.string.log_in_error), Snackbar.LENGTH_LONG)
                        .show();
			}
		});
	}

	private void sendResult(long userId, String username, String token) {
		Intent intent = new Intent();
		intent.putExtra(FIELD_USERID, userId);
		intent.putExtra(FIELD_USERNAME, username);
		intent.putExtra(FIELD_TOKEN, token);
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
	}

	private void startBrowser(Context context, Uri uri){
//		Intent startIntent = new Intent(Intent.ACTION_VIEW, uri);
//		startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(startIntent);
		if(callback != null) callback.onFunstreamLoginClicked(uri);
	}

	/**
	 * OAUTH Exchange code for token
	 */
	private void getToken(){
		if(!NetworkUtils.isNetworkConnectionPresent(getContext())){
			Snackbar.make(loginRoot, getString(R.string.error_no_connection), Snackbar.LENGTH_LONG)
					.setAction(R.string.error_loading_action, view -> {
						getToken();
					})
					.show();
			return;
		}

//		loginObs = restClient.getApiService().getTokenObs(new OAuthResponse(oAuthCode))
//				.subscribeOn(Schedulers.io())
//				.map(OAuthResponse::userObservable)
//				.observeOn(AndroidSchedulers.mainThread())
//				.compose(bindToLifecycle());
//
//		loginObs
//				.subscribe(new Subscriber<CurrentUser>() {
//					@Override
//					public void onCompleted() {
//
//					}
//
//					@Override
//					public void onError(Throwable e) {
//						Snackbar.make(loginRoot, getString(R.string.error_didnt_grant_access), Snackbar.LENGTH_LONG)
//								.show();
//						prefUtils.clearUserCode();
//						btOAuthLogin.setEnabled(true);
//						if (progressDialog != null && progressDialog.isShowing())
//							progressDialog.dismiss();
//					}
//
//					@Override
//					public void onNext(CurrentUser user) {
//						Log.i(LogUtils.TAG, user.getToken());
//						sendResult(user.getId(), user.getName(), user.getToken());
//					}
//				});

		Call<OAuthResponse> call = api.getToken(new OAuthResponse(oAuthCode));
		call.enqueue(new Callback<OAuthResponse>() {
            @Override
            public void onResponse(Call<OAuthResponse> call, Response<OAuthResponse> response) {
				OAuthResponse oAuthResponse = response.body();
				if (oAuthResponse != null) token = response.body().getToken();
				if (!isEmpty(token)) {
					Log.i(LogUtils.TAG, token);
					// Delete code from user prefs
					// Parse token to get username
					// SimpleToken simpleToken = userStore.parseToken(token);

					// TODO Save user
		//					if (progressDialog != null && progressDialog.isShowing())
		//						progressDialog.dismiss();
					// Send data back to caller and close activity
					sendResult(oAuthResponse.getUser().getId(), oAuthResponse.getUser().getName(), token);
				}
				prefUtils.clearUserCode();
				btOAuthLogin.setEnabled(true);
            }

            @Override
            public void onFailure(Call<OAuthResponse> call, Throwable t) {
				Snackbar.make(loginRoot, getString(R.string.error_didnt_grant_access), Snackbar.LENGTH_LONG)
						.show();
				prefUtils.clearUserCode();
				btOAuthLogin.setEnabled(true);
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
            }
        });
	}

	private void showProgressDialog(){
		progressDialog = new ProgressDialog(getContext(), R.style.Dialog_Dark);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage(getString(R.string.dialog_login_pending));
		progressDialog.show();
	}


	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)
				getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(usernameInput.getWindowToken(), 0);
	}

	public interface LoginHandler {
		void onFunstreamLoginClicked(Uri uri);
		void onCredsEntered();
	}
}
