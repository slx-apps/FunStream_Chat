package com.slx.funstream.chat;


import android.util.Log;

import com.google.gson.Gson;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.chat.events.ChatErrorEvent;
import com.slx.funstream.chat.events.ChatUserListEvent;
import com.slx.funstream.model.ChatChannelRequest;
import com.slx.funstream.model.ChatHistoryRequest;
import com.slx.funstream.model.ChatListResponse;
import com.slx.funstream.model.ChatMessage;
import com.slx.funstream.model.ChatResponse;
import com.slx.funstream.rest.APIUtils;
import com.slx.funstream.rest.model.CurrentUser;
import com.slx.funstream.utils.PrefUtils;
import com.slx.funstream.utils.RxBus;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_JOIN;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_LEAVE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_LOGIN;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_MESSAGE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_NEW_MESSAGE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_HISTORY;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_LOGIN_TOKEN;
import static com.slx.funstream.chat.ChatApiUtils.DEFAULT_AMOUNT_MESSAGES;

public class ChatServicePresenter implements Presenter<ChatService> {
    private static final String TAG = "ChatServicePresenter";

    public static final int DEFAULT_CHANNEL_ID = -9999;
    private long currentChannelId = DEFAULT_CHANNEL_ID;

    private PrefUtils prefUtils;
    private UserStore userStore;
    private Gson gson;

    private ChatService service;
    private IO.Options opts;
    private Socket mSocket;
    private BehaviorSubject<ChatMessage> chatMessagesObservable = BehaviorSubject.create();
    private RxBus rxBus;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription userStoreSubscription;

    @Inject
    public ChatServicePresenter(RxBus rxBus, PrefUtils prefUtils, UserStore userStore, Gson gson) {
        this.prefUtils = prefUtils;
        this.userStore = userStore;
        this.gson = gson;
        this.rxBus = rxBus;
    }

    @Override
    public void setView(ChatService service) {
        Log.d(TAG, "setView");
        if (service != null) {
            this.service = service;
            userStoreSubscription = userStore
                    .getUser()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<CurrentUser>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "ChatServicePresenter->setView->onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "ChatServicePresenter->setView->onError " + e);
                        }

                        @Override
                        public void onNext(CurrentUser user) {
                            Log.d(TAG, "ChatServicePresenter->setView->onNext");
                            // Login if connected
                            if (user != null && mSocket != null) {
                                loginChat();
                            }
                        }
                    });
        } else {
            this.service = null;
            if (userStoreSubscription != null && !userStoreSubscription.isUnsubscribed())
                userStoreSubscription.unsubscribe();
        }
    }

    public BehaviorSubject<ChatMessage> getChatMessagesObservable() {
        return chatMessagesObservable;
    }

    public void connect(long channelId) {
        Log.d(TAG, "connect channelId="+channelId);
        if (currentChannelId == channelId) return;//TODO check connected
        this.currentChannelId = channelId;
        openSocket();
        if (mSocket != null) {
            mSocket.connect();
            subscribeEvents();
        }

        if (userStore.getCurrentUser() != null) loginChat();

        subscriptions.add(Observable
                .interval(0, 1, TimeUnit.MINUTES)
                .subscribe(timer -> getUserIds(this.currentChannelId)));
    }

    public void disconnect() {
        Log.d(TAG, "disconnect");
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            unSubscribeEvents();
            closeSocket();
        }
        subscriptions.unsubscribe();
    }

    public void joinChannel(long channel) {
        Log.d(TAG, "joinChannel: channel="+channel);
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
            Log.e(TAG, "joinChannel: e=" + e.toString());
            e.printStackTrace();
        }
    }

    public void leaveChannel(long channel) {
        Log.d(TAG, "leaveChannel: channel="+channel);
        try {
            JSONObject leaveJsonObject = new JSONObject(gson.toJson(new ChatChannelRequest(channel)));
            mSocket.emit(CHAT_EVENT_LEAVE, leaveJsonObject, (Ack) args -> {
                ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
                if (chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)) {
                    notifyListenersError(chatResponse.getResult().getMessage());
                } else{
                    loadChatHistory(currentChannelId);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "leaveChannel: e=" + e.toString());
            e.printStackTrace();
        }
        subscriptions.unsubscribe();
    }

    public void sendMessage(ChatMessage newMessage) {
        if (mSocket == null) return;
        try {
            mSocket.emit(CHAT_EVENT_NEW_MESSAGE, new JSONObject(gson.toJson(newMessage)), (Ack) args -> {
                ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
                if (chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)){
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
        if (mSocket != null) {
            try {
                JSONObject login = new JSONObject();
                login.put(CHAT_LOGIN_TOKEN, userStore.getCurrentUser().getToken());
                mSocket.emit(CHAT_EVENT_LOGIN, login, (Ack) args -> {
                    ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
                    if (chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)){
                        notifyListenersError(chatResponse.getResult().getMessage());
                    }
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
        opts.transports = new String[] { WebSocket.NAME };
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
        if(mSocket != null){
            mSocket.close();
        }
    }

    private void subscribeEvents() {
        mSocket.on(CHAT_EVENT_MESSAGE, onNewMessage);
        //mSocket.on(CHAT_EVENT_REMOVE_MESSAGE, onRemoveMessage);

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
        //mSocket.off(CHAT_EVENT_REMOVE_MESSAGE, onRemoveMessage);
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

//    // Chat server events listeners
//    private Emitter.Listener onRemoveMessage = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            final ChatMessage removeMessage = gson.fromJson(String.valueOf(args[0]), ChatMessage.class);
//            //removeMessage(removeMessage);
//        }
//    };

    private Emitter.Listener onNewMessage = args -> {
        Object message = args[0];
        if (message != null) {
            Observable.just(args[0])
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(obj -> String.valueOf(obj))
                    .map(jsonString -> gson.fromJson(jsonString, ChatMessage.class))
                    // TODO fix duplicate message
                    //.filter(chatMessage -> chatMessage.getType().equals(TYPE_MESSAGE) && !mMessages.contains(message))
                    .distinct()
                    .subscribe(new Subscriber<ChatMessage>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            notifyListenersError("Can't parse message");
                        }

                        @Override
                        public void onNext(ChatMessage chatMessage) {
                            chatMessagesObservable.onNext(chatMessage);
                        }
                    });
        } else {
            Log.e(TAG, "onNewMessage args are null");
        }
    };

    private Emitter.Listener onError = args -> {
        Log.d(TAG, "onError");
        dumpArgs(args);
    };

    private Emitter.Listener onConnectError = args -> {
        Log.d(TAG, "onConnectError");
        dumpArgs(args);
    };

    private Emitter.Listener onReconnectError = args -> {
        Log.d(TAG, "onReconnectError");
        dumpArgs(args);
    };

    private Emitter.Listener onReconnect = args -> {
        Log.d(TAG, "onReconnect");
        joinChannel(currentChannelId);
        loginChat();
    };

    private Emitter.Listener onConnect = args -> Log.i(TAG, "onConnect");

    public void getUserIds(Long channelId) {
        if (mSocket != null) {
            try {
                mSocket.emit(ChatApiUtils.CHAT_USER_LIST,
                        new JSONObject(gson.toJson(new ChatChannelRequest(channelId))), (Ack) args -> {
                    ChatListResponse chatListResponse = gson.fromJson(args[0].toString(), ChatListResponse.class);
                    rxBus.send(new ChatUserListEvent(chatListResponse.getResult().getUsers()));
                    if (chatListResponse.getStatus().equals(APIUtils.ERROR_STATUS)) {
                        notifyListenersError(chatListResponse.getResult().getMessage());
                    } else {
                        notifyListenersUserListLoaded(chatListResponse.getResult().getUsers());
                    }

                });
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    private void notifyListenersError(String message) {
        rxBus.send(new ChatErrorEvent(message));
    }

    private void notifyListenersUserListLoaded(Integer[] users) {
        rxBus.send(new ChatUserListEvent(users));
    }

    private void loadChatHistory(long channel) {
        Log.d(TAG, "loadChatHistory: channel="+channel);
        ChatHistoryRequest chatHistoryRequest = new ChatHistoryRequest();
        chatHistoryRequest.setAmount(DEFAULT_AMOUNT_MESSAGES);
        chatHistoryRequest.setChannel(channel);
        String req = gson.toJson(chatHistoryRequest);

        try {
            mSocket.emit(CHAT_HISTORY,  new JSONObject(req), (Ack) args -> {
                ChatResponse chatResponse = gson.fromJson(String.valueOf(args[0]), ChatResponse.class);
                List<ChatMessage> list = chatResponse.getResult().getChatMessages();
            });
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
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


}
