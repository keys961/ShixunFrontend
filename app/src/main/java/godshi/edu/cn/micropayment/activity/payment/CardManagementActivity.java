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
import godshi.edu.cn.micropayment.activity.user.LoginActivity;
import godshi.edu.cn.micropayment.constant.ApiConstant;
import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.entity.User;
import godshi.edu.cn.micropayment.util.HttpUtils;
import godshi.edu.cn.micropayment.util.LoadingUtils;

public class CardManagementActivity extends Activity
{
    private User user;

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private Handler getCardsHandler = new Handler()
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
            Log.i("message", message); // body
            //LoadingUtils.cancel();
            if(!status)
            {
                // Login failed
                toastMsg = "获取银行卡列表失败！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);

                if("success".equals(body.getString("status")))
                {
                    toastMsg = "获取银行卡列表成功！";
                    JSONArray cardList = body.getJSONArray("payload");
                    List<Map<String, Object>> cards = new ArrayList<>(10);
                    for(int i = 0; i < cardList.length(); i++)
                    {
                        Map<String, Object> map = new HashMap<>();
                        JSONObject card = (JSONObject) cardList.get(i);
                        map.put("title", card.get("cardNumber"));
                        map.put("info", card.get("bankName"));
                        map.put("img", R.drawable.card);

                        cards.add(map);
                    }

                    SimpleAdapter adapter = new SimpleAdapter(CardManagementActivity.this,
                            cards, R.layout.layout_card_list_item,
                            new String[]{"title", "info", "img"},
                            new int[]{R.id.card_item_title, R.id.card_item_info, R.id.card_item_img});
                    ListView listView = findViewById(R.id.list_cardmanagement_cards);
                    listView.setAdapter(adapter);
                    //LoadingUtils.cancel();
                    showToastMessage(toastMsg);
                    return;
                }
            }
            catch (JSONException e)
            {
                Log.e("error", e.getMessage());
            }
            // false
            toastMsg = "服务器错误，获取银行卡列表失败！";
            showToastMessage(toastMsg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_management);
        user = new User();
        user.setUsername(getIntent().getExtras().getString("username"));
        user.setPassword(getIntent().getExtras().getString("password"));
        user.setName(user.getUsername());

        bindAddCardAction();
        removeCardItemAction();
        threadPool.submit(new GetAllCardsRunnable());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        threadPool.submit(new GetAllCardsRunnable());
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindAddCardAction()
    {
        Button button = findViewById(R.id.btn_cardmanagement_addcard);
        button.setOnClickListener(view ->
        {
            Intent intent = new Intent(this, AddCardActivity.class);
            intent.putExtra("username", user.getUsername());
            intent.putExtra("password", user.getPassword());

            startActivity(intent);
        });
    }

    private void removeCardItemAction()
    {
        ListView listView = findViewById(R.id.list_cardmanagement_cards);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
        {
            TextView bankNameView = view.findViewById(R.id.card_item_info);
            TextView cardNumView = view.findViewById(R.id.card_item_title);

            String bankName = bankNameView.getText().toString();
            String cardNumber = cardNumView.getText().toString();

            Intent intent = new Intent(this, RemoveCardActivity.class);
            intent.putExtra("bankName", bankName);
            intent.putExtra("cardNumber", cardNumber);
            intent.putExtra("username", user.getUsername());
            intent.putExtra("password", user.getPassword());

            startActivity(intent);
        });
    }

    public class GetAllCardsRunnable implements Runnable
    {
        @Override
        public void run()
        {
           // LoadingUtils.show(CardManagementActivity.this);
            Message message = HttpUtils.doGet(ApiConstant.API_GET_ALL_CARDS, new HashMap<>());
            getCardsHandler.sendMessage(message);
        }
    }

}
