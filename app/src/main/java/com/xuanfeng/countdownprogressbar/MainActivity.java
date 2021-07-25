package com.xuanfeng.countdownprogressbar;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xuanfeng.countdownprogressview.OvalProgressBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OvalProgressBar count_down = findViewById(R.id.count_down);
        count_down.setOnCountDownFinishListener(new OvalProgressBar.OnCountDownFinishListener() {
            @Override
            public void countDownFinished() {
                Toast.makeText(MainActivity.this, "计时结束", Toast.LENGTH_SHORT).show();
            }
        });
        count_down.startCountDown();
        count_down.removeOnCountDownFinishListener();
    }
}
