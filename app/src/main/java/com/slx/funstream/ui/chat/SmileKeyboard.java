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

package com.slx.funstream.ui.chat;


import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatPopupWindow;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;

import com.slx.funstream.R;
import com.slx.funstream.rest.model.Smile;
import com.slx.funstream.ui.chat.SmileGridView.OnSmileClickListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Alex Neeky
 *
 * Based on version
 * Hieu Rocker (rockerhieu@gmail.com)
 * Ankush Sachdeva (sankush@yahoo.co.in)
 *
 */

public class SmileKeyboard extends AppCompatPopupWindow {
	private static final int MAX_TABS = 9;
	private Context context;
	private WeakReference<View> contentRoot;
	private Collection<Smile> smileList;
	private int softKeyBoardHeight = 0;
	private Boolean pendingOpen = false;
	private Boolean isOpened = false;

	OnSmileClickListener onSmileClickListener;
	OnSmileBackspaceClickListener onSmileBackspaceClickListener;
	OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;

	public SmileKeyboard(Context context, View contentRoot, Collection<Smile> smileList) {
		super(context, null, 0);
		this.context = context;
		this.contentRoot = new WeakReference<>(contentRoot);
		this.smileList = smileList;
		final View view = createView();
		setContentView(view);
		// please always make the soft input area visible when this window receives input focus
		setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		setSize((int) context.getResources().getDimension(R.dimen.keyboard_default_height), LayoutParams.MATCH_PARENT);
	}

	private View createView() {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.popup_smile_keyboard, null);
		TabLayout smileTablayout = (TabLayout) view.findViewById(R.id.smile_tablayout);
		ViewPager smilePager = (ViewPager) view.findViewById(R.id.smile_pager);
		ImageButton ibBackspace = (ImageButton) view.findViewById(R.id.ibBackspace);
		ibBackspace.setOnClickListener(v -> {
            if (onSmileBackspaceClickListener != null)
                onSmileBackspaceClickListener.onSmileBackspaceClick(v);
        });

		List<SmileGridView> pagersGridViews = new ArrayList<>();
		List<List<Smile>> tabs = new ArrayList<>();
		// create tabs
		for (int i = 0; i <= MAX_TABS; i++) {
			tabs.add(i, new ArrayList<>());
		}
		// populate tabs
		for (Smile smiley : smileList) {
			tabs.get(smiley.getLevel()).add(smiley);
		}
		for (List<Smile> tab : tabs) {
			pagersGridViews.add(new SmileGridView(context, tab, this));
		}

		SmileTabPagerAdapter mSmileTabAdapter = new SmileTabPagerAdapter(pagersGridViews);


		smilePager.setAdapter(mSmileTabAdapter);
		smileTablayout.setupWithViewPager(smilePager);
		smileTablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
		smileTablayout.setTabGravity(TabLayout.GRAVITY_FILL);

		return view;
	}


//	public void updateKeyboardPager(List<Map<String, Smile>> smilesList){
//		List<SmileGridView> pagersGridViews = new ArrayList<>();
//		for(Map<String, Smile> map : smilesList){
//			pagersGridViews.add(new SmileGridView(context, Collections.list(Collections.enumeration(map.values())), this));
//		}
//		mSmileTabAdapter.update(pagersGridViews);
//		smileTablayout.invalidate();
//	}

	public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
		this.onSoftKeyboardOpenCloseListener = listener;
	}
	public void setOnSmileClickedListener(OnSmileClickListener listener) {
		this.onSmileClickListener = listener;
	}
	public void setOnSmileBackspaceClickedListener(OnSmileBackspaceClickListener listener) {
		this.onSmileBackspaceClickListener = listener;
	}

	public void showAtBottom() {
		showAtLocation(contentRoot.get(), Gravity.BOTTOM, 0, 0);
	}

	public void showAtBottomPending() {
		if (isKeyBoardOpen())
			showAtBottom();
		else
			pendingOpen = true;
	}

	public void setSize(int height, int width) {
		setWidth(width);
		setHeight(height);
	}

	public Boolean isKeyBoardOpen() {
		return isOpened;
	}

	public void setSizeForSoftKeyboard() {
		View view = contentRoot.get();
		view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
        	Rect r = new Rect();
			view.getWindowVisibleDisplayFrame(r);

			int screenHeight = view.getRootView().getHeight();
			int heightDifference = screenHeight - (r.bottom - r.top);
			int resourceId = context.getResources().getIdentifier("status_bar_height",
							"dimen", "android");
			if (resourceId > 0) {
				heightDifference -= context.getResources().getDimensionPixelSize(resourceId);
			}
			if (heightDifference > 100) {
				softKeyBoardHeight = heightDifference - 150;// ~150 height of bottom buttons
                // if soft keyboard not displayed
				if (softKeyBoardHeight < 0) softKeyBoardHeight = 0;
				setSize(softKeyBoardHeight, LayoutParams.MATCH_PARENT);
				if (!isOpened) {
					if (onSoftKeyboardOpenCloseListener != null)
						onSoftKeyboardOpenCloseListener.onKeyboardOpen(softKeyBoardHeight);
				}
				isOpened = true;
				if (pendingOpen) {
					showAtBottom();
					pendingOpen = false;
				}
			} else{
				isOpened = false;
				if (onSoftKeyboardOpenCloseListener != null)
					onSoftKeyboardOpenCloseListener.onKeyboardClose();
			}
        });
	}

	private static class SmileTabPagerAdapter extends PagerAdapter {
		private List<SmileGridView> views;

//		public SmileRecentsGridView getRecentFragment(){
//			for (SmileGridView it : views) {
//				if(it instanceof SmileRecentsGridView)
//					return (SmileGridView)it;
//			}
//			return null;
//		}
		public SmileTabPagerAdapter(List<SmileGridView> tabViews) {
			super();
			views = tabViews;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return String.valueOf(position);
		}

		@Override
		public int getCount() {
			return views != null ? views.size() : 0;
		}


		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = views.get(position).rootView;
			container.addView(v, 0);
			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			container.removeView((View)view);
		}

		@Override
		public boolean isViewFromObject(View view, Object key) {
			return key == view;
		}
	}

	public interface OnSmileBackspaceClickListener {
		void onSmileBackspaceClick(View v);
	}

	public interface OnSoftKeyboardOpenCloseListener {
		void onKeyboardOpen(int keyBoardHeight);
		void onKeyboardClose();
	}

}
