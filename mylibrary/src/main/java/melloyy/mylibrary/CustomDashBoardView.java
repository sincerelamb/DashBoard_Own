package melloyy.mylibrary;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private String mtitleText;     //中间数值上方
    private String mtitleNumText;  //中间数值
    private String mbottomNumText; //下方数值整个字符串
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
    /**
     * 透明度
     */
    private int malph;
    /**
     * 总值是下面小字的值，占值是中间的值
     * 最小值最大值是对应view中的坐标值
     */
    private int mMin = 0; // 最小值
    private int mMax = 950; // 最大值
    private int mAddAllValue = mMax;  // 总值
    private int mCurrentValue; // 占值
    /**
     * 角度可以设置
     */
    private int mAngle = 0;


    /**
     * 标识↓
     */
    private int mRadius; // 画布边缘半径（去除padding后的半径）
    private int mStartAngle = 150; // 起始角度
    private int mSweepAngle = 240; // 绘制角度
    private int mSection = 10; // 值域（mMax-mMin）等分份数
    private int mPortion = 3; // 一个mSection等分份数
    private String mHeaderText = "BETA"; // 表头
    private int mCreditValue = 650; // 信用分
    private int mSolidCreditValue = mCreditValue; // 信用分(设定后不变)
    private int mSparkleWidth; // 亮点宽度
    private int mProgressWidth; // 进度圆弧宽度
    private float mLength1; // 刻度顶部相对边缘的长度
    private int mCalibrationWidth; // 刻度圆弧宽度
    private float mLength2; // 刻度读数顶部相对边缘的长度

    private int mPadding;
    private float mCenterX, mCenterY; // 圆心坐标
    private Paint mPaint;
    private RectF mRectFProgressArc;
    private RectF mRectFCalibrationFArc;
    private RectF mRectFTextArc;
    private Path mPath;
    private Rect mRectText;
    private int mBackgroundColor;
    private int[] mBgColors;
    /**
     * 由于界面值不是线性排布，所以播放动画时若以值为参考，则会出现忽慢忽快
     * 的情况（开始以为是卡顿）。因此，先计算出最终到达角度，以扫过的角度为线性参考，动画就流畅了
     */
    private boolean isAnimFinish = true;
    private float mAngleWhenAnim;


    public CustomDashBoardView(Context context) {
        this(context, null);
    }

    public CustomDashBoardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDashBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获得我们所定义的自定义样式属性
        //3个文本的内容颜色大小
        //中间背景色的设定
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomDashBoardView, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.CustomDashBoardView_titleText){
                mtitleText = a.getString(attr);
            }else if(attr == R.styleable.CustomDashBoardView_titleTextColor){
                // 默认颜色设置为黑色
                mtitleTextColor = a.getColor(attr, Color.BLUE);
            }else if(attr == R.styleable.CustomDashBoardView_titleTextSize){
// 默认设置为16sp，TypeValue也可以把sp转化为px
                mtitleTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            }else if(attr == R.styleable.CustomDashBoardView_titleNumText){
                mtitleText = a.getString(attr);
            }else if(attr == R.styleable.CustomDashBoardView_titleNumTextColor){
                mtitleTextColor = a.getColor(attr, Color.BLUE);
            }else if(attr == R.styleable.CustomDashBoardView_titleNumTextSize){
                mtitleTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            }else if(attr == R.styleable.CustomDashBoardView_bottomNumText){
                mtitleText = a.getString(attr);
            }else if(attr == R.styleable.CustomDashBoardView_bottomTextColor){
                mtitleTextColor = a.getColor(attr, Color.BLUE);
            }else if(attr == R.styleable.CustomDashBoardView_bottomTextSize){
                mtitleTextSize = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
            }else if(attr == R.styleable.CustomDashBoardView_insideBackGroundColor){
                minsideBackGroundColor = a.getColor(attr, Color.GRAY);
            }else if(attr == R.styleable.CustomDashBoardView_cycleBackGroundColor){
                moutsideBackGroundColor = a.getColor(attr,Color.WHITE);
            }
        }
        a.recycle();
    }

    private void init() {
        mProgressWidth = dp2px(3);     // 进度圆弧宽度
        mCalibrationWidth = dp2px(10); // 刻度圆弧宽度

        mPaint = new Paint();          //初始化画笔
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFProgressArc = new RectF();
        mRectFCalibrationFArc = new RectF();
        mRectFTextArc = new RectF();
        mPath = new Path();
        mRectText = new Rect();

        mBackgroundColor = ContextCompat.getColor(getContext(), R.color.customdashboardview_black);


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
        this.mbottomNumText = mbottomNumText;
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

    public void setAlph(int malph){
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
     * 设置双值 总值 中间值
     *
     * @param AddAllValue  总值
     * @param CurrentValue 中间值
     */
    public void setValues(int AddAllValue, int CurrentValue) {
        if (mAddAllValue == AddAllValue) {
            return;
        }
        mMax = AddAllValue;
        mAddAllValue = AddAllValue;
        mCurrentValue = CurrentValue;
        postInvalidate();
    }

    /**
     * 设置双值 有动画
     *
     * @param AddAllValue  总值
     * @param CurrentValue 中间值
     */
    public void setValuesWithAnim(int AddAllValue, int CurrentValue) {
        if (mAddAllValue == AddAllValue) {
            return;
        }
        mMax = AddAllValue;
        mAddAllValue = AddAllValue;
        mCurrentValue = CurrentValue;
        postInvalidate();
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

    /**
     * 计算旋转角度
     * @param value
     * @return
     */
//    private float calculateRelativeAngleWithValue(int value) {
//        float degreePerSection = 1f * mSweepAngle / mSection;
//        if (value > 700) {
//            return 8 * degreePerSection + 2 * degreePerSection / 250 * (value - 700);
//        } else if (value > 650) {
//            return 6 * degreePerSection + 2 * degreePerSection / 50 * (value - 650);
//        } else if (value > 600) {
//            return 4 * degreePerSection + 2 * degreePerSection / 50 * (value - 600);
//        } else if (value > 550) {
//            return 2 * degreePerSection + 2 * degreePerSection / 50 * (value - 550);
//        } else {
//            return 2 * degreePerSection / 200 * (value - 350);
//        }
//    }

    /**
     * 工具方法
     * 生成扫描/梯度渲染
     */
//    private SweepGradient generateSweepGradient() {
//        SweepGradient sweepGradient = new SweepGradient(mCenterX, mCenterY,
//                new int[]{Color.argb(0, 255, 255, 255), Color.argb(200, 255, 255, 255)},
//                new float[]{0, calculateRelativeAngleWithValue(mCreditValue) / 360}
//        );
//        Matrix matrix = new Matrix();
//        matrix.setRotate(mStartAngle - 1, mCenterX, mCenterY);
//        sweepGradient.setLocalMatrix(matrix);
//
//        return sweepGradient;
//    }


    /**
     * 工具方法
     * 环形渲染
     *
     * @param x 坐标
     * @param y 坐标
     */
    private RadialGradient generateRadialGradient(float x, float y) {
        return new RadialGradient(x, y, mSparkleWidth / 2f,
                new int[]{Color.argb(255, 255, 255, 255), Color.argb(80, 255, 255, 255)},
                new float[]{0.4f, 1},
                Shader.TileMode.CLAMP
        );
    }

    /**
     * 工具方法，算出占比的点
     *
     * @param radius 半径
     * @param angle  角度
     *
     * @return 对应的点
     */

    private float[] getCoordinatePoint(float radius, float angle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

}
