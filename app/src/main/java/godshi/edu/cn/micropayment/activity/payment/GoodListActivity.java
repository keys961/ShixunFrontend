package godshi.edu.cn.micropayment.activity.payment;

import android.os.Bundle;
import android.app.Activity;

import godshi.edu.cn.micropayment.R;

/**
 * GoodListActivity - choose an item -> OrderActivity -> ConfirmPayActivity
 */
public class GoodListActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_list);
    }

}
