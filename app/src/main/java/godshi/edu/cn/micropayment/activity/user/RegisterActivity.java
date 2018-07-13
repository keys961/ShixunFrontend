package godshi.edu.cn.micropayment.activity.user;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.payment.IndexActivity;
import godshi.edu.cn.micropayment.constant.ApiConstant;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.util.HttpUtils;
import godshi.edu.cn.micropayment.util.LoadingUtils;
import godshi.edu.cn.micropayment.util.PasswordUtils;
import godshi.edu.cn.micropayment.util.UserInfoUtils;

public class RegisterActivity extends Activity
{
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private Handler registerHandler = new Handler()
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
                toastMsg = "注册失败！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                setResult(2);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);
                if(body.getBoolean("status"))
                {
                    toastMsg = "注册成功！返回登陆页面！";
                    showToastMessage(toastMsg);
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("username", bundle.getString("username"));
                    intent.putExtra("password", bundle.getString("password"));
                    startActivity(intent);
                    setResult(1);
                    RegisterActivity.this.finish();
                    return;
                }
            }
            catch (JSONException e)
            {
                Log.e("error", e.getMessage());
            }
            // failed
            toastMsg = "注册失败!";
            showToastMessage(toastMsg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindRegisterAction();
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindRegisterAction()
    {
        Button button = findViewById(R.id.btn_register_register);
        button.setOnClickListener((view ->
            {
                String msg = "";
                EditText editUsername = (EditText)findViewById(R.id.edit_register_username);
                EditText editPassword1 = (EditText)findViewById(R.id.edit_register_password1);
                EditText editPassword2 = (EditText)findViewById(R.id.edit_register_password2);
                String username = editUsername.getText().toString();
                if (!UserInfoUtils.isUsernameValid(username))
                {
                    Toast.makeText(RegisterActivity.this, "用户名格式有误",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = editPassword1.getText().toString();
                if (!UserInfoUtils.isPasswordValid(password))
                {
                    Toast.makeText(RegisterActivity.this, "密码格式有误",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(editPassword2.getText().toString()))
                {
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                LoadingUtils.show(RegisterActivity.this);
                threadPool.submit(new RegisterCallable(username, password));
            })
        );
    }

    private class RegisterCallable implements Callable<Boolean>
    {
        private String username;

        private String password;

        public RegisterCallable(String username, String password)
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
            Message message = HttpUtils.doPost(ApiConstant.API_REGISTER, params);
            message.getData().putString("username", username);
            message.getData().putString("password", password);
            registerHandler.sendMessage(message);
            return message.getData().getBoolean(MessageKeyConstant.STATUS);
        }
    }

}
