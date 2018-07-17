package godshi.edu.cn.micropayment.activity.payment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.util.EncodingUtils;
import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.user.LoginActivity;
import godshi.edu.cn.micropayment.entity.Order;
import godshi.edu.cn.micropayment.entity.Product;
import godshi.edu.cn.micropayment.util.LoadingUtils;

public class OrderActivity extends Activity
{
    private Product productToBuy; // Product to buy

    private Order order; // Order to submit

    private String username; // Username -> Used to store the most recent order info

    private static final String AKS_FILE_NAME = "setting_aks";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initProductInfo();
        storeRecentUserInfo();

        bindConfirmAction();
        bindCancelAction();
    }

    private void initProductInfo()
    {
        Intent intent = getIntent();
        productToBuy = new Product();
        productToBuy.setId(intent.getLongExtra("productId", 0));
        productToBuy.setPrice(intent.getDoubleExtra("price", 0.0));
        productToBuy.setProductName(intent.getStringExtra("productName"));
        productToBuy.setTypeName(intent.getStringExtra("typeName"));
        username = intent.getStringExtra("username");

        TextView productNameText = findViewById(R.id.text_order_productname);
        productNameText.setText(productToBuy.getProductName());

        TextView typeNameText = findViewById(R.id.text_order_typename);
        typeNameText.setText(productToBuy.getTypeName());

        TextView priceTextView = findViewById(R.id.text_order_singleprice);
        priceTextView.setText(String.valueOf(productToBuy.getPrice()));
    }

    private void storeRecentUserInfo()
    {
        String fileName = "order." + username + ".json"; //filename
        JSONObject body = null;
        try(FileInputStream in = openFileInput(fileName))
        {
            byte[] buffer = new byte[in.available()];

            try
            {
                in.read(buffer);
                String content = EncodingUtils.getString(buffer, "utf-8");
                body = new JSONObject(content);
            }
            catch(JSONException e)
            {
                Log.e("error", e.getMessage());
                return;
            }
        }
        catch (IOException e)
        {
            Log.i("error", e.getMessage());
            return;
        }

        if (body != JSONObject.NULL)
        {
            try
            {
                String address = body.getString("address");
                String phone = body.getString("phone");
                if (StringUtils.isBlank(address) || StringUtils.isBlank(phone))
                    return;

                EditText addressEdit = findViewById(R.id.edit_order_address);
                addressEdit.setText(address);
                EditText phoneEdit = findViewById(R.id.edit_order_phone_number);
                phoneEdit.setText(phone);
            }
            catch (Exception e)
            {
                Log.i("error ", e.getMessage());
            }
        }
    }

    private void bindConfirmAction()
    {
        Button button = findViewById(R.id.btn_order_submit);
        SharedPreferences read = getSharedPreferences(AKS_FILE_NAME, MODE_PRIVATE);
        boolean isSkipConfirm = read.getBoolean(AKS_FILE_NAME,false);

        button.setOnClickListener(view ->
        {
            order = new Order();
            order.setProduct(productToBuy);//product
            EditText addressEdit = findViewById(R.id.edit_order_address);
            order.setAddress(addressEdit.getText().toString());//address
            EditText phoneEdit = findViewById(R.id.edit_order_phone_number);
            order.setPhoneNumber(phoneEdit.getText().toString());//phone
            order.setType(getIntent().getStringExtra("type"));//type
            order.setUsername(username);//username
            EditText quantityEdit = findViewById(R.id.edit_order_quantity);
            order.setQuantity(Integer.parseInt(quantityEdit.getText().toString()));//quantity

            Intent intent = new Intent(this, ConfirmPayActivity.class);

            intent.putExtra("order", order);
            startActivityForResult(intent, 1);
        });

        if(isSkipConfirm){
            button.performClick();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1 && resultCode == 1) // buy success
            finish();
    }

    private void bindCancelAction()
    {
        Button button = findViewById(R.id.btn_order_cancel);
        button.setOnClickListener(view -> finish());
    }
}
