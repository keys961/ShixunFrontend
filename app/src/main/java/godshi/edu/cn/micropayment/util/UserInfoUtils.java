package godshi.edu.cn.micropayment.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInfoUtils
{
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

    public static boolean isUsernameValid(String username) //first alphabet, next alphanumeric 6-20
    {
        return username.matches("[a-zA-Z][0-9a-zA-Z_]{5,19}");
    }

    public static boolean isPasswordValid(String password) //length from 6-20 inclusive
    {
        return password != null && password.length() >= 6 && password.length() <= 20;
    }

    public static boolean isEmailValid(String email)
    {
        if (null == email || "".equals(email))
            return false;

        Matcher m = EMAIL_PATTERN.matcher(email);
        return m.matches();
    }
}
