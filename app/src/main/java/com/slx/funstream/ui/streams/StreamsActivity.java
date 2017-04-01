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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.di.DiProvider;
import com.slx.funstream.rest.services.FunstreamApi;
import com.slx.funstream.ui.AboutActivity;
import com.slx.funstream.ui.settings.AppSettingsActivity;
import com.slx.funstream.utils.PrefUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasDispatchingSupportFragmentInjector;


public class StreamsActivity extends AppCompatActivity
		implements DiProvider, HasDispatchingSupportFragmentInjector {

	@BindView(R.id.toolbar)
	Toolbar toolbar;

    @Inject
    FunstreamApi api;
    @Inject
    UserStore userStore;
    @Inject
    PrefUtils prefUtils;
	@Inject
	DispatchingAndroidInjector<Fragment> fragmentInjector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_streams);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		//getSupportActionBar().setIcon(R.mipmap.ic_launcher);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.container);

		if (fragment == null) {
			fragment = StreamsContainerFragment.newInstance();
			fm.beginTransaction()
					.add(R.id.container, fragment)
					.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			startActivity(new Intent(this, AppSettingsActivity.class));
			return true;
		} else if (id == R.id.action_about) {
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
		return fragmentInjector;
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
}
