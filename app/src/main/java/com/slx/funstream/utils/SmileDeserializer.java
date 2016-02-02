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
import com.slx.funstream.rest.model.Smile;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class SmileDeserializer implements JsonDeserializer<Map<String, Smile>> {
	public static final String SMILE_ID = "id";
	public static final String SMILE_TAB = "tab";
	public static final String SMILE_LEVEL = "level";
	public static final String SMILE_CODE = "code";
	public static final String SMILE_URL = "url";
//	public static final String SMILE_POS = "position";
	public static final String SMILE_HEIGHT = "width";
	public static final String SMILE_WIDTH = "height";


	private static final CharSequence PATTERN_COLOMN = ":";
	@Override
	public Map<String, Smile> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Map<String, Smile> smileys = new HashMap<>();
		JsonArray smileysJson = json.getAsJsonArray();

		for(int i = 0; i < smileysJson.size(); i++) {
			JsonObject smileyJson = smileysJson.get(i).getAsJsonObject();
			Smile smile = new Smile();
			smile.setId(smileyJson.get(SMILE_ID).getAsInt());
			smile.setTab(smileyJson.get(SMILE_TAB).getAsInt());
			smile.setLevel(smileyJson.get(SMILE_LEVEL).getAsInt());
			smile.setHeight(smileyJson.get(SMILE_HEIGHT).getAsInt());
			smile.setCode(smileyJson.get(SMILE_CODE).getAsString());
			smile.setUrl(smileyJson.get(SMILE_URL).getAsString());
//			smile.setPosition(smileJson.get(SMILE_POS).getAsInt());
			smile.setHeight(smileyJson.get(SMILE_HEIGHT).getAsInt());
			smile.setWidth(smileyJson.get(SMILE_WIDTH).getAsInt());

			smileys.put(PATTERN_COLOMN + smile.getCode() + PATTERN_COLOMN, smile);
		}

		return smileys;
	}
}
