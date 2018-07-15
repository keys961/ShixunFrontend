package godshi.edu.cn.micropayment.entity;

import android.os.Parcelable;

import java.io.Serializable;

public class Order implements Serializable
{
    private static final long serialVersionUID = 1000000L;

    private Product product;

    private String username;

    private String payPassword = null;

    private int quantity;

    private String address = "";

    private String phoneNumber = "";

    private String type; // AKS or NFC

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public Product getProduct()
    {
        return product;
    }

    public void setProduct(Product product)
    {
        this.product = product;
    }

    public String getPayPassword()
    {
        return payPassword;
    }

    public void setPayPassword(String payPassword)
    {
        this.payPassword = payPassword;
    }
}
