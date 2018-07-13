package godshi.edu.cn.micropayment.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.payment.IndexActivity;
import godshi.edu.cn.micropayment.entity.User;

public class SettingActivity extends Activity
{
    private static final String ACCOUNT_FILE_NAME = "account.json";

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
                Toast.makeText(SettingActivity.this, "登出成功！", Toast.LENGTH_SHORT)
                        .show();
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                setResult(1); //used to finish parent activity
                startActivity(intent);
                this.finish();
            });
    }

}
