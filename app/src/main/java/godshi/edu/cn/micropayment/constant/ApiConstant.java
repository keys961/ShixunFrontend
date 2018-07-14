package godshi.edu.cn.micropayment.constant;

public class ApiConstant
{
    //TODO: Finish url configurations
    public static final String API_PREFIX = "http://192.168.50.20:8000";

    public static final String API_LOGIN = API_PREFIX + "/account/login/";

    public static final String API_REGISTER = API_PREFIX + "/account/register/";

    public static final String API_CHANGE_PASSWORD = API_PREFIX + "/account/editPassword/";

    public static final String API_GET_ALL_CARDS = API_PREFIX + "/account/card/list/";

    public static final String API_ADD_CARD = API_PREFIX + "/account/card/bind/";

    public static final String API_REMOVE_CARD = API_PREFIX + "/account/card/unbind/";

    public static final String API_GET_ALL_PRODUCTS = API_PREFIX + "/product/all/";

}
