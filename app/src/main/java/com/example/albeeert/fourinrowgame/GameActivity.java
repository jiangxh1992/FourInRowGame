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

import java.util.ArrayList;

/*** 1.记录双方棋局状态数据结构 ***/
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

/*** 2.棋子节点数据结构 ***/
class Piece{
    int value;        // 0:空 1:玩家1的棋子 2:玩家2的棋子
    ImageView image;  // 棋子图片

    // 构造
    Piece(){
        value = 0;
        image = null;
    }
    // 重置
    void Reset(){
        value = 0;
        image.setImageResource(R.drawable.empty);
    }
}

/*** 游戏界面 ***/
public class GameActivity extends AppCompatActivity {

    // 棋盘横竖棋子个数
    public static final int H_NUM = 7;
    public static final int V_NUM = 6;
    // 左部间隙
    private int LEFT_GAP = 10; // 至少10

    // 棋盘宽度(屏幕宽度)
    private float CBWidth = 0;
    // 棋盘单位宽度
    private int CellWidth = 0;

    // 获取xml的UI组件引用
    private Button ReStartButton = null;
    private Button WithDrawButton = null;
    private TextView StatusLeft = null;
    private TextView StatusRight = null;
    private ImageView IconLeft = null;
    private ImageView IconRight = null;
    private AbsoluteLayout CBLayout = null;

    // 玩家1和玩家2的棋局状态
    private CheckNode[] checkNode = null;

    // 当前游戏状态(0:平局 1:玩家1下棋 2:玩家2下棋 11:玩家1胜利 22:玩家2胜利)
    private int gameResult = 0;

    // 棋盘
    private Piece[][] ChessBord = null;
    // 用一个可变数组实现堆栈依次保存所下棋子的顺序
    ArrayList<Point>PieceStack = null;

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

        // UI 初始化
        ReStartButton = (Button)findViewById(R.id.button_restart);
        WithDrawButton = (Button)findViewById(R.id.button_withdraw);
        StatusLeft = (TextView)findViewById(R.id.status_left);
        StatusRight = (TextView)findViewById(R.id.status_right);
        IconLeft = (ImageView)findViewById(R.id.icon_left);
        IconRight = (ImageView)findViewById(R.id.icon_right);
        CBLayout = (AbsoluteLayout) findViewById(R.id.layout_chessboard);

        // 棋盘宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int ScreenW = dm.widthPixels;
        //int ScreenH = dm.heightPixels;

        CBWidth = ScreenW - 2*LEFT_GAP;
        CellWidth = (int)(CBWidth/H_NUM);

        // 棋盘二维数组
        ChessBord = new Piece[H_NUM][V_NUM];
        for (int h=0; h<H_NUM; h++){
            for (int v=0; v<V_NUM; v++){
                ChessBord[h][v] = new Piece();
            }
        }

        // 检测状态初始化
        checkNode = new CheckNode[2];
        checkNode[0] = new CheckNode();
        checkNode[1] = new CheckNode();

        // 绘制棋盘
        DrawChessBoard();

        // 监听重新开始游戏
        ReStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestartGame();
            }
        });
        // 开启悔棋监听
        WithDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 悔棋一步
                WithDraw();
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
                // 选用点击事件
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    // 横坐标

                    float touchX = event.getX() - LEFT_GAP;
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

        // 落子计数器
        pieceCount = 0;

        // 初始化栈
        PieceStack = new ArrayList<Point>();

    }

    /**
     * 游戏结束
     */
    public void GameOver(){
        // 停止游戏棋盘交互
        CBLayout.setOnTouchListener(null);
        // 清空栈
        PieceStack = null;
    }

    /**
     * 重新开始游戏
     */
    public void RestartGame(){

        // 重置棋盘
        for (int h=0; h<H_NUM; h++){
            for (int v=0; v<V_NUM; v++){
                ChessBord[h][v].Reset();
            }
        }
        // 重新初始化游戏
        GameInit();

    }

    /**
     * 绘制棋盘
     */
    public void DrawChessBoard(){

        // 绘制棋子洞和空棋子
        for(int i = 0 ; i<V_NUM ; i++){
            for (int j = 0 ; j<H_NUM ; j++){

                // 土壤
                ImageView view = new ImageView(this);
                view.setImageResource(R.drawable.chess_bg);
                view.setMaxHeight(CellWidth);
                view.setMaxWidth(CellWidth);
                view.setX(CellWidth*j+LEFT_GAP);
                view.setY(CellWidth*i);
                // 绘制到棋盘中
                CBLayout.addView(view);

                // 空棋子
                ImageView piece = new ImageView(this);
                piece.setImageResource(R.drawable.empty);
                piece.setMaxHeight(CellWidth);
                piece.setMaxWidth(CellWidth);
                piece.setX(CellWidth*j + LEFT_GAP);
                piece.setY(CellWidth*(V_NUM-i-1));
                // 棋子图片引用添加到棋盘数据
                ChessBord[j][i].image = piece;
                // 绘制棋子到棋盘
                CBLayout.addView(piece);

            }
        }

    }

    /**
     * 计算落子坐标
     */
    public Point CalCoord(int column){

        // 如果超出棋盘右边界视为最右边的一列
        if (column >= H_NUM)
            column = H_NUM-1;
        // 计算落子坐标
        for (int i = 0 ; i<V_NUM ; i++){
            if (ChessBord[column][i].value == 0){
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

        // 入栈
        PieceStack.add(point);
        // 计数器+1
        ++pieceCount;
        // 界面落子
        DrawPiece(point);
        // 游戏状态更新
        ChessBord[point.x][point.y].value = gameResult;

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
            IconLeft.setImageResource(R.drawable.chess_p1_win);
            IconRight.setImageResource(R.drawable.chess_p2_win);
        }else if (gameResult == 1){
            StatusLeft.setText("Your trun!");
            StatusRight.setText("");

            IconLeft.setImageResource(R.drawable.chess_p1_win);
            IconRight.setImageResource(R.drawable.chess_p2);
        }else if (gameResult == 2){
            StatusLeft.setText("");
            StatusRight.setText("Your turn!");

            IconLeft.setImageResource(R.drawable.chess_p1);
            IconRight.setImageResource(R.drawable.chess_p2_win);
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

        ImageView piece = ChessBord[point.x][point.y].image;
        if (gameResult == 1){
            piece.setImageResource(R.drawable.chess_p1);
        }else {
            piece.setImageResource(R.drawable.chess_p2);
        }

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
            if ((stopDir[0]==1) || (point.y + counter >= V_NUM) || ChessBord[point.x][point.y + counter].value != gameResult) {
                stopDir[0]=1;//向上超出棋盘或遇到非同色棋子终止
            } else {
                checkNode[gameResult - 1].Vertical++;//纵向+1
            }
            // 下
            if ((stopDir[1]==1) || (point.y - counter < 0) || ChessBord[point.x][point.y - counter].value != gameResult) {
                stopDir[1]=1;
            } else {
                checkNode[gameResult - 1].Vertical++;
            }
            // 左
            if ((stopDir[2]==1) || (point.x - counter < 0) || ChessBord[point.x - counter][point.y].value != gameResult) {
                stopDir[2]=1;
            } else {
                checkNode[gameResult - 1].Horizontal++;
            }
            // 右
            if ((stopDir[3]==1) || (point.x + counter >= H_NUM) || ChessBord[point.x + counter][point.y].value != gameResult) {
                stopDir[3]=1;
            } else {
                checkNode[gameResult - 1].Horizontal++;
            }

            // 左上
            if ((stopDir[4]==1) || (point.x - counter < 0) || (point.y + counter >= V_NUM) || (ChessBord[point.x - counter][point.y + counter].value != gameResult)) {
                stopDir[4]=1;
            } else {
                checkNode[gameResult - 1].ADiagonal++;
            }
            // 右下
            if ((stopDir[5]==1) || (point.x + counter >= H_NUM) || (point.y - counter < 0) || (ChessBord[point.x + counter][point.y - counter].value != gameResult)) {
                stopDir[5]=1;
            } else {
                checkNode[gameResult - 1].ADiagonal++;
            }
            // 右上
            if ((stopDir[6]==1) || (point.x + counter >= H_NUM) || (point.y + counter >= V_NUM) || (ChessBord[point.x + counter][point.y + counter].value != gameResult)) {
                stopDir[6]=1;
            } else {
                checkNode[gameResult - 1].MDiagonal++;
            }
            // 左下
            if ((stopDir[7]==1) || (point.x - counter < 0) || (point.y - counter < 0) || (ChessBord[point.x - counter][point.y - counter].value != gameResult)) {
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

    /**
     * 悔棋一步
     */
    public void WithDraw(){

        // 异常检测
        if (PieceStack == null || PieceStack.size() == 0)
            return;
        // 取出最后一个坐标并出站
        Point point = PieceStack.remove(PieceStack.size()-1);
        // 棋盘移除最后一步棋
        ChessBord[point.x][point.y].Reset();
        // 换人
        if (gameResult == 1){
            gameResult = 2;
            UpdateUI(gameResult);
        }else {
            gameResult = 1;
            UpdateUI(gameResult);
        }
        // 计数-1
        --pieceCount;
    }


}