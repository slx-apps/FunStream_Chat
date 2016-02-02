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

import com.google.gson.Gson;
import com.slx.funstream.R;
import com.slx.funstream.auth.UserStore;
import com.slx.funstream.auth.UserStore.UserStoreListener;
import com.slx.funstream.dagger.Injector;
import com.slx.funstream.model.ChatChannelRequest;
import com.slx.funstream.model.ChatHistoryRequest;
import com.slx.funstream.model.ChatListResponse;
import com.slx.funstream.model.ChatMessage;
import com.slx.funstream.model.ChatResponse;
import com.slx.funstream.rest.APIUtils;
import com.slx.funstream.ui.chat.ChatFragment;
import com.slx.funstream.utils.ChatMessageComparator;
import com.slx.funstream.utils.LogUtils;
import com.slx.funstream.utils.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import hugo.weaving.DebugLog;
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
import rx.subjects.PublishSubject;

import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_JOIN;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_LEAVE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_LOGIN;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_MESSAGE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_NEW_MESSAGE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_EVENT_REMOVE_MESSAGE;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_HISTORY;
import static com.slx.funstream.chat.ChatApiUtils.CHAT_LOGIN_TOKEN;
import static com.slx.funstream.chat.ChatApiUtils.DEFAULT_AMOUNT_MESSAGES;
import static com.slx.funstream.chat.ChatApiUtils.TYPE_MESSAGE;

public class ChatService extends Service implements ChatServiceInterface {
	private static final int DEFAULT_CHANNEL_ID = -9;
	private IO.Options opts;
	private Socket mSocket;

	private List<FunstreamChatEventsListener> listeners = new ArrayList<>();
	private LocalBinder mLocalBinder = new LocalBinder();
	private List<ChatMessage> mMessages = new CopyOnWriteArrayList<>();
	private long currChannelId = DEFAULT_CHANNEL_ID;
	private String mToken;

	@Inject
	PrefUtils prefUtils;
	@Inject
	UserStore userStore;
	@Inject
	Gson gson;

	PublishSubject<String> chatMessages = PublishSubject.create();
	Subscription messageSub;
	Subscription checkUserList;
	@DebugLog
	@Override
	public void onCreate() {
		//Log.i(LogUtils.TAG, "ChatService->onCreate");

		Injector.INSTANCE.getApplicationComponent().inject(this);

//		// Obtain sslcontext
//		SSLContext sslContext = null;
//		try {
//			sslContext = createSSLContext();
//		} catch (GeneralSecurityException e) {
//			Log.e(LogUtils.TAG, e.toString());
//			e.printStackTrace();
//		}



		SSLContext sslContext = null;
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[]{};
			}

			public void checkClientTrusted(X509Certificate[] chain,
			                               String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
			                               String authType) throws CertificateException {
			}
		}};

		KeyManager[] keyManager = null;
		try {
			final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			factory.init(keyStore, null);
			keyManager = factory.getKeyManagers();
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
			e.printStackTrace();
		}

		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManager, trustAllCerts, new java.security.SecureRandom());
			//HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}

		IO.setDefaultSSLContext(sslContext);
		IO.setDefaultHostnameVerifier((s, sslSession) -> true);

		// Create connection options
		opts = new IO.Options();
		opts.transports = new String[] { WebSocket.NAME };
		opts.secure = true;
		opts.sslContext = sslContext;
		opts.forceNew = true;
		messageSub = chatMessages
				.map(jsonString -> gson.fromJson(jsonString, ChatMessage.class))
				// TODO fix duplicate message workaround
				// В данный момент из-за синхронизации и нек-х упрощений со стороны сервера,
				// на каждое сообщение может приходить 2 таких события с одинаковыми данными.
				// Для защиты от дубликатов фильтруйте данные по идентификатору сообщения.
				// После правки способа синхронизации с чатом ск2тв эта проблема пропадёт.
				.filter(message -> message.getType().equals(TYPE_MESSAGE) && !mMessages.contains(message))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(
					new Subscriber<ChatMessage>() {
						@Override
						public void onCompleted() {
							Log.i(LogUtils.TAG, "chatMessages onCompleted");
						}

						@Override
						public void onError(Throwable e) {
							Log.e(LogUtils.TAG, e.toString());
						}

						@Override
						public void onNext(ChatMessage chatMessage) {
							mMessages.add(chatMessage);
							notifyListenersNewMessage();
						}
					});

		userStore.registerUserStoreListener(userStoreListener);

	}


//	// in case if funstream switch to self-signed certificate
//	// certificate
//	SSLContext createSSLContext() throws GeneralSecurityException, IOException {
//        //KeyStore ks = KeyStore.getInstance("JKS");
//		// Load CAs from
//		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//		Certificate certificate = certificateFactory.generateCertificate(
//					getResources().openRawResource(R.raw.funstream));//funstream.tv.crt
//
//
//		// Create a KeyStore containing the trusted CAs.
////		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//		KeyStore keyStore = KeyStore.getInstance("JKS");
//		keyStore.load(null, null);
//		keyStore.setCertificateEntry("ca", certificate);
//
//		// Create a TrustManager that trusts the CAs in KeyStore.
//		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
//				//TrustManagerFactory.getDefaultAlgorithm());
//		trustManagerFactory.init(keyStore);
//
////         File file = new File("src/test/resources/keystore.jks");
//				//ks.load(new FileInputStream(file), "password".toCharArray());
//
//
////		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
////         kmf.init(ks, "password".toCharArray());
////
////
////         TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
////         tmf.init(ks);
//
//
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//		sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
////         sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//         return sslContext;
//	}

	SSLContext createSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, null, null);
		return sc;
//		return SSLContext.getDefault();
	}


	@DebugLog
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LogUtils.TAG, "ChatService->onStartCommand");
		if(intent.hasExtra(ChatFragment.CHANNEL_ID)){
			long channelId = intent.getLongExtra(ChatFragment.CHANNEL_ID, -1);
			if(channelId == currChannelId) return START_NOT_STICKY;
			else currChannelId = channelId;
		}

		connectChat();
		joinChannel(currChannelId);
		if(checkUserList != null && !checkUserList.isUnsubscribed()){
			checkUserList.unsubscribe();
		}
		checkUserList = Observable
				.interval(0, 2, TimeUnit.MINUTES)
				.subscribe(aLong -> getUserIds(currChannelId));

		if(userStore.isUserLoggedIn()){
			mToken = userStore.getCurrentUser().getToken();
			loginChat();
		}

		return START_NOT_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return mLocalBinder;
	}

	@Override
	public void onDestroy() {
		Log.i(LogUtils.TAG, "ChatService->onDestroy");
		userStore.unregisterUserStoreListener(userStoreListener);
		disconnectChat();
		if(messageSub != null && !messageSub.isUnsubscribed()){

			messageSub.unsubscribe();
		}
		if(checkUserList != null && !checkUserList.isUnsubscribed()){
			checkUserList.unsubscribe();
		}

	}

	public class LocalBinder extends Binder {
		public ChatService getService() {
			return ChatService.this;
		}
	}

	private void openSocket(){
		try {
			mSocket = IO.socket(ChatApiUtils.CHAT_URL, opts);
		} catch (URISyntaxException e) {
			// Syntax error, probably internal error
			Log.e(LogUtils.TAG, e.toString());
		}
	}
	private void closeSocket(){
		if(mSocket != null){
			mSocket.close();
		}
	}

	@DebugLog
	public void connectChat(){
		openSocket();
		if(mSocket != null){
			mSocket.connect();
			subscribeEvents();
		}
	}
	@DebugLog
	public void disconnectChat(){
		if(mSocket == null) return;
		if(mSocket.connected()){
			mSocket.disconnect();
			unsubscribeEvents();
			closeSocket();
		}
	}

	private void subscribeEvents() {
		mSocket.on(CHAT_EVENT_MESSAGE, onNewMessage);
		mSocket.on(CHAT_EVENT_REMOVE_MESSAGE, onRemoveMessage);

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
	private void unsubscribeEvents() {
		mSocket.off(CHAT_EVENT_REMOVE_MESSAGE, onRemoveMessage);
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

	public void joinChannel(long chan){
		currChannelId = chan;
		if(mSocket == null) return;

		try {
			JSONObject join = new JSONObject(gson.toJson(new ChatChannelRequest(chan)));
			mSocket.emit(CHAT_EVENT_JOIN, join, (Ack) args -> {
				ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
				if(chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)){
					notifyListenersError(chatResponse.getResult().getMessage());
				}else{
					loadChatMessages(currChannelId);
				}
			});
		} catch (JSONException e) {
			notifyListenersError(getString(R.string.error_chat_join));
		}
	}

	private void loadChatMessages(long channel){
		ChatHistoryRequest chatHistoryRequest = new ChatHistoryRequest();
		chatHistoryRequest.setAmount(DEFAULT_AMOUNT_MESSAGES);
		chatHistoryRequest.setChannel(channel);
		String req = gson.toJson(chatHistoryRequest);

		try {
			mSocket.emit(CHAT_HISTORY,  new JSONObject(req), (Ack) args -> {
				//TODO ret message
				clearMessages();
				convertMessages(String.valueOf(args[0]));
			});
		} catch (JSONException e) {
			Log.e(LogUtils.TAG, e.toString());
			notifyListenersError(getString(R.string.error_chat_history));
		}
	}

	public void leaveChannel(long channelId){
		try {
			JSONObject leave = new JSONObject(gson.toJson(new ChatChannelRequest(channelId)));
			mSocket.emit(CHAT_EVENT_LEAVE, leave, (Ack) args -> {
				ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
				if(chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)){
					notifyListenersError(chatResponse.getResult().getMessage());
				}else{
					loadChatMessages(currChannelId);
				}
			});
		} catch (JSONException e) {
			Log.e(LogUtils.TAG, e.toString());
			notifyListenersError(getString(R.string.error_chat_leave));
		}
	}

	/**
	 * Convert json string to List<ChatMessage>
	 * @param jsonMessages json string
	 */
	private void convertMessages(String jsonMessages){
		ChatResponse chatResponse = gson.fromJson(jsonMessages, ChatResponse.class);
		if(chatResponse.getStatus().equals(APIUtils.OK_STATUS)){
			addMessages(chatResponse.getResult().getChatMessages());
		}else{
			notifyListenersError(chatResponse.getResult().getMessage());
		}
	}

	@DebugLog
	public void sendMessage(ChatMessage newMessage) {
		if(mSocket == null) return;
		try {
			mSocket.emit(CHAT_EVENT_NEW_MESSAGE, new JSONObject(gson.toJson(newMessage)), (Ack) args -> {
					ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
					if(chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)){
						notifyListenersError(chatResponse.getResult().getMessage());
					}
			});
		} catch (JSONException e) {
			Log.e(LogUtils.TAG, e.toString());
			notifyListenersError(getString(R.string.error_chat_send_message));
		}
	}
	@DebugLog
	public void loginChat(){
		if(userStore.isUserLoggedIn()){
			mToken = userStore.getCurrentUser().getToken();
		}
		if(mSocket == null && mToken == null) return;
		try {
			JSONObject login = new JSONObject();
			login.put(CHAT_LOGIN_TOKEN, mToken);
			mSocket.emit(CHAT_EVENT_LOGIN, login, (Ack) args -> {
				ChatResponse chatResponse = gson.fromJson(args[0].toString(), ChatResponse.class);
				if(chatResponse.getStatus().equals(APIUtils.ERROR_STATUS)){
					notifyListenersError(chatResponse.getResult().getMessage());
				}
			});
		} catch (JSONException e) {
			Log.e(LogUtils.TAG, e.toString());
			notifyListenersError(getString(R.string.error_chat_login));
		}

	}

	public List<ChatMessage> getChatMessages(){
		return mMessages;
	}

	public int getChatMessagesSize(){
		return mMessages.size();
	}

	private void removeMessage(ChatMessage removeMessage) {
		// waiting for java 1.8 stream :(
		for(ChatMessage chatMessage : mMessages){
			if(chatMessage.getId() == removeMessage.getId()) mMessages.remove(chatMessage);
		}
		notifyListenersRemoveMessage();
	}

	private void addMessages(List<ChatMessage> list) {
		Collections.sort(list, new ChatMessageComparator());
		mMessages.addAll(list);
		notifyListenersEventMessages();
	}

	// Chat server events listeners
	private Emitter.Listener onRemoveMessage = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			final ChatMessage removeMessage = gson.fromJson(String.valueOf(args[0]), ChatMessage.class);
			removeMessage(removeMessage);
		}
	};

	private Emitter.Listener onNewMessage = args -> {
		chatMessages.onNext((String.valueOf(args[0])));
	};

	private Emitter.Listener onError = args -> {
		Log.i(LogUtils.TAG, "onError");
		dumpArgs(args);
		// Disabled due funstream inner error spam, not my fault
		// TODO Enable after fix
//		notifyListenersError(getString(R.string.chat_error));
	};

	private Emitter.Listener onConnectError = args -> {
		Log.i(LogUtils.TAG, "onConnectError");
		dumpArgs(args);
		notifyListenersServerMessage(getString(R.string.chat_connect_error));
	};

	private Emitter.Listener onReconnectError = args -> {
		Log.i(LogUtils.TAG, "onReconnectError");
		dumpArgs(args);
		notifyListenersServerMessage(getString(R.string.chat_reconnect_error));
	};

	private Emitter.Listener onReconnect = new Emitter.Listener() {
		@Override
		public void call(final Object... args) {
			Log.i(LogUtils.TAG, "onReconnect");
			notifyListenersServerMessage(getString(R.string.chat_reconnect));
			joinChannel(currChannelId);
			loginChat();
		}
	};

	private Emitter.Listener onConnect = args -> {
		notifyListenersServerMessage(getString(R.string.chat_connect));
		Log.i(LogUtils.TAG, "onConnect");
	};

	private void clearMessages(){
		mMessages.clear();
		notifyListenersEventMessages();
	}

	public void getUserIds(Long channelId){
		if(mSocket != null) {
			try {
				mSocket.emit(ChatApiUtils.CHAT_USER_LIST, new JSONObject(gson.toJson(new ChatChannelRequest(channelId))), (Ack) args -> {
					ChatListResponse chatListResponse = gson.fromJson(args[0].toString(), ChatListResponse.class);
					if(chatListResponse.getStatus().equals(APIUtils.ERROR_STATUS)){
						notifyListenersError(chatListResponse.getResult().getMessage());
					} else {
						notifyListenersUserListLoaded(chatListResponse.getResult().getUsers());
					}

				});
			} catch (JSONException e) {
				Log.e(LogUtils.TAG, e.toString());
				notifyListenersError(getString(R.string.error_chat_user_list));
			}
		}
	}

	private void notifyListenersNewMessage(){
		for(FunstreamChatEventsListener listener : listeners){
			listener.onNewMessage();
		}
	}
	private void notifyListenersEventMessages(){
		for(FunstreamChatEventsListener listener : listeners){
			listener.onEventMessages();
		}
	}
	@DebugLog
	private void notifyListenersError(String error){
		Log.e(LogUtils.TAG, error);
		for(FunstreamChatEventsListener listener : listeners){
			listener.onError(error);
		}
	}
	private void notifyListenersRemoveMessage(){
		for(FunstreamChatEventsListener listener : listeners){
			listener.onRemoveMessage();
		}
	}

	private void notifyListenersServerMessage(String message){
		for(FunstreamChatEventsListener listener : listeners){
			listener.onServerMessage(message);
		}
	}

	private void notifyListenersUserListLoaded(Integer[] userIds){
		for(FunstreamChatEventsListener listener : listeners){
			listener.onUserListLoaded(userIds);
		}
	}

	@DebugLog
	public void addFunstreamChatEventsListener(FunstreamChatEventsListener listener) {
		if(!this.listeners.contains(listener)){
			this.listeners.add(listener);
		}
	}

	@DebugLog
	public boolean removeFunstreamChatEventsListener(FunstreamChatEventsListener listener) {
		return this.listeners.remove(listener);
	}

	private void dumpArgs(Object[] args){
		if(args != null) {
			for (int i = 0; i < args.length; i++) {
				Log.e(LogUtils.TAG, args[i].toString());
			}
		}
	}

	UserStoreListener userStoreListener = () -> loginChat();
}
