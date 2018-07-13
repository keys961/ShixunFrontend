package godshi.edu.cn.micropayment.entity;

public class Order
{
    private long productId;

    private String payPassword;

    private int quantity;

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public long getProductId()
    {
        return productId;
    }

    public void setProductId(long productId)
    {
        this.productId = productId;
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
