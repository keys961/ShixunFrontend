package godshi.edu.cn.micropayment.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.payment.IndexActivity;
import godshi.edu.cn.micropayment.constant.ApiConstant;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.util.HttpUtils;
import godshi.edu.cn.micropayment.util.LoadingUtils;
import godshi.edu.cn.micropayment.util.UserInfoUtils;

public class ChangePwdActivity extends Activity
{
    private static final String ACCOUNT_FILE_NAME = "account.json";

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private Handler changePwdHandler = new Handler()
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
                toastMsg = "修改成功！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);
                if(body.getBoolean("status"))
                {
                    toastMsg = "修改成功！";
                    showToastMessage(toastMsg);
                    ChangePwdActivity.this.finish();
                    return;
                }
            }
            catch (JSONException e)
            {
                Log.e("error", e.getMessage());
            }
            // false
            toastMsg = "修改密码失败，请检查输入是否正确！";
            showToastMessage(toastMsg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);

        bindChangePasswordAction();
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateAccountInfo(String username, String password)
    {
        try (FileOutputStream outputStream = openFileOutput(ACCOUNT_FILE_NAME, MODE_PRIVATE))
        {
            JSONObject object = new JSONObject();
            object.put("username", username);
            object.put("password", password);
            outputStream.write(object.toString().getBytes());
        }
        catch (JSONException | IOException e)
        {
            Log.e("error", e.getMessage());
        }
    }


    private void bindChangePasswordAction()
    {
        Button button = findViewById(R.id.btn_changepwd);
        button.setOnClickListener(view ->
            {
                String username = getIntent().getExtras().getString("username");
                EditText editOld = findViewById(R.id.edit_changepwd_oldpassword);
                EditText editNew = findViewById(R.id.edit_changepwd_newpassword);
                EditText editNew2 = findViewById(R.id.edit_changepwd_newpassword2);

                if(UserInfoUtils.isPasswordValid(editOld.getText().toString())
                        && UserInfoUtils.isPasswordValid(editNew.getText().toString())
                        && UserInfoUtils.isPasswordValid(editNew2.getText().toString()))
                {
                    LoadingUtils.show(ChangePwdActivity.this);
                    threadPool.submit(new ChangePasswordRunnable(username,
                            editOld.getText().toString(),
                            editNew.getText().toString(),
                            editNew2.getText().toString()));
                }
                else
                    Toast.makeText(this, "密码输入格式有误!", Toast.LENGTH_SHORT)
                            .show();
            });
    }

    private class ChangePasswordRunnable implements Runnable
    {
        private String username;

        private String oldPassword;

        private String newPassword;

        private String newPassword2;

        public ChangePasswordRunnable(String username, String oldPassword
                                      , String newPassword, String newPassword2)
        {
            this.newPassword = newPassword;
            this.username = username;
            this.oldPassword = oldPassword;
            this.newPassword2 = newPassword2;
        }

        @Override
        public void run()
        {
            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("oldPassword", oldPassword);
            params.put("newPassword", newPassword);
            params.put("newPassword2", newPassword2);
            Message message = HttpUtils.doPost(ApiConstant.API_CHANGE_PASSWORD, params);
            changePwdHandler.sendMessage(message);
        }
    }
}
