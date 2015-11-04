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

package com.slx.funstream.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetworkUtils {

	public static boolean isNetworkConnectionPresent(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}


//	/**
//	 * Get IP address from first non-localhost interface
//	 * @param useIPv4  true=return ipv4, false=return ipv6
//	 * @return  address or empty string
//	 */
//	public static String getIPAddress(boolean useIPv4) {
//		try {
//			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
//			for (NetworkInterface intf : interfaces) {
//				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
//				for (InetAddress addr : addrs) {
//					if (!addr.isLoopbackAddress()) {
//						String sAddr = addr.getHostAddress().toUpperCase();
//						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
//						if (useIPv4) {
//							if (isIPv4)
//								return sAddr;
//						} else {
//							if (!isIPv4) {
//								int delim = sAddr.indexOf('%');// drop ip6 port suffix
//								return delim<0 ? sAddr : sAddr.substring(0, delim);
//							}
//						}
//					}
//				}
//			}
//		} catch (Exception ex) {}
//		return "";
//	}
}
