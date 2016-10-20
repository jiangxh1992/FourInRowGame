package com.example.albeeert.fourinrowgame;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // 数据存储
    public final String PREFS_NAME = "JXHFile";

    // 云朵
    private ImageView cloud1 = null;
    private ImageView cloud2 = null;
    private ImageView cloud3 = null;
    // 云朵移动速度(值不可太大否则看上去不流畅)
    private final float cloudspeed1 = 8.0f;
    private final float cloudspeed2 = 15.0f;
    private final float cloudspeed3 = 12.0f;
    // 游戏准备图片
    private ImageView ready = null;
    // 开始游戏按钮和玩家数据按钮
    private Button startGameBtn = null;
    private Button dataButton = null;
    // 对话框
    private View dialogView;
    // 弹出窗口
    private Dialog dialog;


    // 按钮浮动幅度
    private final float range = 10.0f;
    // 计时间隔
    private final int Interval = 100;
    // sin角度
    private double sinAngle = 0;
    // 开始按钮的Y坐标
    private float initY = 0;

    // 屏幕宽高
    private float ScreenW = 0;
    private float ScreenH = 0;

    // 动画线程
    private MyThread animationThread = null;
    // 当前页面是否活跃
    private boolean isActive = true;

    // 音乐播放器
    private static MediaPlayer media_bg;    // 背景音乐
    private static MediaPlayer media_start; // 游戏开始音乐
    private static MediaPlayer media_button; // 按钮音效

    /**
     * Activity life cycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 界面初始化
        UIInit();
        // 音效设置
        AudioInit();
        // 事件监听
        setListener();

        // 开始按钮浮动动画
        animationThread = new MyThread();
        new Thread(animationThread).start();
    }

    // 离开当前mainactivity
    @Override
    protected void onPause() {
        super.onPause();
        // 暂停背景音乐
        media_bg.pause();
        // 隐藏准备图片
        ready.setVisibility(View.INVISIBLE);
        isActive = false;
    }

    // 回到mainactivity
    @Override
    protected void onRestart() {
        super.onRestart();
        isActive = true;
        // 继续背景音乐
        media_bg.start();
        // 开始按钮浮动动画
        new Thread(animationThread).start();
    }

    // 返回键
    @Override
    public void onBackPressed(){
        System.exit(0);// 退出程序
    }

    /**
     *  界面初始化
     */
    public void UIInit(){
        // 基本静态界面
        setContentView(R.layout.activity_main);

        // 获取开始游戏按钮
        startGameBtn = (Button)findViewById(R.id.startbutton);
        // 玩家数据按钮
        dataButton = (Button)findViewById(R.id.databutton);
        // 云朵
        cloud1 = (ImageView) findViewById(R.id.cloud1);
        cloud2 = (ImageView) findViewById(R.id.cloud2);
        cloud3 = (ImageView) findViewById(R.id.cloud3);
        // 游戏准备图片
        ready = (ImageView) findViewById(R.id.start_ready);
        // 对话框
        dialogView= LayoutInflater.from(this).inflate(
                R.layout.data_dialog, null);
        dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Player Data");
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(true);

        // 屏幕尺寸
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenH = dm.heightPixels;
        ScreenW = dm.widthPixels;
        // 开始按钮起始位置
        initY = ScreenH*2/3;
        //startGameBtn.setX(ScreenW*3/8);
        //startGameBtn.setY(initY);
        // 玩家数据按钮起始位置
        //dataButton.setX(ScreenW-170);
        //dataButton.setY(ScreenH-150);
    }

    /**
     * 音效设置
     */
    public void AudioInit(){
        // 1.背景音乐
        media_bg = MediaPlayer.create(this, R.raw.menu_bg);
        media_bg.start();
        // 实现循环播放
        media_bg.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                media_bg.start();
            }
        });

        // 2.开始按钮音效
        media_start = MediaPlayer.create(this, R.raw.zombiesmile);
        // 监听音效结束事件
        media_start.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 跳转到游戏界面
                Intent intent = new Intent(getBaseContext(),GameActivity.class);
                startActivity(intent);
            }
        });

        // 3.普通按钮音效
        media_button = MediaPlayer.create(this, R.raw.yoho);
    }

    /**
     * 事件监听
     */
    public void setListener(){
        // 开始游戏按钮点击事件
        startGameBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // 播放音效
                                                media_start.start();
                                                // 显示准备图片
                                                ready.setVisibility(View.VISIBLE);
                                                // 停止动画
                                                isActive = false;
                                            }
                                        }
        );

        // 玩家数据按钮点击事件
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 播放音效
                media_button.start();
                // 获取本地数据
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
                int dataP1 = sharedPreferences.getInt("data_player1",0);
                int dataP2 = sharedPreferences.getInt("data_player2",0);
                //System.out.print("datap1:"+dataP1+";  "+"datap2:"+dataP2+"\n");

                // 更新对话框文字数据
                TextView player1DataShow = (TextView) dialogView.findViewById(R.id.player1datashow);
                TextView player2DataShow = (TextView) dialogView.findViewById(R.id.player2datashow);
                player1DataShow.setText("PLAYER1 WIN:  "+dataP1);
                player2DataShow.setText("PLAYER2 WIN:  "+dataP2);

                // 弹出窗口
                dialog.show();

            }
        });
    }

    /**
     * 线程类
     */
    class MyThread implements Runnable {
        @Override
        public void run() {

            // update
            while (isActive) {
                try {
                    Thread.sleep(Interval);
                    // 1.开始按钮浮动
                    sinAngle += 0.3f;
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
