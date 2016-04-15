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


import com.slx.funstream.utils.TextUtils;

public class ChatMessage {
	private long id;
	private String channel;
	private ChatUser from;
	private ChatUser to;
	private String text;
	private String time;
	private String type;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ChatUser getFrom() {
		return from;
	}

	public void setFrom(ChatUser from) {
		this.from = from;
	}

	public ChatUser getTo() {
		return to;
	}

	public void setTo(ChatUser to) {
		this.to = to;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(long channelId) {
		this.channel = TextUtils.setChatChannel(channelId);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ChatMessage() {
	}

	@Override
	public String toString() {
		return "ChatMessage{" +
				"text='" + text + '\'' +
				", type='" + type + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ChatMessage that = (ChatMessage) o;

		return id == that.id;

	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}
}
