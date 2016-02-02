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


import android.content.Context;
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
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.dagger.Injector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StreamsContainerFragment extends Fragment {

	@Bind(R.id.tab_layout)
	TabLayout tabLayout;
	@Bind(R.id.viewpager)
	ViewPager viewpager;

	@Inject
	UserStore userStore;

	private List<ChatTabItem> mTabs = new ArrayList<>();

	public static final int TYPE_LIST_STREAMS = 0;
	public static final int TYPE_LIST_CHANNELS = 1;
	public static final int TYPE_LIST_FAVORITE = 2;
	private StreamListFragmentAdapter mPagerAdapter;

	public static List<String> sChannels = new ArrayList<>();

	static {
		sChannels.add("main");
		sChannels.add("admin");
//		sChannels.add("support");//support/<id>
//		sChannels.add("private");//private/<from_id>/<to_id>
//		sChannels.add("notifications");//notifications/<user_id>
	}

	public StreamsContainerFragment() {
		// Required empty public constructor
	}

	public static StreamsContainerFragment newInstance() {
		//StreamsContainerFragment fragment = new StreamsContainerFragment();
		//Bundle args = new Bundle();
		//args.putBoolean(KEY, VALUE);
		//fragment.setArguments(args);
		return new StreamsContainerFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Injector.INSTANCE.getApplicationComponent().inject(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTabs.add(new ChatTabItem(getString(R.string.tab_title_streams), TYPE_LIST_STREAMS));
		mTabs.add(new ChatTabItem(getString(R.string.tab_title_channels), TYPE_LIST_CHANNELS));
		//if(userStore.isUserLoggedIn()) mTabs.add(new ChatTabItem(getString(R.string.tab_title_favorites), TYPE_LIST_FAVORITE));


		mPagerAdapter = new StreamListFragmentAdapter(getChildFragmentManager(), mTabs);

		// Check whether we're recreating a previously destroyed instance
//		if (savedInstanceState != null) {
//			//TODO handle filter
//		}

		// Option menu
		setHasOptionsMenu(true);
//		// Save
//		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_streams, container, false);
		ButterKnife.bind(this, view);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		viewpager.setAdapter(mPagerAdapter);
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		tabLayout.setTabMode(TabLayout.MODE_FIXED);
		tabLayout.setupWithViewPager(viewpager);
		//initializeFilterBox();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	public class ChatTabItem {
		private final CharSequence mTitle;
		private final int mFragmentType;

		public ChatTabItem(CharSequence title, int frag_type) {
			mTitle = title;
			mFragmentType = frag_type;
		}

		public ChatListFragment createFragment() {
			return ChatListFragment.newInstance(getFragmentType());
		}

		public int getFragmentType() {
			return mFragmentType;
		}

		public CharSequence getTitle() {
			return mTitle;
		}
	}
}
