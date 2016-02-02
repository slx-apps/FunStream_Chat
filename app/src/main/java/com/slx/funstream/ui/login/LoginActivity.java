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


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.slx.funstream.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements LoginFragment.LoginHandler {

	@Bind(R.id.toolbar)
	Toolbar toolbar;

	private FragmentManager fm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
}
