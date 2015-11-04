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


public class OAuthResponse {
	String code;
	String token;
	String message;
	CurrentUser user;


	public String getCode() {
		return code;
	}

	public OAuthResponse(String code) {
		this.code = code;
	}

	public String getToken() {
		return token;
	}

	public String getMessage() {
		return message;
	}

	public CurrentUser getUser() {
		return user;
	}

	public OAuthResponse() {
	}

	@Override
	public String toString() {
		return "OAuthResponse{" +
				"code='" + code + '\'' +
				", token='" + token + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}
