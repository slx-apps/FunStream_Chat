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


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slx.funstream.R;
import com.slx.funstream.adapters.StreamListFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class StreamsContainerFragment extends Fragment {

	@BindView(R.id.tab_layout)
	TabLayout tabLayout;
	@BindView(R.id.viewpager)
	ViewPager viewpager;


	private List<ChatTabItem> mTabs = new ArrayList<>();
	public static final int TYPE_LIST_STREAMS = 0;
	public static final int TYPE_LIST_CHANNELS = 1;
	public static final int TYPE_LIST_FAVORITE = 2;
	public static final int TYPE_LIST_ROOMS = 3;

	private StreamListFragmentAdapter mPagerAdapter;
	private Unbinder unbinder;

	public static List<String> sChannels = new ArrayList<>();

	static {
		sChannels.add("main");
		sChannels.add("admin");
	}

	public StreamsContainerFragment() {

	}

	public static StreamsContainerFragment newInstance() {
		//StreamsContainerFragment fragment = new StreamsContainerFragment();
		//Bundle args = new Bundle();
		//args.putBoolean(KEY, VALUE);
		//fragment.setArguments(args);
		return new StreamsContainerFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTabs.add(new ChatTabItem(getString(R.string.tab_title_streams), TYPE_LIST_STREAMS));
		mTabs.add(new ChatTabItem(getString(R.string.tab_title_channels), TYPE_LIST_CHANNELS));
		//mTabs.add(new ChatTabItem(getString(R.string.tab_title_rooms), TYPE_LIST_ROOMS));
		//if(userStore.isUserLoggedIn()) mTabs.add(new ChatTabItem(getString(R.string.tab_title_favorites), TYPE_LIST_FAVORITE));


		mPagerAdapter = new StreamListFragmentAdapter(getChildFragmentManager(), mTabs);


		// Option menu
		setHasOptionsMenu(true);
//		// Save
//		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_streams, container, false);
        unbinder = ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		viewpager.setAdapter(mPagerAdapter);
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		tabLayout.setTabMode(TabLayout.MODE_FIXED);
		tabLayout.setupWithViewPager(viewpager);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
        unbinder.unbind();
	}

	public class ChatTabItem {
		private final CharSequence mTitle;
		private final int mFragmentType;

		public ChatTabItem(CharSequence title, int frag_type) {
			mTitle = title;
			mFragmentType = frag_type;
		}

		public ChannelListFragment createFragment() {
			return ChannelListFragment.newInstance(getFragmentType());
		}

		public int getFragmentType() {
			return mFragmentType;
		}

		public CharSequence getTitle() {
			return mTitle;
		}
	}
}
