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


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.slx.funstream.R;
import com.slx.funstream.adapters.StreamsAdapter.OnChatChannelClick;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatChannelsAdapter extends UltimateViewAdapter implements
		View.OnClickListener {
	private List<String> channels;
	private OnChatChannelClick mListener;

	public ChatChannelsAdapter(OnChatChannelClick listener, List<String> channels) {
		this.channels = channels;
		this.mListener = listener;
	}


	@Override
	public void onClick(View view) {
		final int viewId = view.getId();
		if (viewId == R.id.contentRoot) {
			if (mListener != null) {
				ViewHolder holder = (ViewHolder) view.getTag();
				mListener.onChatChannelClicked(channels.get(holder.getAdapterPosition()));
			}
		}
	}

	@Override
	public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
		final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_channel, parent, false);
		final ViewHolder streamViewHolder = new ViewHolder(view);
		streamViewHolder.contentRoot.setOnClickListener(this);
		return streamViewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		String channel = channels.get(position);

		// Set Stream title
		((ViewHolder) holder).tvName.setText(channel);
		((ViewHolder) holder).contentRoot.setTag(holder);

//		switch (channel){
//			case "support":
//			case "private":
//			case "notifications":
//				((ViewHolder) holder).contentRoot.setEnabled(false);
//				((ViewHolder) holder).tvName.setText(channel + " Coming soon");
//		}
	}

	@Override
	public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
		//
	}

	@Override
	public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
		return null;
	}

	@Override
	public int getAdapterItemCount() {
		return channels == null ? 0 : channels.size();
	}

	@Override
	public long generateHeaderId(int i) {
		return 0;
	}

	class ViewHolder extends UltimateRecyclerviewViewHolder {
		@Bind(R.id.tvName)
		TextView tvName;
		@Bind(R.id.contentRoot)
		LinearLayout contentRoot;

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

	}
}
