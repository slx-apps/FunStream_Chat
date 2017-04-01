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

package com.slx.funstream.rest.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.slx.funstream.model.Player;
import com.slx.funstream.model.Streamer;

import java.util.List;

public class Stream implements Parcelable {

	@SerializedName("owner")
	private Streamer streamer;
	private String name;
	private Boolean adult;
	private int rating;
	private List<Player> players;

    public Stream() {}

	public Streamer getStreamer() {
		return streamer;
	}

	public String getName() {
		return name;
	}

	public Boolean isAdult() {
		return adult;
	}

	public int getRating() {
		return rating;
	}

	public List<Player> getPlayers() {
		return players;
	}


	@Override
	public String toString() {
		return "Stream{" +
				"name='" + name + '\'' +
				", rating=" + rating +
				'}';
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.streamer, flags);
		dest.writeString(this.name);
		dest.writeValue(this.adult);
		dest.writeInt(this.rating);
		dest.writeTypedList(this.players);
	}

	protected Stream(Parcel in) {
		this.streamer = in.readParcelable(Streamer.class.getClassLoader());
		this.name = in.readString();
		this.adult = (Boolean) in.readValue(Boolean.class.getClassLoader());
		this.rating = in.readInt();
		this.players = in.createTypedArrayList(Player.CREATOR);
	}

	public static final Parcelable.Creator<Stream> CREATOR = new Parcelable.Creator<Stream>() {
		@Override
		public Stream createFromParcel(Parcel source) {
			return new Stream(source);
		}

		@Override
		public Stream[] newArray(int size) {
			return new Stream[size];
		}
	};
}
