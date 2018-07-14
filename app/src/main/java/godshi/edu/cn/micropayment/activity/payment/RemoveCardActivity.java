package godshi.edu.cn.micropayment.activity.payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.constant.ApiConstant;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.util.HttpUtils;
import godshi.edu.cn.micropayment.util.LoadingUtils;

public class RemoveCardActivity extends Activity
{

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);


    @SuppressLint("HandlerLeak")
    private Handler removeHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            super.handleMessage(msg);
            super.handleMessage(msg);

            String toastMsg = "";
            Bundle bundle = msg.getData();

            boolean status = bundle.getBoolean(MessageKeyConstant.STATUS);
            String message = bundle.getString(MessageKeyConstant.BODY);
            Log.i("status", Boolean.toString(status));
            Log.i("message", message); // body
            LoadingUtils.cancel();
            if(!status)
            {
                // Login failed
                toastMsg = "移除银行卡失败！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);

                if("success".equals(body.getString("status")))
                {
                    toastMsg = "移除银行卡成功！";
                    showToastMessage(toastMsg);
                    RemoveCardActivity.this.finish();
                    return;
                }
            }
            catch (JSONException e)
            {
                Log.e("error", e.getMessage());
            }
            // false
            toastMsg = "服务器错误，移除银行卡失败！";
            showToastMessage(toastMsg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_card);

        initInfo();
        bindRemoveAction();
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initInfo()
    {
        TextView cardNumberView = findViewById(R.id.text_removecard_cardnum);
        TextView bankNameView = findViewById(R.id.text_removecard_bankname);

        cardNumberView.setText(getIntent().getStringExtra("cardNumber"));
        bankNameView.setText(getIntent().getStringExtra("bankName"));
    }

    private void bindRemoveAction()
    {
        Button button = findViewById(R.id.btn_removecard_confirm);
        button.setOnClickListener(view ->
        {
            LoadingUtils.show(this);
            String cardNumber = getIntent().getStringExtra("cardNumber");
            threadPool.submit(new RemoveCardRunnable(cardNumber));
        });
    }

    private class RemoveCardRunnable implements Runnable
    {
        private String cardNumber;

        public RemoveCardRunnable(String cardNumber)
        {
            this.cardNumber = cardNumber;
        }

        @Override
        public void run()
        {
            Map<String, String> params = new HashMap<>();
            params.put("cardNumber", cardNumber);
            removeHandler.sendMessage(HttpUtils.doPost(ApiConstant.API_REMOVE_CARD, params));
        }
    }

}
