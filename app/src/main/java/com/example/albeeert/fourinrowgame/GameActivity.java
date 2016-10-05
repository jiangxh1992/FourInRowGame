package com.example.albeeert.fourinrowgame;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

// 记录双方棋局状态
class CheckNode{
    int Horizontal; // 水平方向上相同棋子的个数
    int Vertical;   // 竖直方向...
    int MDiagonal;  // 主对角线...
    int ADiagonal;  // 辅对角线...

    // 构造
    CheckNode(){
        Horizontal = 1;
        Vertical = 1;
        MDiagonal = 1;
        ADiagonal = 1;
    }
    // 重置
    void Reset(){
        Horizontal = 1;
        Vertical = 1;
        MDiagonal = 1;
        ADiagonal = 1;
    }
}

public class GameActivity extends AppCompatActivity {

    // 棋盘横竖棋子个数
    public static final int H_NUM = 7;
    public static final int V_NUM = 6;
    // 上部间隙
    public static final int TOP_GAP = 250;

    // 棋盘宽度(屏幕宽度)
    private float CBWidth = 0;
    // 棋盘高度
    private float CBHeight = 0;
    // 棋盘单位宽度
    private float CellWidth = 0;

    // 获取xml的UI组件引用
    private Button ReStartButton = null;
    private TextView StatusLeft = null;
    private TextView StatusRight = null;
    private ImageView IconLeft = null;
    private ImageView IocnRight = null;
    private ImageView ImageCave = null;
    private AbsoluteLayout CBLayout = null;

    // 玩家1和玩家2的棋局状态
    private CheckNode[] checkNode = null;

    // 当前游戏状态(0:平局 1:玩家1下棋 2:玩家2下棋 11:玩家1胜利 22:玩家2胜利)
    private int gameResult = 0;

    // 棋盘状态(0:空 1:玩家1的棋子 2:玩家2的棋子)
    private int[][] ChessBord = null;

    // 落子计数器
    private int pieceCount = 0;

    /**
     * 游戏界面初始化入口
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 基础页面
        setContentView(R.layout.activity_game);
        // 全局初始化
        ActivityInit();
        // 游戏初始化
        GameInit();

    }

    /**
     * 全局初始化(只初始化一次)
     */
    public void ActivityInit(){
        // 棋盘宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        CBWidth = dm.widthPixels;
        CBHeight = CBWidth/H_NUM*V_NUM;
        CellWidth = CBWidth/H_NUM;

        // UI 初始化
        ReStartButton = (Button)findViewById(R.id.button_restart);
        StatusLeft = (TextView)findViewById(R.id.status_left);
        StatusRight = (TextView)findViewById(R.id.status_right);
        IconLeft = (ImageView)findViewById(R.id.icon_left);
        IocnRight = (ImageView)findViewById(R.id.icon_right);
        ImageCave = (ImageView)findViewById(R.id.image_cave);
        CBLayout = (AbsoluteLayout) findViewById(R.id.layout_chessboard);

        // 检测状态初始化
        checkNode = new CheckNode[2];
        checkNode[0] = new CheckNode();
        checkNode[1] = new CheckNode();

        // 监听重新开始游戏
        ReStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestartGame();
            }
        });
    }

    /**
     * 游戏初始化(每次重新游戏都要初始化)
     */
    public void GameInit(){

        // 开始游戏监听
        CBLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 选用抬起的事件
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    // 横坐标
                    float touchX = event.getX();
                    // 列数
                    int column = (int)(touchX/CellWidth);
                    // 计算并落子
                    SetPiece(CalCoord(column));
                }
                return false;
            }
        });

        // 默认玩家1先手
        gameResult = 1;
        UpdateUI(1);

        // 棋盘二维数组,默认初始化元素为0
        ChessBord = new int[H_NUM][V_NUM];

        // 落子计数器
        pieceCount = 0;

        // 绘制棋盘
        DrawChessBoard();
    }

    /**
     * 游戏结束
     */
    public void GameOver(){
        CBLayout.setOnTouchListener(null);
        // 释放棋盘数据
        ChessBord = null;
    }

    /**
     * 重新开始游戏
     */
    public void RestartGame(){
        // 移除棋盘棋子界面
        CBLayout.removeAllViews();
        // 重新初始化游戏
        GameInit();

    }

    /**
     * 绘制棋盘
     */
    public void DrawChessBoard(){

        // 棋子
        ImageView left = new ImageView(this);
        left.setImageResource(R.drawable.chess_p1);
        left.setMaxWidth((int)(CellWidth));
        left.setMaxWidth((int)(CellWidth));
        left.setMinimumWidth((int)(CellWidth));
        left.setMinimumWidth((int)(CellWidth));
        left.setY(5);
        left.setX(0);
        //CBLayout.addView(left);

        // 绘制棋子洞
        for(int i = 0 ; i<V_NUM ; i++){
            for (int j = 0 ; j<H_NUM ; j++){
                ImageView view = new ImageView(this);
                view.setImageResource(R.drawable.chess_bg);
                view.setMaxWidth((int)(CellWidth));
                view.setMaxWidth((int)(CellWidth));
                view.setMinimumWidth((int)(CellWidth));
                view.setMinimumWidth((int)(CellWidth));

                view.setX(CellWidth*j);
                view.setY(CellWidth*i+TOP_GAP);

                CBLayout.addView(view);
            }
        }

    }

    /**
     * 计算落子坐标
     */
    public Point CalCoord(int column){
        // 计算落子坐标
        for (int i = 0 ; i<V_NUM ; i++){
            if (ChessBord[column][i] == 0){
                Point point = new Point(column,i);
                return point;
            } else {
                continue;
            }
        }
        return null;
    }

    /**
     * 落子并计算更新游戏状态
     */
    public void SetPiece(Point point){
        // 该列棋子已落满或者程序出错
        if (point == null)
            return;

        // 计数器+1
        ++pieceCount;
        // 界面落子
        DrawPiece(point);
        // 游戏状态更新
        ChessBord[point.x][point.y] = gameResult;

        // 检查输赢情况
        // 是否已平局
        if (pieceCount == V_NUM*H_NUM){
            // 平局
            UpdateUI(0);
            // 游戏结束
            GameOver();
            // 终止
            return;
        }
        // 是否有人赢
        int result = CheckResult(point);
        // 重置检测状态
        checkNode[0].Reset();
        checkNode[1].Reset();

        if (result == 0){
            // 换人继续
            if (gameResult == 1){
                gameResult = 2;
                UpdateUI(gameResult);
            }else {
                gameResult = 1;
                UpdateUI(gameResult);
            }
        }else {

            if (gameResult == 1)
                UpdateUI(11);
            else
                UpdateUI(22);
             // 游戏结束
            GameOver();
        }

    }

    /**
     * 更新UI显示状态
     */
    public void UpdateUI(int gameResult){
        if (gameResult == 0){
            StatusLeft.setText("Draw");
            StatusRight.setText("Draw");
        }else if (gameResult == 1){
            StatusLeft.setText("Your trun!");
            StatusRight.setText("");
        }else if (gameResult == 2){
            StatusLeft.setText("");
            StatusRight.setText("Your turn!");
        }else if(gameResult == 11){
            StatusLeft.setText("Your Win!");
            StatusRight.setText("");
        }else if (gameResult == 22){
            StatusLeft.setText("");
            StatusRight.setText("Your Win!");
        }else {
            return;
        }
    }


    /**
     * 绘制指定棋子
     */
    public void DrawPiece(Point point){
        ImageView piece = new ImageView(this);
        if (gameResult == 1){
            piece.setImageResource(R.drawable.chess_p1);
        }else {
            piece.setImageResource(R.drawable.chess_p2);
        }

        piece.setMaxWidth((int)(CellWidth));
        piece.setMaxWidth((int)(CellWidth));
        piece.setMinimumWidth((int)(CellWidth));
        piece.setMinimumWidth((int)(CellWidth));

        piece.setX(CellWidth*point.x);
        piece.setY(CellWidth*(V_NUM-point.y-1)+TOP_GAP);

        CBLayout.addView(piece);
    }

    /**
     * 检查某个棋子处各个方向上相同棋子的个数,用于判断游戏胜负(返回0表示游戏继续,1表示玩家1赢,2表示玩家2赢)
     */
    public int CheckResult(Point point) {
        // 检查水平和竖直方向
        int stopCount = 0; // 8个方向到边界的个数
        int[] stopDir = new int[8];
        int counter = 0;
        while (stopCount < 8) {
            stopCount = 0;
            ++counter;
            // 上
            if ((stopDir[0]==1) || (point.y + counter >= V_NUM) || ChessBord[point.x][point.y + counter] != gameResult) {
                stopDir[0]=1;//向上超出棋盘或遇到非同色棋子终止
            } else {
                checkNode[gameResult - 1].Vertical++;//纵向+1
            }
            // 下
            if ((stopDir[1]==1) || (point.y - counter < 0) || ChessBord[point.x][point.y - counter] != gameResult) {
                stopDir[1]=1;
            } else {
                checkNode[gameResult - 1].Vertical++;
            }
            // 左
            if ((stopDir[2]==1) || (point.x - counter < 0) || ChessBord[point.x - counter][point.y] != gameResult) {
                stopDir[2]=1;
            } else {
                checkNode[gameResult - 1].Horizontal++;
            }
            // 右
            if ((stopDir[3]==1) || (point.x + counter >= H_NUM) || ChessBord[point.x + counter][point.y] != gameResult) {
                stopDir[3]=1;
            } else {
                checkNode[gameResult - 1].Horizontal++;
            }

            // 左上
            if ((stopDir[4]==1) || (point.x - counter < 0) || (point.y + counter >= V_NUM) || (ChessBord[point.x - counter][point.y + counter] != gameResult)) {
                stopDir[4]=1;
            } else {
                checkNode[gameResult - 1].ADiagonal++;
            }
            // 右下
            if ((stopDir[5]==1) || (point.x + counter >= H_NUM) || (point.y - counter < 0) || (ChessBord[point.x + counter][point.y - counter] != gameResult)) {
                stopDir[5]=1;
            } else {
                checkNode[gameResult - 1].ADiagonal++;
            }
            // 右上
            if ((stopDir[6]==1) || (point.x + counter >= H_NUM) || (point.y + counter >= V_NUM) || (ChessBord[point.x + counter][point.y + counter] != gameResult)) {
                stopDir[6]=1;
            } else {
                checkNode[gameResult - 1].MDiagonal++;
            }
            // 左下
            if ((stopDir[7]==1) || (point.x - counter < 0) || (point.y - counter < 0) || (ChessBord[point.x - counter][point.y - counter] != gameResult)) {
                stopDir[7]=1;
            } else {
                checkNode[gameResult - 1].MDiagonal++;
            }

            // 终止循环搜索
            for(int i =0 ; i<8 ;i++){
                stopCount += stopDir[i];
            }
        }

        // 判断是否有人赢
        if (checkNode[gameResult-1].MDiagonal>=4 || checkNode[gameResult-1].ADiagonal>=4 || checkNode[gameResult-1].Horizontal>=4 || checkNode[gameResult-1].Vertical>=4){
            return gameResult;
        }else {
            return 0;
        }

    }

}