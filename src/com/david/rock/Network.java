package com.david.rock;

import android.content.ContentValues;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import org.json.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Network {
    public static final String URL  = "http://192.168.0.103:3000/hand/";
    private static DefaultHttpClient sHttpClient = null;

    public static String post(String aUrl, HashMap<String, String> aParams)
        throws Exception
    {
        String response = null;

        setupHttpClient();
        //I know his is retarded
        String params = "?loca=" + (aParams.get("loca")) + "&move=" + aParams.get("move");
        String newUrl = aUrl.concat(params);
        HttpPost httpPost = new HttpPost(newUrl);
        Log.d("XXX",newUrl);
        //httpPost.setEntity(generateParametersEntity(aParams));

        HttpResponse httpResponse = sHttpClient.execute(httpPost);
        HttpEntity responseEntity = httpResponse.getEntity();

        if (responseEntity != null) {
            response = EntityUtils.toString(responseEntity);
        }

        return response;
    }

    public static void setupHttpClient() {
        if (sHttpClient == null) {
            BasicHttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpConnectionParams.setConnectionTimeout(params, 30000);
            HttpConnectionParams.setSoTimeout(params, 30000);

            final SchemeRegistry schemeRegistry = new SchemeRegistry(); 
            schemeRegistry.register(new Scheme("http", 
                        PlainSocketFactory.getSocketFactory(), 80)); 
            schemeRegistry.register(new Scheme("https", 
                        SSLSocketFactory.getSocketFactory(), 443)); 
            final ThreadSafeClientConnManager cm = new 
                ThreadSafeClientConnManager(params, schemeRegistry); 

            sHttpClient = new DefaultHttpClient(cm, params); 
        }
    }

    private static UrlEncodedFormEntity generateParametersEntity(HashMap<String, String> aParams) 
        throws UnsupportedEncodingException
    {
        Iterator<?> iter = aParams.entrySet().iterator();

        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();

        while (iter.hasNext()) {
            Map.Entry<String, String> param = 
                (Map.Entry<String, String>) iter.next();

            parameters.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return new UrlEncodedFormEntity(parameters);
    }


    public static void resetHttpClient() {
        sHttpClient = null;
    }
}

