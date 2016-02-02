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


import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.slx.funstream.model.ChatMessage;
import com.slx.funstream.rest.model.CurrentUser;

import java.lang.reflect.Type;

public class ChatMessageDeserializer implements JsonDeserializer<ChatMessage[]> {

	private static final String ID = "id";
	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String CHANNEL = "channel";
	private static final String TEXT = "text";
	private static final String TIME = "time";

	@Override
	public ChatMessage[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		if(json.isJsonArray()){
			JsonArray jsonMessages = json.getAsJsonArray();
			ChatMessage[] messages = new ChatMessage[jsonMessages.size()];

			for(int i = 0; i < jsonMessages.size(); i++){
				JsonObject jsonMessage = jsonMessages.get(i).getAsJsonObject();
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.setId(jsonMessage.get(ID).getAsLong());
				chatMessage.setChannel(jsonMessage.get(CHANNEL).getAsLong());
				chatMessage.setFrom(context.deserialize(jsonMessage.get(FROM).getAsJsonObject(), CurrentUser.class));
				chatMessage.setTo(context.deserialize(jsonMessage.get(TO).getAsJsonObject(), CurrentUser.class));
				chatMessage.setText(jsonMessage.get(TEXT).getAsString());
				chatMessage.setTime(jsonMessage.get(TIME).getAsString());
				messages[i] = chatMessage;
			}
			return messages;
		}
		return null;

	}
}
