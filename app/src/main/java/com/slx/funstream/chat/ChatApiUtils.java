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


public final class ChatApiUtils {

	private ChatApiUtils() {
	}
	// Обычное сообщение(включая другие стримсервисы)
	public static final String TYPE_MESSAGE = "message";
	// Системные сообщения
	public static final String TYPE_SYSTEM = "system";
	// Микродонат
	public static final String TYPE_FASTDONATE = "fastdonate";
	// Донат, на данный момент включает в себя и сообщения связанные с челенджами
	public static final String TYPE_DONATE = "donate";
	// Подписка на мастер стримера
	public static final String TYPE_SUBSCIBE = "subscribe";
	// Анонсы стримов
	public static final String TYPE_ANNOUNCE = "announce";


	public static final String CHAT_EVENT_MESSAGE = "/chat/message";
	public static final String CHAT_EVENT_REMOVE_MESSAGE = "/chat/message/remove";

	public static final String CHAT_EVENT_NEW_MESSAGE = "/chat/publish";
	public static final String CHAT_EVENT_JOIN = "/chat/join";
	public static final String CHAT_EVENT_LEAVE = "/chat/leave";
	public static final String CHAT_EVENT_LOGIN = "/chat/login";
	public static final String CHAT_HISTORY = "/chat/history";

	public static final String CHAT_LOGIN_TOKEN = "token";
	public static final String CHAT_CHANNEL = "channel";

	public static final  String CHAT_CHANNEL_STREAM  = "stream/";
	public static final String CHAT_CHANNEL_MAIN = "main";
	public static final String CHAT_CHANNEL_ADMIN = "admin";

	public static final String CHAT_URL = "http://funstream.tv";
	public static final String CHAT_WS_URL = "wss://funstream.tv/socket.io/?EIO=3&transport=websocket";

	public static final String CHAT_EVENT_USER_JOINED = "/chat/user/join";
	public static final String CHAT_EVENT_USER_LEAVED = "/chat/user/leave";

	public static final String CHAT_USER_LIST = "/chat/channel/list";

	public static final int DEFAULT_AMOUNT_MESSAGES = 100;
//	public static final String DEFAULT_DIRECTION_MESSAGES = "up";

	public static final long CHANNEL_MAIN = -1L;
	public static final long CHANNEL_ADMIN = -2L;
}
