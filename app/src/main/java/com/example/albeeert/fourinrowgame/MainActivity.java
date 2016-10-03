package com.example.albeeert.fourinrowgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // 开始游戏按钮
    public Button startGameBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化
        setContentView(R.layout.activity_main);

        // 获取开始游戏按钮
        startGameBtn = (Button)findViewById(R.id.startbutton);
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
    }
}
