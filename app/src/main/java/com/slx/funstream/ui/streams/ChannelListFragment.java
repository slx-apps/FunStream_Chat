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

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.slx.funstream.App;
import com.slx.funstream.R;
import com.slx.funstream.adapters.ChatChannelsAdapter;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.ChatApiUtils;
import com.slx.funstream.rest.model.Stream;
import com.slx.funstream.rest.FSRestClient;
import com.slx.funstream.rest.StreamsRepo;
import com.slx.funstream.rest.model.Category;
import com.slx.funstream.ui.DividerItemDecoration;
import com.slx.funstream.ui.streams.ChannelsAdapter.OnChatChannelClick;
import com.slx.funstream.utils.LogUtils;
import com.slx.funstream.utils.PrefUtils;
import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.slx.funstream.ui.streams.StreamsContainerFragment.TYPE_LIST_FAVORITE;
import static com.slx.funstream.ui.streams.StreamsContainerFragment.TYPE_LIST_STREAMS;
import static com.slx.funstream.utils.NetworkUtils.isNetworkConnectionPresent;

public class ChannelListFragment extends RxFragment implements OnChatChannelClick,
		AdapterView.OnItemSelectedListener {
    private static final String TAG = "ChannelListFragment";
    public static final String KEY_CHAT_FRAGMENT_TYPE = "chat_fragment_type";
	public static final int SEARCH_VIEW_TIMEOUT = 400;

	@BindView(R.id.rvStreams)
	RecyclerView rvStreams;
	@BindView(R.id.swipeContainer)
	SwipeRefreshLayout swipeContainer;
	@BindView(R.id.rootView)
	LinearLayout rootView;

	private int fragType;

	private List<Stream> streams = new LinkedList<>();
	private ChannelsAdapter mChannelsAdapter;
	private ChatChannelsAdapter mChatChannelsAdapter;

	@Inject
	Picasso picasso;
	@Inject
	PrefUtils prefUtils;
    @Inject
    StreamsRepo streamsRepo;

	private AppCompatSpinner mNavigationSpinner;
	private Toolbar mToolbar;
	private Category category;
	private List<Category> categories;
	private Unbinder unbinder;

	public ChannelListFragment() {}

	public static ChannelListFragment newInstance(int frag_type) {
		ChannelListFragment fragment = new ChannelListFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_CHAT_FRAGMENT_TYPE, frag_type);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
//		AndroidInjection.inject(this);
        ((StreamsActivity) context).supportFragmentInjector().inject(this);
		super.onAttach(context);
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

        createAdapter();

		setHasOptionsMenu(true);
	}


    private void createAdapter() {
        if (fragType == TYPE_LIST_STREAMS) {
            mChannelsAdapter = new ChannelsAdapter(this, picasso);
        } else if (fragType == TYPE_LIST_FAVORITE) {
			//
        } else {
            mChatChannelsAdapter = new ChatChannelsAdapter(this, StreamsContainerFragment.sChannels);
        }
    }


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chat_list_layout, container, false);
		unbinder = ButterKnife.bind(this, view);
		return view;
	}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.primary,
                R.color.accent);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);

        rvStreams.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvStreams.addItemDecoration(itemDecoration);

        if (fragType == TYPE_LIST_STREAMS) {
            rvStreams.setAdapter(mChannelsAdapter);
            swipeContainer.setOnRefreshListener(() -> fetchStreams(category));
//            // fetch only for stream tab
//            fetchStreams(category);
        } else {
            rvStreams.setAdapter(mChatChannelsAdapter);
        }

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
					.skip(1)
					.debounce(SEARCH_VIEW_TIMEOUT, TimeUnit.MILLISECONDS)
//					.map(q -> q.toString().toLowerCase())
					.observeOn(AndroidSchedulers.mainThread())
					.compose(bindToLifecycle())
					.subscribe(new DefaultObserver<CharSequence>() {
                        @Override
                        public void onNext(CharSequence charSequence) {
                            filterChannels(charSequence);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
		}
	}

    private void filterChannels(CharSequence query) {
        List<Stream> filtered = new LinkedList<>();
        streamsRepo.getAllStreams(category)
                .flattenAsFlowable(new Function<List<Stream>, Iterable<?>>() {
                    @Override
                    public Iterable<Stream> apply(@NonNull List<Stream> streams) throws Exception {
                        return streams;
                    }
                })
//                .flatMap(new Function<List<Stream>, SingleSource<?>>() {
//					@Override
//					public SingleSource<?> apply(@NonNull List<Stream> streams) throws Exception {
//						return Flowable.();
//					}
//				})
                .filter(object -> {
                    Stream stream = (Stream) object;
                    return stream.getStreamer().getName().toLowerCase().contains(query)
                            ||
                            stream.getName().toLowerCase().contains(query);
                })
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        /*

        new Subscriber<Stream>() {
                    @Override
                    public void onCompleted() {
                        mChannelsAdapter.setData(filtered);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "filterChannels onError: e " + e);
                    }

                    @Override
                    public void onNext(Stream stream) {
                        filtered.add(stream);
                    }
                }

         */
    }

    @Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Hide search and refresh for static channels.
		if (fragType != TYPE_LIST_STREAMS) {
			menu.findItem(R.id.search).setVisible(false);
			menu.findItem(R.id.action_refresh).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// show filter box
		if (id == R.id.action_refresh) {
			if (fragType == TYPE_LIST_STREAMS){
				fetchStreams(category);
				return true;
			} else {
				return false;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Fetch stream only for stream list
        // TODO opt remove
		if (fragType == TYPE_LIST_STREAMS) {
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
		unbinder.unbind();
	}

	View.OnClickListener reloadListener = view -> fetchStreams(category);

    private void fetchStreams(Category category) {
		swipeContainer.setRefreshing(true);

		streamsRepo.getAllStreams(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
				.compose(bindToLifecycle())
				.subscribe(new DisposableSingleObserver<List<Stream>>() {
					@Override
					public void onSuccess(List<Stream> list) {
						swipeContainer.setRefreshing(false);
                        streams = list;
						if (mChannelsAdapter != null) {
							mChannelsAdapter.setData(streams);
						}
					}

					@Override
					public void onError(Throwable e) {
						Snackbar.make(rootView, getString(R.string.error_streams_loading), Snackbar.LENGTH_LONG)
								.setAction(R.string.error_loading_action, reloadListener)
								.show();
						swipeContainer.setRefreshing(false);
						Log.e(LogUtils.TAG, e.toString());
					}
				});
	}



	@Override
	public void onStreamClicked(Stream stream) {
		startStreamChatActivity(stream);
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

	private void startStreamChatActivity(Stream stream) {
		if (stream.getStreamer() != null) {
			Intent startStream = new Intent(getActivity(), StreamActivity.class);
			startStream.putExtra(StreamActivity.STREAMER_NAME, stream.getStreamer().getName());
			startStream.putExtra(StreamActivity.STREAMER_ID, stream.getStreamer().getId());
			startActivity(startStream);
		} else {
			Log.e(TAG, "startStreamChatActivity: Streamer is null");
		}
	}

	private void startChannelChatActivity(long id, String channel) {
		Intent intent = new Intent(getActivity(), StreamActivity.class);
		intent.putExtra(StreamActivity.STREAMER_NAME, channel);
		intent.putExtra(StreamActivity.STREAMER_ID, id);
		startActivity(intent);
	}

//	@Override
//	public boolean onQueryTextSubmit(String query) {
//		if (mStreams == null) return false;
//		mChannelsAdapter.setData(filterStreamsByNameAndTitle(mStreams, query));
//		return true;
//	}
//
//	@Override
//	public boolean onQueryTextChange(String newText) {
//		return false;
//	}


//	public static List<Stream> filterStreamsByNameAndTitle(List<Stream> unfiltered, String query){
//		List<Stream> filtered = new ArrayList<>();
//		if (isEmpty(query)) return unfiltered;
//		for(Stream stream : unfiltered){
//			if (stream.getStreamer().getName().toLowerCase().contains(query) ||
//					stream.getName().toLowerCase().contains(query)){
//				filtered.add(stream);
//			}
//		}
//		return filtered;
//	}

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

    private void addSpinner() {
		final CategoryAdapter spinnerAdapter = new CategoryAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, categories);
//						spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//						mNavigationTags = getResources().getStringArray(R.array.main_navigation_list);
//
//
		if (mNavigationSpinner == null) {
			mNavigationSpinner = new AppCompatSpinner(getContext());//actionBar.getThemedContext()
			mNavigationSpinner.setAdapter(spinnerAdapter);
			mNavigationSpinner.setOnItemSelectedListener(this);
			mNavigationSpinner.setSelection(categories.indexOf(category), false);
		}

		mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		mToolbar.addView(mNavigationSpinner);
	}
}
