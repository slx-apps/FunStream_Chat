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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.slx.funstream.R;
import com.slx.funstream.model.Stream;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StreamsAdapter extends UltimateViewAdapter implements View.OnClickListener {
	private final Picasso picasso;
	private OnChatChannelClick mOnChatChannelClickCallback;
	private List<Stream> streams = new ArrayList<>();

	public StreamsAdapter(OnChatChannelClick onChatChannelClick, Picasso picasso) {
		this.picasso = picasso;
		this.mOnChatChannelClickCallback = onChatChannelClick;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

		final Stream stream = streams.get(position);
		((ViewHolder) holder).bind(stream);
		// Set Stream title
		((ViewHolder) holder).setName();
		// Set Streamer name
		((ViewHolder) holder).setStreamer();
		// Set rating
		((ViewHolder) holder).setRating();
		// Set adult
		((ViewHolder) holder).setAdult();

		// TODO change after fix 18+
		// Set Stream preview image
//		picasso
//			.load(APIUtils.FUNSTREAM_API_ENDPOINT + stream.getImage())
//			.placeholder(R.mipmap.default_preview)
//			.error(R.mipmap.default_preview)
//			.resizeDimen(R.dimen.row_card_width, R.dimen.row_image_height)
//			.centerCrop()
////				.transform(new RoundedTransformation(100, 0))
//		.into(((ViewHolder) holder).ivStreamImage);

//		((ViewHolder) holder).setDesc();

		((ViewHolder) holder).contentRoot.setTag(holder);
	}

	@Override
	public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
		final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stream, parent, false);
		final ViewHolder streamViewHolder = new ViewHolder(view);
		streamViewHolder.contentRoot.setOnClickListener(this);
		return streamViewHolder;
	}

	@Override
	public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
		return null;
	}

	@Override
	public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
		//
	}

	@Override
	public int getAdapterItemCount() {
		return streams == null ? 0 : streams.size();
	}

	@Override
	public long generateHeaderId(int i) {
		return 0;
	}


	@Override
	public void onClick(View view) {
		final int viewId = view.getId();
		if (viewId == R.id.contentRoot) {
			if (mOnChatChannelClickCallback != null) {
				ViewHolder holder = (ViewHolder) view.getTag();
				mOnChatChannelClickCallback.onStreamClicked(streams.get(holder.getAdapterPosition()));
			}
		}
	}

	public void setData(List<Stream> list) {
		if (list == null)
			return;
		streams = list;
		this.notifyDataSetChanged();
	}

	class ViewHolder extends UltimateRecyclerviewViewHolder {
		@Bind(R.id.tvName)
		TextView tvName;
		@Bind(R.id.tvStreamer)
		TextView tvStreamer;
//		@Bind(R.id.ivStreamImage)
//		ImageView ivStreamImage;
		@Bind(R.id.ivAdult)
		ImageView ivAdult;
		@Bind(R.id.tvRating)
		TextView tvRating;
		@Bind(R.id.contentRoot)
		RelativeLayout contentRoot;

		private Stream mStream;

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		public void bind(Stream stream) {
			mStream = stream;
		}

		public Stream getStream() {
			return mStream;
		}

		public void setName() {
			if (mStream.getName() != null) {
				tvName.setVisibility(View.VISIBLE);
				tvName.setText(mStream.getName());
			}
		}

		public void setStreamer() {
			if (mStream.getStreamer() != null) {
				tvStreamer.setVisibility(View.VISIBLE);
				tvStreamer.setText(mStream.getStreamer().getName());
			}
		}

		public void setAdult() {
			if (mStream.isAdult() != null) {
				ivAdult.setVisibility(View.VISIBLE);
			}
		}

		public void setRating() {
			if (mStream.getRating() != null) {
				tvRating.setVisibility(View.VISIBLE);
				tvRating.setText(String.valueOf(mStream.getRating()));
			}
		}
	}

	public interface OnChatChannelClick {
		void onStreamClicked(Stream stream);
		void onChatChannelClicked(String channel);
	}
}
