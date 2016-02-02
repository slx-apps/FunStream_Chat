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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.slx.funstream.R;
import com.slx.funstream.model.ChatUser;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SimpleUserArrayAdapter extends ArrayAdapter<ChatUser> {
	private ChatUser[] users;
	private Context context;

	public SimpleUserArrayAdapter(Context context, int resource, ChatUser[] users) {
		super(context, resource);
		this.context = context;
		this.users = users;
	}


	@Override
	public ChatUser getItem(int position) {
		return users[position];
	}

	@Override
	public int getCount() {
		return users == null ? 0 : users.length;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ChatUser user = getItem(position);
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.row_user, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}


		holder.setUser(user.getName());

		return convertView;
	}

	public void setData(ChatUser[] users){
		this.users = users;
	}


	static class ViewHolder {
		@Bind(R.id.tvUser)
		TextView tvUser;

		ViewHolder(View view) {
			ButterKnife.bind(this, view);

		}

		public void setUser(String username){
			tvUser.setText(username);
		}
	}
}
