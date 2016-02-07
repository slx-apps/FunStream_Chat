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


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.SmileRepo;
import com.slx.funstream.di.Injector;
import com.slx.funstream.model.ChatMessage;
import com.slx.funstream.model.ChatUser;
import com.slx.funstream.rest.model.Smile;
import com.slx.funstream.utils.LogUtils;
import com.slx.funstream.utils.PrefUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

import static android.text.TextUtils.isEmpty;
import static com.slx.funstream.chat.SmileRepo.SMILE_PATTERN;
import static com.slx.funstream.utils.TextUtils.makeFrom;
import static com.slx.funstream.utils.TextUtils.makeTo;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> implements View.OnClickListener {

	private static final int FROM_TWITCH = -100;
	private static final int FROM_GG = -101;
	private List<ChatMessage> mMessages;
	private Context context;

	private int primaryColor;
	private int darkColor;

	// Picasso does not hold a strong reference to the Target object
	private ArrayList<Target> targets = new ArrayList<>();

	@Inject
	SmileRepo smileRepo;
	@Inject
	Picasso picasso;
	@Inject
	PrefUtils prefUtils;
	@Inject
	UserStore userStore;
	private boolean isShowSmileys = true;

	public void setOnChatMessageClickListener(OnChatMessageClick callback) {
		this.callback = callback;
	}

	private OnChatMessageClick callback;

	public ChatAdapter(Context context) {
		this.context = context;
		Injector.INSTANCE.getApplicationComponent().inject(this);
		isShowSmileys = prefUtils.isShowSmileys();

		primaryColor = ContextCompat.getColor(context, R.color.primary);
		darkColor = ContextCompat.getColor(context, R.color.black);
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(context).inflate(R.layout.row_chat_message, parent, false);

		final ViewHolder viewHolder = new ViewHolder(view);
		viewHolder.messageRoot.setOnClickListener(this);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		// Reset
		// clear after non-funstream chat messages disabling
		holder.messageRoot.setEnabled(true);
		// clear tw and gg icons
		holder.tvFrom.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		// clear conversation
		holder.messageRoot.setBackgroundResource(0);
//		holder.tvText.setTextColor(darkColor);

		final ChatMessage message = mMessages.get(position);
		long fromId = message.getFrom().getId();

		if( fromId == FROM_TWITCH || fromId == FROM_GG ){
			if(fromId == FROM_TWITCH)  {
				holder.tvFrom
						.setCompoundDrawablesWithIntrinsicBounds(R.drawable.twitch, 0, 0, 0);
			}
			if(fromId == FROM_GG)  {
				holder.tvFrom
						.setCompoundDrawablesWithIntrinsicBounds(R.drawable.goodgame, 0, 0, 0);
			}

			// Disable row for non non-funstream chat messages
			holder.messageRoot.setEnabled(false);
		}
		holder.setFrom(message.getFrom());
		holder.setTo(message.getTo());

//		//	handle /me
//		if(message.getText().contains("/me")){
//			message.setText(message.getText().replace("/me ", ""));
//			//
//			holder.tvText.setTextColor(primaryColor);
//		}

		Spannable spannable = spannableFactory.newSpannable(message.getText());
		holder.setMessage(spannable);
		if(isShowSmileys) {
			// Add smileys
			addSmiles(spannable, holder.tvText);
		}
		// Add links
		addLinks(spannable);
		// Add Images
		// Check if user allowed it
//		if(prefUtils.isShowImages()){
//			linksToImages(spannable, ((ViewHolder holder).tvText);
//		}

		//
		if(userStore.isUserLoggedIn() && message.getTo() != null && message.getTo().getId() == userStore.getCurrentUser().getId()){
			holder.messageRoot.setBackgroundResource(R.drawable.selector_row_chat_message_to);
		}

		// Set Tag
		holder.messageRoot.setTag(holder);
	}

	@Override
	public int getItemCount() {
		return mMessages != null ? mMessages.size() : 0;
	}

	private void addSmiles(Spannable spannable, TextView textView) {
		Matcher matcher = SMILE_PATTERN.matcher(spannable);
		while (matcher.find()) {
			ImageTarget target = new ImageTarget(
					matcher.start(),
					matcher.end(),
					textView,
					spannable);

			// Add target to an ArrayList to keep a strong reference
			// to prevent that the target gets garbage collected
			// before the image is placed into the view
			targets.add(target);
			Log.d(LogUtils.TAG, "targets.size=" + targets.size());

			Smile smile = smileRepo.getSmile(matcher.group(1));
			if(smile != null){
				picasso
					.load(smile.getUrl())
					.into(target);
			}
		}
	}

	private void addLinks(Spannable spannable) {
		Linkify.addLinks(spannable, Linkify.WEB_URLS);
	}

	private void linksToImages(Spannable spannable, TextView textView){
//		Matcher matcher = Linkify.WEB_URLS.matcher(spannable);
//		URLSpan[] spans = textView.getUrls();
		URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
		Log.d(LogUtils.TAG, "URLSpan[] size="+spans.length);
		for(URLSpan span : spans) {
			String url = span.getURL();
			Log.d(LogUtils.TAG, "START: " + spannable.getSpanStart(span) +
					"END: " + spannable.getSpanEnd(span) + " " + url);
			ImageTarget t = new ImageTarget(spannable.getSpanStart(span), spannable.getSpanEnd(span), textView, spannable);
			picasso
					.load(url)
					.resize(300, 300)
					.centerInside()
					.onlyScaleDown()
					.into(t);
			targets.add(t);
		}
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		@Bind(R.id.tvText)
		TextView tvText;
		@Bind(R.id.tvFrom)
		TextView tvFrom;
		@Bind(R.id.tvTo)
		TextView tvTo;
		@Bind(R.id.messageRoot)
		RelativeLayout messageRoot;

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		public void setMessage(CharSequence message) {
			if (message == null) return;
			tvText.setText(message);
		}

		public void setFrom(ChatUser user) {
			if (user == null || isEmpty(user.getName())) {
				tvFrom.setText("");
			} else {
				tvFrom.setText(makeFrom(user));
			}
		}

		public void setTo(ChatUser user) {
			if (user == null || isEmpty(user.getName())) {
				tvTo.setText("");
			} else {
				tvTo.setText(makeTo(user));
			}
		}


	}

	private class ImageTarget implements Target {
		int start;
		int end;
		WeakReference<TextView> messageText;
		Spannable spannable;

		@DebugLog
		public ImageTarget(int start, int end, TextView messageText, Spannable spannable) {
			this.start = start;
			this.end = end;
			this.messageText = new WeakReference<>(messageText);
			this.spannable = spannable;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			// Get a weak reference to the TextView
			// because holder pattern in Recyclerview will replace the View
			// inside the holder before the AsyncTask is done
			TextView textView = messageText.get();

			// Get existing ImageSpans to add them later again,
			ImageSpan[] spans = spannable.getSpans(0, textView.length(), ImageSpan.class);

			// Create drawable from target bitmap
			BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
			bitmapDrawable.setBounds(
					0,
					0,
					bitmapDrawable.getIntrinsicWidth(),
					bitmapDrawable.getIntrinsicHeight());
			ImageSpan imageSpan = new ImageSpan(bitmapDrawable, ImageSpan.ALIGN_BASELINE);

			// Add ImageSpan to the Spannable
			spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

			// Add previously added ImageSpans
			if (spans.length >= 1) {
				for (ImageSpan image : spans) {
					spannable.setSpan(image,
							spannable.getSpanStart(image),
							spannable.getSpanEnd(image),
							Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				}
			}
			// Update edited Spannable in the TextView
			textView.setText(spannable, TextView.BufferType.SPANNABLE);

			// Remove target from ArrayList to allow GC
			targets.remove(this);
		}

		@DebugLog
		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			targets.remove(this);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			//
		}
	}

	private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

	@Override
	public void onClick(View view) {
		final int viewId = view.getId();
		if (viewId == R.id.messageRoot) {
			if (callback != null) {
				ViewHolder holder = (ViewHolder) view.getTag();
				callback.onChatMessageClicked(mMessages.get(holder.getLayoutPosition()));
			}
		}
	}

	public void updateChatAdapter(List<ChatMessage> messages){
		if(messages != null){
			mMessages = messages;
			this.notifyDataSetChanged();
		}
	}

	public interface OnChatMessageClick {
		void onChatMessageClicked(ChatMessage chatMessage);
	}
}
