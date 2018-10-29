package y.auto.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;

public class HttpUtil{
	
	public static final String POST = "post";
	
	public static final String GET = "get";
	
	public static final String ENC_UTF_8 = "UTF-8";
	
	public static final String ENC_GBK = "GBK";

	private static void trustAllHttpsCertificates() throws Exception {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	/**
	 * 向指定URL发送GET方法的请求 
	 * @param url		发送请求的URL
	 * @param params	请求参数，请求参数应该是name1=value1&name2=value2的形式。
	 * @param encode	编码方式（防止乱码）
	 * @return			URL所代表远程资源的响应
	 */
	public static String sendGet(String url, String params,String encode){
		return send(GET, url, params, null, encode, null);
		 
	}

	/**
	 * 向指定URL发送POST方法的请求
	 * @param url 发送请求的URL
	 * @param params
	 * @param encode	编码方式（防止乱码）
	 * @return URL所代表远程资源的响应
	 */
	public static String sendPost(String url, String params,String encode){
		return send(POST, url, params, null, encode, null);
	}
	
	/**
	 * 向指定URL发送POST方法的请求 UTF-8
	 * @param url
	 * @param params
	 * @return
	 */
	public static String sendPost(String url, String params){
		return send(POST, url, params, null, HttpUtil.ENC_UTF_8, null);
	}
	
	/**
	 * 向指定的URL发送POST请求
	 * @param url				指定的URL
	 * @param params			参数
	 * @param requestPropertys	请求头
	 * @param encode			编码方式
	 * @return
	 */
	public static String sendPost(String url, String params, Map<String,Object> requestPropertys,String encode){
		return send(POST, url, params, requestPropertys, encode, null);
	}

	public static String sendPost(String url, Map params){
		return sendPost(url, params, null, HttpUtil.ENC_UTF_8);
	}

	public static String sendPost(String url, Map params,String encode){
		return sendPost(url, params, null, encode);
	}

	public static String sendPost(String url, Map params, Map<String,Object> requestPropertys,String encode){
		StringBuilder paramStr = new StringBuilder();
		if(null != params){
			Set<String> keys = params.keySet();
			for(String key : keys){
				if(paramStr.length() != 0){
					paramStr.append("&");
				}
				paramStr.append(key).append("=").append(params.get(key));
			}
		}
		return send(POST, url, paramStr.toString(), requestPropertys, encode, null);
	}
			
	/**
	 * 发送Http请求
	 * @param method			请求的方法
	 * @param url				请求的URL
	 * @param params			请求的参数
	 * @param requestPropertys	请求头Map
	 * @param encode			编码方式（防止乱码）
	 * @param cookieManager		cookie机制
	 * @return
	 */
	public static String send(String method, String url, String params, Map<String,Object> requestPropertys,String encode,CookieManager cookieManager) {
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		OutputStreamWriter out = null;
		try{
			if(method.equalsIgnoreCase(GET) && StringUtils.isNotBlank(params)){
				if(url.contains("?")){
					url += "&" + params;
				} else{
					url += "?" + params;
				}
			}
			if(url.toLowerCase().startsWith("https")){
				trustAllHttpsCertificates();
				HostnameVerifier hv = new HostnameVerifier() {
					public boolean verify(String urlHostName, SSLSession session) {
						return true;
					}
				};
				HttpsURLConnection.setDefaultHostnameVerifier(hv);
			}

			URL realUrl = new URL(url);
			if(StringUtils.isBlank(encode)){
				encode = HttpUtil.ENC_UTF_8;
			}
			
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + encode);
			conn.setRequestProperty("Accept-Charset", encode);
			// 设置通用的自定义属性
			if(requestPropertys != null && !requestPropertys.isEmpty()){
				Iterator<String> it = requestPropertys.keySet().iterator();
				String key;
				String value;
				while(it.hasNext()){
					key = it.next();
					value = requestPropertys.get(key) + "";
					conn.setRequestProperty(key, value);
				}
			}
			//设置Cookie
			if(cookieManager != null && cookieManager instanceof CookieManager){
				cookieManager.setCookies(conn);
			}
			if(method.equalsIgnoreCase(GET)){		//如果是发送GET请求
				// 建立实际的连接
				conn.connect();
				// 定义BufferedReader输入流来读取URL的响应
				InputStream is = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(is, encode));
			}else if(method.equalsIgnoreCase(POST)){//如果是发送POST请求
				// 发送POST请求必须设置如下两行
				conn.setDoOutput(true);
				conn.setConnectTimeout(10000);
				conn.setDoInput(true);
				// 获取URLConnection对象对应的输出流
				out = new OutputStreamWriter(conn.getOutputStream(), encode); 
				// 发送请求参数
				out.write(params);
				// flush输出流的缓冲
				out.flush();
				// 定义BufferedReader输入流来读取URL的响应
				InputStream is = conn.getInputStream();
				in = new BufferedReader(new InputStreamReader(is,encode));
			}
			String line;
			while ((line = in.readLine()) != null){
				result.append("\n" ).append(line);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}	
		return result.toString();
	}

	public static String sendPost(String url,Map<String,Object> params,byte[] data){
		StringBuilder result = new StringBuilder();
		PrintWriter out = null;
		BufferedReader in = null;
		try{
			String url2 = url;
			if(params != null && params.size() > 0){
				boolean ishas = false;
				if(url.contains("?")){
					ishas = true;
				}
				if(ishas == false){
					url2 +="?";
				}
				Object[] keys = params.keySet().toArray();
				for(int i=0;i<keys.length;i++){
					String key = "" + keys[i];
					if(ishas == true){
						url2 +="&";
					}
					ishas = true;
					url2 += key +"="+params.get(key);
				}
			}
			URL realUrl = new URL(url2);
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
			conn.setRequestMethod("POST");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setConnectTimeout(10000);
			conn.setDoInput(true);
			conn.connect();
			if(data != null ){
				conn.getOutputStream().write(data);
				conn.getOutputStream().flush();
				conn.getOutputStream().close();
			}
			// 定义BufferedReader输入流来读取URL的响应
			InputStream is = conn.getInputStream();
			in = new BufferedReader(new InputStreamReader(is,"gb2312"));
			String line;
			while ((line = in.readLine()) != null){
				result.append("\n" ).append(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}

}
