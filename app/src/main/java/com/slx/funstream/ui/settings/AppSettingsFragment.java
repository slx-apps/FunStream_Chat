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

package com.slx.funstream.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.slx.funstream.R;
import com.slx.funstream.utils.PrefUtils;


public class AppSettingsFragment extends PreferenceFragment {

//	private ListPreference mListPreference;
//	private Preference preference;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(PrefUtils.PREFS_FILE);
		prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE);

//		PreferenceManager.setDefaultValues(getActivity(),
//				R.xml.app_preferences, false);

		addPreferencesFromResource(R.xml.app_preferences);
	}

//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
////		mListPreference = (ListPreference)  getPreferenceManager().findPreference("preference_key");
////		mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
////			@Override
////			public boolean onPreferenceChange(Preference preference, Object newValue) {
////				return false;
////			}
////		});
//
//		preference  = getPreferenceManager().findPreference("preference_key");
//
//		return inflater.inflate(R.layout.fragment_settings, container, false);
//	}

}