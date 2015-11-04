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


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.divideritemdecoration.HorizontalDividerItemDecoration;
import com.slx.funstream.R;
import com.slx.funstream.adapters.ChatChannelsAdapter;
import com.slx.funstream.adapters.StreamsAdapter;
import com.slx.funstream.adapters.StreamsAdapter.OnChatChannelClick;
import com.slx.funstream.chat.ChatApiUtils;
import com.slx.funstream.dagger.Injector;
import com.slx.funstream.model.Stream;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.model.Category;
import com.slx.funstream.rest.model.ContentRequest;
import com.slx.funstream.utils.LogUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static android.text.TextUtils.isEmpty;
import static com.slx.funstream.ui.streams.StreamsContainerFragment.TYPE_LIST_STREAMS;
import static com.slx.funstream.utils.NetworkUtils.isNetworkConnectionPresent;

public class ChatListFragment extends Fragment implements OnChatChannelClick,
		SearchView.OnQueryTextListener {
	public static final String KEY_CHAT_FRAGMENT_TYPE = "chat_fragment_type";

	private static final String STREAM = "stream";
	private static final String TYPE = "all";

	@Bind(R.id.rvStreams)
	UltimateRecyclerView rvStreams;
	@Bind(R.id.rootView)
	LinearLayout rootView;

	private int fragType;

	private List<Stream> mStreams = new ArrayList<>();

	private StreamsAdapter mStreamsAdapter;
	private ChatChannelsAdapter mChatChannelsAdapter;

	@Inject
	FSRestClient restClient;
	@Inject
	Picasso picasso;

	public ChatListFragment() {
	}

	public static ChatListFragment newInstance(int frag_type) {
		ChatListFragment fragment = new ChatListFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_CHAT_FRAGMENT_TYPE, frag_type);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Injector.INSTANCE.getApplicationComponent().inject(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		if (bundle != null) {
			fragType = bundle.getInt(KEY_CHAT_FRAGMENT_TYPE, TYPE_LIST_STREAMS);
		} else {
			fragType = TYPE_LIST_STREAMS;
		}

		if (fragType == TYPE_LIST_STREAMS) {
			mStreamsAdapter = new StreamsAdapter(this, picasso);
		}else{
			mChatChannelsAdapter = new ChatChannelsAdapter(this, StreamsContainerFragment.sChannels);
		}

		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chat_list_layout, container, false);
		ButterKnife.bind(this, view);
		return view;
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_streams, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager =
				(SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

		MenuItem searchItem = menu.findItem(R.id.search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

		if (searchView != null) {
			searchView
					.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
			searchView
					.setOnQueryTextListener(this);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
//		// show filter box
		if (id == R.id.action_refresh) {
			if(fragType == TYPE_LIST_STREAMS){
				fetchStreams();
				return true;
			} else{
				return false;
			}

		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (fragType == TYPE_LIST_STREAMS) {
			rvStreams.setAdapter(mStreamsAdapter);
			rvStreams.setDefaultOnRefreshListener(ChatListFragment.this::fetchStreams);
		}else{
			rvStreams.setAdapter(mChatChannelsAdapter);
		}

		rvStreams.setLayoutManager(new LinearLayoutManager(getActivity()));
		rvStreams.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());
	}

	@Override
	public void onResume() {
		super.onResume();
		// Fetch stream only for stream list
		if(fragType == TYPE_LIST_STREAMS){
			if (isNetworkConnectionPresent(getActivity())) {
				fetchStreams();
			} else {
				rvStreams.setRefreshing(false);
				Snackbar.make(rootView, getString(R.string.error_no_connection), Snackbar.LENGTH_LONG)
						.setAction(R.string.error_loading_action, reloadListener)
						.show();
			}
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	View.OnClickListener reloadListener = view -> fetchStreams();

	private void fetchStreams() {
		Call<List<Stream>> call = restClient.getApiService().getContent(new ContentRequest(STREAM, TYPE, new Category("top")));
		call.enqueue(new Callback<List<Stream>>() {
			@Override
			public void onResponse(Response<List<Stream>> response, Retrofit retrofit) {
				if (response.body() != null) {
					mStreams = response.body();
					if (mStreamsAdapter != null) {
						mStreamsAdapter.setData(mStreams);
					}
				}
			}

			@Override
			public void onFailure(Throwable t) {
				Snackbar.make(rootView, getString(R.string.error_streams_loading), Snackbar.LENGTH_LONG)
						.setAction(R.string.error_loading_action, reloadListener)
						.show();
				rvStreams.setRefreshing(false);
				Log.e(LogUtils.TAG, t.toString());
			}
		});
	}

	@Override
	public void onStreamClicked(Stream stream) {
		startStreamChatActivity(stream.getStreamer().getId(), stream.getStreamer().getName());
	}

	@Override
	public void onChatChannelClicked(String channel) {
		switch (channel){
			case "main":
				startChannelChatActivity(ChatApiUtils.CHANNEL_MAIN, "Main channel");
				break;
			case "admin":
				startChannelChatActivity(ChatApiUtils.CHANNEL_ADMIN, "Admin channel");
				break;
			case "support":
			case "private":
			case "notifications":
				break;
			default:
				Log.e(LogUtils.TAG, "Wrong chat channel");
				break;
		}
	}

	private void startStreamChatActivity(long streamerId, String streamerName){
		Intent startStream = new Intent(getActivity(), StreamActivity.class);
		startStream.putExtra(StreamActivity.STREAMER_NAME, streamerName);
		startStream.putExtra(StreamActivity.STREAMER_ID, streamerId);
		startActivity(startStream);
	}

	private void startChannelChatActivity(long id, String channel){
		Intent intent = new Intent(getActivity(), StreamActivity.class);
		intent.putExtra(StreamActivity.STREAMER_NAME, channel);
		intent.putExtra(StreamActivity.STREAMER_ID, id);
		startActivity(intent);
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		if(mStreams == null) return false;
		mStreamsAdapter.setData(filterStreamsByName(mStreams, query));
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}


	public static List<Stream> filterStreamsByName(List<Stream> unfiltered, String streamerName){
		List<Stream> filtered = new ArrayList<>();
		if (isEmpty(streamerName)) return unfiltered;
		for(Stream stream : unfiltered){
			if (stream.getStreamer().getName().toLowerCase()
					.contains(streamerName.toLowerCase())){
				filtered.add(stream);
			}
		}
		return filtered;
	}
}
