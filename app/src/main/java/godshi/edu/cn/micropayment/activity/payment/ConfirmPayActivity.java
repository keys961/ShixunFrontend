package godshi.edu.cn.micropayment.activity.payment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.constant.ApiConstant;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.entity.Order;
import godshi.edu.cn.micropayment.entity.Product;
import godshi.edu.cn.micropayment.util.HttpUtils;

public class ConfirmPayActivity extends Activity
{
    private Order order;

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    private static final String AKS_FILE_NAME = "setting_aks";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_pay);
        SharedPreferences read = getSharedPreferences(AKS_FILE_NAME, MODE_PRIVATE);
        boolean isSkipConfirm = read.getBoolean(AKS_FILE_NAME,false);

        initOrder();
        bindCancelAction();
        bindSubmitAction();

        if(isSkipConfirm)
            new SubmitOrderRunnable().run();
    }

    private void initOrder()
    {
        order = (Order) getIntent().getSerializableExtra("order");
        TextView productNameText = findViewById(R.id.text_confirm_productname);
        productNameText.setText(order.getProduct().getProductName());
        TextView singlePriceText = findViewById(R.id.text_confirm_singleprice);
        singlePriceText.setText(String.valueOf(order.getProduct().getPrice()));
        TextView quantityText = findViewById(R.id.text_confirm_quantity);
        quantityText.setText(String.valueOf(order.getQuantity()));
        TextView totalPriceText = findViewById(R.id.text_confirm_totalprice);
        totalPriceText.setText(String.valueOf(order.getQuantity() *
                        order.getProduct().getPrice()));
        TextView addressText = findViewById(R.id.text_confirm_address);
        addressText.setText(order.getAddress());
        TextView phoneText = findViewById(R.id.text_confirm_phonenum);
        phoneText.setText(order.getPhoneNumber());
    }

    private void bindCancelAction()
    {
        Button button = findViewById(R.id.btn_confirm_cancel);
        button.setOnClickListener(view -> finish());
    }

    private void bindSubmitAction()
    {
        Button button = findViewById(R.id.btn_confirm_submit);
        button.setOnClickListener(view ->
        {
            new SubmitOrderRunnable().run();
        });
    }

    private class SubmitOrderCallable implements Callable<Message>
    {
        @Override
        public Message call()
        {
            Map<String, String> params = new HashMap<>();
            params.put("productId", String.valueOf(order.getProduct().getId()));
            params.put("quantity", String.valueOf(order.getQuantity()));
            params.put("address", order.getAddress());
            params.put("phone", order.getPhoneNumber());
            params.put("type", order.getType());
            if(order.getPayPassword() != null)
                params.put("payPassword", order.getPayPassword());
            Message message = HttpUtils.doPost(ApiConstant.API_BUY_PRODUCT, params);
            return message;
        }
    }

    private class SubmitOrderRunnable implements Runnable
    {
        @Override
        public void run()
        {
            Future<Message> messageFuture = threadPool.submit(new SubmitOrderCallable());
            Message message = null;
            try
            {
                message = messageFuture.get(20000L, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e)
            {
                Log.e("error", e.getMessage());
                return;
            }
            Bundle bundle = message.getData();
            boolean status = bundle.getBoolean(MessageKeyConstant.STATUS);
            String bodyMessage = bundle.getString(MessageKeyConstant.BODY);
            Log.i("status", Boolean.toString(status));
            Log.i("message", bodyMessage);
            if (!status)
            {
                Toast.makeText(ConfirmPayActivity.this, "请求失败，请重试!", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            JSONObject body = null;
            try
            {
                body = new JSONObject(bodyMessage);
            }
            catch (JSONException e)
            {
                Log.e("error", e.getMessage());
            }
            if(body == JSONObject.NULL)
            {
                Toast.makeText(ConfirmPayActivity.this, "请求失败，请重试!", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            parseJsonResponse(body);
        }
    }

    private void parseJsonResponse(JSONObject body)
    {
        try
        {
            String status = body.getString("status");
            if("success".equals(status))
            {
                Toast.makeText(ConfirmPayActivity.this, "购买成功!", Toast.LENGTH_LONG)
                        .show();
                storeRecentInfo();
                setResult(1);
                finish();
                return;
            }

            //failed
            JSONObject payload = body.getJSONObject("payload");
            String reason = payload.getString("reason");
            if("需要银行卡的支付密码！".equals(reason) || "支付密码错误！".equals(reason))
            {
                Toast.makeText(ConfirmPayActivity.this, "支付失败!\n" + reason,
                        Toast.LENGTH_LONG).show();
                // init a dialog
                startPayPasswordDialog(payload);
            }
            else if("无可用银行卡！".equals(reason))
            {
                Toast.makeText(ConfirmPayActivity.this, "无可用银行卡，请添加银行卡/为银行卡充值!",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(ConfirmPayActivity.this, "支付错误!\n"
                        + reason, Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            Log.e("error", e.getMessage());
        }
    }

    private void storeRecentInfo()
    {
        String fileName = "order." + order.getUsername() + ".json"; //filename
        JSONObject body = null;
        if(!StringUtils.isBlank(order.getAddress()) && !StringUtils.isBlank(order.getPhoneNumber()))
        {
            try (FileOutputStream out = openFileOutput(fileName, MODE_PRIVATE))
            {
                body = new JSONObject();

                body.put("address", order.getAddress());
                body.put("phone", order.getPhoneNumber());

                out.write(body.toString().getBytes());
            }
            catch (Exception e)
            {
                Log.e("error", e.getMessage());
            }
        }
    }

    private void startPayPasswordDialog(JSONObject payload)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.layout_pay_password, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("输入支付密码")
                .setView(dialogView).create();
        try
        {
            TextView cardNumberText = dialogView.findViewById(R.id.text_dia_cardnum);
            cardNumberText.setText("银行卡号: " + payload.getString("cardNumber"));
            TextView bankNameText = dialogView.findViewById(R.id.text_dia_bankname);
            bankNameText.setText("银行: " + payload.getString("bankName"));

            Button button = dialogView.findViewById(R.id.btn_dia_confirmpay);
            button.setOnClickListener(view ->
            {
                EditText passwordEdit = dialogView.findViewById(R.id.edit_dia_password);
                String password = passwordEdit.getText().toString();
                order.setPayPassword(password);

                new SubmitOrderRunnable().run();
            });
            dialog.show();
        }
        catch (JSONException e)
        {
            Log.e("error", e.getMessage());
        }
    }
}
