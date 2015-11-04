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

package com.slx.funstream.ui.login;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.dagger.Injector;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.model.AuthRequest;
import com.slx.funstream.rest.model.AuthResponse;
import com.slx.funstream.rest.model.OAuthRequest;
import com.slx.funstream.rest.model.OAuthResponse;
import com.slx.funstream.utils.LogUtils;
import com.slx.funstream.utils.NetworkUtils;
import com.slx.funstream.utils.PrefUtils;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static android.text.TextUtils.isEmpty;
import static com.slx.funstream.rest.APIUtils.OAUTH_BROWSER_LINK;

public class LoginActivity extends AppCompatActivity {
	public static final String FIELD_USERID = "id";
	public static final String FIELD_USERNAME = "name";
	public static final String FIELD_TOKEN = "token";

	String login;
	String password;
	String token;

	@Bind(R.id.username_input)
	EditText usernameInput;
	@Bind(R.id.password_input)
	EditText passwordInput;
	@Bind(R.id.bt_log_in)
	Button btLogIn;
	@Bind(R.id.username_text_input_layout)
	TextInputLayout usernameTextInputLayout;
	@Bind(R.id.password_text_input_layout)
	TextInputLayout passwordTextInputLayout;
	@Bind(R.id.login_root)
	RelativeLayout loginRoot;
	@Bind(R.id.btOAuthLogin)
	Button btOAuthLogin;
	@Bind(R.id.stream_toolbar)
	Toolbar toolbar;

	@Inject
	FSRestClient restClient;
	@Inject
	UserStore userStore;
	@Inject
	PrefUtils prefUtils;
	@Inject
	Context context;

	private String oAuthCode;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.LoginActivityStyle);
		setContentView(R.layout.login_layout);
		ButterKnife.bind(this);
		Injector.INSTANCE.getApplicationComponent().inject(this);

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(R.string.log_in);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		toolbar.setNavigationOnClickListener(view -> onBackPressed());

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


	}

	@Override
	protected void onStart() {
		super.onStart();
		// Retrieve code from shared preferences
		oAuthCode = prefUtils.getUserCode();
		// Only if code exists
		if(oAuthCode != null){
			// Disable button
			btOAuthLogin.setEnabled(false);
			showProgressDialog();
			getToken();
		}
	}

	/**
	 * OAUTH login method
	 */
	@OnClick(R.id.btOAuthLogin)
	void loginOAuth() {
		// Check if connection is present and working
		if(!NetworkUtils.isNetworkConnectionPresent(this)){
			Snackbar.make(loginRoot, getString(R.string.error_no_connection), Snackbar.LENGTH_LONG)
					.setAction(R.string.error_loading_action, view -> {
						getToken();
					})
					.show();
			return;
		}

		Call<OAuthResponse> call = restClient.getApiService().getPermissionCode(new OAuthRequest(UserStore.APP_KEY));
		call.enqueue(new Callback<OAuthResponse>() {
			@Override
			public void onResponse(Response<OAuthResponse> response, Retrofit retrofit) {
				OAuthResponse oAuthResponse = response.body();
				if (!isEmpty(oAuthResponse.getCode())) {
					// Retrieve code from response
					oAuthCode = oAuthResponse.getCode();
					// Save code to SP
					prefUtils.setUserCode(oAuthCode);
					// Start browser with code
					startBrowser(context, Uri.parse(OAUTH_BROWSER_LINK + oAuthCode));
				}
			}

			@Override
			public void onFailure(Throwable t) {
				Snackbar.make(loginRoot, getString(R.string.error_login), Snackbar.LENGTH_LONG)
						.show();
				prefUtils.clearUserCode();
//				btOAuthLogin.setEnabled(false);
//				pbLogin.setVisibility(View.VISIBLE);
			}
		});
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
		showProgressDialog();
		Call<AuthResponse> call = restClient.getApiService().login(new AuthRequest(login, password));
		call.enqueue(new Callback<AuthResponse>() {
			@DebugLog
			@Override
			public void onResponse(Response<AuthResponse> response, Retrofit retrofit) {
				if(response.body() != null){
					AuthResponse authResponse = response.body();
					token = authResponse.getToken();

					sendResult(authResponse.getUser().getId(),
							authResponse.getUser().getName(),
							token);
				}

				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();

			}

			@DebugLog
			@Override
			public void onFailure(Throwable t) {
		             if(progressDialog != null && progressDialog.isShowing())
			             progressDialog.dismiss();
		             Snackbar.make(loginRoot, getString(R.string.log_in_error), Snackbar.LENGTH_LONG)
				             .show();
	             }
		});
	}

	@DebugLog
	private void sendResult(long userId, String username, String token) {
		Intent intent = new Intent();
		intent.putExtra(FIELD_USERID, userId);
		intent.putExtra(FIELD_USERNAME, username);
		intent.putExtra(FIELD_TOKEN, token);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void showLoginError() {
		Snackbar.make(loginRoot, getString(R.string.log_in_error), Snackbar.LENGTH_LONG)
				.show();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setResult(RESULT_CANCELED);
		finish();
	}

	private static void startBrowser(Context context, Uri uri){
		Intent startIntent = new Intent(Intent.ACTION_VIEW, uri);
		startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(startIntent);
	}

	/**
	 * OAUTH Exchange code for token
	 */
	private void getToken(){
		if(!NetworkUtils.isNetworkConnectionPresent(this)){
			Snackbar.make(loginRoot, getString(R.string.error_no_connection), Snackbar.LENGTH_LONG)
					.setAction(R.string.error_loading_action, view -> {
						getToken();
					})
					.show();
			return;
		}

		Call<OAuthResponse> call = restClient.getApiService().getToken(new OAuthResponse(oAuthCode));
		call.enqueue(new Callback<OAuthResponse>() {

			@Override
			public void onResponse(Response<OAuthResponse> response, Retrofit retrofit) {
				OAuthResponse oAuthResponse = response.body();
				if (oAuthResponse != null) token = response.body().getToken();
				if (!isEmpty(token)) {
					Log.i(LogUtils.TAG, token);
					// Delete code from user prefs
					prefUtils.clearUserCode();
					// Parse token to get username
					// SimpleToken simpleToken = userStore.parseToken(token);

					// TODO Save user
					btOAuthLogin.setEnabled(true);
					if(progressDialog != null && progressDialog.isShowing())
						progressDialog.dismiss();
					// Send data back to caller and close activity
					sendResult(oAuthResponse.getUser().getId(), oAuthResponse.getUser().getName(), token);
				}
			}

			@Override
			public void onFailure(Throwable t) {
				Snackbar.make(loginRoot, getString(R.string.error_didnt_grant_access), Snackbar.LENGTH_LONG)
						.show();
				prefUtils.clearUserCode();
				btOAuthLogin.setEnabled(true);
				if(progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
			}
		});
	}

	private void showProgressDialog(){
		progressDialog = new ProgressDialog(this, R.style.Dialog_Dark);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage(getString(R.string.dialog_login_pending));
		progressDialog.show();
	}
}
