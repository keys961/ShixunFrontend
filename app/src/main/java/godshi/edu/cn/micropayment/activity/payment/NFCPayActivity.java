package godshi.edu.cn.micropayment.activity.payment;

import android.nfc.NdefRecord;
import android.os.Bundle;
import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONObject;

import godshi.edu.cn.micropayment.R;
import godshi.edu.cn.micropayment.entity.Order;
import godshi.edu.cn.micropayment.entity.Product;
import godshi.edu.cn.micropayment.util.TextRecord;

/**
 * NFCPayActivity -> ConfirmPayActivity (auto generate order)
 */
public class NFCPayActivity extends Activity
{
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcpay);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
        {
            Toast.makeText(NFCPayActivity.this, "找不到NFC设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled())
        {
            Toast.makeText(NFCPayActivity.this, "请在设置中启用NFC设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 2 && resultCode == 1)
            finish();
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        Parcelable[] temp = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage ndefMessage = (NdefMessage) temp[0];
        NdefRecord ndefRecord = ndefMessage.getRecords()[0];
        String info = TextRecord.parse(ndefRecord).getText();
        try
        {
            JSONArray jsonArray = new JSONArray(info);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            //nfcInfo.setText(jsonObject.getString("id") + jsonObject.getString("price") + jsonObject.getString("pn") + jsonObject.getString("tn")); // TEST
            Product product = new Product();
            product.setId(Integer.parseInt(jsonObject.getString("id")));
            product.setPrice(Double.parseDouble(jsonObject.getString("price")));
            product.setProductName(jsonObject.getString("pn"));
            product.setTypeName(jsonObject.getString("tn"));

            Order order = new Order();
            order.setQuantity(1);
            order.setPhoneNumber("");
            order.setAddress("");
            order.setType("NFC");
            order.setUsername(getIntent().getStringExtra("username"));
            order.setProduct(product);

            Intent confirmIntent = new Intent(this, ConfirmPayActivity.class);
            confirmIntent.putExtra("order", order);

            startActivityForResult(confirmIntent, 2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
