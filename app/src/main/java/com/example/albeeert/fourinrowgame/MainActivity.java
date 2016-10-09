package com.example.albeeert.fourinrowgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    // 云朵
    private ImageView cloud1 = null;
    private ImageView cloud2 = null;
    private ImageView cloud3 = null;
    // 云朵移动速度(值不可太大否则看上去不流畅)
    private final float cloudspeed1 = 8.0f;
    private final float cloudspeed2 = 15.0f;
    private final float cloudspeed3 = 12.0f;

    // 开始游戏按钮
    private Button startGameBtn = null;
    // 按钮浮动幅度
    private final float range = 20.0f;
    // 计时间隔
    private final int Interval = 100;
    // sin角度
    private double sinAngle = 0;
    // 开始按钮的Y坐标
    private float initY = 0;

    // 屏幕宽高
    private float ScreenW = 0;
    private float ScreenH = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化
        setContentView(R.layout.activity_main);

        // 获取开始游戏按钮
        startGameBtn = (Button)findViewById(R.id.startbutton);
        // 云朵
        cloud1 = (ImageView) findViewById(R.id.cloud1);
        cloud2 = (ImageView) findViewById(R.id.cloud2);
        cloud3 = (ImageView) findViewById(R.id.cloud3);

        // Y坐标
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenH = dm.heightPixels;
        ScreenW = dm.widthPixels;
        initY = ScreenH/3*2;

        // 添加按钮点击事件
        startGameBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // 跳转到游戏界面
                                                Intent intent = new Intent(getBaseContext(),GameActivity.class);
                                                startActivity(intent);
                                            }
                                        }
        );

        // 开始按钮浮动动画
        new Thread(new MyThread()).start();

    }

    /**
     * 线程类
     */
    class MyThread implements Runnable {
        @Override
        public void run() {
            // update
            while (true) {
                try {
                    // 1.开始按钮浮动
                    sinAngle += 0.5f;
                    Thread.sleep(Interval);
                    startGameBtn.setY(initY + (float)(range*Math.sin(sinAngle)));

                    // 2.云朵移动
                    if (cloud1.getX() < -500)
                        cloud1.setX(ScreenW + 10.0f);
                    cloud1.setX(cloud1.getX() - cloudspeed1);

                    if (cloud2.getX() > ScreenW)
                        cloud2.setX(-1000.0f);
                    cloud2.setX(cloud2.getX()+cloudspeed2);

                    if (cloud3.getX() > ScreenW)
                        cloud3.setX(-1200.0f);
                    cloud3.setX(cloud3.getX()+cloudspeed3);

                }catch (Exception e) {
                    e.printStackTrace();
                    System.out.print("thread error!");
                }
            }
        }
    }
}
