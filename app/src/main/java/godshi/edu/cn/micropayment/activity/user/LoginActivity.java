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
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cz.msebera.android.httpclient.util.EncodingUtils;
import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.payment.IndexActivity;
import godshi.edu.cn.micropayment.constant.ApiConstant;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.util.ConnectionUtils;
import godshi.edu.cn.micropayment.util.HttpUtils;
import godshi.edu.cn.micropayment.util.LoadingUtils;

public class LoginActivity extends Activity
{
    private static final String ACCOUNT_FILE_NAME = "account.json";

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private Handler loginHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            String toastMsg = "";
            Bundle bundle = msg.getData();

            boolean status = bundle.getBoolean(MessageKeyConstant.STATUS);
            String message = bundle.getString(MessageKeyConstant.BODY);
            Log.i("status", Boolean.toString(status));
            Log.i("message", message);
            LoadingUtils.cancel();
            if(!status)
            {
                // Login failed
                toastMsg = "登陆失败！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);
                if("success".equals(body.getString("status")))
                {
                    toastMsg = "登陆成功！";
                    showToastMessage(toastMsg);
                    Intent intent = new Intent(LoginActivity.this, IndexActivity.class);
                    intent.putExtra("username", bundle.getString("username"));
                    intent.putExtra("password", bundle.getString("password"));
                    startActivity(intent);
                    LoginActivity.this.finish();
                    return;
                }
            }
            catch (JSONException e)
            {
                Log.e("error", e.getMessage() == null ? "" : e.getMessage());
            }
            // false
            // Login failed
            toastMsg = "登陆失败! 用户名或密码错误！";
            showToastMessage(toastMsg);
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

        if(restoreAndLogin())
            return;

        bindLoginAction();
        bindRegisterAction();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == 1)
                Log.i("register back", "succeed!");
            else if(resultCode == 2)
                Log.i("register back", "failed!");
            else
                Log.i("register back", "do nothing");
        }
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

                EditText editTextUsername = findViewById(R.id.edit_login_username);
                editTextUsername.setText(username);
                EditText editTextPassword = findViewById(R.id.edit_login_password);
                editTextPassword.setText(password);
                LoadingUtils.show(LoginActivity.this);
                Future<Boolean> result = threadPool.submit(new LoginCallable(username, password));
                return result.get(21000, TimeUnit.MILLISECONDS);
            }
            catch (JSONException e)
            {
                Log.e("error", "Parsing json failed.");
                return false;
            }
            catch (InterruptedException e)
            {
                Log.e("error", "fetched result interrupted.");
                LoadingUtils.cancel();
                return false;
            }
            catch (ExecutionException e)
            {
                Log.e("error", "fetched result execution failed.");
                LoadingUtils.cancel();
                return false;
            }
            catch (TimeoutException e)
            {
                Log.e("error", "connection timeout");
                LoadingUtils.cancel();
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
                LoadingUtils.show(LoginActivity.this);
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
                    startActivityForResult(intent, 1); // Request code: 1
                })
        );
    }

    private class LoginCallable implements Callable<Boolean>
    {
        private String username;

        private String password;

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
            Message message = HttpUtils.doPost(ApiConstant.API_LOGIN, params);
            message.getData().putString("username", username);
            message.getData().putString("password", password);
            loginHandler.sendMessage(message);
            return message.getData().getBoolean(MessageKeyConstant.STATUS);
        }
    }
}
