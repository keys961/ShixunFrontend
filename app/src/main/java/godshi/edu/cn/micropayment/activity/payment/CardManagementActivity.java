package godshi.edu.cn.micropayment.activity.payment;

import android.os.Bundle;
import android.app.Activity;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.entity.User;

public class CardManagementActivity extends Activity
{
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_management);
        user = new User();
        
    }

}
