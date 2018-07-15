package godshi.edu.cn.micropayment.activity.payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import godshi.edu.cn.micropayment.dao.ProductCollectionRepository;
import godshi.edu.cn.micropayment.entity.UserFavorProduct;
import godshi.edu.cn.micropayment.util.HttpUtils;
import godshi.edu.cn.micropayment.util.LoadingUtils;

/**
 * GoodListActivity(Show list) -choose an item-> OrderActivity -> ConfirmPayActivity
 * GoodListActivity - User add list button to add a good
 */
public class GoodListActivity extends Activity
{
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    private ProductCollectionRepository collectionRepository
            = new ProductCollectionRepository(this);

    @SuppressLint("HandlerLeak")
    private Handler goodListHandler = new Handler()
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
            //LoadingUtils.cancel();
            if(!status)
            {
                // Login failed
                toastMsg = "获取商品列表失败！\n失败消息: " + (message == null ? "未知" : message);
                showToastMessage(toastMsg);
                return;
            }
            JSONObject body;
            try
            {
                body = new JSONObject(message);
                if("success".equals(body.getString("status")))
                {
                    toastMsg = "获取商品列表成功！";
                    JSONArray productList = body.getJSONArray("payload");
                    List<Map<String, Object>> products = new ArrayList<>(10);
                    for(int i = 0; i < productList.length(); i++)
                    {
                        Map<String, Object> map = new HashMap<>();
                        JSONObject product = (JSONObject) productList.get(i);
                        map.put("image", R.drawable.item);
                        map.put("title", product.get("productName"));
                        map.put("price", "价格(￥): " + product.get("price"));
                        map.put("productId", product.get("id"));
                        map.put("type", "类型: " +
                                ((JSONObject)product.get("type")).get("typeName"));

                        products.add(map);
                    }

                    SimpleAdapter adapter = new SimpleAdapter(GoodListActivity.this,
                            products, R.layout.layout_product_list_item,
                            new String[]{"image", "title", "price", "type", "productId"},
                            new int[]{R.id.product_item_img, R.id.product_item_title,
                                    R.id.product_item_price, R.id.product_item_type, R.id.product_item_id});
                    ListView listView = findViewById(R.id.list_goodlist);
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
            // Login failed
            toastMsg = "获取商品列表失败! ";
            showToastMessage(toastMsg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_list);
        bindBuyProductAction();
        bindAddToCollectionAction();
        threadPool.submit(new GetAllProductRunnable());
    }

    private void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindAddToCollectionAction()
    {
        ListView listView = findViewById(R.id.list_goodlist);
        listView.setOnItemLongClickListener((adapterView, view, i, l) ->
        {
            UserFavorProduct product = new UserFavorProduct();

            TextView productIdTextView = view.findViewById(R.id.product_item_id);
            TextView productNameTextView = view.findViewById(R.id.product_item_title);
            TextView priceTextView = view.findViewById(R.id.product_item_price);
            TextView typeTextView = view.findViewById(R.id.product_item_type);

            long productId = Long.parseLong(productIdTextView.getText().toString());
            String productName = productNameTextView.getText().toString();
            String typeName = typeTextView.getText().toString().substring(
                    typeTextView.getText().toString().indexOf(":") + 2
            );
            double price = Double.parseDouble(priceTextView.getText().toString().substring(
                    priceTextView.getText().toString().indexOf(":") + 2
            ));
            String username = getIntent().getStringExtra("username");
            product.setUsername(username);
            product.setTypeName(typeName);
            product.setPrice(price);
            product.setProductName(productName);
            product.setProductId(productId);
            if(collectionRepository.query(username, productId) != null)
                Toast.makeText(this, "该商品已被收藏!", Toast.LENGTH_SHORT)
                        .show();
            else
            {
                collectionRepository.insert(product);
                Toast.makeText(this, "添加该商品收藏成功!", Toast.LENGTH_SHORT)
                        .show();
            }
            setResult(1);
            return true;
        });
    }

    private void bindBuyProductAction()
    {
        ListView listView = findViewById(R.id.list_goodlist);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
        {
            TextView productIdTextView = view.findViewById(R.id.product_item_id);
            TextView productNameTextView = view.findViewById(R.id.product_item_title);
            TextView priceTextView = view.findViewById(R.id.product_item_price);
            TextView typeTextView = view.findViewById(R.id.product_item_type);

            long productId = Long.parseLong(productIdTextView.getText().toString());
            String productName = productNameTextView.getText().toString();
            String typeName = typeTextView.getText().toString().substring(
                    typeTextView.getText().toString().indexOf(":") + 2
            );
            double price = Double.parseDouble(priceTextView.getText().toString().substring(
                    priceTextView.getText().toString().indexOf(":") + 2
            ));

            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra("productName", productName);
            intent.putExtra("productId", productId);
            intent.putExtra("typeName", typeName);
            intent.putExtra("price", price);
            intent.putExtra("type", "AKS");
            intent.putExtra("username", getIntent().getStringExtra("username"));

            startActivity(intent);
        });
    }

    private class GetAllProductRunnable implements Runnable
    {
        @Override
        public void run()
        {
            //LoadingUtils.show(GoodListActivity.this);
            Message message = HttpUtils.doGet(ApiConstant.API_GET_ALL_PRODUCTS, new HashMap<>());
            goodListHandler.sendMessage(message);
        }
    }

}
