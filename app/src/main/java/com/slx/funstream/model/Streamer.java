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

public class Streamer implements Parcelable {
	private long id;
	private String name;
	private String slug;

	public Streamer() {}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSlug() {
		return slug;
	}

    @Override
    public String toString() {
        return "Streamer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeString(this.name);
		dest.writeString(this.slug);
	}

	protected Streamer(Parcel in) {
		this.id = in.readLong();
		this.name = in.readString();
		this.slug = in.readString();
	}

	public static final Parcelable.Creator<Streamer> CREATOR = new Parcelable.Creator<Streamer>() {
		@Override
		public Streamer createFromParcel(Parcel source) {
			return new Streamer(source);
		}

		@Override
		public Streamer[] newArray(int size) {
			return new Streamer[size];
		}
	};
}
