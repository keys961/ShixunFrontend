package godshi.edu.cn.micropayment.activity.payment;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.user.LoginActivity;
import godshi.edu.cn.micropayment.constant.ApiConstant;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.util.HttpUtils;
import godshi.edu.cn.micropayment.util.LoadingUtils;

public class AddCardActivity extends Activity
{
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private Handler addCardHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            String toastMsg = "";
            boolean status = msg.getData().getBoolean(MessageKeyConstant.STATUS); // Status code
            String message = msg.getData().getString(MessageKeyConstant.BODY);
            Bundle bundle = msg.getData();

            Log.i("status", Boolean.toString(status));
            Log.i("message", message);
            LoadingUtils.cancel();

            if(!status)
            {
                // Login failed
                toastMsg = "添加失败！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);
                if("success".equals(body.getString("status")))
                {
                    toastMsg = "添加成功！";
                    showToastMessage(toastMsg);
                    return;
                }
            }
            catch (JSONException e)
            {
                Log.e("error", e.getMessage());
            }
            // false
            // Login failed
            toastMsg = "添加失败，请检查输入";
            showToastMessage(toastMsg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        bindAddCardAction();
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindAddCardAction()
    {
        Button button = findViewById(R.id.btn_addcard_confirm);
        button.setOnClickListener(view ->
        {
            LoadingUtils.show(this);
            EditText cardNumberEdit = findViewById(R.id.edit_addcard_cardnum);
            EditText cardPasswordEdit = findViewById(R.id.edit_addcard_cardpassword);

            String cardNumber = cardNumberEdit.getText().toString();
            String cardPassword = cardPasswordEdit.getText().toString();
            //LoadingUtils.show(this);
            threadPool.submit(new AddCardRunnable(cardNumber, cardPassword));
        });
    }

    private class AddCardRunnable implements Runnable
    {
        private String cardNumber;

        private String password;

        public AddCardRunnable(String cardNumber, String password)
        {
            this.cardNumber = cardNumber;
            this.password = password;
        }

        @Override
        public void run()
        {
            Map<String, String> params = new HashMap<>();
            params.put("cardNumber", cardNumber);
            params.put("cardPassword", password);

            Message message = HttpUtils.doPost(ApiConstant.API_ADD_CARD, params);
            addCardHandler.sendMessage(message);
        }
    }
}
