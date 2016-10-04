package com.example.albeeert.fourinrowgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.PublicKey;

// 记录双方棋局状态
class CheckNode{
    int Horizontal; // 水平方向上相同棋子的个数
    int Vertical;   // 竖直方向...
    int MDiagonal;  // 主对角线...
    int ADiagonal;  // 辅对角线...

    CheckNode(){
        Horizontal = 0;
        Vertical = 0;
        MDiagonal = 0;
        ADiagonal = 0;
    }
}

public class GameActivity extends AppCompatActivity {

    // 棋盘横竖棋子个数
    public static final int H_NUM = 7;
    public static final int V_NUM = 6;

    // 棋盘宽度(屏幕宽度)
    private float CBWidth;
    // 棋盘高度(CBWidth/H_NUM*V_NUM)
    private float CBHeight;

    // 获取xml的UI组件引用
    private TextView StatusLeft;
    private TextView StatusRight;
    private ImageView IconLeft;
    private ImageView IocnRight;
    private AbsoluteLayout CBLayout;

    // 玩家1和玩家2的棋局状态
    CheckNode checkNode_P1;
    CheckNode checkNode_P2;

    // 当前游戏状态(0:平局 1:玩家1下棋 2:玩家2下棋 11:玩家1胜利 22:玩家2胜利)
    int gameResult;

    // 棋盘状态
    int[][] ChessBord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 基础页面
        setContentView(R.layout.activity_game);
        // 游戏初始化
        GameInit();
        // 绘制棋盘
        DrawChessBoard();

    }

    /**
     * 游戏初始化
     */
    public  void  GameInit(){
        // 棋盘宽高
        WindowManager wm = this.getWindowManager();
        CBWidth = wm.getDefaultDisplay().getWidth();
        CBHeight = CBWidth/H_NUM*V_NUM;

        // UI 初始化
        StatusLeft = (TextView)findViewById(R.id.status_left);
        StatusRight = (TextView)findViewById(R.id.status_right);
        IconLeft = (ImageView)findViewById(R.id.icon_left);
        IocnRight = (ImageView)findViewById(R.id.icon_right);
        CBLayout = (AbsoluteLayout)findViewById(R.id.layout_chessboard);

        // 变量初始化
        checkNode_P1 = new CheckNode();
        checkNode_P2 = new CheckNode();

        gameResult = 1;// 默认玩家1先手

        ChessBord = new int[H_NUM][V_NUM];//棋盘二维数组,默认初始化元素为0
        System.out.print(ChessBord);
    }

    /**
     * 绘制棋盘
     */
    public void DrawChessBoard(){
        for(int i = 0 ; i<=H_NUM ; ++i){
            for (int j = 0 ; j<=V_NUM ; ++j){
                ImageView view = new ImageView(this);
                view.setImageResource(R.drawable.chess_bg);
                view.setX(CBWidth/(H_NUM+1)*i);
                view.setY(CBHeight/(V_NUM)*j);
                CBLayout.addView(view);
            }
        }
    }

    /**
     * 检查某个棋子处各个方向上相同棋子的个数,用于判断游戏胜负
     */

}
