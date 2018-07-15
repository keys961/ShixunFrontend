package godshi.edu.cn.micropayment.activity.payment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

public class TransactionListActivity extends Activity
{

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    @SuppressLint("HandlerLeak")
    private Handler historyHandler = new Handler()
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
                toastMsg = "获取交易历史列表失败！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);

                if("success".equals(body.getString("status")))
                {
                    toastMsg = "获取交易列表列表成功！";
                    JSONArray transactionList = body.getJSONArray("payload");
                    List<Map<String, Object>> mapList = new ArrayList<>(10);
                    for(int i = 0; i < transactionList.length(); i++)
                    {
                        Map<String, Object> map = new HashMap<>();
                        JSONObject transaction = (JSONObject) transactionList.get(i);
                        map.put("productName", transaction.get("productName"));
                        map.put("totalPay", transaction.get("totalPay"));
                        map.put("cardNumber", "卡号: " + transaction.get("cardNumber"));
                        map.put("time", "时间: " + transaction.get("time"));
                        map.put("quantity", "数量: " + transaction.get("quantity"));
                        map.put("img", R.drawable.item);

                        mapList.add(map);
                    }

                    SimpleAdapter adapter = new SimpleAdapter(TransactionListActivity.this,
                            mapList, R.layout.layout_transaction_list_item,
                            new String[]{ "productName", "totalPay", "cardNumber",
                                    "time", "quantity", "img" },
                            new int[]{ R.id.text_rec_productname,
                                    R.id.text_rec_money,
                                    R.id.text_rec_cardnum,
                                    R.id.text_rec_time,
                                    R.id.text_rec_quantity,
                                    R.id.img_rec_img });
                    ListView listView = findViewById(R.id.list_transaction_history);
                    listView.setAdapter(adapter);
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
        setContentView(R.layout.activity_transaction_list);

        threadPool.submit(new FetchTransactionHistoryRunnable());
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class FetchTransactionHistoryRunnable implements Runnable
    {
        @Override
        public void run()
        {
            Message message = HttpUtils.doGet(ApiConstant.API_TRANSACTION_HISTORY,
                    new HashMap<>());
            historyHandler.sendMessage(message);
        }
    }
}
