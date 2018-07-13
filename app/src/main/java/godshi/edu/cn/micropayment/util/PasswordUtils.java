package godshi.edu.cn.micropayment.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class PasswordUtils
{
    /**
     * Convert the value to SHA-1 message digest
     * @param value: Init value.
     * @return SHA-1 message digest.
     */
    public static String getMessageDigest(String value)
    {
        return encrypt(value);
    }

    private static String encrypt(String value)
    {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(value.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
            formatter.format("%02x", b);
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
