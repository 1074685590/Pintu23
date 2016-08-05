package com.example.leiyang.pintu23;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    /**
     * 利用二维数组创建游戏小方块
     */
    private ImageView[][] iv_game_arr = new ImageView[3][5];
    private GridLayout gl_main_game ;

    private ImageView iv_null_imageView;  //空方块的实例
    private GestureDetector gestureDetector;
    /*判断游戏是否开始*/
    boolean isGameStart = false;
    /*动画是否正在进行*/
    boolean isAniMove = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int flag = getDirByGes(e1.getX(),e1.getY(),e2.getX(),e2.getY());
//                Toast.makeText(MainActivity.this,""+flag,Toast.LENGTH_SHORT).show();
                changeByDir(flag);
                return false;
            }
        });

        /*初始化游戏小方块*/
        //获取一张大图
        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.back)).getBitmap();
        Log.i("info","宽度"+bitmap.getWidth());
        int tuWandH = bitmap.getWidth()/5;
        int ivWandH = getWindowManager().getDefaultDisplay().getWidth()/5;
        for (int i = 0; i < iv_game_arr.length; i++ ) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                Bitmap bm = Bitmap.createBitmap(bitmap,tuWandH*j,tuWandH*i,tuWandH,tuWandH);
                iv_game_arr[i][j] = new ImageView(this);
                iv_game_arr[i][j].setImageBitmap(bm);
                iv_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(ivWandH,ivWandH));
                iv_game_arr[i][j].setPadding(2,2,2,2);
                iv_game_arr[i][j].setTag(new GameData(i,j,bm));
                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = isHasByNullImageView((ImageView) v);
//                        Toast.makeText(MainActivity.this, "当前是否可以移动："+flag, Toast.LENGTH_SHORT).show();
                        if (flag) {
                            changeDataByImageView((ImageView) v);
                        }
                    }
                });
            }
        }

        /*初始化游戏界面,并添加游戏小方块*/
        gl_main_game = (GridLayout) findViewById(R.id.gl_game);
        for (int i = 0; i < iv_game_arr.length; i++ ) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                gl_main_game.addView(iv_game_arr[i][j]);
            }
        }

        /*设置一张图片为空图*/
        setNullImageView(iv_game_arr[0][4]);

        /*随机打乱方块*/
        randomMove();
        isGameStart = true; //初始化之后游戏开始

    }

    public void randomMove() {
        //打乱的次数
        for (int i = 0; i < 5; i++) {
            int type = (int) ((Math.random()*4)+1);
            //根据手势，无动画交换
            changeByDir(type,false);
        }
    }

    /**
     * 根据返回手势int类型切换图片位置
     * @param type 手势int类型
     */
    public void changeByDir(int type) {
        changeByDir(type,true);
    }

    /**
     * 根据返回手势int类型切换图片位置
     * @param type 手势int类型
     * @param isAni 是否有动画 true:有动画 false：无动画
     */
    public void changeByDir(int type, boolean isAni) {
        GameData mNullGameData = (GameData) iv_null_imageView.getTag();
        int new_x = mNullGameData.x;
        int new_y = mNullGameData.y;
        if (type == 1) {
            new_x++;
        } else if (type == 2) {
            new_x--;
        } else if (type == 3) {
            new_y++;
        } else if (type == 4) {
            new_y--;
        }
        if (new_x >= 0 && new_x < iv_game_arr.length && new_y >= 0 && new_y < iv_game_arr[0].length) {
            if (isAni) {
                changeDataByImageView(iv_game_arr[new_x][new_y]);   //iv_game_arr[new_x][new_y]被交换的方块（同被点击的方块）
            } else {
                changeDataByImageView(iv_game_arr[new_x][new_y],false);
            }
        }

    }


    /**
     * 判断游戏结束的方法
     */
    public void isGameOver() {
        boolean isGameOver  = true;
        //遍历每个游戏小方块
        for (int i = 0; i < iv_game_arr.length; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                //为空的跳过
                if (iv_game_arr[i][j]==iv_null_imageView) {
                    continue;
                }
                GameData mGameData = (GameData) iv_game_arr[i][j].getTag();
                if (!mGameData.isTrue()) {
                    isGameOver = false;
                    break;
                }
            }
        }
        //结束提示
        if (isGameOver) {
            Toast.makeText(this, "游戏结束！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 手势类型判断
     * @param start_x 起始点x坐标
     * @param start_y 起始点y坐标
     * @param end_x 终止点x坐标
     * @param end_y 终止点y坐标
     * @return 返回判断结果类型的int值 上 ：1 下：2 左：3 右 ：4
     */
    public int getDirByGes(float start_x, float start_y, float end_x, float end_y ) {
        //先判断是左右手势还是上下手势
        boolean isRightOrLeft = Math.abs(start_x - end_x) > Math.abs(start_y - end_y)?true:false ;
        boolean isLeft = start_x - end_x > 0 ? true : false;
        boolean isUp = start_y - end_y > 0 ? true : false;
        if (isRightOrLeft) {
            if (isLeft) return 3;
            else return 4;
        } else {
            if (isUp) return 1;
            else return 2;
        }
    }

    /**
     * 利用动画结束之后，交换两个方块的数据
     * @param mImageView 点击的方块
     */
    public void changeDataByImageView(final ImageView  mImageView) {
        changeDataByImageView(mImageView,true);
    }

    /**
     * 利用动画结束之后，交换两个方块的数据
     * @param mImageView 点击的方块
     * @param isAni 是否有动画 true:有动画 false：无动画
     *
     */
    public void changeDataByImageView(final ImageView  mImageView, boolean isAni) {
        if (isAniMove) {
            return;
        }
        if (!isAni) {
            GameData mGamaData = (GameData) mImageView.getTag();
            iv_null_imageView.setImageBitmap(mGamaData.bm);
            GameData mNullGameData = (GameData) iv_null_imageView.getTag();
            mNullGameData.bm = mGamaData.bm;
            mNullGameData.p_x = mGamaData.p_x;
            mNullGameData.p_y = mGamaData.p_y;

            //设置当前点击的是空方块
            setNullImageView(mImageView);
            //游戏开始后才判断是否结束 ，避免一开始就判断游戏是否结束
            if (isGameStart) {
                isGameOver();
            }
            /*不在执行后面的代码*/
            return;
        }
        //创建一个动画，设置好方向，移动距离
        TranslateAnimation translateAnimation = null;
        if (mImageView.getX() > iv_null_imageView.getX()) {
            translateAnimation = new TranslateAnimation(0.1f,-mImageView.getWidth(),0.1f,0.1f);
        } else if (mImageView.getX() < iv_null_imageView.getX()) {
            translateAnimation = new TranslateAnimation(0.1f,mImageView.getWidth(),0.1f,0.1f);
        }else if (mImageView.getY() > iv_null_imageView.getY()) {
            translateAnimation = new TranslateAnimation(0.1f,0.1f,0.1f,-mImageView.getWidth());
        }else if (mImageView.getY() < iv_null_imageView.getY()) {
            translateAnimation = new TranslateAnimation(0.1f,0.1f,0.1f,mImageView.getWidth());
        }
        //设置动画时长
        translateAnimation.setDuration(70);
        //设置动画动画结束之后是否停留
        translateAnimation.setFillAfter(true);
        //动画结束之后，交换数据
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAniMove = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAniMove = false;
                mImageView.clearAnimation();
                GameData mGamaData = (GameData) mImageView.getTag();
                iv_null_imageView.setImageBitmap(mGamaData.bm);
                GameData mNullGameData = (GameData) iv_null_imageView.getTag();
//                 mImageView.setTag(mNullGameData);
//                 iv_null_imageView.setTag(mGamaData);
                mNullGameData.bm = mGamaData.bm;
                mNullGameData.p_x = mGamaData.p_x;
                mNullGameData.p_y = mGamaData.p_y;

                //设置当前点击的是空方块
                setNullImageView(mImageView);
//                游戏开始后才判断是否结束 ，避免一开始就判断游戏是否结束
                if (isGameStart) {
                    isGameOver();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //执行动画
        mImageView.startAnimation(translateAnimation);
    }

    /**
     * 设置某一个方块为空方块
     * @param mImageView 当前要设置的空方块实例
     */
    public void setNullImageView(ImageView mImageView) {
        mImageView.setImageBitmap(null);
        iv_null_imageView = mImageView;
    }

    /**
     * 判断当前点击的方块，是否与空方块是相邻关系
     * @param mImageView 所点击的方块
     * @return true:相邻 false:不相邻
     */
    public boolean isHasByNullImageView(ImageView mImageView) {
        //分别获取当前空方块和点击方块的位置
        GameData mNullGameData = (GameData) iv_null_imageView.getTag();
        GameData mGameData = (GameData) mImageView.getTag();
        /*点击的方块在空方块的 上 下 左 右 */
        if (mGameData.y==mNullGameData.y&&mGameData.x==mNullGameData.x+1) {
            return true;
        } else if (mGameData.y==mNullGameData.y&&mGameData.x==mNullGameData.x-1) {
            return true;
        } else if (mGameData.y==mNullGameData.y-1&&mGameData.x==mNullGameData.x) {
            return true;
        } else if (mGameData.y==mNullGameData.y+1&&mGameData.x==mNullGameData.x) {
            return true;
        }

        return false;
    }



    class GameData {
        /*每个小方块的实际位置 x */
        public int x = 0;
        /*每个小方块的实际位置 y */
        public int y = 0;
        /*每个小方块的图片*/
        public Bitmap bm;
        /*每个小方块的图片位置 x */
        public int p_x = 0;
        /*每个小方块的图片位置 y */
        public int p_y = 0;

        public GameData(int x, int y, Bitmap bm) {
            this.x = x;
            this.y = y;
            this.bm = bm;
            this.p_x = x;
            this.p_y = y;
        }


        /**
         * 判断方块与图片是否重合
         * @return 判断结果
         */
        public boolean isTrue() {
            if (x == p_x && y == p_y) {
                return true;
            } else {
                return false;
            }
        }
    }


}
