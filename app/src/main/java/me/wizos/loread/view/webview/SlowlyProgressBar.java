package me.wizos.loread.view.webview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

/**
 * @author 林冠宏 on 2016/7/11. Wizos on 2018/3/18
 *         https://github.com/af913337456/SlowlyProgressBar
 */

public class SlowlyProgressBar {
    private ProgressBar progressBar;
    private boolean isStart = false;

    public SlowlyProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    /**
     * 在 WebViewClient onPageStarted 调用
     */
    public void onProgressStart() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setAlpha(1.0f);
    }

    /**
     * 在 WebChromeClient onProgressChange 调用
     */
    public void onProgressChange(int newProgress) {
        int currentProgress = progressBar.getProgress();
        newProgress = newProgress > currentProgress ? newProgress : currentProgress;
//        KLog.e("进度" + newProgress );
        if (newProgress >= 100 && !isStart) {
            /** 防止调用多次动画 */
            isStart = true;
            progressBar.setProgress(newProgress);
            /** 开启属性动画让进度条平滑消失*/
            startDismissAnimation(progressBar.getProgress());
        } else {
            /** 开启属性动画让进度条平滑递增 */
            startProgressAnimation(newProgress, currentProgress);
        }
    }

    /**
     * progressBar 进度缓慢递增，300ms/次
     */
    private void startProgressAnimation(int newProgress, int currentProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, newProgress);
        animator.setDuration(300);
        /* 减速形式的加速器，个人喜好 */
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private void startDismissAnimation(final int progress) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(progressBar, "alpha", 1.0f, 0.0f);
        // 动画时长
        anim.setDuration(1500);
        // 减速
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 0.0f ~ 1.0f
                float fraction = valueAnimator.getAnimatedFraction();
                int offset = 100 - progress;
                progressBar.setProgress((int) (progress + offset * fraction));
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setProgress(0);
                progressBar.setVisibility(View.GONE);
                isStart = false;
            }
        });
        anim.start();
    }

}
