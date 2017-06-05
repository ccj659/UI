package com.ccj.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;


/**
 * 仿QQ拖拽气泡
 * Created by chenchangjun on 17/5/25.
 *
 */

public class DragBuubbleView extends View {

    public Context mContext;

    /**
     * state  根据 ontouch事件进行判断,
     */
    private final int STATE_DEFAULT = 0;//  1.气泡静止状态 --气泡小球和数字
    private final int STATE_CONECTED = 1;//2.气泡相连 -- 两个相连的气泡小球(带黏连效果),和数字

    private final int STATE_APART = 2;//3.气泡分离 -- 单个气泡小球的拖动

    private final int STATE_DISMISS = 3;//4.气泡消失 -- 爆炸



    //default values;
    private float default_radius;
    private int default_bubble_color;
    private String default_bubble_text;
    private int default_bubble_textColor;
    private float default_bubble_textSize;
    private float default_bubble_radius;


    private float bubble_radius;//小球半径
    private int bubble_color;//小球颜色
    private String bubble_text;//小球显示字
    private int bubble_textColor; //字体颜色
    private float bubble_textSize;//字体大小

    /**
     * 静止气泡和动态气泡相关
     */
    private float mBubStillRadius;//不动气泡的半径
    private float mBubMoveableRadius;//可动气泡的半径
    private PointF mBubStillCenter;//不动气泡的圆心
    private PointF mBubMoveableCenter;//可动气泡的圆心
    private Paint mBubblePaint;//气泡的画笔


    /**
     * 贝塞尔曲线path相关
     */
    private Path mBezierPath;

    private Paint mTextPaint;

    private Rect mTextRect;

    private Paint mBurstPaint;

    private Rect mBurstRect;


    private int current_state = STATE_DEFAULT;//当前状态
    private float bubbleDistance; //气泡间距
    private float maxDistance; //气泡最大间距

    private float MOVE_OFFSET = 0;//允许你的 手指触摸偏移量
    private Bitmap[] burstBitmapsArray;//气泡爆炸bitmap数组
    private boolean isBurstAnimStart = false; //是否执行 气泡爆炸

    private int curDrawableIndex; //当前气泡爆炸图片index

    //气泡爆炸的图片id数组
    private int[] burstDrawablesArray = {R.drawable.burst_1, R.drawable.burst_2
            , R.drawable.burst_3, R.drawable.burst_4, R.drawable.burst_5};


    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragBubbleView);

            bubble_radius = typedArray.getDimension(R.styleable.DragBubbleView_bubble_radius, default_bubble_radius);
            bubble_color = typedArray.getColor(R.styleable.DragBubbleView_bubble_color, default_bubble_color);

            bubble_textSize = typedArray.getDimension(R.styleable.DragBubbleView_bubble_textSize, default_bubble_textSize);
            bubble_textColor = typedArray.getColor(R.styleable.DragBubbleView_bubble_textColor, default_bubble_textColor);
            bubble_text = typedArray.getString(R.styleable.DragBubbleView_bubble_text);
            typedArray.recycle();

        }
        mBubStillRadius = bubble_radius;
        mBubMoveableRadius = mBubStillRadius;
        maxDistance = 8 * bubble_radius;

        MOVE_OFFSET = maxDistance / 4;


        //气泡画笔
        mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubblePaint.setColor(bubble_color);
        mBubblePaint.setStyle(Paint.Style.FILL);
        mBezierPath = new Path();
        //文字画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(bubble_textColor);
        mTextPaint.setTextSize(bubble_textSize);
        mTextRect = new Rect();
        //爆炸画笔
        mBurstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBurstPaint.setFilterBitmap(true);
        mBurstRect = new Rect();
        burstBitmapsArray = new Bitmap[burstDrawablesArray.length];
        for (int i = 0; i < burstDrawablesArray.length; i++) {
            //将气泡爆炸的drawable转为bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), burstDrawablesArray[i]);
            burstBitmapsArray[i] = bitmap;
        }

    }

    private void initView(int w, int h) {

        //设置两气泡圆心初始坐标
        if (mBubStillCenter == null) {
            mBubStillCenter = new PointF(w / 2, h / 2);
        } else {
            mBubStillCenter.set(w / 2, h / 2);
        }
        //设置动点坐标
        if (mBubMoveableCenter == null) {
            mBubMoveableCenter = new PointF(w / 2, h / 2);
        } else {
            mBubMoveableCenter.set(w / 2, h / 2);
        }
        current_state = STATE_DEFAULT;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //当 当前状态 != 消失, 则, 画  移动的气泡 和文字
        if (current_state != STATE_DISMISS) {
            canvas.drawCircle(mBubMoveableCenter.x,mBubMoveableCenter.y,
                    mBubMoveableRadius,mBubblePaint);
            mTextPaint.getTextBounds(bubble_text, 0, bubble_text.length(), mTextRect);

            canvas.drawText(bubble_text, mBubMoveableCenter.x - mTextRect.width() / 2, mBubMoveableCenter.y + mTextRect.height() / 2, mTextPaint);
        }



        //2.相连的气泡状态,绘制贝塞尔曲线
        if (current_state == STATE_CONECTED) {
            //1.静止气泡
            canvas.drawCircle(mBubStillCenter.x, mBubStillCenter.y, mBubStillRadius, mBubblePaint);
            //重置path状态
            mBezierPath.reset();


            //2.相连曲线---贝塞尔曲线
            //计算控制点坐标---两个球圆心的中点.
            int iAnchorX = (int) ((mBubStillCenter.x + mBubMoveableCenter.x) / 2);
            int iAnchorY = (int) ((mBubStillCenter.y + mBubMoveableCenter.y) / 2);
            //计算 三角函数
            float cosTheta = (mBubStillCenter.x - mBubMoveableCenter.x)/bubbleDistance;
            float sinTheta = (mBubStillCenter.y - mBubMoveableCenter.y)/bubbleDistance;


            //画上半弧 B-->A---------------

            //A点静坐标计算
            float mBubStillAX = mBubStillCenter.x -mBubStillRadius*sinTheta;
            float mBubStillAY = mBubStillCenter.y +mBubStillRadius*cosTheta;

            //B点动坐标计算
            float mBubMoveBX = mBubMoveableCenter.x -mBubMoveableRadius*sinTheta;
            float mBubMoveBY = mBubMoveableCenter.y +mBubMoveableRadius*cosTheta;

            mBezierPath.moveTo(mBubStillAX,mBubStillAY);
            mBezierPath.quadTo(iAnchorX,iAnchorY,mBubMoveBX,mBubMoveBY);

            //画下半弧 C-->D-------------

            //C点动坐标计算
            float mBubMoveCX = mBubMoveableCenter.x +mBubMoveableRadius*sinTheta;
            float mBubMoveCY = mBubMoveableCenter.y -mBubMoveableRadius*cosTheta;

            //D点静坐标计算
            float mBubStillDX = mBubStillCenter.x +mBubStillRadius*sinTheta;
            float mBubStillDY = mBubStillCenter.y -mBubStillRadius*cosTheta;

            mBezierPath.lineTo(mBubMoveCX,mBubMoveCY);
            mBezierPath.quadTo(iAnchorX,iAnchorY,mBubStillDX,mBubStillDY);

            //释放资源,并画----------
            mBezierPath.close();
            canvas.drawPath(mBezierPath,mBubblePaint);


        }

        //boom,爆炸状态, 爆炸效果动画
        if(isBurstAnimStart){
            mBurstRect.set((int)(mBubMoveableCenter.x - mBubMoveableRadius),
                    (int)(mBubMoveableCenter.y - mBubMoveableRadius),
                    (int)(mBubMoveableCenter.x + mBubMoveableRadius),
                    (int)(mBubMoveableCenter.y + mBubMoveableRadius));

            canvas.drawBitmap(burstBitmapsArray[curDrawableIndex],null,
                    mBurstRect,mBubblePaint); }


    }

    /**
     * 重置状态
     */
    public void reset(){

        initView(getWidth(),getHeight());
        invalidate();
    }


    /**
     * 开始气泡 重置状态 的动画
     */
    private void startBubbleRestAnim() {
        ValueAnimator anim = ValueAnimator.ofObject(new PointFEvaluator(),
                new PointF(mBubMoveableCenter.x,mBubMoveableCenter.y),
                new PointF(mBubStillCenter.x,mBubStillCenter.y));

        anim.setDuration(200);
        anim.setInterpolator(new OvershootInterpolator(5f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBubMoveableCenter = (PointF) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                current_state= STATE_DEFAULT;
            }
        });
        anim.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initView(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            //按下, 如果 当前状态 没有消失, 则计算 当前气泡距离,和 最大允许偏移量 作比较 ,进行状态切换
            case MotionEvent.ACTION_DOWN:
            if(current_state!=STATE_DISMISS){
                bubbleDistance=(float) Math.hypot(event.getX()-mBubStillCenter.x,event.getY()-mBubStillCenter.y);

                if (bubbleDistance<bubble_radius+MOVE_OFFSET){
                    current_state=STATE_CONECTED;
                }else {
                    current_state=STATE_DEFAULT;
                }
            }


                break;
            //移动 ,改变动点的位置, 计算两点距离, 改变 静止点的半径,切换状态,并重绘制,调用ondraw
            case MotionEvent.ACTION_MOVE:
            {
                if(current_state != STATE_DEFAULT){
                    mBubMoveableCenter.x = event.getX();
                    mBubMoveableCenter.y = event.getY();
                    bubbleDistance = (float) Math.hypot(event.getX() - mBubStillCenter.x,
                            event.getY() - mBubStillCenter.y);
                    if(current_state == STATE_CONECTED){
                        // 减去MOVE_OFFSET是为了让不动气泡半径到一个较小值时就直接消失
                        // 或者说是进入分离状态
                     if(bubbleDistance < maxDistance - MOVE_OFFSET){

                            mBubStillRadius = bubble_radius - bubbleDistance / 8;
                        }else{
                            current_state = STATE_APART;
                        }
                    }
                    invalidate();
                }
            }
            break;
            //抬起手, 根据 链接 状态, 开始进行 动画
            case MotionEvent.ACTION_UP:
            {
                if(current_state == STATE_CONECTED){
                    startBubbleRestAnim();

                }else if(current_state == STATE_APART){
                    if(bubbleDistance < 2 * bubble_radius){
                        startBubbleRestAnim();
                    }else{
                        startBubbleBurstAnim();
                    }
                }
            }
            break;
        }

        return true;

    }

    /**
     * 气泡消失动画
     */
    private void startBubbleBurstAnim() {
        //气泡改为消失状态
        current_state = STATE_DISMISS;
        isBurstAnimStart = true;
        //做一个int型属性动画，从0~mBurstDrawablesArray.length结束
        ValueAnimator anim = ValueAnimator.ofInt(0, burstDrawablesArray.length);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //设置当前绘制的爆炸图片index
                curDrawableIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //修改动画执行标志
                isBurstAnimStart = false;
            }
        });
        anim.start();

    }






















    public DragBuubbleView(Context context) {
        super(context);
        init(context, null);

    }


    public DragBuubbleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public DragBuubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragBuubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);

    }


}
