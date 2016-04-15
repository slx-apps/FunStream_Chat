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
import com.slx.funstream.ui.chat.ChatFragment;

import javax.inject.Inject;

import rx.subjects.BehaviorSubject;

public class ChatService extends Service implements ChatServiceInterface {
	private static final String TAG = "ChatService";
	private LocalBinder mLocalBinder = new LocalBinder();

	@Inject
    ChatServicePresenter presenter;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		App.initializeChatComponent().inject(this);
		presenter.setView(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		long channelId = -1;

		if (intent.hasExtra(ChatFragment.CHANNEL_ID)) {
			channelId = intent.getLongExtra(ChatFragment.CHANNEL_ID, -1);
		}
		connectChat(channelId);
		joinChannel(channelId);

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mLocalBinder;
	}

	@Override
	public void onDestroy() {
		disconnectChat();
		presenter.setView(null);
	}

    @Override
    public void connectChat(long channelId) {
        presenter.connect(channelId);
    }

    @Override
    public void disconnectChat() {
        presenter.disconnect();
    }

    @Override
    public void joinChannel(long channel) {
        presenter.joinChannel(channel);
    }

    @Override
    public void leaveChannel(long channel) {
        presenter.leaveChannel(channel);
    }

    @Override
    public void sendMessage(ChatMessage newMessage) {
        presenter.sendMessage(newMessage);
    }

    @Override
    public void loginChat() {
        presenter.loginChat();
    }

	@Override
	public BehaviorSubject<ChatMessage> getChatMessagesObservable() {
		return presenter.getChatMessagesObservable();
	}

    public void loadHistory() {
        presenter.loadHistory();
    }

    public class LocalBinder extends Binder {
		public ChatService getService() {
			return ChatService.this;
		}
	}
}
