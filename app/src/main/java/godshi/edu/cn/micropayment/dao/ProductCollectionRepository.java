package godshi.edu.cn.micropayment.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import godshi.edu.cn.micropayment.db.ProductCollectionHelper;
import godshi.edu.cn.micropayment.entity.Product;
import godshi.edu.cn.micropayment.entity.User;
import godshi.edu.cn.micropayment.entity.UserFavorProduct;

public class ProductCollectionRepository
{
    private Context context;

    private ProductCollectionHelper helper;

    public ProductCollectionRepository(Context context)
    {
        this.context = context;
        helper = new ProductCollectionHelper(context);
    }

    public void insert(UserFavorProduct product)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new ContentValues();
        values.put("username", product.getUsername());
        values.put("productId", product.getProductId());
        values.put("productName", product.getProductName());
        values.put("price", product.getPrice());
        values.put("typeName", product.getTypeName());

        db.insert(ProductCollectionHelper.TABLE_NAME,
                null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void delete(String username, long productId)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        db.delete(ProductCollectionHelper.TABLE_NAME,
                "username = ? and productId = ?",
                new String[]{ username, Long.toString(productId) });
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public UserFavorProduct query(String username, long productId)
    {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " + ProductCollectionHelper.TABLE_NAME + " WHERE username = ?" +
                " AND productId = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {username, Long.toString(productId)});
        if(cursor.getCount() < 1)
            return null;

        cursor.moveToNext();
        UserFavorProduct product = parse(cursor);
        cursor.close();

        return product;
    }

    public List<UserFavorProduct> queryAll(String username)
    {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM " + ProductCollectionHelper.TABLE_NAME + " WHERE username = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {username});
        if(cursor.getCount() < 1)
            return new ArrayList<>(1);
        List<UserFavorProduct> list = new ArrayList<>(20);
        while(cursor.moveToNext())
        {
            UserFavorProduct product = parse(cursor);
            list.add(product);
        }

        cursor.close();
        return list;
    }

    private UserFavorProduct parse(Cursor cursor)
    {
        UserFavorProduct product = new UserFavorProduct();
        product.setId(cursor.getInt(0));
        product.setUsername(cursor.getString(1));
        product.setProductId(cursor.getLong(2));
        product.setProductName(cursor.getString(3));
        product.setPrice(cursor.getDouble(4));
        product.setTypeName(cursor.getString(5));

        return product;
    }
}
