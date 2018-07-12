package godshi.edu.cn.micropayment.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.util.ConnectionUtils;
import godshi.edu.cn.micropayment.util.HttpUtils;

public class LoginActivity extends Activity
{

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private static Handler loginHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
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

    private boolean restoreAndLogin()
    {
        return false;
    }

    private void bindLoginAction()
    {
        Button loginButton = findViewById(R.id.btn_login_login);
        loginButton.setOnClickListener((view) ->
            {
                String username = ((EditText)findViewById(R.id.edit_login_username)).getText().toString();
                String password = ((EditText)findViewById(R.id.edit_login_password)).getText().toString();
                threadPool.submit(new LoginRunnable(username, password));
            }
        );
    }

    private void bindRegisterAction()
    {
        Button registerButton = findViewById(R.id.btn_login_register);
        registerButton.setOnClickListener(((View view) ->
                {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    //startActivityForResult(intent, 2);
                })
        );
    }

    private class LoginRunnable implements Runnable
    {
        private String username;

        private String password;

        private static final String URL = "http://www.zju.edu.cn";

        public LoginRunnable(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        @Override
        public void run()
        {
            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);
            Message message = HttpUtils.doGet(URL, params);
            loginHandler.sendMessage(message);
        }
    }

}
