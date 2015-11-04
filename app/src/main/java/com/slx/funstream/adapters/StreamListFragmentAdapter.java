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

package com.slx.funstream.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.slx.funstream.ui.streams.StreamsContainerFragment;

import java.util.List;


public class StreamListFragmentAdapter extends FragmentPagerAdapter {
	//private final FragmentManager mFragmentManager;
	private final List<StreamsContainerFragment.ChatTabItem> mTabs;

	public StreamListFragmentAdapter(FragmentManager fm, List<StreamsContainerFragment.ChatTabItem> tabs) {
		super(fm);
		//this.mFragmentManager = fm;
		this.mTabs = tabs;
	}

	@Override
	public Fragment getItem(int position) {
		return mTabs.get(position).createFragment();
	}

	@Override
	public int getCount() {
		return mTabs.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTabs.get(position).getTitle();
	}
}
