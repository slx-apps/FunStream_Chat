package com.slx.funstream.chat;


import android.util.Log;

import com.google.gson.Gson;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.events.ChatErrorEvent;
import com.slx.funstream.model.ChatChannelRequest;
import com.slx.funstream.model.ChatHistoryRequest;
import com.slx.funstream.model.ChatListResponse;
import com.slx.funstream.model.ChatListResult;
import com.slx.funstream.model.ChatMessage;
import com.slx.funstream.model.ChatResponse;
import com.slx.funstream.model.Message;
import com.slx.funstream.rest.APIUtils;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.utils.PrefUtils;
import com.slx.funstream.utils.RxBus;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_JOIN;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_LEAVE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_LOGIN;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_MESSAGE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_NEW_MESSAGE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_HISTORY;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_LOGIN_TOKEN;
import static com.slx.funstream.chat.ChatApiUtils.DEFAULT_AMOUNT_MESSAGES;

public class ChatServiceController {
    private static final String TAG = "ChatServiceController";

    public static final int MESSAGE_TYPE_SYSTEM = 1;
    public static final int MESSAGE_TYPE_CHAT = 0;

    private static final int DEFAULT_CHANNEL_ID = -9999;
    private static final int INITIAL_DELAY = 0;
    private static final int PERIOD_IN_MIN = 1;
    private long currentChannelId = DEFAULT_CHANNEL_ID;

    private PrefUtils prefUtils;
    private UserStore userStore;
    private Gson gson;

    private ChatService service;
    private IO.Options opts;
    private Socket mSocket;
    private Relay<Message> messagesRelay = PublishRelay.<Message>create().toSerialized();
    private RxBus rxBus;
    private CompositeDisposable subscriptions = new CompositeDisposable();
    private CurrentUser user;

    public ChatServiceController(RxBus rxBus, PrefUtils prefUtils, UserStore userStore, Gson gson) {
        this.prefUtils = prefUtils;
        this.userStore = userStore;
        this.gson = gson;
        this.rxBus = rxBus;
    }

    public void setService(ChatService service) {
        Log.d(TAG, "setService");
        subscriptions.clear();
        if (service != null) {
            this.service = service;
            DisposableSubscriber disposableSubscriber = new DisposableSubscriber<CurrentUser>() {

                @Override
                public void onNext(CurrentUser currentUser) {
                    user = currentUser;
                    Log.d(TAG, "ChatServiceController->setView->onNext " + currentUser);
                    loginChat();
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    Log.d(TAG, "UserStore->setView->onComplete");
                }
            };

            userStore.userObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(disposableSubscriber);

            subscriptions.add(disposableSubscriber);
        } else {
            this.service = null;
            subscriptions.clear();
        }
    }

    public Flowable<Message> getChatMessagesObservable() {
        return messagesRelay.toFlowable(BackpressureStrategy.BUFFER);
    }

    public void connect(long channelId) {
        Log.d(TAG, "connect channelId " + channelId);
        if (currentChannelId == channelId) {
            Log.d(TAG, "Already connected");
            return;
        }

        this.currentChannelId = channelId;
        openSocket();
        if (mSocket != null) {
            mSocket.connect();
            subscribeEvents();
        }

        DisposableSubscriber disposableSubscriber = new DisposableSubscriber<Long>() {

            @Override
            public void onNext(Long aLong) {
                getUserIds(currentChannelId);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        };


        Flowable.interval(INITIAL_DELAY, PERIOD_IN_MIN, TimeUnit.MINUTES)
                .subscribe(disposableSubscriber);

        subscriptions.add(disposableSubscriber);
    }

    public void disconnect() {
        Log.d(TAG, "disconnect");
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            unSubscribeEvents();
            closeSocket();
        }
        if (subscriptions != null && !subscriptions.isDisposed()) {
            subscriptions.clear();
        }
    }

    public void joinChannel(long channel) {
        Log.d(TAG, "joinChannel: channel=" + channel);
        currentChannelId = channel;
        if (mSocket == null) return;

        try {
            JSONObject join = new JSONObject(gson.toJson(new ChatChannelRequest(currentChannelId)));
            mSocket.emit(CHAT_EVENT_JOIN, join, (Ack) args -> {
                ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
                if (chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)) {
                    notifyListenersError(chatResponse.getResult().getMessage());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void leaveChannel(long channel) {
        Log.d(TAG, "leaveChannel: channel " + channel);
        try {
            JSONObject leaveJsonObject = new JSONObject(gson.toJson(new ChatChannelRequest(channel)));
            mSocket.emit(CHAT_EVENT_LEAVE, leaveJsonObject, (Ack) args -> {
                ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
                if (chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)) {
                    notifyListenersError(chatResponse.getResult().getMessage());
                } else {
                    loadChatHistory(currentChannelId);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (subscriptions != null && !subscriptions.isDisposed()) {
            subscriptions.clear();
        }
    }

    public void sendMessage(ChatMessage newMessage) {
        Log.d(TAG, "sendMessage: " + newMessage);
        if (mSocket == null) return;
        try {
            mSocket.emit(CHAT_EVENT_NEW_MESSAGE, new JSONObject(gson.toJson(newMessage)), (Ack) args -> {
                ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
                if (chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)) {
                    notifyListenersError(chatResponse.getResult().getMessage());
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public void loginChat() {
        Log.d(TAG, "loginChat");
        if (mSocket != null && user != null && user.getToken() != null) {
            try {
                JSONObject login = new JSONObject();
                login.put(CHAT_LOGIN_TOKEN, user.getToken());
                mSocket.emit(CHAT_EVENT_LOGIN, login, (Ack) args -> {
                    ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
                    Log.d(TAG, "loginChat: " + chatResponse);
                    if (chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)) {
                        notifyListenersError(chatResponse.getResult().getMessage());
                    } else {
                        Log.d(TAG, "loginChat: " + chatResponse.getStatus());
                    }

                    Message message = buildSystemMessage("Logged in");
                    messagesRelay.accept(message);
                });
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    public void loadHistory() {
        if (mSocket != null) {
            loadChatHistory(currentChannelId);
        }
    }

    private void openSocket() {
        try {
            createSocketOptions();
            mSocket = IO.socket(ChatApiUtils.CHAT_URL, opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    private void createSocketOptions() {
        // Create connection options
        opts = new IO.Options();
        opts.transports = new String[]{WebSocket.NAME};
        opts.secure = true;
        opts.sslContext = createSSLContext();
        opts.forceNew = true;

//        IO.setDefaultSSLContext(sslContext);
//        IO.setDefaultHostnameVerifier((s, sslSession) -> true);
    }

    private SSLContext createSSLContext() {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, null);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sc;
//		return SSLContext.getDefault();
    }

    private void closeSocket() {
        if (mSocket != null) {
            mSocket.close();
        }
    }

    private void subscribeEvents() {
        mSocket.on(CHAT_EVENT_MESSAGE, onNewMessage);

        mSocket.on(Socket.EVENT_ERROR, onError);

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

        mSocket.on(Socket.EVENT_RECONNECT_ERROR, onError);
        mSocket.on(Socket.EVENT_RECONNECT_FAILED, onError);

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_RECONNECT, onReconnect);

//		mSocket.on(Socket.EVENT_RECONNECT_ATTEMPT, onError);
//		mSocket.on(Socket.EVENT_RECONNECTING, onError);
    }

    private void unSubscribeEvents() {
        mSocket.off(CHAT_EVENT_MESSAGE, onNewMessage);

        mSocket.off(Socket.EVENT_ERROR, onError);

        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);


        mSocket.off(Socket.EVENT_RECONNECT_ERROR, onReconnectError);
        mSocket.off(Socket.EVENT_RECONNECT_FAILED, onReconnectError);

        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_RECONNECT, onReconnect);
//		mSocket.off(Socket.EVENT_RECONNECT_ATTEMPT, onError);
//		mSocket.off(Socket.EVENT_RECONNECTING, onError);
    }

    private Emitter.Listener onNewMessage = args -> {
        Object message = args[0];
        if (message != null) {
            Single.just(args[0])
                    .subscribeOn(Schedulers.computation())
                    .map(obj -> String.valueOf(obj))
                    .map(jsonString -> gson.fromJson(jsonString, ChatMessage.class))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<ChatMessage>() {
                        @Override
                        public void onSuccess(ChatMessage chatMessage) {
                            Log.d(TAG, "onSuccess " + chatMessage);
                            chatMessage.setMessageType(MESSAGE_TYPE_CHAT);
                            messagesRelay.accept(chatMessage);
                        }

                        @Override
                        public void onError(Throwable e) {
                            notifyListenersError("Can't parse message");
                        }
                    });
        } else {
            Log.e(TAG, "onNewMessage args are null");
        }
    };

    private Emitter.Listener onError = args -> {
        Log.d(TAG, "onError");
        Message message = new Message();
        message.setMessageType(MESSAGE_TYPE_SYSTEM);
        message.setText("Error");

        messagesRelay.accept(message);
        dumpArgs(args);
    };

    private Emitter.Listener onConnectError = args -> {
        Log.d(TAG, "onConnectError");
        buildSystemMessage("Connect Error");

        dumpArgs(args);
    };

    private Emitter.Listener onReconnectError = args -> {
        Log.d(TAG, "onReconnectError");
        buildSystemMessage("Reconnect Error");

        dumpArgs(args);
    };

    private Emitter.Listener onReconnect = args -> {
        Log.d(TAG, "onReconnect");
        buildSystemMessage("Reconnecting");

        joinChannel(currentChannelId);
        loginChat();
    };

    private Emitter.Listener onConnect = args -> {
        Log.d(TAG, "onConnect");

        Message message = buildSystemMessage("Connected");

        messagesRelay.accept(message);
    };

    public void getUserIds(Long channelId) {
        Log.d(TAG, "getUserIds " + channelId);
        if (mSocket != null) {
            try {
                mSocket.emit(ChatApiUtils.CHAT_USER_LIST,
                        new JSONObject(gson.toJson(new ChatChannelRequest(channelId))), (Ack) args -> {
                            ChatListResponse chatListResponse = gson.fromJson(args[0].toString(), ChatListResponse.class);

                            if (chatListResponse.getStatus().equals(APIUtils.ERROR_STATUS)) {
                                notifyListenersError(chatListResponse.getResult().getMessage());
                            } else {
                                notifyListenersUserListLoaded(chatListResponse.getResult());
                            }

                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyListenersError(String message) {
        Log.d(TAG, "notifyListenersError: " + message);
        rxBus.send(new ChatErrorEvent(message));
    }

    private void notifyListenersUserListLoaded(ChatListResult chatListResult) {
        //Log.d(TAG, "notifyListenersUserListLoaded: " + users.length);
        rxBus.send(chatListResult);
    }

    private void loadChatHistory(long channel) {
        Log.d(TAG, "loadChatHistory: channel " + channel);
        ChatHistoryRequest chatHistoryRequest = new ChatHistoryRequest();
        chatHistoryRequest.setAmount(DEFAULT_AMOUNT_MESSAGES);
        chatHistoryRequest.setChannel(channel);
        String req = gson.toJson(chatHistoryRequest);

        try {
            mSocket.emit(CHAT_HISTORY, new JSONObject(req), (Ack) args -> {
                ChatResponse chatResponse = gson.fromJson(String.valueOf(args[0]), ChatResponse.class);
                List<ChatMessage> list = chatResponse.getResult().getChatMessages();
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void dumpArgs(Object[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Log.e(TAG, args[i].toString());
            }
        }
    }

    private Message buildSystemMessage(String text) {
        Message message = new Message();
        message.setMessageType(MESSAGE_TYPE_SYSTEM);
        message.setText(text);

        return message;
    }
}
