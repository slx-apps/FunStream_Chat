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

package com.slx.funstream.rest.model;


import com.slx.funstream.model.ChatUser;

public class Smile {
	/*
	{
	    "id": 1,
	    "tab": 0,
	    "position": 0,
	    "width": 30,
	    "height": 30,
	    "code": "happy",
	    "url": "https://funstream.tv/build/images/smiles/happy.png",
	    "user": null,
	    "level": 0
	}
    */

	int id;
	int tab;
//	int position;
	int width;
	int height;
	String code;
	String url;
	int level;
	boolean animated;
	ChatUser user;

	public int getId() {
		return id;
	}

	public int getTab() {
		return tab;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getCode() {
		return code;
	}

	public String getUrl() {
		return url;
	}

	public int getLevel() {
		return level;
	}

	public ChatUser getUser() {
		return user;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setTab(int tab) {
		this.tab = tab;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setUser(ChatUser user) {
		this.user = user;
	}

	public boolean isAnimated() {
		return animated;
	}

	public void setAnimated(boolean animated) {
		this.animated = animated;
	}

	public Smile() {
	}

	@Override
	public String toString() {
		return "Smile{" +
				"code='" + code + '\'' +
				'}';
	}
}
