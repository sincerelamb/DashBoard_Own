package melloyy.mylibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 魔改dashboard
 * Created by since on 2017/5/19.
 */

public class CustomDashBoardView extends View {
    /**
     * 文本
     */
    private String mtitleText = "";     //中间数值上方
    private String mtitleNumText = "";  //中间数值
    private String mbottomText = ""; //下方数值整个字符串
    /**
     * 文本的颜色
     */
    private int mtitleTextColor;
    private int mtitleNumTextColor;
    private int mbottomTextColor;
    /**
     * 文本的大小
     */
    private int mtitleTextSize;
    private int mtitleNumTextSize;
    private int mbottomTextSize;
    /**
     * 内部圆的背景色
     */
    private int minsideBackGroundColor;
    /**
     * 外圈背景色
     */
    private int moutsideBackGroundColor;
    private int moutsideForeGroundColor;
    /**
     * 透明度
     * (暂无)
     */
    private int malph;
    /**
     * 总值是下面小字的值，占值是中间的值
     * 最小值最大值是对应view中的坐标值
     */
    private int mMin = 0; // 最小值
    private int mMax = 1; // 最大值
    private int mAddAllValue = mMax;  // 总值
    private int mCurrentValue = mAddAllValue; //  占值
    private int mAnimValue = mCurrentValue;    //  动画值
    /**
     * 角度可以设置
     */
    private int mAngle = 60;
//以上均可使用set方法

    /**
     * 标识↓
     */
    private int mStartAngle = 150; // 起始角度
    private int mSweepAngle = 240; // 绘制角度
    private int mSection = 30; // 值域（mMax-mMin）等分份数
    private int mPortion = 2; // 一个mSection等分份数
    private float mLength1; // 刻度顶部相对边缘的长度
    private int mCalibrationWidth; // 刻度圆弧宽度

    private int mPadding;
    private float mCenterX, mCenterY; // 圆心坐标
    private Paint mPaint;
    private RectF mRectFCalibrationFArc;
    private int mBackgroundColor;
    /**
     * 由于界面值不是线性排布，所以播放动画时若以值为参考，则会出现忽慢忽快
     * 的情况（开始以为是卡顿）。因此，先计算出最终到达角度，以扫过的角度为线性参考，动画就流畅了
     */
    private boolean isAnimFinish = true;
    private float mAngleWhenAnim;
    /**
     * 光滑处理
     */
    private PaintFlagsDrawFilter mSetfil;

    public CustomDashBoardView(Context context) {
        this(context, null);
    }

    public CustomDashBoardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDashBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //光滑处理
        mSetfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
        //获得我们所定义的自定义样式属性
        //3个文本的内容颜色大小
        //中间背景色的设定
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomDashBoardView, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.CustomDashBoardView_titleText && !(null == a.getString(attr))) {
                mtitleText = a.getString(attr);
            } else if (attr == R.styleable.CustomDashBoardView_titleTextColor) {
                // 默认颜色设置为黑色
                mtitleTextColor = a.getColor(attr, Color.WHITE);
            } else if (attr == R.styleable.CustomDashBoardView_titleTextSize) {
// 默认设置为16sp，TypeValue也可以把sp转化为px
                mtitleTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.CustomDashBoardView_titleNumText && !(null == a.getString(attr))) {
                mtitleNumText = a.getString(attr);
            } else if (attr == R.styleable.CustomDashBoardView_titleNumTextColor) {
                mtitleNumTextColor = a.getColor(attr, Color.WHITE);
            } else if (attr == R.styleable.CustomDashBoardView_titleNumTextSize) {
                mtitleNumTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.CustomDashBoardView_bottomNumText && !(null == a.getString(attr))) {
                mbottomText = a.getString(attr);
            } else if (attr == R.styleable.CustomDashBoardView_bottomTextColor) {
                mbottomTextColor = a.getColor(attr, Color.WHITE);
            } else if (attr == R.styleable.CustomDashBoardView_bottomTextSize) {
                mbottomTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.CustomDashBoardView_insideBackGroundColor) {
                minsideBackGroundColor = a.getColor(attr, Color.BLACK);
            } else if (attr == R.styleable.CustomDashBoardView_cycleBackGroundColor) {
                moutsideBackGroundColor = a.getColor(attr, Color.YELLOW);
            } else if (attr == R.styleable.CustomDashBoardView_outsideForeGroundColor) {
                moutsideForeGroundColor = a.getColor(attr, Color.BLUE);
            } else if (attr == R.styleable.CustomDashBoardView_backgroundColor) {
                mBackgroundColor = a.getColor(attr, Color.BLACK);
            }
        }
        a.recycle();
        init();
    }

    private void init() {

        mStartAngle = 90 + (mAngle / 2);
        mSweepAngle = 360 - mAngle;

        mCalibrationWidth = dp2px(10);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFCalibrationFArc = new RectF();
        mBackgroundColor = ContextCompat.getColor(getContext(), R.color.customdashboardview_black);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mPadding = Math.max(
                Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom())
        );
        setPadding(mPadding, mPadding, mPadding, mPadding);

        mLength1 = mPadding + dp2px(10) / 2f + dp2px(8);                //刻度圆弧外一圈

        int width = resolveSize(dp2px(220), widthMeasureSpec);

        //设置长宽
        setMeasuredDimension(width, width + dp2px(30));

        mCenterX = mCenterY = getMeasuredWidth() / 2f;

        mRectFCalibrationFArc.set(
                mLength1 + mCalibrationWidth / 2f,
                mLength1 + mCalibrationWidth / 2f,
                getMeasuredWidth() - mLength1 - mCalibrationWidth / 2f,
                getMeasuredWidth() - mLength1 - mCalibrationWidth / 2f
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mStartAngle = 90 + (mAngle / 2);
        mSweepAngle = 360 - mAngle;
        canvas.drawColor(mBackgroundColor);

        //光滑处理
        canvas.setDrawFilter( mSetfil );

        /**
         *  初始化动画
         */
        mPaint.setShader(null);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(moutsideForeGroundColor);//先设置颜色，再设置透明度
        mPaint.setAlpha(80);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setStrokeWidth(mCalibrationWidth);
        if (isAnimFinish) {
            /**
             * 画进度圆弧(起始到信用值)
             */
            canvas.drawArc(mRectFCalibrationFArc, mStartAngle + 3,
                    calculateRelativeAngleWithValue(mAnimValue), false, mPaint);
        } else {
            /**
             * 画进度圆弧(起始到信用值)
             */
            canvas.drawArc(mRectFCalibrationFArc, mStartAngle + 3,
                    mAngleWhenAnim - (mStartAngle + 2.99f), false, mPaint);
        }

        /**
         * 画刻度圆弧
         */
        mPaint.setShader(null);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(moutsideBackGroundColor);
        mPaint.setAlpha(80);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setStrokeWidth(mCalibrationWidth);
        canvas.drawArc(mRectFCalibrationFArc, mStartAngle + 3, mSweepAngle - 6, false, mPaint);

        /**
         * 画短刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画短刻度
         */
        mPaint.setStrokeWidth(dp2px(1));
        mPaint.setAlpha(80);
        float degree = mSweepAngle / mSection;
        float x10 = mCenterX;
        float y10 = mPadding + mLength1 - dp2px(1);
        float x11 = mCenterX;
        float y11 = y10 + mCalibrationWidth;
        // 逆时针到开始处
        canvas.save();
        canvas.drawLine(x10, y10, x11, y11, mPaint);
        for (int i = 0; i < mSection / 2; i++) {
            canvas.rotate(-degree, mCenterX, mCenterY);
            canvas.drawLine(x10, y10, x11, y11, mPaint);
        }
        canvas.restore();
        // 顺时针到结尾处
        canvas.save();
        for (int i = 0; i < mSection / 2; i++) {
            canvas.rotate(degree, mCenterX, mCenterY);
            canvas.drawLine(x10, y10, x11, y11, mPaint);
        }
        canvas.restore();

        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(dp2px(2));
        mPaint.setAlpha(120);
        float x00 = mCenterX;
        float y00 = mPadding + mLength1 - dp2px(5);
        float x01 = mCenterX;
        float y01 = y00 + mCalibrationWidth + dp2px(4);
        // 逆时针到开始处
        canvas.save();
        int degree0 = mSweepAngle / (mSection * mPortion);
        canvas.rotate(-degree0, mCenterX, mCenterY);//先旋转一点点
        canvas.drawLine(x00, y00, x01, y01, mPaint);
        for (int i = 0; i < (mSection / 2) - 1; i++) {
            canvas.rotate(-degree, mCenterX, mCenterY);
            canvas.drawLine(x00, y00, x01, y01, mPaint);
        }
        canvas.restore();
        // 顺时针到结尾处
        canvas.save();
        canvas.rotate(degree0, mCenterX, mCenterY);//先旋转一点点
        canvas.drawLine(x00, y00, x01, y01, mPaint);
        for (int i = 0; i < (mSection / 2) - 1; i++) {
            canvas.rotate(degree, mCenterX, mCenterY);
            canvas.drawLine(x00, y00, x01, y01, mPaint);
        }
        canvas.restore();

        /**
         * 画背景
         */
        mPaint.setColor(minsideBackGroundColor);                    //设置画笔颜色
        mPaint.setStrokeWidth((float) 3.0);              //线宽
        mPaint.setStyle(Paint.Style.FILL);                   
        //半径为centerY-长线Y
        canvas.drawCircle(mCenterX, mCenterY, mCenterY - (mPadding + mLength1 + mCalibrationWidth - dp2px(1)), mPaint);           //绘制圆形


        /**
         * 画实时度数值
         */
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setColor(mtitleNumTextColor);
        mPaint.setStyle(Paint.Style.FILL);  //内部填满
        mPaint.setAlpha(255);
        mPaint.setTextSize(sp2px(mtitleNumTextSize));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mtitleNumText = String.valueOf(mCurrentValue);
        canvas.drawText(mtitleNumText, mCenterX, mCenterY + dp2px(20), mPaint);

        /**
         * 画表头
         */
        mPaint.setAlpha(160);
        mPaint.setColor(mtitleTextColor);
        mPaint.setTextSize(sp2px(mtitleTextSize));
        canvas.drawText(mtitleText, mCenterX, mCenterY - dp2px(20), mPaint);

        /**
         * 画数字下方
         */
        mPaint.setAlpha(160);
        mPaint.setColor(mbottomTextColor);
        mPaint.setTextSize(sp2px(mbottomTextSize));
        canvas.drawText(mbottomText, mCenterX, mCenterY + dp2px(70), mPaint);
    }

    /**
     * 相对起始角度计算分值所对应的角度大小
     */
    private float calculateRelativeAngleWithValue(int value) {
        float i = 0f;
        float degreePerSection = 1f * mSweepAngle / mSection;
        i = (value - mMin) * mSection * degreePerSection / (mMax - mMin);
        if (i > 6) {
            return i - 6;
        } else {
            return 0.01f;
        }
    }

    /**
     * 设置分数值
     *
     * @param creditValue 值
     * @param AddAllValue 总值
     */
    public void setValues(int AddAllValue, int creditValue) {
        if (mAddAllValue == AddAllValue && creditValue == mCurrentValue) {
            return;
        }
        mMax = AddAllValue;
        mAddAllValue = AddAllValue;

        mCurrentValue = creditValue;
        mAnimValue = creditValue;
        postInvalidate();
    }

    /**
     * 设置分数值并播放动画
     *
     * @param creditValue 值
     * @param AddAllValue 总值
     */
    public void setValuesWithAnim(int AddAllValue, int creditValue) {
        if (mAddAllValue == AddAllValue && creditValue == mCurrentValue) {
            return;
        }
        mMax = AddAllValue;
        mAddAllValue = AddAllValue;
        mCurrentValue = creditValue;

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(350, mCurrentValue);
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimValue = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });

        // 计算最终值对应的角度，以扫过的角度的线性变化来播放动画
        float degree = calculateRelativeAngleWithValue(mCurrentValue);
//        Log.d("angle",(mStartAngle + 3)+","+(mStartAngle + 3 + degree));
        ValueAnimator degreeValueAnimator = ValueAnimator.ofFloat(mStartAngle + 3, mStartAngle + 3 + degree);
        degreeValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAngleWhenAnim = (float) animation.getAnimatedValue();
            }
        });

        long delay = 1000;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .setDuration(delay).playTogether(creditValueAnimator, degreeValueAnimator);
//                .playTogether(creditValueAnimator, degreeValueAnimator, colorAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimFinish = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimFinish = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isAnimFinish = true;
            }
        });
        animatorSet.start();
    }


    /**
     * 设置标题字符串，中间数值字符串，下字符串
     *
     * @param mtitleText
     */
    public void setMtitleText(String mtitleText) {
        this.mtitleText = mtitleText;
    }

    /**
     * 设置标题字符串颜色，中间数值字符串，下字符串
     *
     * @param mtitleTextColor
     */
    public void setMtitleTextColor(int mtitleTextColor) {
        this.mtitleTextColor = mtitleTextColor;
    }

    /**
     * 设置标题字符串大小，中间数值字符串，下字符串
     *
     * @param mtitleTextSize
     */
    public void setMtitleTextSize(int mtitleTextSize) {
        this.mtitleTextSize = mtitleTextSize;
    }

    /**
     * 设置中间数值字符串
     *
     * @param mtitleNumText
     */
    public void setMtitleNumText(String mtitleNumText) {
        this.mtitleNumText = mtitleNumText;
    }

    /**
     * 设置中间数值字符串颜色
     *
     * @param mtitleNumTextColor
     */
    public void setMtitleNumTextColor(int mtitleNumTextColor) {
        this.mtitleNumTextColor = mtitleNumTextColor;
    }

    /**
     * 设置中间数值字符串大小
     *
     * @param mtitleNumTextSize
     */
    public void setMtitleNumTextSize(int mtitleNumTextSize) {
        this.mtitleNumTextSize = mtitleNumTextSize;
    }

    /**
     * 设置下面的字符串
     *
     * @param mbottomNumText
     */
    public void setMbottomNumText(String mbottomNumText) {
        this.mbottomText = mbottomNumText;
    }

    /**
     * 设置下面的字符串颜色
     *
     * @param mtitleNumTextColor
     */
    public void setMbottomTextColor(int mtitleNumTextColor) {
        this.mtitleNumTextColor = mtitleNumTextColor;
    }

    /**
     * 设置下面的字符串大小
     *
     * @param mtitleNumTextSize
     */
    public void setMbottomTextSize(int mtitleNumTextSize) {
        this.mtitleNumTextSize = mtitleNumTextSize;
    }

    /**
     * 设置背景色
     */
    public void setMBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }


    /**
     * 设置中间背景色
     *
     * @param minsideBackGroundColor
     */
    public void setMinsideBackGroundColor(int minsideBackGroundColor) {
        this.minsideBackGroundColor = minsideBackGroundColor;
    }

    /**
     * 设置外圈背景色
     *
     * @param moutsideBackGroundColor
     */
    public void setMoutsideBackGroundColor(int moutsideBackGroundColor) {
        this.moutsideBackGroundColor = minsideBackGroundColor;
    }

    public void setMoutsideForeGroundColor(int moutsideBackGroundColor) {
        this.moutsideForeGroundColor = moutsideBackGroundColor;
    }

    /**
     * 设置透明度
     *
     * @param malph
     */
    public void setAlph(int malph) {
        this.malph = malph;
    }

    /**
     * 设置角度开口
     *
     * @param angle
     */
    public void setmAngle(int angle) {
        mAngle = angle;
    }

    /**
     * 工具方法 dp转px
     *
     * @param dp
     *
     * @return px
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 工具方法 sp转px
     *
     * @param sp
     *
     * @return px
     */
    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

}
