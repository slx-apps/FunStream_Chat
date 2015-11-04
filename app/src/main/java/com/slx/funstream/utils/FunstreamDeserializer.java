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


import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class FunstreamDeserializer<T> implements JsonDeserializer<T> {
	private static final String CONTENT_ROOT = "content";
	@Override
	public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException
	{
		// Get the "content" element from the parsed JSON
		JsonElement content = je.getAsJsonObject().get(CONTENT_ROOT);

		return new Gson().fromJson(content, type);
	}
}
