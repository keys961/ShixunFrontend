package godshi.edu.cn.micropayment.activity.payment;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import godshi.edu.cn.micropayment.R;

/**
 * Show products that user favors
 * Onclick an item -> OrderActivity
 * Long click -> remove it
 */
public class AKSHomeActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akshome);
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
                //TODO: All product -> GoodListActivity
                return true;
            default:
                return false;
        }
    }
}
