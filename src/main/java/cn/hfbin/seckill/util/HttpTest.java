package cn.hfbin.seckill.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
 
public class HttpTest {
	private static HttpClient hc = new DefaultHttpClient();
	
	
	public static void main(String args[]) throws ClientProtocolException, IOException, ParseException, URISyntaxException{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("wd","test"));
		
		String url = "http://localhost:8888/goods/list";
		String body = get(url, params);
		//真的要压力测试的话，要用线程，for没用的
		//System.out.println("服务器响应的内容:"+body);
	}
	
	/*
	 * post请求
	 */
	public static String post(String url,List<NameValuePair> params) throws ClientProtocolException, IOException{
		String body = null;
		
		//post请求
		HttpPost httppost = new HttpPost(url);
		//设置参数
		httppost.setEntity(new UrlEncodedFormEntity(params));
		System.out.println("请求的地址:"+httppost.getURI());
		//发送请求
		HttpResponse re = hc.execute(httppost);
		
		//打印请求信息
		System.out.println("请求的信息:"+httppost.getRequestLine());
		
		//获取响应实体
		HttpEntity entity = re.getEntity();
		
		//打印服务器返回的状态
	    System.out.println("服务器返回的状态:"+re.getStatusLine());
	    
	    //打印返回的信息长度
	    System.out.println("响应内容长度:"+entity.getContentLength());
	    
		//获取响应内容
		body = EntityUtils.toString(entity);
		
		hc.getConnectionManager().shutdown();
		
		return body;
	}
	
	/*
	 * get请求
	 */
	
	public static String get(String url,List<NameValuePair> params) throws ParseException, UnsupportedEncodingException, IOException, URISyntaxException{
		String body = null;
		
		//get请求
		HttpGet httpget = new HttpGet(url);
		//设置参数
		String str = EntityUtils.toString(new UrlEncodedFormEntity(params));
		httpget.setURI(new URI(httpget.getURI().toString()+"?"+str));
		//发送请求
		HttpResponse re = hc.execute(httpget);
		
		//打印请求信息
		System.out.println("请求的信息:"+httpget.getRequestLine());
		
		//获取响应实体
		HttpEntity entity1 = re.getEntity();
		
		//打印服务器返回的状态
	    System.out.println("服务器返回的状态:"+re.getStatusLine());
	    
	    //打印返回的信息长度
	    System.out.println("响应内容长度:"+entity1.getContentLength());
	    
		//输出获取响应内容
	    System.out.println("服务器响应的内容:"+EntityUtils.toString(entity1));
	    
	    System.out.println("=======================================分割线==========================================");
		
		//释放get连接
		httpget.releaseConnection();
		
		
		
		//打印cookie信息
		List<Cookie> cookies = ((AbstractHttpClient) hc).getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("cookie信息:" + cookies.get(i).toString());
            }
        }
		
		hc.getConnectionManager().shutdown();
		
		return body;
	}
}
