package godshi.edu.cn.micropayment.util;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.CoreConnectionPNames;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.util.EntityUtils;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;

public class HttpUtils
{
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectionRequestTimeout(10000) // 10s connection timeout
            .setSocketTimeout(10500) // 10.5s transfer timeout
            .build();

    private static AtomicReference<CookieStore> session = new AtomicReference<>(null);

    public static Message doPost(String url, Map<String, String> params)
    {
        Message message = new Message();
        Bundle bundle = new Bundle();

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        HttpEntity entity = null;
        HttpResponse response = null;
        if(session.get() != null)
            client.setCookieStore(session.get());
        try
        {
            JSONObject jsonObject = new JSONObject();
            for(String key : params.keySet())
                jsonObject.put(key, params.get(key));

            entity = new StringEntity(jsonObject.toString(), "utf-8");
            post.setHeader("Content-Type", "application/json");
            post.setEntity(entity);
            post.setConfig(REQUEST_CONFIG);
            response = client.execute(post);
            if(response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                Log.e("error", "request failed");
                bundle.putBoolean(MessageKeyConstant.STATUS, false);
            }
            else
            {
                Log.i("info", "request succeed");
                if(!client.getCookieStore().getCookies().isEmpty())
                    session.getAndSet(client.getCookieStore());
                bundle.putBoolean(MessageKeyConstant.STATUS, true);
            }

            if(response != null && response.getEntity() != null)
                bundle.putString(MessageKeyConstant.BODY, EntityUtils.toString(response.getEntity(),
                        "utf-8"));
            else
                bundle.putString(MessageKeyConstant.BODY, "");
        }
        catch (JSONException | IOException e)
        {
            Log.e("error", e.getMessage());
            bundle.putBoolean(MessageKeyConstant.STATUS, false);
            bundle.putString(MessageKeyConstant.BODY, e.getMessage());
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
        if(session.get() != null)
            client.setCookieStore(session.get());
        try
        {
            httpParams = new BasicHttpParams();
            for(String key : params.keySet())
                httpParams.setParameter(key, params.get(key));
            get.setParams(httpParams);
            get.setConfig(REQUEST_CONFIG);
            response = client.execute(get);
            if(response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                Log.e("error", "request failed");
                bundle.putBoolean(MessageKeyConstant.STATUS, false);
            }
            else
            {
                Log.i("info", "request succeed");
                if(!client.getCookieStore().getCookies().isEmpty())
                    session.getAndSet(client.getCookieStore());
                bundle.putBoolean(MessageKeyConstant.STATUS, true);
            }
            if(response != null && response.getEntity() != null)
                bundle.putString(MessageKeyConstant.BODY, EntityUtils.toString(response.getEntity(),
                    "utf-8"));
            else
                bundle.putString(MessageKeyConstant.BODY, "");
        }
        catch (IOException e)
        {
            Log.e("error", e.getMessage());
            bundle.putBoolean(MessageKeyConstant.STATUS, false);
            bundle.putString(MessageKeyConstant.BODY, e.getMessage());
        }
        finally
        {
            client.close();
        }

        message.setData(bundle);
        return message;
    }

    public static void clearSession()
    {
        session = new AtomicReference<>(null);
    }
}
