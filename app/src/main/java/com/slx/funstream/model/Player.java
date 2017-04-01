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

package com.slx.funstream.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {

	private String provider;
	private String channel;
	private Boolean active;
	private Boolean online;
	private int position;
	private String preview;
	private String url;

	public Player() {}

	public String getChannel() {
		return channel;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}


	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getOnline() {
		return online;
	}

	public void setOnline(Boolean online) {
		this.online = online;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.provider);
		dest.writeString(this.channel);
		dest.writeValue(this.active);
		dest.writeValue(this.online);
		dest.writeInt(this.position);
		dest.writeString(this.preview);
		dest.writeString(this.url);
	}

	protected Player(Parcel in) {
		this.provider = in.readString();
		this.channel = in.readString();
		this.active = (Boolean) in.readValue(Boolean.class.getClassLoader());
		this.online = (Boolean) in.readValue(Boolean.class.getClassLoader());
		this.position = in.readInt();
		this.preview = in.readString();
		this.url = in.readString();
	}

	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
		@Override
		public Player createFromParcel(Parcel source) {
			return new Player(source);
		}

		@Override
		public Player[] newArray(int size) {
			return new Player[size];
		}
	};
}
