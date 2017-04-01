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


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.slx.funstream.App;
import com.slx.funstream.model.ChatMessage;
import com.slx.funstream.model.Message;
import com.slx.funstream.ui.chat.ChatFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Flowable;

public class ChatService extends Service {
	private static final String TAG = "ChatService";
	private LocalBinder mLocalBinder = new LocalBinder();

	@Inject
    ChatServiceController controller;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
        AndroidInjection.inject(this);
		controller.setService(this);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		long channelId = -1;

		if (intent.hasExtra(ChatFragment.CHANNEL_ID)) {
			channelId = intent.getLongExtra(ChatFragment.CHANNEL_ID, -1);

            connectChat(channelId);
            joinChannel(channelId);
		} else {
            Log.e(TAG, "onStartCommand no channel id to connect, stop service");
            stopSelf();
        }

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mLocalBinder;
	}

	@Override
	public void onDestroy() {
		disconnectChat();
		controller.setService(null);
	}

    public void connectChat(long channelId) {
        controller.connect(channelId);
    }

    public void disconnectChat() {
        controller.disconnect();
    }

    public void joinChannel(long channel) {
        controller.joinChannel(channel);
    }

    public void leaveChannel(long channel) {
        controller.leaveChannel(channel);
    }

    public void sendMessage(ChatMessage newMessage) {
        controller.sendMessage(newMessage);
    }

    public void loginChat() {
        controller.loginChat();
    }

	public Flowable<Message> getChatMessagesObservable() {
        return controller.getChatMessagesObservable();
	}

    public void loadHistory() {
        controller.loadHistory();
    }

    public class LocalBinder extends Binder {
		public ChatService getService() {
			return ChatService.this;
		}
	}
}
