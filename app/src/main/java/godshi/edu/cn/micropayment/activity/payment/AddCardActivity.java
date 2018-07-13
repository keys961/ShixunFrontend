package godshi.edu.cn.micropayment.activity.payment;

import android.os.Bundle;
import android.app.Activity;

import godshi.edu.cn.micropayment.R;

public class AddCardActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
    }

    private class AddCardRunnable implements Runnable
    {
        private String cardNumber;

        private String password;

        public AddCardRunnable(String cardNumber, String password)
        {
            this.cardNumber = cardNumber;
            this.password = password;
        }

        @Override
        public void run()
        {

        }
    }
}
