package godshi.edu.cn.micropayment.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionUtils
{
    public static boolean isConnectedToNetwork(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
                return info.getState() == NetworkInfo.State.CONNECTED;
        }

        return false;
    }
}
