package godshi.edu.cn.micropayment.activity.user;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.util.EncodingUtils;
import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.util.ConnectionUtils;
import godshi.edu.cn.micropayment.util.HttpUtils;

public class LoginActivity extends Activity
{
    private static final String ACCOUNT_FILE_NAME = "account.json";

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private static Handler loginHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            //TODO: at here to finish this activity if succeed
            Log.i("status", Boolean.toString(bundle.getBoolean(HttpUtils.HttpMessageKey.STATUS)));
            Log.i("message", bundle.getString(HttpUtils.HttpMessageKey.MESSAGE));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!ConnectionUtils.isConnectedToNetwork(this))
        {
            new AlertDialog.Builder(this)
                    .setTitle("网络错误")
                    .setMessage("网络连接失败，请确认网络连接")
                    .setPositiveButton("确定", (arg1, arg2) -> {finish();})
                    .show();
            return;
        }

        if(!restoreAndLogin())
            return;

        bindLoginAction();
        bindRegisterAction();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //TODO
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean restoreAndLogin()
    {
        JSONObject jsonObject = null;
        try(FileInputStream inputStream = openFileInput(ACCOUNT_FILE_NAME))
        {
            byte[] buffer = new byte[inputStream.available()];
            try
            {
                inputStream.read(buffer);
                String account = EncodingUtils.getString(buffer, "utf-8");
                jsonObject = new JSONObject(account);
            }
            catch(JSONException e)
            {
                Log.e("error", e.getMessage());
                return false;
            }
        }
        catch (IOException e)
        {
            Log.e("error", e.getMessage());
            return false;
        }

        if (jsonObject != JSONObject.NULL)
        {
            try {
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");
                if (StringUtils.isBlank(username) || StringUtils.isBlank(password))
                    return false;

                EditText editTextUsername = (EditText)findViewById(R.id.edit_login_username);
                editTextUsername.setText(username);
                EditText editTextPassword = (EditText)findViewById(R.id.edit_login_password);
                editTextPassword.setText(password);

                Future<Boolean> result = threadPool.submit(new LoginCallable(username, password));
                return result.get(21000, TimeUnit.MILLISECONDS);
            }
            catch (Exception e)
            {
                Log.i("error ", e.getMessage());
                return false;
            }
        }

        return false;
    }

    private void bindLoginAction()
    {
        Button loginButton = findViewById(R.id.btn_login_login);
        loginButton.setOnClickListener((view) ->
            {
                String username = ((EditText)findViewById(R.id.edit_login_username)).getText().toString();
                String password = ((EditText)findViewById(R.id.edit_login_password)).getText().toString();
                threadPool.submit(new LoginCallable(username, password));
            }
        );
    }

    private void bindRegisterAction()
    {
        Button registerButton = findViewById(R.id.btn_login_register);
        registerButton.setOnClickListener(((View view) ->
                {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    //TODO:
                    startActivityForResult(intent, 2);
                })
        );
    }

    private class LoginCallable implements Callable<Boolean>
    {
        private String username;

        private String password;

        private static final String URL = "http://www.zju.edu.cn";

        public LoginCallable(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        @Override
        public Boolean call()
        {
            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            Message message = HttpUtils.doPost(URL, params);
            message.getData().putString("username", username);
            message.getData().putString("password", password);
            loginHandler.sendMessage(message);
            return message.getData().getBoolean(HttpUtils.HttpMessageKey.STATUS);
        }
    }

}
