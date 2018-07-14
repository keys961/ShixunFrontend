package godshi.edu.cn.micropayment.activity.payment;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.activity.user.LoginActivity;
import godshi.edu.cn.micropayment.dao.ProductCollectionRepository;
import godshi.edu.cn.micropayment.db.ProductCollectionHelper;
import godshi.edu.cn.micropayment.entity.Order;
import godshi.edu.cn.micropayment.entity.UserFavorProduct;
import godshi.edu.cn.micropayment.util.LoadingUtils;

/**
 * Show products that user favors
 * Onclick an item -> OrderActivity
 * Long click -> remove it
 */
public class AKSHomeActivity extends Activity
{
    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    private ProductCollectionRepository productCollectionRepository =
            new ProductCollectionRepository(this);

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akshome);
        username = getIntent().getStringExtra("username");

        bindRemoveCollectionAction();
        bindBuyProductAction();
        threadPool.submit(new GetProductCollectionRunnable(username));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_aks_product, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_all_product:
                Intent intent = new Intent(AKSHomeActivity.this, GoodListActivity.class);
                intent.putExtra("username", username);
                startActivityForResult(intent, 1);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1 && resultCode == 1)
            new GetProductCollectionRunnable(username).run();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //new GetProductCollectionRunnable(username).run();
    }

    private void bindBuyProductAction()
    {
        ListView listView = findViewById(R.id.list_akshome_userfavor);
        listView.setOnItemClickListener((adapterView, view, i, l) ->
        {
            TextView productIdTextView = view.findViewById(R.id.product_item_id);
            long productId = Long.parseLong(productIdTextView.getText().toString());

            UserFavorProduct product = productCollectionRepository.query(username, productId);

            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productId", productId);
            intent.putExtra("typeName", product.getTypeName());
            intent.putExtra("price", product.getPrice());

            startActivity(intent);
        });
    }

    private void bindRemoveCollectionAction()
    {
        ListView listView = findViewById(R.id.list_akshome_userfavor);
        listView.setOnItemLongClickListener((adapterView, view, i, l) ->
        {
            TextView textView = view.findViewById(R.id.product_item_id);
            long productId = Long.parseLong(textView.getText().toString());
            productCollectionRepository.delete(username, productId);

            Toast.makeText(this, "删除收藏成功！", Toast.LENGTH_SHORT)
                    .show();
            new GetProductCollectionRunnable(username).run();
            return true;
        }
        );
    }

    private class GetProductCollectionRunnable implements Runnable
    {
        private String username;

        public GetProductCollectionRunnable(String username)
        {
            this.username = username;
        }

        @Override
        public void run()
        {
            //LoadingUtils.show(AKSHomeActivity.this);
            List<UserFavorProduct> list = AKSHomeActivity.this.productCollectionRepository.queryAll(username);
            fillContent(list);
            //LoadingUtils.cancel();
        }
    }

    private void fillContent(List<UserFavorProduct> list)
    {
        List<UserFavorProduct> copyList = new ArrayList<>();
        for(UserFavorProduct product : list)
        {
            UserFavorProduct newProduct = new UserFavorProduct();
            newProduct.setId(product.getId());
            newProduct.setProductName(product.getProductName());
            newProduct.setPrice(product.getPrice());
            newProduct.setProductId(product.getProductId());
            newProduct.setTypeName(product.getTypeName());
            newProduct.setUsername(product.getUsername());

            copyList.add(newProduct);
        }

        List<Map<String, Object>> userFavorProducts = new ArrayList<>(10);

        for(UserFavorProduct product : copyList)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("image", R.drawable.item);
            map.put("title", product.getProductName());
            map.put("price", "价格(￥): " + product.getPrice());
            map.put("type", "类型: " + product.getTypeName());
            map.put("productId", product.getProductId());

            userFavorProducts.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(AKSHomeActivity.this,
                userFavorProducts, R.layout.layout_product_list_item,
                new String[]{"image", "title", "price", "type", "productId"},
                new int[]{R.id.product_item_img, R.id.product_item_title,
                    R.id.product_item_price, R.id.product_item_type, R.id.product_item_id});
        ListView listView = findViewById(R.id.list_akshome_userfavor);
        listView.setAdapter(adapter);
        Log.i("list count", String.valueOf(listView.getAdapter().getCount()));
        Log.i("avaliablility", String.valueOf(listView.getAdapter().areAllItemsEnabled()));
    }
}
