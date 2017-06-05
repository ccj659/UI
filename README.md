
#贝塞尔曲线实践--拖拽气泡

![这里写图片描述](http://img.blog.csdn.net/20170605150114910?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY2NqNjU5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

##贝塞尔曲线扫盲

[大家不明白贝塞尔的可以来这里逛逛~~ 浅显易懂](http://www.html-js.com/article/1628)


贝塞尔曲线主要由于三个部分控制：起点，终点，中间的辅助控制点。在android自带的Path类中自带了方法，可以帮助我们实现贝塞尔曲线：



###二阶贝塞尔 
```
/** 
 * Add a quadratic bezier from the last point, approaching control point 
 * (x1,y1), and ending at (x2,y2). If no moveTo() call has been made for 
 * this contour, the first point is automatically set to (0,0). 
 * 
 * @param x1 The x-coordinate of the control point on a quadratic curve 
 * @param y1 The y-coordinate of the control point on a quadratic curve 
 * @param x2 The x-coordinate of the end point on a quadratic curve 
 * @param y2 The y-coordinate of the end point on a quadratic curve 
 */  
public void quadTo(float x1, float y1, float x2, float y2) {  
    isSimplePath = false;  
    native_quadTo(mNativePath, x1, y1, x2, y2);  
} 

```
`quadTo()`方法从上一个点为起点开始绘制贝塞尔曲线，其中（x1，y1）为辅助控制点，（x2，y2）为终点。

```
Path mPath = new Path();
mPath.moveTo(x0,y0);
mPath.quadTo(x1,y1,x2,y2);
```
如调用以上代码，即绘制起点（x0，y0），终点（x2，y2），辅助控制点（x1，y1）的贝塞尔曲线。因此，通过不断改变这三个点的位置，我们可以绘制出各种各样的曲线
###三阶贝塞尔
```
/** 
 * Add a cubic bezier from the last point, approaching control points 
 * (x1,y1) and (x2,y2), and ending at (x3,y3). If no moveTo() call has been 
 * made for this contour, the first point is automatically set to (0,0). 
 * 
 * @param x1 The x-coordinate of the 1st control point on a cubic curve 
 * @param y1 The y-coordinate of the 1st control point on a cubic curve 
 * @param x2 The x-coordinate of the 2nd control point on a cubic curve 
 * @param y2 The y-coordinate of the 2nd control point on a cubic curve 
 * @param x3 The x-coordinate of the end point on a cubic curve 
 * @param y3 The y-coordinate of the end point on a cubic curve 
 */  
public void cubicTo(float x1, float y1, float x2, float y2,  
                    float x3, float y3) {  
    isSimplePath = false;  
    native_cubicTo(mNativePath, x1, y1, x2, y2, x3, y3);  
}  
```

`cubicTo()`方法从上一个点为起点开始绘制三阶贝塞尔曲线，其中（x1，y1）,( x2, y2 )为辅助控制点，（x3，y3）为终点。

##拖拽气泡实践

###思路
拖拽气泡,其实 就是这样的几个状态

- 1.气泡静止状态 --气泡小球和数字
- 2.气泡相连 -- 两个相连的气泡小球(带黏连效果),和数字
- 3.气泡分离 -- 单个气泡小球的拖动
- 4.气泡消失 -- 爆炸

我们需要在这几个状态分别,做出处理.

![这里写图片描述](http://img.blog.csdn.net/20170605141902838?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY2NqNjU5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
####1.两个圆和一个角度


- 一个静止的圆O1
- 一个动的圆O2
- 互补角和内错角<@


根据圆的关系,和Anchor点,计算出角度<@,用来求贝塞尔曲线的ABCD点坐标.

####2.两条贝塞尔二阶曲线

- 上半边: A---Ar---B, 以Ar为锚点,A和B重点的二阶曲线

- 下半边: C---Ar---D,以Ar为锚点,C和D重点的二阶曲线

具体坐标的求法,在上图中可见.

根据贝塞尔曲线的要求:
起点，终点，中间的辅助控制点。用在android自带的Path类中自带了方法.

举个例子,就可以这么做

######上边线:

```java

            //画上半弧 B-->A---------------

            //A点静坐标计算
            float mBubStillAX = mBubStillCenter.x -mBubStillRadius*sinTheta;
            float mBubStillAY = mBubStillCenter.y +mBubStillRadius*cosTheta;

            //B点动坐标计算
            float mBubMoveBX = mBubMoveableCenter.x -mBubMoveableRadius*sinTheta;
            float mBubMoveBY = mBubMoveableCenter.y +mBubMoveableRadius*cosTheta;

            mBezierPath.moveTo(mBubStillAX,mBubStillAY);
            mBezierPath.quadTo(iAnchorX,iAnchorY,mBubMoveBX,mBubMoveBY);

```


####3.状态判断`OnTouch`
在`onTouchEvent()`中,对touch事件进行自己处理,即`return true`进行以下几个状态切换.

- 按下, 如果 当前状态 没有消失, 则计算 当前气泡距离,和 最大允许偏移量 作比较 ,进行状态切换
- 移动 ,改变动点的位置, 计算两点距离, 改变 静止点的半径,切换状态,并重绘制,调用ondraw
- 抬起手, 根据 链接 状态, 开始进行 动画



####4.绘制不同 `OnDraw`
根据上述不同的状态,绘制不同的view.


- 1当 当前状态 != 消失, 则, 画  移动的气泡 和文字
- 2.相连的气泡状态,绘制贝塞尔曲线
- 3.boom,爆炸状态, 爆炸效果动画

##最终效果

![这里写图片描述](http://img.blog.csdn.net/20170605150114910?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY2NqNjU5/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)



##实现源码
```java

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



```
---

代码下载:


##参考
1.[一篇很好的贝塞尔扫盲文章-----送给你
](http://www.html-js.com/article/1628)

2.[一篇贝塞尔,代码绘制文章---送给你](http://blog.csdn.net/z82367825/article/details/51599245)


