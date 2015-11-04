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


public class Smile {
	/*
	    {
            "code": "peka",
            "image": "mini-happy.png?3",
            "position":
            usable: <bool> smile can be posted by user,
            width: <int> width of the smile,
            height: <int> height of the smile,
        }
    */

	String code;
	String image;
//	int position;
	Boolean usable;
	Integer width;
	Integer height;

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getUsable() {
		return usable;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setUsable(Boolean usable) {
		this.usable = usable;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Smile() {
	}

//	public Smile(String code, String image, int position) {
//		this.code = code;
//		this.image = image;
//		this.position = position;
//	}

	public Smile(String code, String image) {
		this.code = code;
		this.image = image;
	}

	@Override
	public String toString() {
		return "Smile{" +
				"code='" + code + '\'' +
				'}';
	}
}
