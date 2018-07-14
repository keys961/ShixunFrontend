package godshi.edu.cn.micropayment.activity.payment;

import android.os.Bundle;
import android.app.Activity;

import godshi.edu.cn.micropayment.R;

/**
 * NFCPayActivity -> ConfirmPayActivity (auto generate order)
 */
public class NFCPayActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcpay);
    }

}
