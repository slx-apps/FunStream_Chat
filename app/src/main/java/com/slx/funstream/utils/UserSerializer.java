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
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.slx.funstream.rest.model.CurrentUser;

import java.lang.reflect.Type;

public class UserSerializer implements JsonSerializer<CurrentUser>, JsonDeserializer<CurrentUser> {
	public static final String USER_ID = "id";
	public static final String USER_NAME = "name";
	public static final String USER_COLOR = "namecolor";

	@Override
	public JsonElement serialize(CurrentUser user, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jUser = new JsonObject();
		jUser.addProperty(USER_ID, user.getId());
		jUser.addProperty(USER_NAME, user.getName());

		return jUser;
	}

	@Override
	public CurrentUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonUser = json.getAsJsonObject();
		if(jsonUser.isJsonNull()) return null;
		CurrentUser user = new CurrentUser();
		user.setId(jsonUser.get(USER_ID).getAsLong());
		user.setName(jsonUser.get(USER_NAME).getAsString());

		return user;
	}
}
