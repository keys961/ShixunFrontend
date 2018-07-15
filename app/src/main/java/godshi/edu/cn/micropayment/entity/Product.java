package godshi.edu.cn.micropayment.entity;

import java.io.Serializable;

public class Product implements Serializable
{
    private static final long serialVersionUID = 1000001L;
    // Product id
    private long id;

    private String productName;

    private double price;

    private String typeName;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }
}
