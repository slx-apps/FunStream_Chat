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

package com.slx.funstream.chat;


import com.slx.funstream.model.ChatMessage;

import org.json.JSONException;

import java.util.List;

import rx.subjects.BehaviorSubject;

public interface ChatServiceInterface {
	void connectChat(long channelId);
	void disconnectChat();
	void joinChannel(long channel);
	void leaveChannel(long channel);
	void sendMessage(ChatMessage newMessage);
	void loginChat();
	BehaviorSubject<ChatMessage> getChatMessagesObservable();
}
