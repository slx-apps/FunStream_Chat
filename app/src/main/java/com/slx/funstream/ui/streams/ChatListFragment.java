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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.slx.funstream.R;
import com.slx.funstream.adapters.ChatChannelsAdapter;
import com.slx.funstream.adapters.StreamsAdapter;
import com.slx.funstream.adapters.StreamsAdapter.OnChatChannelClick;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.ChatApiUtils;
import com.slx.funstream.dagger.Injector;
import com.slx.funstream.model.Stream;
import com.slx.funstream.rest.APIUtils;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.model.Category;
import com.slx.funstream.rest.model.CategoryOptions;
import com.slx.funstream.rest.model.CategoryRequest;
import com.slx.funstream.rest.model.ContentRequest;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.ui.DividerItemDecoration;
import com.slx.funstream.utils.LogUtils;
import com.slx.funstream.utils.PrefUtils;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;
import static com.slx.funstream.ui.streams.StreamsContainerFragment.TYPE_LIST_FAVORITE;
import static com.slx.funstream.ui.streams.StreamsContainerFragment.TYPE_LIST_STREAMS;
import static com.slx.funstream.utils.NetworkUtils.isNetworkConnectionPresent;

public class ChatListFragment extends RxFragment implements OnChatChannelClick,
		SearchView.OnQueryTextListener,
		AdapterView.OnItemSelectedListener {
	public static final String KEY_CHAT_FRAGMENT_TYPE = "chat_fragment_type";

	private static final String STREAM = "stream";
	private static final String TYPE = "all";

	@Bind(R.id.rvStreams)
	RecyclerView rvStreams;
	@Bind(R.id.swipeContainer)
	SwipeRefreshLayout swipeContainer;
	@Bind(R.id.rootView)
	LinearLayout rootView;

	private int fragType;

	private List<Stream> mStreams = new ArrayList<>();
	private Observable<List<Stream>> mStreamsObs;
	private StreamsAdapter mStreamsAdapter;
	private ChatChannelsAdapter mChatChannelsAdapter;

	@Inject
	FSRestClient restClient;
	@Inject
	Picasso picasso;
	@Inject
	PrefUtils prefUtils;
	@Inject
	UserStore userStore;

	private AppCompatSpinner mNavigationSpinner;
	private Toolbar mToolbar;
	private Category category;
	private List<Category> categories;

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

		category = new Category(prefUtils.getLastSelectedCategory());
		if (fragType == TYPE_LIST_STREAMS) {
			restClient.getApiService().getCategoriesWithSubs(new CategoryRequest(APIUtils.CONTENT_STREAM, new CategoryOptions(true)))
					.map(cat -> {
						List<Category> cats = new ArrayList<>();
						// Костыль пустого имени топ категории id == 1
						if(cat.getId() == 1) cat.setName(getString(R.string.category_top));
						cats.add(cat);
						if(cat.getSubCategories() != null && cat.getSubCategories().length > 0) {
							Category[] catsArray = cat.getSubCategories();
							for (int i = 0; i < cat.getSubCategories().length; i++) {
								cats.add(catsArray[i]);
							}
						}
						return cats;
					})
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.compose(bindToLifecycle());
//					.subscribe(new Subscriber<List<Category>>() {
//						@Override
//						public void onCompleted() {
//
//						}
//
//						@Override
//						public void onError(Throwable e) {
//							Log.e(LogUtils.TAG, e.toString());
//						}
//
//						@Override
//						public void onNext(List<Category> cats) {
//							Log.i(LogUtils.TAG, "cats size = " + cats.size());
//							categories = cats;
//							addSpinner();
//						}
//					});
			mStreamsAdapter = new StreamsAdapter(this, picasso);

		}
		else if (fragType == TYPE_LIST_FAVORITE) {
			restClient.getApiService().getCurrentUser(userStore.getCurrentUser().getToken())
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.compose(bindToLifecycle())
					.subscribe(new Subscriber<CurrentUser>() {
						@Override
						public void onCompleted() {

						}

						@Override
						public void onError(Throwable e) {

						}

						@Override
						public void onNext(CurrentUser currentUser) {
							//
						}
					});
		} else {
			mChatChannelsAdapter = new ChatChannelsAdapter(this, StreamsContainerFragment.sChannels);
		}

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		if (fragType == TYPE_LIST_STREAMS) {
//			((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
//		}

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

		SearchView searchView =
				(SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));

		if (searchView != null) {
			//searchView.setQueryHint(getString(R.string.search_query_hint));
//			searchView
//					.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

			RxSearchView
					.queryTextChanges(searchView)
					.debounce(400, TimeUnit.MILLISECONDS, Schedulers.computation())
					.map(q -> q.toString().toLowerCase())
					.observeOn(AndroidSchedulers.mainThread())
					.compose(bindToLifecycle())
					.subscribe(new Subscriber<String>() {
						@Override
						public void onCompleted() {
							Log.i(LogUtils.TAG, "RxSearchView onCompleted");
						}

						@Override
						public void onError(Throwable e) {
							Log.e(LogUtils.TAG, e.toString());
						}

						@Override
						public void onNext(String query) {
							mStreamsAdapter.setData(filterStreamsByNameAndTitle(mStreams, query));
						}
					});

//			searchView.setOnCloseListener(() -> {
//				mStreamsAdapter.setData(mStreams);
//				return true;
//			});
//			searchView
//					.setOnQueryTextListener(this);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

//		if(fragType != TYPE_LIST_STREAMS) {
//			menu.findItem(R.id.search).setVisible(false);
//			menu.findItem(R.id.action_refresh).setVisible(false);
//		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
//		// show filter box
		if (id == R.id.action_refresh) {
			if(fragType == TYPE_LIST_STREAMS){
				fetchStreams(category);
				return true;
			} else {
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
			swipeContainer.setOnRefreshListener(() -> fetchStreams(category));
		}else{
			rvStreams.setAdapter(mChatChannelsAdapter);
		}

		// Configure the refreshing colors
		swipeContainer.setColorSchemeResources(R.color.primary,
				R.color.accent);

		RecyclerView.ItemDecoration itemDecoration = new
				DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);

		rvStreams.setLayoutManager(new LinearLayoutManager(getActivity()));
		rvStreams.addItemDecoration(itemDecoration);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Fetch stream only for stream list
		if(fragType == TYPE_LIST_STREAMS){
			if (isNetworkConnectionPresent(getActivity())) {
				fetchStreams(category);
			} else {
				swipeContainer.setRefreshing(false);
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

	View.OnClickListener reloadListener = view -> fetchStreams(category);

	private void fetchStreams(Category category) {
		swipeContainer.setRefreshing(true);
		restClient.getApiService().getContentObs(new ContentRequest(STREAM, TYPE, category))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.compose(bindToLifecycle())
				.subscribe(new Subscriber<List<Stream>>() {
					@Override
					public void onCompleted() {
						Log.i(LogUtils.TAG, "mStreamsObs onCompleted");
						swipeContainer.setRefreshing(false);
					}

					@Override
					public void onError(Throwable e) {
						Snackbar.make(rootView, getString(R.string.error_streams_loading), Snackbar.LENGTH_LONG)
								.setAction(R.string.error_loading_action, reloadListener)
								.show();
						swipeContainer.setRefreshing(false);
						Log.e(LogUtils.TAG, e.toString());
					}

					@Override
					public void onNext(List<Stream> streams) {
						mStreams = streams;
						if (mStreamsAdapter != null) {
							mStreamsAdapter.setData(mStreams);
						}
					}
				});
		//ContentResponse
//		Call<List<Stream>> call = restClient.getApiService().getContent(new ContentRequest(STREAM, TYPE, new Category("top")));
//		call.enqueue(new Callback<List<Stream>>() {
//			@Override
//			public void onResponse(Response<List<Stream>> response, Retrofit retrofit) {
//				if (response.body() != null) {
//					mStreams = response.body();
//					if (mStreamsAdapter != null) {
//						mStreamsAdapter.setData(mStreams);
//					}
//				}
//			}
//
//			@Override
//			public void onFailure(Throwable t) {
//				Snackbar.make(rootView, getString(R.string.error_streams_loading), Snackbar.LENGTH_LONG)
//						.setAction(R.string.error_loading_action, reloadListener)
//						.show();
//				rvStreams.setRefreshing(false);
//				Log.e(LogUtils.TAG, t.toString());
//			}
//		});
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
		mStreamsAdapter.setData(filterStreamsByNameAndTitle(mStreams, query));
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}


	public static List<Stream> filterStreamsByNameAndTitle(List<Stream> unfiltered, String query){
		List<Stream> filtered = new ArrayList<>();
		if (isEmpty(query)) return unfiltered;
		for(Stream stream : unfiltered){
			if (stream.getStreamer().getName().toLowerCase().contains(query) ||
					stream.getName().toLowerCase().contains(query)){
				filtered.add(stream);
			}
		}
		return filtered;
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		prefUtils.setLastSelectedCategory(categories.get(i).getId());
		fetchStreams(categories.get(i));
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}

	private class CategoryAdapter extends ArrayAdapter<Category> {
		List<Category> categories;
		public CategoryAdapter(Context context, int resource, List<Category> categories) {
			super(context, resource);
			this.categories = categories;
		}

		@Override
		public Category getItem(int position) {
			return categories.get(position);
		}

		@Override
		public int getCount() {
			return categories == null ? 0 : categories.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Category category = categories.get(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
			}
			TextView txt1 = (TextView) convertView.findViewById(android.R.id.text1);
			txt1.setText(category.getName()) ;


			return convertView;
		}

	}

	@DebugLog
	private void addSpinner(){
		final CategoryAdapter spinnerAdapter = new CategoryAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
//						spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//						mNavigationTags = getResources().getStringArray(R.array.main_navigation_list);
//
//
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if(mNavigationSpinner == null) {
			mNavigationSpinner = new AppCompatSpinner(getContext());//actionBar.getThemedContext()
			mNavigationSpinner.setAdapter(spinnerAdapter);
			mNavigationSpinner.setOnItemSelectedListener(this);
			mNavigationSpinner.setSelection(categories.indexOf(category), false);
		}

		mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		mToolbar.addView(mNavigationSpinner);
	}
}
