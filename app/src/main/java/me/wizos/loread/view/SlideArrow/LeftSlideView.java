package me.wizos.loread.view.SlideArrow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


public class LeftSlideView extends View {
    String TAG = "LeftSlideView";

    Path path, arrowPath;
    Paint paint, arrowPaint;

    //曲线的控制点
    float controlX = 0;

    String backViewColor = "#B3000000";

    float backViewHeight = 0;
    public static float width = 40;
    public static float height = 260;

    public LeftSlideView(Context context) {
        this(context, null);
    }

    public LeftSlideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftSlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backViewHeight = dp2px(260);

        path = new Path();
        arrowPath = new Path();

        paint = new Paint();
        paint.setAntiAlias(true); // 防止边缘的锯齿
        paint.setStyle(Paint.Style.FILL_AND_STROKE); // 既绘制轮廓也绘制内容
        paint.setColor(Color.parseColor(backViewColor));
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
        //画外面的东西
        path.reset();
        path.moveTo(0, 0);
//        KLog.e("画笔X：" + backViewHeight + "  " + controlX );
        path.quadTo(0, backViewHeight / 4, controlX / 3, backViewHeight * 3 / 8);
//        KLog.e("画笔A：" + backViewHeight/4 + "  " + controlX/3  + backViewHeight*3/8  );
        path.quadTo(controlX * 5 / 8, backViewHeight / 2, controlX / 3, backViewHeight * 5 / 8);
//        KLog.e("画笔B：" + controlX*5/8 + "  " + backViewHeight/2 + "  " + controlX/3 + "  " + backViewHeight*5/8 );
        path.quadTo(0, backViewHeight * 6 / 8, 0, backViewHeight);
//        KLog.e("画笔C：" + backViewHeight*6/8 + "  " + backViewHeight );
        canvas.drawPath(path, paint);

        //画里面的箭头
        arrowPath.reset();
        arrowPath.moveTo(controlX / 6 + (dp2px(5) * (controlX / (SlideArrowLayout.screenWidth / 6))), backViewHeight * 15 / 32);
        arrowPath.lineTo(controlX / 6, backViewHeight * 16.1f / 32);
        arrowPath.moveTo(controlX / 6, backViewHeight * 15.9f / 32);
        arrowPath.lineTo(controlX / 6 + (dp2px(5) * (controlX / (SlideArrowLayout.screenWidth / 6))), backViewHeight * 17 / 32);
        canvas.drawPath(arrowPath, arrowPaint);

        setAlpha(controlX / (SlideArrowLayout.screenWidth / 6));
    }

    public void cancelSlide() {
//        KLog.e(TAG, "做 cancelSlide: " );
        updateControlPoint(0);
    }

    public void updateControlPoint(float controlX) {
//        KLog.e(TAG, "左 updateControlPoint: "+controlX);
        this.controlX = controlX;
        invalidate();
    }

    public float dp2px(final float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }
}
