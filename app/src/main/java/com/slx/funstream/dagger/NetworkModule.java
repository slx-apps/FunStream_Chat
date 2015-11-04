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

package com.slx.funstream.dagger;


import android.content.Context;
import android.util.Log;

import com.slx.funstream.BuildConfig;
import com.slx.funstream.rest.UserAgentInterceptor;
import com.slx.funstream.utils.LogUtils;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {
	private static final long DISK_CACHE_SIZE = 50 * 1024 * 1024;//50mb
	private static final int PICASSO_MEMORY_CASHE_SIZE = 20 * 1024 * 1024;//20mb

	private static final String USER_AGENT = "AndroidClient v/" + BuildConfig.VERSION_NAME;
	@Provides
	@PerApp
	OkHttpClient provideOkHttpClient(Context context,
	                                 UserAgentInterceptor userAgentInterceptor) {
		OkHttpClient client = new OkHttpClient();

		// Obtain sslContext
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getDefault();
		} catch (GeneralSecurityException e) {
			Log.e(LogUtils.TAG, e.toString());
		}


//		try {
//			// loading CAs from an InputStream
//			CertificateFactory cf = CertificateFactory.getInstance("X.509");
//			InputStream cert = context.getResources().openRawResource(R.raw.funstream);
//
//			Certificate ca;
//			try {
//				ca = cf.generateCertificate(cert);
//			} finally {
//				cert.close();
//			}
//
//			// creating a KeyStore containing our trusted CAs
//			String keyStoreType = KeyStore.getDefaultType();
//			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//			keyStore.load(null, null);
//			keyStore.setCertificateEntry("ca", ca);
//
//			// creating a TrustManager that trusts the CAs in our KeyStore
//			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//			tmf.init(keyStore);
//
//			// creating an SSLSocketFactory that uses our TrustManager
//			SSLContext sslContext = SSLContext.getInstance("TLS");
//			sslContext.init(null, tmf.getTrustManagers(), null);
//			client.setSslSocketFactory(sslContext.getSocketFactory());
//
//		} catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
//			e.printStackTrace();
//		}

//		SSLContext sslContext = null;
//		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//				return new java.security.cert.X509Certificate[]{};
//			}
//
//			public void checkClientTrusted(X509Certificate[] chain,
//			                               String authType) throws CertificateException {
//			}
//
//			public void checkServerTrusted(X509Certificate[] chain,
//			                               String authType) throws CertificateException {
//			}
//		}};
//
//		KeyManager[] keyManager = null;
//		try {
//			final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//			KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//			factory.init(keyStore, null);
//			keyManager = factory.getKeyManagers();
//		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
//			e.printStackTrace();
//		}
//
//		try {
//			sslContext = SSLContext.getInstance("TLS");
//			sslContext.init(keyManager, trustAllCerts, new java.security.SecureRandom());
//			//HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		client.setSslSocketFactory(sslContext.getSocketFactory());

		client.networkInterceptors().add(userAgentInterceptor);
//		client.setConnectTimeout(5, SECONDS);
//		client.setReadTimeout(5, SECONDS);
//		client.setWriteTimeout(5, SECONDS);

		// Install an HTTP cache in the application cache directory.
		File cacheDir = new File(context.getCacheDir(), "http");
		Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
		client.setCache(cache);

		return client;
	}

	@Provides
	@PerApp
	UserAgentInterceptor provideUserAgentInterceptor(){
		return new UserAgentInterceptor(USER_AGENT);
	}

	@Provides
	@PerApp
	Picasso providePicassoClient(Context context, OkHttpDownloader downloader) {
		return new Picasso.Builder(context)
				.downloader(downloader)
	//			.indicatorsEnabled(true)
				.memoryCache(new LruCache(PICASSO_MEMORY_CASHE_SIZE))
				.build();
	}

	@Provides
	@PerApp
	OkHttpDownloader provideDownloader(OkHttpClient okHttpClient) {
		return new OkHttpDownloader(okHttpClient);
	}

//	@Provides
//	@PerApp
//	Client provideRetrofitClient(OkHttpClient okHttpClient) {
//		return new OkClient(okHttpClient);
//	}
}
