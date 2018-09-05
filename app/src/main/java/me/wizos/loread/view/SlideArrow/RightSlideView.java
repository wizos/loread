package me.wizos.loread.view.SlideArrow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class RightSlideView extends View {
    String TAG = "RightSlideView";

    Path path, arrowPath;
    Paint paint, arrowPaint;

    //曲线的控制点
    float controlX = 0;

    String backViewColor = "#B3000000";

    float backViewHeight = 0;
    public static float width = 40;
    public static float height = 260;

    public RightSlideView(Context context) {
        this(context, null);
    }

    public RightSlideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightSlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    float rightSlideViewWidth = 0;

    private void init() {
        rightSlideViewWidth = dp2px(40);
        controlX = rightSlideViewWidth;

//        KLog.e("屏幕宽度A：" + rightSlideViewWidth );

        backViewHeight = dp2px(260);

        path = new Path();
        arrowPath = new Path();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.parseColor(backViewColor));
        //圆环宽度
        paint.setStrokeWidth(1);

        arrowPaint = new Paint();
        arrowPaint.setAntiAlias(true);
        arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        arrowPaint.setColor(Color.WHITE);
        arrowPaint.setStrokeWidth(6);

        setAlpha(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        //画外面的东西
        path.reset();
//        KLog.e("画笔0：" + backViewHeight + "  " + controlX );
        path.moveTo(rightSlideViewWidth, 0);
        path.quadTo(rightSlideViewWidth, backViewHeight / 4, rightSlideViewWidth - controlX / 3, backViewHeight * 3 / 8);
//        KLog.e("画笔1：" + rightSlideViewWidth + "  " + backViewHeight/4 + "  "  + (rightSlideViewWidth - controlX/3)  + "  " + backViewHeight*3/8 );
        path.quadTo(rightSlideViewWidth - controlX * 5 / 8, backViewHeight / 2, rightSlideViewWidth - controlX / 3, backViewHeight * 5 / 8);
//        KLog.e("画笔2：" + (rightSlideViewWidth - controlX*5/8) + "  " + backViewHeight/2  + "  " + (rightSlideViewWidth - controlX/3)  + "  " + backViewHeight*5/8 );
        path.quadTo(rightSlideViewWidth, backViewHeight * 6 / 8, rightSlideViewWidth, backViewHeight);
//        KLog.e("画笔3：" + rightSlideViewWidth + "  " + backViewHeight*6/8  + "  " +  rightSlideViewWidth  + "  " +  backViewHeight);
        canvas.drawPath(path, paint);

        // 画里面的箭头
        arrowPath.reset();
        arrowPath.moveTo(rightSlideViewWidth - controlX / 6, backViewHeight * 15 / 32);
        arrowPath.lineTo(rightSlideViewWidth - controlX / 6 + (dp2px(5) * (controlX / (SlideArrowLayout.screenWidth / 6))), backViewHeight * 16.1f / 32);
        arrowPath.moveTo(rightSlideViewWidth - controlX / 6 + (dp2px(5) * (controlX / (SlideArrowLayout.screenWidth / 6))), backViewHeight * 15.9f / 32);
        arrowPath.lineTo(rightSlideViewWidth - controlX / 6, backViewHeight * 17 / 32);
        canvas.drawPath(arrowPath, arrowPaint);

        setAlpha(controlX / (SlideArrowLayout.screenWidth / 6));
    }

    // 默认坐标为右边界，此处的由边界1080为x=0
    public void cancelSlide() {
//        KLog.e(TAG, "右 cancelSlide: " );
        updateControlPoint(0);
    }

    // 默认坐标为右边界，此处的由边界1080为x=0
    public void updateControlPoint(float controlX) {
//        KLog.e(TAG, "右 updateControlPoint: "+ rightSlideViewWidth + "   " + controlX);
        this.controlX = controlX;
        invalidate();
    }

    public float dp2px(final float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }
}
