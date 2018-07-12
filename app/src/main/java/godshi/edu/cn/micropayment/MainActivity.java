package godshi.edu.cn.micropayment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

import godshi.edu.cn.micropayment.activity.LoginActivity;

public class MainActivity extends Activity
{

    private static final int TIME_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent loginIntent = new Intent(this, LoginActivity.class);
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                startActivity(loginIntent);
                MainActivity.this.finish();
            }
        };

        timer.schedule(task, TIME_DELAY);
    }
}
