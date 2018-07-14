package godshi.edu.cn.micropayment.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductCollectionHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "app.db";

    public static final String TABLE_NAME = "product_collection";

    private static final int DB_VERSION = 1;

    private static final String CREATE_SCRIPT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + " (id integer PRIMARY KEY AUTOINCREMENT, "
            + "username text, "
            + "productId integer, "
            + "productName text, "
            + "price real, "
            + "typeName text)";

    public ProductCollectionHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}
