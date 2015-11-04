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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SmileDeserializer implements JsonDeserializer<List<Map<String, Smile>>> {
	public static final String SMILE_CODE = "code";
	public static final String SMILE_IMAGE = "image";
//	public static final String SMILE_POS = "position";
	public static final String SMILE_USAGE = "usable";
	public static final String SMILE_HEIGHT = "width";
	public static final String SMILE_WIDTH = "height";

	private static final CharSequence PATTERN_COLOMN = ":";
	@Override
	public List<Map<String, Smile>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		List<Map<String, Smile>> smileTabs = new ArrayList<>();
		JsonArray smileTabsJson = json.getAsJsonArray();

		for(int i = 0; i < smileTabsJson.size(); i++){
			JsonArray smileList = smileTabsJson.get(i).getAsJsonArray();
			Map<String, Smile> smiles = new HashMap<>();

			for(int j = 0; j < smileList.size(); j++){
				JsonObject smileJson = smileList.get(j).getAsJsonObject();
				Smile smile = new Smile();
				smile.setCode(smileJson.get(SMILE_CODE).getAsString());
				smile.setImage(smileJson.get(SMILE_IMAGE).getAsString());
//				smile.setPosition(smileJson.get(SMILE_POS).getAsInt());
				smile.setUsable(smileJson.get(SMILE_USAGE).getAsBoolean());
				smile.setHeight(smileJson.get(SMILE_HEIGHT).getAsInt());
				smile.setWidth(smileJson.get(SMILE_WIDTH).getAsInt());

				smiles.put(PATTERN_COLOMN+smile.getCode()+PATTERN_COLOMN, smile);
			}

			smileTabs.add(smiles);
		}

		return smileTabs;
	}
}
