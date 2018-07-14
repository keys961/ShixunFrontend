package godshi.edu.cn.micropayment.activity.payment;

import android.os.Bundle;
import android.app.Activity;

import godshi.edu.cn.micropayment.R;

/**
 * GoodListActivity(Show list) -choose an item-> OrderActivity -> ConfirmPayActivity
 * GoodListActivity - User add list button to add a good
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
