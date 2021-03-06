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

package com.slx.funstream.utils;


import com.slx.funstream.chat.ChatApiUtils;
import com.slx.funstream.model.ChatUser;

public final class TextUtils {

	public static String makeFrom(ChatUser user){
		//return user.getName()+':';
		return user.getName();
	}
	public static String makeTo(ChatUser user){
		return user.getName()+',';
	}

	public static String setChatChannel(long channelId){
		if(channelId == ChatApiUtils.CHANNEL_MAIN)
			return ChatApiUtils.CHAT_CHANNEL_MAIN;
		else if (channelId == ChatApiUtils.CHANNEL_ADMIN)
			return ChatApiUtils.CHAT_CHANNEL_ADMIN;
		else
			return ChatApiUtils.CHAT_CHANNEL_STREAM + String.valueOf(channelId);
	}

	private TextUtils() {
	}
}
