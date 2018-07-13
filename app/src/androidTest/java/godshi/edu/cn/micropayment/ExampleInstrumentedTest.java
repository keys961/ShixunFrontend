package godshi.edu.cn.micropayment;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import godshi.edu.cn.micropayment.constant.MessageKeyConstant;
import godshi.edu.cn.micropayment.util.HttpUtils;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest
{
    @Test
    public void useAppContext()
    {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("godshi.edu.cn.micropayment", appContext.getPackageName());
    }

    @Test
    @Ignore
    public void httpClientGetTest()
    {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Message message = HttpUtils.doGet("http://www.zju.edu.cn", new HashMap<>());
        assertNotNull(message);
        Log.i("info", Boolean.toString(message.getData().getBoolean(MessageKeyConstant.STATUS)));
    }

    @Test
    @Ignore
    public void httpClientPostTest()
    {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Map<String, String> map = new HashMap<>();
        map.put("username", "123455");
        map.put("password", "23123213");
        Message message = HttpUtils.doPost("http://10.180.91.84:8080/test", map);
        assertNotNull(message);
        Log.i("msg", message.getData().getString(MessageKeyConstant.BODY));
    }
}
