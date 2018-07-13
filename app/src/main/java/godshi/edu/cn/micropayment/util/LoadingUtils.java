package godshi.edu.cn.micropayment.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import godshi.edu.cn.micropayment.R;

public class LoadingUtils
{
    private static AlertDialog longinDialog;

    public static void show(Context context)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View longinDialogView = layoutInflater.inflate(R.layout.layout_loading, null);
        longinDialog = new AlertDialog.Builder(context).setView(longinDialogView).create();
        longinDialog.show();
    }

    public static void cancel()
    {
        longinDialog.cancel();
    }

    @Override
    protected void finalize()
    {
        longinDialog.cancel();
    }
}
