package godshi.edu.cn.micropayment.activity.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.util.EncodingUtils;
import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.payment.IndexActivity;
import godshi.edu.cn.micropayment.entity.User;
import godshi.edu.cn.micropayment.util.HttpUtils;

public class SettingActivity extends Activity
{
    private static final String ACCOUNT_FILE_NAME = "account.json";
    private static final String AKS_FILE_NAME = "setting_aks";

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        user = new User();
        user.setUsername(getIntent().getExtras().getString("username"));
        user.setPassword(getIntent().getExtras().getString("password"));

        TextView textView = findViewById(R.id.text_setting_header);
        textView.setText(String.format("账户: %s", user.getUsername()));

        TextView aksTextView = findViewById(R.id.text_setting_enable_aks);
        SharedPreferences read = getSharedPreferences(AKS_FILE_NAME, MODE_PRIVATE);
        boolean isSkipConfirm = read.getBoolean(AKS_FILE_NAME,false);

        if(!isSkipConfirm)
        {
            String text = getResources().getString(R.string.enable_aks);
            aksTextView.setText(text);
        }
        else {
            String text = getResources().getString(R.string.disable_aks);
            aksTextView.setText(text);
        }
        bindEnableAKSAction();
        bindChangePwdAction();
        bindLogoutAction();
    }

    private void clearAccountInfo()
    {
        try (FileOutputStream outputStream = openFileOutput(ACCOUNT_FILE_NAME, MODE_PRIVATE))
        {
            JSONObject object = new JSONObject();
            object.put("username", "");
            object.put("password", "");
            outputStream.write(object.toString().getBytes());
        }
        catch (JSONException | IOException e)
        {
            Log.e("error", e.getMessage());
        }
    }
    private void bindEnableAKSAction()
    {
        LinearLayout linearLayout = findViewById(R.id.btn_setting_enable_aks);

        linearLayout.setOnClickListener(view ->
        {
            String toastMsg = "";
            TextView aksTextView = findViewById(R.id.text_setting_enable_aks);
            SharedPreferences read = getSharedPreferences(AKS_FILE_NAME, MODE_PRIVATE);
            boolean isSkipConfirm = read.getBoolean(AKS_FILE_NAME,false);

            if(!isSkipConfirm)
            {
                toastMsg = "成功开启一键购物功能，订单确认流程将被跳过！";
                showToastMessage(toastMsg);
                String text = getResources().getString(R.string.disable_aks);
                aksTextView.setText(text);
            }
            else {
                toastMsg = "成功关闭一键购物功能，订单将需要经过确认！";
                showToastMessage(toastMsg);
                String text = getResources().getString(R.string.enable_aks);
                aksTextView.setText(text);
            }
            SharedPreferences.Editor editor = getSharedPreferences(AKS_FILE_NAME, MODE_PRIVATE).edit();
            editor.putBoolean( AKS_FILE_NAME, !isSkipConfirm);
            editor.apply();
        });
    }

    private void bindChangePwdAction()
    {
        LinearLayout linearLayout = findViewById(R.id.btn_setting_change_pwd);

        linearLayout.setOnClickListener(view ->
        {
            Intent intent = new Intent(SettingActivity.this, ChangePwdActivity.class);
            intent.putExtra("username", user.getUsername());
            intent.putExtra("password", user.getPassword());
            startActivity(intent);
        });
    }

    private void bindLogoutAction()
    {
        LinearLayout linearLayout = findViewById(R.id.btn_setting_logout);

        linearLayout.setOnClickListener(view ->
            {
                clearAccountInfo();
                HttpUtils.clearSession();
                Toast.makeText(SettingActivity.this, "登出成功！", Toast.LENGTH_SHORT)
                        .show();
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                setResult(1); //used to finish parent activity
                startActivity(intent);
                this.finish();
            });
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
