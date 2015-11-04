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


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.slx.funstream.model.ChatMessage;
import com.slx.funstream.model.ChatResponse;
import com.slx.funstream.model.Result;
import com.slx.funstream.rest.APIUtils;

import java.lang.reflect.Type;
import java.util.Arrays;

public class ChatResponseDeserializer implements JsonDeserializer<ChatResponse> {
	private static final String STATUS = "status";
	private static final String RESULT = "result";
	private static final String MESSAGE = "message";

	@Override
	public ChatResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		JsonObject response = json.getAsJsonObject();
		final String status = response.get(STATUS).getAsString();
		ChatResponse chatResponse = new ChatResponse();
		chatResponse.setStatus(status);
		if(status.equals(APIUtils.OK_STATUS)){
			// messages
			if(response.has(RESULT) && response.get(RESULT).isJsonArray()){
				ChatMessage[] messages = context.deserialize(response.get(RESULT), ChatMessage[].class);
				chatResponse.setResult(new Result(Arrays.asList(messages)));
			}

		}else{
			JsonObject result = response.get(RESULT).getAsJsonObject();
			chatResponse.setResult(new Result(result.get(MESSAGE).getAsString()));
		}

		return chatResponse;
	}
}
