package godshi.edu.cn.micropayment.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.user.LoginActivity;
import godshi.edu.cn.micropayment.activity.user.SettingActivity;
import godshi.edu.cn.micropayment.entity.User;

public class IndexActivity extends Activity
{

    private static final String ACCOUNT_FILE_NAME = "account.json";

    private static long exitTime = 0;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        storeUserInfo();

        bindNfcPayAction();
        bindGoodListAction();
        bindCardManagementAction();
        bindSettingAction();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if ((System.currentTimeMillis() - exitTime) > 2000)
            {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else
            {
                finish();
                System.exit(0);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1 && resultCode == 1)
            this.finish();
    }

    private void storeUserInfo()
    {
        user = new User();
        Bundle extraBundle = getIntent().getExtras();
        if(extraBundle != null)
        {
            String username = extraBundle.getString("username");
            String password = extraBundle.getString("password");
            user.setUsername(username);
            user.setPassword(password);
            user.setName(username);
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
    }

    private void bindNfcPayAction()
    {
        LinearLayout linearLayout = findViewById(R.id.btn_index_nfc_pay);
        linearLayout.setOnClickListener(view ->
            {
                Intent intent = new Intent(IndexActivity.this, NFCPayActivity.class);
                intent.putExtra("user", user.getUsername());
                intent.putExtra("password", user.getPassword());
                startActivity(intent);
            });
    }

    private void bindGoodListAction()
    {
        LinearLayout linearLayout = findViewById(R.id.btn_index_get_good_list);
        linearLayout.setOnClickListener(view ->
            {
                Intent intent = new Intent(IndexActivity.this, GoodListActivity.class);
                intent.putExtra("username", user.getUsername());
                intent.putExtra("password", user.getPassword());
                startActivity(intent);
            });
    }

    private void bindCardManagementAction()
    {
        LinearLayout linearLayout = findViewById(R.id.btn_index_manage_card);
        linearLayout.setOnClickListener(view ->
        {
            Intent intent = new Intent(IndexActivity.this, CardManagementActivity.class);
            intent.putExtra("username", user.getUsername());
            intent.putExtra("password", user.getPassword());
            startActivity(intent);
        });
    }

    private void bindSettingAction()
    {
        LinearLayout linearLayout = findViewById(R.id.btn_index_setting);
        linearLayout.setOnClickListener(view ->
        {
            Intent intent = new Intent(IndexActivity.this, SettingActivity.class);
            intent.putExtra("username", user.getUsername());
            intent.putExtra("password", user.getPassword());
            startActivityForResult(intent, 1);
        });
    }


}
