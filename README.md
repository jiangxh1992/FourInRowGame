---
layout: post
title:  "JAVA安卓植物大战僵尸主题四子棋游戏"
categories: [android]
tags: [android]

---

### @前言

1. 这里使用安卓最基本的API实现双人四子棋游戏（无AI），开发语言为java，开发环境为Android Studio 2.1.2，目标SDK版本为24，最低为15；

2. 界面采用植物大战僵尸主题，图片资源来源于网络，进行了PS加工，非原创；

3. 游戏界面基本可以适配所有安卓手机分辨率，不过在分辨率太大或太小的手机上整体效果会有影响；



***Github源码：*** [https://github.com/jiangxh1992/FourInRowGame](https://github.com/jiangxh1992/FourInRowGame)

***视频演示：*** [https://vimeo.com/187532089](https://vimeo.com/187532089)

***

### @更新：

这里另外使用表格布局的方式又写了一个四子棋基础版本，逻辑思路和本文的也不一样了，表格布局虽然没有绝对布局灵活，但容易适配屏幕操作简单。

***github源码：***[https://github.com/jiangxh1992/Connect4Game](https://github.com/jiangxh1992/Connect4Game)

***

### 1.游戏规则功能目标和界面预览

四子棋的游戏规则和五子棋不同，虽然都是横向纵向或对角线方向上有五个或四个相同的棋子即胜利，但四子棋落子有限制，必须要从棋盘底部往上堆叠，不是可以下在任意位置，就相当于将棋盘竖立起来一样，同时四子棋的棋盘是6x7宫格，横向7个，纵向6个，因此出现平局的现象很正常。

这里游戏首先有一个开始界面，点击开始界面的开始游戏按钮进入游戏界面，游戏界面需要实现**提示玩家轮流下子**，以及**胜利**和**平局**等状态。

![这里写图片描述](http://img.blog.csdn.net/20161023160701873)

![这里写图片描述](http://img.blog.csdn.net/20161023160719066)

![这里写图片描述](http://img.blog.csdn.net/20161023160731972)

### 2.UI界面布局设计

基本的静态界面使用xml来布局，开始界面整体使用绝对布局（为了方便实现小动画），放一张背景图片，开始按钮按照sin函数上下浮动，天空中几朵云按照不同的速度来回飘动：

![这里写图片描述](http://img.blog.csdn.net/20161021181202078)

游戏界面主要分三部分，头部，棋盘和底部，背景图片被PS分割成三张图片作为三部分的背景，三部分按照权重高度比例为**1：5：2**，这样**棋盘高度占屏幕高度的5/8,手机屏幕宽高比最大一般就是10：16了，这种情况下棋盘高度刚好和宽度相等，而棋盘实际宽高为6：7，可以保证棋盘足以放下所有棋子了**。布局上头部使用相对布局，主要放一些按钮；棋盘因为需要获取点击的坐标进行精确定位，所以要使用绝对布局；底部布局都可以只要将剩余的空间填满就好。

![这里写图片描述](http://img.blog.csdn.net/20161017221501194)

### 3.开始界面逻辑实现（动画）

为了实现开始界面的小动画，单独**开一个线程**，线程放一个while循环结合sleep函数制造一个update定时更新环境：

对于开始按钮，使其按照**sin正弦函数**上下摆动，通过**振幅参数**控制上下摆动的幅度，以及每次更新增长的**步幅参数**来控制上下摆动的速度；

乌云动画是在界面上布置三个ImageView，然后在每次更新中按照**速度参数**水平移动云彩的位置，通过速度参数控制云移动的速度和方向，当云超出屏幕足够远时将其重置回合适的位置，调整让三朵云的移动速度不同从而制造一种交错运动的自然效果。

### 4.游戏界面逻辑实现

#### **1.棋盘的绘制更新**

这里棋盘中棋子的更换采用图片资源替换的方式，即游戏初始化时将棋盘每个棋子的位置都放置一个ImageView，图片为一张透明图片，当需要放置棋子或者更换更换棋子时，只要给出指定坐标位置，然后根据该坐标对应的棋子状态替换相应的图片资源即可。

整个棋盘的数据模型是一个二维数组，数组元素的类型是一个自定义的数据结构(包含棋子状态值和对应ImageView的引用)：

```java
/*** 棋子节点数据结构 ***/
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
```

棋盘模型：

```java
// 棋盘
private Piece[][] ChessBord = null;
```

棋盘初始化时：

```java
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
```

更新棋子图片的方法：

```java
ChessBord[point.x][point.y].image.setImageResource(R.drawable.chess_p1_win);
```

#### **2.玩家下子事件**

首先是获取在棋盘中玩家触点的坐标，坐标系原点在绝对布局的棋盘的左上角，根据坐标可以计算玩家选择的是哪一列：然后根据当前期盼状态可以定位到某个棋子，然后可以更新棋盘状态和棋盘界面：

```java
// 开始游戏监听,其中CBLayout为棋盘的绝对布局引用，LEFT_GAP是让左边空出一部分，因为背景图片左右两边有部分墙体内容
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
```

#### **3.判断是否有人赢**

每当有人下棋时都要进行一次是否有人胜利检查，这里定义一个数据结构来记录以当前棋子位置为中心在横向、纵向、两个对角线四个方向上的相同棋子的个数，初始值都为1因为已经中心位置已经有一个棋子了，统计结束后如果四个方向上有数量大于4的则当前玩家胜利：

```java
/*** 记录双方棋局状态数据结构 ***/
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
```

具体统计过程通过一个循环即可实现，以当前点为中心向八个方向扩散，遇到对方棋子则停止，当八个方向都遇到对方棋子时循环终止：

```java
	/**
     * 检查某个棋子处各个方向上相同棋子的个数,用于判断游戏胜负(返回0表示游戏继续,1表示玩家1赢，2表示玩家2赢)
     */
    public int CheckResult(Point point) {
        // 检查水平和竖直方向
        int stopCount = 0; // 8个方向到边界停止的个数
        int[] stopDir = new int[8];// 八个方向是否已经终止
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
```

#### **4.悔棋功能**

悔棋的实现很简单，只要每次下棋都将所下的棋子的位置入栈即可，悔棋依次将节点从栈取出然后根据节点信息更新UI，更新棋盘数据等相关状态，当所有棋子都取出即栈为空时停止悔棋响应：

```java
// 用一个可变数组实现堆栈依次保存所下棋子的顺序
ArrayList<Point>PieceStack = null;
// 初始化栈
PieceStack = new ArrayList<Point>();
// 入栈
PieceStack.add(point);

	/**
     * 悔棋一步
     */
    public void WithDraw(){

        // 异常检测
        if (PieceStack == null || PieceStack.size() == 0)
            return;
        // 取出最后一个坐标并出栈
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
```

#### **5.特殊显示获胜棋子功能**

要特殊显示获胜棋子难点在于要找到同一个方向上个数大于4的棋子，来对其更换图片。当然可以在当时判断是否有人赢时将可能获胜的棋子保存起来，但每次下棋都要判断都要保存，因此操作冗余太大。这里是在确定有人赢之后再根据计算得到的各方向相同棋子个数重新搜索大于4个棋子的行列来确定获胜的棋子：

![这里写图片描述](http://img.blog.csdn.net/20161021191437501)


```java
    /**
     * 显示获胜的棋子
     */
    public void SearchWonPieces(Point point){
        // 判断是哪个玩家赢了
        int winimg = 0;
        if (gameResult == 1)
            winimg = R.drawable.chess_p1_win;
        else if (gameResult == 2)
            winimg = R.drawable.chess_p2_win;
        else{
            System.out.print("error!");
            return;
        }
        // 当前下的一个棋子直接特殊显示
        ChessBord[point.x][point.y].image.setImageResource(winimg);
        // 某个方向上最多的相同棋子个数肯定不会比最大宽度或者高度棋子数多
        int maxStep = H_NUM > V_NUM ? H_NUM : V_NUM;
        for (int i=1 ; i<maxStep; i++){
            // 上
            if (checkNode[gameResult-1].Vertical >= 4 && (point.y + i < V_NUM) && ChessBord[point.x][point.y + i].value == gameResult) {
                // 满足不越棋盘的界，并且是当前获胜玩家的棋子
                UpdateWinImage(new Point(point.x, point.y+i), new Point(point.x, point.y+i-1), winimg);
            }
            // 下
            if (checkNode[gameResult-1].Vertical >= 4 && (point.y - i >= 0) && ChessBord[point.x][point.y - i].value == gameResult) {
                UpdateWinImage(new Point(point.x, point.y-i), new Point(point.x, point.y-i+1), winimg);
            }
            // 左
            if (checkNode[gameResult-1].Horizontal >= 4 && (point.x - i >= 0) && ChessBord[point.x - i][point.y].value == gameResult) {
                UpdateWinImage(new Point(point.x-i, point.y), new Point(point.x-i+1, point.y), winimg);
            }
            // 右
            if (checkNode[gameResult-1].Horizontal >= 4 && (point.x + i < H_NUM) && ChessBord[point.x + i][point.y].value == gameResult) {
                UpdateWinImage(new Point(point.x+i, point.y), new Point(point.x+i-1, point.y), winimg);
            }
            // 左上
            if (checkNode[gameResult-1].ADiagonal >= 4 && (point.x - i >= 0) && (point.y + i < V_NUM) && (ChessBord[point.x - i][point.y + i].value == gameResult)) {
                UpdateWinImage(new Point(point.x-i, point.y+i), new Point(point.x-i+1, point.y+i-1), winimg);
            }
            // 右下
            if (checkNode[gameResult-1].ADiagonal >= 4 && (point.x + i < H_NUM) && (point.y - i >= 0) && (ChessBord[point.x + i][point.y - i].value == gameResult)) {
                UpdateWinImage(new Point(point.x+i, point.y-i), new Point(point.x+i-1, point.y-i+1), winimg);
            }
            // 右上
            if (checkNode[gameResult-1].MDiagonal >= 4 && (point.x + i < H_NUM) && (point.y + i < V_NUM) && (ChessBord[point.x + i][point.y + i].value == gameResult)) {
                UpdateWinImage(new Point(point.x+i, point.y+i), new Point(point.x+i-1, point.y+i-1), winimg);
            }
            // 左下
            if (checkNode[gameResult-1].MDiagonal >= 4 && (point.x - i >= 0) && (point.y - i >= 0) && (ChessBord[point.x - i][point.y - i].value == gameResult)) {
                UpdateWinImage(new Point(point.x-i, point.y-i), new Point(point.x-i+1, point.y-i+1), winimg);
            }
        }
    }

    /**
     * 替换获胜棋子图片(这里要同时检验当前获胜棋子是否和上一个连续，如果中间有对方棋子间隔那么当前这个获胜玩家的棋子并不是真的获胜棋子)
     */
    public void UpdateWinImage(Point curPoint, Point prePoint, int winimg){
        if (ChessBord[curPoint.x][curPoint.y].value == ChessBord[prePoint.x][prePoint.y].value){
            ChessBord[curPoint.x][curPoint.y].image.setImageResource(winimg);
        }else {
            // 出现不连续获胜棋子则将这一排剩下的所有获胜玩家的棋子全部排除掉（事实上顶多出现两个不连续的获胜玩家棋子，因为H_NUM为7）
            int delX = curPoint.x - prePoint.x;
            int delY = curPoint.y - prePoint.y;
            Point nextPoint = new Point(curPoint.x+delX, curPoint.y+delY);
            while (nextPoint.x>=0 && nextPoint.x<H_NUM && nextPoint.y>=0 && nextPoint.y<V_NUM){
                ChessBord[nextPoint.x][nextPoint.y].value = 0;
                nextPoint.x += delX;
                nextPoint.y += delY;
            }
        }
    }
```



### 5.声音特效的添加（MediaPlayer & SoundPool）

1. 安卓中基本的声音组件是**MediaPlayer**，通过create函数初始化之后就可以调用start等函数很简单的控制声音播放暂停等等，另外可以为MediaPlayer对象添加监听事件，监听试音播放结束从而进行其他操作，比如这里开始界面在点击开始游戏按钮后播放僵尸笑的音效，然后需要在声音播放结束后再进入游戏界面，这就需要用到这个监听了，具体用法示例如下：

```java
// 创建并初始化MeidaPlayer对象
MediaPlayer media_start = MediaPlayer.create(this, R.raw.zombiesmile);
//播放音效
media_start.start();
// 监听音效结束事件
media_start.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 跳转到游戏界面
                // ...
            }
        });
```

另外可以通过监听播放结束实现背景音乐的循环播放，即在播放结束时再调用start函数重新播放该音乐。

2. MediaPlayer虽然容易操作，但只能管理一个音乐，延迟较大，不支持多个音乐同时播放，且消耗资源较大，对于游戏中反复使用的多个短促音效通常使用SoundPool来进行管理操作，这里游戏界面用到的音效都使用SoundPool来管理了，背景音乐仍然采用MediaPlayer单独管理，具体用法如下：

```java
// 定义音效池。第一个参数是管理的音乐片段的最大数量，系统会根据这个参数分配适当的缓存空间；第二个参数表示将音效定义为系统音效；最后一个参数是音效的品质，品质越高相应对系统消耗越大
SoundPool soundPool = new SoundPool(4, AudioManager.STREAM_SYSTEM,5);

// 这里依次添加四段音效，最后一个参数指的是播放冲突时的播放优先级，加载的先后顺序决定了每段音效的ID号（从1开始），之后播放是通过ID判断播放哪一段音效
soundPool.load(this, R.raw.game_bg, 1);  // 背景音效(弃用)
soundPool.load(this, R.raw.peng, 2);     // 落子音效
soundPool.load(this, R.raw.dingdong, 3); // 玩家胜利音效
soundPool.load(this, R.raw.dingdong, 4); // 平局音效

// 播放：第一个参数是音效的ID，也就是第4个加载的音效（平局音效）；第2和3个参数分别表示左右声道的音量；第四个参数表示播放优先级，数值越大优先级越高；第五个参数表示是否循环播放，0为不循环，-1为循环；最后一个参数指定播放的比率，数值可从0.5到2， 1为正常比率
soundPool.play(4,1,1,0,0,1);

```

### 6.简单游戏数据的存储

这里简单存储两个玩家各自获胜的次数两个数字，使用sharedpreferance保存维护两个键值对，sharedpreferance的基本用法如下，其中获取的时候第二个参数是在数据不存在时的默认值，正好实现当第一次获取时两个键值对还不存在，返回默认的0同时创建了该键值对，然后进行存储以及后面的数据获取，也就是默认的0有且只有使用到依次来进行初始化：

```java
    /**
     * 更新数据
     */
    public void setData(int player){
        // 简单数据存储
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
        // 数据存储编辑器
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 获取旧数据，如果没有存储过返回默认的0
        int dataP1 = sharedPreferences.getInt("data_player1",0);
        int dataP2 = sharedPreferences.getInt("data_player2",0);
        // 更新数据
        if (player == 1){
            editor.putInt("data_player1", dataP1+1);
        }else if (player == 2){
            editor.putInt("data_player2", dataP2+1);
        }else {
            return;
        }
        // 提交数据
        editor.commit();
    }
```

![这里写图片描述](http://img.blog.csdn.net/20161021190258112)


### 7.java源码预览

![这里写图片描述](http://img.blog.csdn.net/20161021192329198)

***MainActivity.java***

```java
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
        // 开始按钮起始Y坐标
        initY = ScreenH*2/3;
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
                player1DataShow.setText(String.valueOf(dataP1));
                player2DataShow.setText(String.valueOf(dataP2));

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

            // 开始按钮起始位置
            //startGameBtn.setX(ScreenW*3/8);
            //startGameBtn.setY(initY);
            // 玩家数据按钮起始位置
            //dataButton.setX(ScreenW-170);
            //dataButton.setY(ScreenH-150);

            // update
            while (isActive) {
                try {
                    Thread.sleep(Interval);
                    // 1.按钮浮动
                    sinAngle += 0.3f;
                    startGameBtn.setY(initY + (float)(range*Math.sin(sinAngle)));
                    dataButton.setX((float)(range/3*Math.sin(sinAngle)));

                    // 3.云朵移动
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

```

***GameActivity.java***

```java
package com.example.albeeert.fourinrowgame;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    // 数据存储
    public final String PREFS_NAME = "JXHFile";

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
    private TextView StatusTop = null;
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

    // 音效池
    private SoundPool soundPool = null;
    // 背景音乐播放器
    private MediaPlayer media_bg = null;

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
     * 返回键监听
     */
    @Override
    public void onBackPressed(){
        // 关闭背景音乐
        media_bg.stop();
        this.finish();
    }

    /**
     * 全局初始化(只初始化一次)
     */
    public void ActivityInit(){

        // UI 初始化
        ReStartButton = (Button)findViewById(R.id.button_restart);
        WithDrawButton = (Button)findViewById(R.id.button_withdraw);
        StatusTop = (TextView)findViewById(R.id.topStatus);
        StatusLeft = (TextView)findViewById(R.id.status_left);
        StatusRight = (TextView)findViewById(R.id.status_right);
        IconLeft = (ImageView)findViewById(R.id.icon_left);
        IconRight = (ImageView)findViewById(R.id.icon_right);
        CBLayout = (AbsoluteLayout) findViewById(R.id.layout_chessboard);

        // 音效初始化
        soundPool = new SoundPool(4, AudioManager.STREAM_SYSTEM,5);
        soundPool.load(this, R.raw.game_bg, 1);  // 背景音效(弃用)
        soundPool.load(this, R.raw.peng, 2);     // 落子音效
        soundPool.load(this, R.raw.dingdong, 3); // 玩家胜利音效
        soundPool.load(this, R.raw.dingdong, 4); // 平局音效
        // 背景音乐
        media_bg = MediaPlayer.create(this, R.raw.game_bg);
        media_bg.start();
        // 循环播放
        media_bg.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                media_bg.start();
            }
        });

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
        // 保证隐藏draw
        StatusTop.setText("");

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
            /** 已经有人赢了 **/
            // 显示获胜的棋子
            SearchWonPieces(point);
            // 更新数据
            setData(gameResult);
            // 更新UI
            if (gameResult == 1){
                // 玩家1赢
                UpdateUI(11);
            }else {
                // 玩家2赢
                UpdateUI(22);
            }

            // 游戏结束
            GameOver();
        }
        // 重置检测状态
        checkNode[0].Reset();
        checkNode[1].Reset();

    }

    /**
     * 显示获胜的棋子
     */
    public void SearchWonPieces(Point point){
        // 判断是哪个玩家赢了
        int winimg = 0;
        if (gameResult == 1)
            winimg = R.drawable.chess_p1_win;
        else if (gameResult == 2)
            winimg = R.drawable.chess_p2_win;
        else{
            System.out.print("error!");
            return;
        }
        // 当前下的一个棋子直接特殊显示
        ChessBord[point.x][point.y].image.setImageResource(winimg);
        // 某个方向上最多的相同棋子个数肯定不会比最大宽度或者高度棋子数多
        int maxStep = H_NUM > V_NUM ? H_NUM : V_NUM;
        for (int i=1 ; i<maxStep; i++){
            // 上
            if (checkNode[gameResult-1].Vertical >= 4 && (point.y + i < V_NUM) && ChessBord[point.x][point.y + i].value == gameResult) {
                // 满足不越棋盘的界，并且是当前获胜玩家的棋子
                UpdateWinImage(new Point(point.x, point.y+i), new Point(point.x, point.y+i-1), winimg);
            }
            // 下
            if (checkNode[gameResult-1].Vertical >= 4 && (point.y - i >= 0) && ChessBord[point.x][point.y - i].value == gameResult) {
                UpdateWinImage(new Point(point.x, point.y-i), new Point(point.x, point.y-i+1), winimg);
            }
            // 左
            if (checkNode[gameResult-1].Horizontal >= 4 && (point.x - i >= 0) && ChessBord[point.x - i][point.y].value == gameResult) {
                UpdateWinImage(new Point(point.x-i, point.y), new Point(point.x-i+1, point.y), winimg);
            }
            // 右
            if (checkNode[gameResult-1].Horizontal >= 4 && (point.x + i < H_NUM) && ChessBord[point.x + i][point.y].value == gameResult) {
                UpdateWinImage(new Point(point.x+i, point.y), new Point(point.x+i-1, point.y), winimg);
            }
            // 左上
            if (checkNode[gameResult-1].ADiagonal >= 4 && (point.x - i >= 0) && (point.y + i < V_NUM) && (ChessBord[point.x - i][point.y + i].value == gameResult)) {
                UpdateWinImage(new Point(point.x-i, point.y+i), new Point(point.x-i+1, point.y+i-1), winimg);
            }
            // 右下
            if (checkNode[gameResult-1].ADiagonal >= 4 && (point.x + i < H_NUM) && (point.y - i >= 0) && (ChessBord[point.x + i][point.y - i].value == gameResult)) {
                UpdateWinImage(new Point(point.x+i, point.y-i), new Point(point.x+i-1, point.y-i+1), winimg);
            }
            // 右上
            if (checkNode[gameResult-1].MDiagonal >= 4 && (point.x + i < H_NUM) && (point.y + i < V_NUM) && (ChessBord[point.x + i][point.y + i].value == gameResult)) {
                UpdateWinImage(new Point(point.x+i, point.y+i), new Point(point.x+i-1, point.y+i-1), winimg);
            }
            // 左下
            if (checkNode[gameResult-1].MDiagonal >= 4 && (point.x - i >= 0) && (point.y - i >= 0) && (ChessBord[point.x - i][point.y - i].value == gameResult)) {
                UpdateWinImage(new Point(point.x-i, point.y-i), new Point(point.x-i+1, point.y-i+1), winimg);
            }
        }
    }

    /**
     * 替换获胜棋子图片(这里要同时检验当前获胜棋子是否和上一个连续，如果中间有对方棋子间隔那么当前这个获胜玩家的棋子并不是真的获胜棋子)
     */
    public void UpdateWinImage(Point curPoint, Point prePoint, int winimg){
        if (ChessBord[curPoint.x][curPoint.y].value == ChessBord[prePoint.x][prePoint.y].value){
            ChessBord[curPoint.x][curPoint.y].image.setImageResource(winimg);
        }else {
            // 出现不连续获胜棋子则将这一排剩下的所有获胜玩家的棋子全部排除掉（事实上顶多出现两个不连续的获胜玩家棋子，因为H_NUM为7）
            int delX = curPoint.x - prePoint.x;
            int delY = curPoint.y - prePoint.y;
            Point nextPoint = new Point(curPoint.x+delX, curPoint.y+delY);
            while (nextPoint.x>=0 && nextPoint.x<H_NUM && nextPoint.y>=0 && nextPoint.y<V_NUM){
                ChessBord[nextPoint.x][nextPoint.y].value = 0;
                nextPoint.x += delX;
                nextPoint.y += delY;
            }
        }
    }

    /**
     * 更新UI显示状态
     */
    public void UpdateUI(int gameResult){
        if (gameResult == 0){
            // 平局
            StatusTop.setText("Draw");
            StatusLeft.setText("");
            StatusRight.setText("");
            // 两个玩家都高亮
            IconLeft.setImageResource(R.drawable.chess_p1_win);
            IconRight.setImageResource(R.drawable.chess_p2_win);
            // 平局音效
            soundPool.play(4,1,1,0,0,1);

        }else if (gameResult == 1){
            // 玩家1下棋
            StatusLeft.setText("   !");
            StatusRight.setText("");
            // 玩家1高亮
            IconLeft.setImageResource(R.drawable.chess_p1_win);
            IconRight.setImageResource(R.drawable.chess_p2);

        }else if (gameResult == 2){
            // 玩家2下棋
            StatusLeft.setText("");
            StatusRight.setText("!   ");
            // 玩家2高亮
            IconLeft.setImageResource(R.drawable.chess_p1);
            IconRight.setImageResource(R.drawable.chess_p2_win);

        }else if(gameResult == 11){
            // 玩家1赢
            StatusLeft.setText("Win!");
            StatusRight.setText("");
            // 胜利音效
            soundPool.play(3,1,1,2,0,1);

        }else if (gameResult == 22){
            // 玩家2赢
            StatusLeft.setText("");
            StatusRight.setText("Win!");
            // 胜利音效
            soundPool.play(3,1,1,2,0,1);

        }else {
            return;
        }
    }


    /**
     * 绘制指定棋子
     */
    public void DrawPiece(Point point){

        // 播放音效
        soundPool.play(2,1,1,1,0,1);
        // 绘制
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
        int stopCount = 0; // 8个方向到边界停止的个数
        int[] stopDir = new int[8];// 八个方向是否已经终止
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
        // 取出最后一个坐标并出栈
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

    /**
     * 更新数据
     */
    public void setData(int player){
        // 简单数据存储
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,0);
        // 数据存储编辑器
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 获取旧数据，如果没有存储过返回默认的0
        int dataP1 = sharedPreferences.getInt("data_player1",0);
        int dataP2 = sharedPreferences.getInt("data_player2",0);
        // 更新数据
        if (player == 1){
            editor.putInt("data_player1", dataP1+1);
        }else if (player == 2){
            editor.putInt("data_player2", dataP2+1);
        }else {
            return;
        }
        // 提交数据
        editor.commit();
    }

}
```
