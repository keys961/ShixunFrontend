package godshi.edu.cn.micropayment.util;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.util.EntityUtils;

public class HttpUtils
{
    public static class HttpMessageKey
    {
        public static final String STATUS = "status";

        public static final String MESSAGE = "message";
    }

    public static Message doPost(String url, Map<String, String> params)
    {
        Message message = new Message();
        Bundle bundle = new Bundle();

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        HttpEntity entity = null;
        HttpResponse response = null;
        try
        {
            JSONObject jsonObject = new JSONObject();
            for(String key : params.keySet())
                jsonObject.put(key, params.get(key));

            entity = new StringEntity(jsonObject.toString(), "utf-8");
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            post.setEntity(entity);
            response = client.execute(post);
            if(response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                Log.e("error", "Login failed");
                bundle.putBoolean(HttpMessageKey.STATUS, false);
            }
            else
            {
                Log.i("info", "Login succeed");
                bundle.putBoolean(HttpMessageKey.STATUS, true);
            }

            if(response != null && response.getEntity() != null)
                bundle.putString(HttpMessageKey.MESSAGE, EntityUtils.toString(response.getEntity(),
                        "utf-8"));
            else
                bundle.putString(HttpMessageKey.MESSAGE, "");
        }
        catch (JSONException | IOException e)
        {
            Log.e("error", e.getMessage());
            bundle.putBoolean(HttpMessageKey.STATUS, false);
            bundle.putString(HttpMessageKey.MESSAGE, e.getMessage());
        }
        finally
        {
            client.close();
        }

        message.setData(bundle);
        return message;
    }

    public static Message doGet(String url, Map<String, String> params)
    {
        Message message = new Message();
        Bundle bundle = new Bundle();

        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        HttpParams httpParams = null;
        HttpResponse response = null;
        try
        {
            httpParams = new BasicHttpParams();
            for(String key : params.keySet())
                httpParams.setParameter(key, params.get(key));
            get.setParams(httpParams);
            response = client.execute(get);
            if(response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                Log.e("error", "Login failed");
                bundle.putBoolean(HttpMessageKey.STATUS, false);
            }
            else
            {
                Log.i("info", "Login succeed");
                bundle.putBoolean(HttpMessageKey.STATUS, true);
            }
            if(response != null && response.getEntity() != null)
                bundle.putString(HttpMessageKey.MESSAGE, EntityUtils.toString(response.getEntity(),
                    "utf-8"));
            else
                bundle.putString(HttpMessageKey.MESSAGE, "");
        }
        catch (IOException e)
        {
            Log.e("error", e.getMessage());
            bundle.putBoolean(HttpMessageKey.STATUS, false);
            bundle.putString(HttpMessageKey.MESSAGE, e.getMessage());
        }
        finally
        {
            client.close();
        }

        message.setData(bundle);
        return message;
    }
}
