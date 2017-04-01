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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.slx.funstream.App;
import com.slx.funstream.R;
import com.slx.funstream.rest.model.Smile;
import com.slx.funstream.ui.chat.SmileGridView.OnSmileClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;


/**
 * @author Alex Neeky
 *
 * Based on version
 * Ankush Sachdeva (sankush@yahoo.co.in)
 *
 */
public class SmileAdapter extends ArrayAdapter<Smile> {
	private OnSmileClickListener smileClickListener;
	private Picasso picasso;

	public SmileAdapter(Context context, List<Smile> data, Picasso picasso) {
		super(context, R.layout.smile_item, data);
		this.picasso = picasso;
	}

	public void setSmileClickListener(OnSmileClickListener listener){
		this.smileClickListener = listener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = View.inflate(getContext(), R.layout.smile_item, null);
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView) v.findViewById(R.id.smile_icon);
			v.setTag(holder);
		}
		final Smile smile = getItem(position);
		ViewHolder holder = (ViewHolder) v.getTag();
		picasso.load(smile.getUrl()).into(holder.icon);
		holder.icon.setOnClickListener(view -> smileClickListener.onSmileClicked(getItem(position)));
		return v;
	}

	class ViewHolder {
		/*
		<TextView
        android:layout_gravity="center"
        android:id="@+id/smile_icon"
        android:layout_width="36dip"
        android:layout_height="36dip"
        android:focusable="false"
        android:focusableInTouchMode="false"
        android:gravity="center"/>
		*/
		ImageView icon;
	}
}
