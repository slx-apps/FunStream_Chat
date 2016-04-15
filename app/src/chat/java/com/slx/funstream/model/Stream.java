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


import com.google.gson.annotations.SerializedName;

public class Stream {

	@SerializedName("owner")
	private Streamer streamer;
	private String name;
	private Boolean adult;
	private int rating;

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

}
