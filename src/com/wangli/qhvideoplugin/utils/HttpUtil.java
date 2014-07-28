package com.wangli.qhvideoplugin.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;


public class HttpUtil {
	private static HttpClient httpclient = getHttpClient(5*1000, 10*1000);
	/**
	 * 执行url 请求
	 * @param url
	 * @return InputStream
	 */
	public static HttpEntity exeRequest(String url) throws Exception{
		HttpGet httpRequest = new HttpGet(url);
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 30000);
		httpRequest.setParams(httpParameters);
		HttpResponse httpResponse = httpclient.execute(httpRequest);
		int statesCode = httpResponse.getStatusLine().getStatusCode();
		if (statesCode == HttpStatus.SC_OK) {
			return httpResponse.getEntity();
		}
		return null;
	}
	/**
	 * 发送get请求
	 * @param uri UrlUrils 获取URI
	 * @return 返回请求体信息
	 */
	public static String doGetRequest(URI uri) {
		String strResult = null;
		try {
			HttpUriRequest httpRequest = new HttpGet(uri);
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			int statesCode = httpResponse.getStatusLine().getStatusCode();
			if (statesCode == HttpStatus.SC_OK) {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
	
	/**
	 * 发送get请求，并返回cookie参数
	 * @param uri UrlUrils 获取URI
	 * @return 返回请求Response
	 */
	public static HttpResponse doGetRequestWithCookieResponse(URI uri) {
		HttpResponse httpResponse = null;
		try {
			HttpUriRequest httpRequest = new HttpGet(uri);
			httpResponse = httpclient.execute(httpRequest);
			int statesCode = httpResponse.getStatusLine().getStatusCode();
			if (statesCode == HttpStatus.SC_OK) {
				return httpResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpResponse;
	}
	
	/**
	 * 发送get请求，并带上cookie参数
	 * @param uri UrlUrils 获取URI
	 * @return 返回请求体信息
	 */
	public static String doGetRequestWithCookie(URI uri, List<String> cookies) {
		String strResult = null;
		try {
			HttpUriRequest httpRequest = new HttpGet(uri);
			String str = "";
			for(String cookie:cookies) {
				str += cookie;
				str += ";";
			}
			httpRequest.addHeader("Cookie", str);
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			int statesCode = httpResponse.getStatusLine().getStatusCode();
			if (statesCode == HttpStatus.SC_OK) {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
	
	/**
	 * 发送post请求,并带上cookie参数
	 * @param uri
	 * @param parameters post 参数键值对
	 * @return
	 */
	public static String doPostRequestWithCookie(URI uri,List<NameValuePair> parameters, List<String> cookies) {
		String strResult = null;
		try {
			HttpPost httpRequest = new HttpPost(uri);
			String str = "";
			for(String cookie:cookies) {
				str += cookie;
				str += ";";
			}
			httpRequest.addHeader("Cookie", str);
			HttpEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
			httpRequest.setEntity(entity);
			
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			int statesCode = httpResponse.getStatusLine().getStatusCode();
			if (statesCode == HttpStatus.SC_OK) {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strResult;
	}
	public static String doGetRequest(String url) {
		String strResult = null;
		try {
			HttpGet httpRequest = new HttpGet(url);
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			int statesCode = httpResponse.getStatusLine().getStatusCode();
			if (statesCode == HttpStatus.SC_OK) {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
	/**
	 * 发送post请求
	 * @param uri
	 * @param parameters post 参数键值对
	 * @return
	 */
	public static String doPostRequest(URI uri,List<NameValuePair> parameters) {
		String strResult = null;
		try {
			HttpPost httpRequest = new HttpPost(uri);
			HttpEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
			httpRequest.setEntity(entity);
			
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			int statesCode = httpResponse.getStatusLine().getStatusCode();
			if (statesCode == HttpStatus.SC_OK) {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strResult;
	}
	
	/**
	 * 发送post请求
	 * @param uri
	 * @param parameters post 参数键值对
	 * @return
	 */
	public static String doPostWithHttps(URI uri,List<NameValuePair> parameters) {
		String strResult = null;
		try {
			HttpPost httpRequest = new HttpPost(uri);
			//httpRequest.addHeader("Content-Type", "multipart/form-data");
			HttpEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
			httpRequest.setEntity(entity);
			
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			int statesCode = httpResponse.getStatusLine().getStatusCode();
			if (statesCode == HttpStatus.SC_OK) {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strResult;
	}
	
	/**
	 * 获取网络图片数据
	 * @param url
	 * @return
	 */
	public static byte[] getImage(String imageUrl) {
		try {
			return getImageFromUrl(imageUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static byte[] getImageFromUrl(String imageUrl) throws Exception{
		HttpGet httpGet = new HttpGet(imageUrl);
		HttpResponse httpResponse = httpclient.execute(httpGet);
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			HttpEntity responseEntity = httpResponse.getEntity();
			BufferedHttpEntity entity = new BufferedHttpEntity(responseEntity);
			InputStream iStream = entity.getContent();
			return readStream(iStream);
		}
		return null;
	}
	/**
	 * 获取网络图片数据
	 * @param url
	 * @return
	 */
	public static InputStream getImageInputStream(String imageUrl) {
		try {
			HttpClient httpclient = getHttpClient(5*1000, 5*1000);
			HttpGet httpGet = new HttpGet(imageUrl);
			HttpResponse httpResponse = httpclient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity responseEntity = httpResponse.getEntity();
				return responseEntity.getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static HttpResponse getImageResponseEntity(String imageUrl) {
		try {
			HttpClient httpclient = getHttpClient(5*1000, 5*1000);
			HttpGet httpGet = new HttpGet(imageUrl);
			return httpclient.execute(httpGet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 初始化 HttpClient
	 * @param connTime 连接 超时时间
	 * @param soTime 等待响应 超时时间
	 * @return
	 */
	private static HttpClient getHttpClient(int connTime,int soTime){
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, connTime);
		HttpConnectionParams.setSoTimeout(httpParameters, soTime);
		
		// 设置我们的HttpClient支持HTTP和HTTPS两种模式
        SchemeRegistry schReg =new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        

        // 使用线程安全的连接管理来创建HttpClient
        ClientConnectionManager conMgr =new ThreadSafeClientConnManager(httpParameters, schReg);
//      Context context = Application.getInstance().getApplicationContext();
//		HttpHost httpHost = ApnUtil.getHttpHost(context);
//      HttpHost httpHost = NetUtils.getCurrentProxy(context);
		HttpClient client = new DefaultHttpClient(conMgr, httpParameters);
//		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
        
		return client;
	}
	
	/**
     * 读取流中的数据
     * @param inStream
     */
    private static byte[] readStream(InputStream inStream){
        if(inStream==null){
            return null;
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
                if(inStream!=null){
                    inStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        buffer = null;
        return outStream.toByteArray();
    }
}
