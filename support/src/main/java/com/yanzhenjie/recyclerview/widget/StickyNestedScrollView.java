/*
* Copyright 2017 Yan Zhenjie
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.yanzhenjie.recyclerview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.widget.NestedScrollView;

import java.util.ArrayList;
import java.util.List;

/**
* Created by YanZhenjie on 2017/7/20.
*/
public class StickyNestedScrollView extends NestedScrollView {

   public interface OnViewStickyListener {
       void onSticky(View view);
       void onUnSticky(View view);
   }

   /**
    * Tag for views that should stick and have constant drawing. e.g. TextViews, ImageViews etc
    */
   public static final String STICKY_TAG = "sticky";
   /**
    * Flag for views that should stick and have non-constant drawing. e.g. Buttons, ProgressBars etc
    */
   public static final String FLAG_NONCONSTANT = "-nonconstant";
   /**
    * Flag for views that have aren't fully opaque
    */
   public static final String FLAG_HASTRANSPARENCY = "-hastransparency";
   /**
    * Default height of the shadow peeking out below the stuck view.
    */
   private static final int DEFAULT_SHADOW_HEIGHT = 10; // dp;
   private ArrayList<View> stickyViews;
   private View currentlyStickingView;
   private float stickyViewTopOffset;
   private final Runnable invalidateRunnable = new Runnable() {
       @Override
       public void run() {
           if (currentlyStickingView != null) {
               int l = getLeftForViewRelativeOnlyChild(currentlyStickingView);
               int t = getBottomForViewRelativeOnlyChild(currentlyStickingView);
               int r = getRightForViewRelativeOnlyChild(currentlyStickingView);
               int b = (int)(getScrollY() + (currentlyStickingView.getHeight() + stickyViewTopOffset));
               invalidate(l, t, r, b);
           }
           postDelayed(this, 16);
       }
   };
   private int stickyViewLeftOffset;
   private boolean redirectTouchesToStickyView;
   private boolean clippingToPadding;
   private boolean clipToPaddingHasBeenSet;
   private int mShadowHeight = DEFAULT_SHADOW_HEIGHT;
   private Drawable mShadowDrawable;
   private boolean hasNotDoneActionDown = true;

   private List<OnViewStickyListener> mOnViewStickyListeners;

   public StickyNestedScrollView(Context context) {
       this(context, null);
   }

   public StickyNestedScrollView(Context context, AttributeSet attrs) {
       this(context, attrs, android.R.attr.scrollViewStyle);
   }

   public StickyNestedScrollView(Context context, AttributeSet attrs, int defStyle) {
       super(context, attrs, defStyle);
       setup();
   }

   public void addOnViewStickyListener(OnViewStickyListener stickyListener) {
       if (mOnViewStickyListeners == null) mOnViewStickyListeners = new ArrayList<>();
       mOnViewStickyListeners.add(stickyListener);
   }

   public void removeOnViewStickyListener(OnViewStickyListener stickyListener) {
       if (mOnViewStickyListeners != null) mOnViewStickyListeners.remove(stickyListener);
   }

   public void clearOnViewStickyListener() {
       if (mOnViewStickyListeners != null) mOnViewStickyListeners.clear();
   }

   public void setShadowHeight(int height) {
       mShadowHeight = height;
   }

   public void setShadowDrawable(Drawable shadowDrawable) {
       mShadowDrawable = shadowDrawable;
   }

   public void setup() {
       Log.e("Sticky", "setup");
       stickyViews = new ArrayList<>();
   }

   private int getLeftForViewRelativeOnlyChild(View v) {
       Log.e("Sticky", "getLeftForViewRelativeOnlyChild");
       int left = v.getLeft();
       while (v.getParent() != null && v.getParent() != getChildAt(0)) {
           v = (View)v.getParent();
           left += v.getLeft();
       }
       return left;
   }

   private int getTopForViewRelativeOnlyChild(View v) {
       Log.e("Sticky", "getTopForViewRelativeOnlyChild");
       int top = v.getTop();
       while (v.getParent() != null && v.getParent() != getChildAt(0)) {
           v = (View)v.getParent();
           top += v.getTop();
       }
       return top;
   }

   private int getRightForViewRelativeOnlyChild(View v) {
       Log.e("Sticky", "getRightForViewRelativeOnlyChild");
       int right = v.getRight();
       while (v.getParent() != null && v.getParent() != getChildAt(0)) {
           v = (View)v.getParent();
           right += v.getRight();
       }
       return right;
   }

   private int getBottomForViewRelativeOnlyChild(View v) {
       Log.e("Sticky", "getBottomForViewRelativeOnlyChild");
       int bottom = v.getBottom();
       while (v.getParent() != null && v.getParent() != getChildAt(0)) {
           v = (View)v.getParent();
           bottom += v.getBottom();
       }
       return bottom;
   }

   @Override
   protected void onLayout(boolean changed, int l, int t, int r, int b) {
       super.onLayout(changed, l, t, r, b);
       Log.e("Sticky", "onLayout");
       if (!clipToPaddingHasBeenSet) {
           clippingToPadding = true;
       }
       notifyHierarchyChanged();
   }

   @Override
   public void setClipToPadding(boolean clipToPadding) {
       super.setClipToPadding(clipToPadding);
       Log.e("Sticky", "setClipToPadding");
       clippingToPadding = clipToPadding;
       clipToPaddingHasBeenSet = true;
   }

   @Override
   public void addView(View child) {
       super.addView(child);
       Log.e("Sticky", "addView   1  ");
       findStickyViews(child);
   }

   @Override
   public void addView(View child, int index) {
       super.addView(child, index);
       Log.e("Sticky", "addView   2  ");
       findStickyViews(child);
   }

   @Override
   public void addView(View child, int index, ViewGroup.LayoutParams params) {
       super.addView(child, index, params);
       Log.e("Sticky", "addView   3  ");
       findStickyViews(child);
   }

   @Override
   public void addView(View child, int width, int height) {
       super.addView(child, width, height);
       Log.e("Sticky", "addView   4 ");
       findStickyViews(child);
   }

   @Override
   public void addView(View child, ViewGroup.LayoutParams params) {
       super.addView(child, params);
       Log.e("Sticky", "addView   5  ");
       findStickyViews(child);
   }

   @Override
   protected void dispatchDraw(Canvas canvas) {
       super.dispatchDraw(canvas);
       Log.e("Sticky", "dispatchDraw");
       if (currentlyStickingView != null) {
           canvas.save();
           canvas.translate(getPaddingLeft() + stickyViewLeftOffset,
               getScrollY() + stickyViewTopOffset + (clippingToPadding ? getPaddingTop() : 0));
           canvas.clipRect(0, (clippingToPadding ? -stickyViewTopOffset : 0), getWidth() - stickyViewLeftOffset,
               currentlyStickingView.getHeight() + mShadowHeight + 1);
           if (mShadowDrawable != null) {
               int left = 0;
               int top = currentlyStickingView.getHeight();
               int right = currentlyStickingView.getWidth();
               int bottom = currentlyStickingView.getHeight() + mShadowHeight;
               mShadowDrawable.setBounds(left, top, right, bottom);
               mShadowDrawable.draw(canvas);
           }
           canvas.clipRect(0, (clippingToPadding ? -stickyViewTopOffset : 0), getWidth(),
               currentlyStickingView.getHeight());
           if (getStringTagForView(currentlyStickingView).contains(FLAG_HASTRANSPARENCY)) {
               showView(currentlyStickingView);
               currentlyStickingView.draw(canvas);
               hideView(currentlyStickingView);
           } else {
               currentlyStickingView.draw(canvas);
           }
           canvas.restore();
       }
   }

   @Override
   public boolean dispatchTouchEvent(MotionEvent ev) {
       Log.e("Sticky", "dispatchTouchEvent");
       if (ev.getAction() == MotionEvent.ACTION_DOWN) {
           redirectTouchesToStickyView = true;
       }
       if (redirectTouchesToStickyView) {
           redirectTouchesToStickyView = currentlyStickingView != null;
           if (redirectTouchesToStickyView) {
               redirectTouchesToStickyView = ev.getY() <= (currentlyStickingView.getHeight() + stickyViewTopOffset) &&
                   ev.getX() >= getLeftForViewRelativeOnlyChild(currentlyStickingView) &&
                   ev.getX() <= getRightForViewRelativeOnlyChild(currentlyStickingView);
           }
       } else if (currentlyStickingView == null) {
           redirectTouchesToStickyView = false;
       }
       if (redirectTouchesToStickyView) {
           ev.offsetLocation(0,
               -1 * ((getScrollY() + stickyViewTopOffset) - getTopForViewRelativeOnlyChild(currentlyStickingView)));
       }
       return super.dispatchTouchEvent(ev);
   }

   @Override
   public boolean onTouchEvent(MotionEvent ev) {
       Log.e("Sticky", "onTouchEvent");
       if (redirectTouchesToStickyView) {
           ev.offsetLocation(0,
               ((getScrollY() + stickyViewTopOffset) - getTopForViewRelativeOnlyChild(currentlyStickingView)));
       }
       if (ev.getAction() == MotionEvent.ACTION_DOWN) {
           hasNotDoneActionDown = false;
       }
       if (hasNotDoneActionDown) {
           MotionEvent down = MotionEvent.obtain(ev);
           down.setAction(MotionEvent.ACTION_DOWN);
           super.onTouchEvent(down);
           hasNotDoneActionDown = false;
       }
       if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
           hasNotDoneActionDown = true;
       }
       return super.onTouchEvent(ev);
   }

   @Override
   protected void onScrollChanged(int l, int t, int oldl, int oldt) {
       super.onScrollChanged(l, t, oldl, oldt);
       Log.e("Sticky", "处理滚动：" + l + " , " + t + " , " + oldl + " , " + oldt);
       doTheStickyThing();
   }

   private void doTheStickyThing() {
       View viewThatShouldStick = null;
       // 接近视图
       View approachingView = null;
       for (View v : stickyViews) {
           int viewTop = getTopForViewRelativeOnlyChild(v) - getScrollY() + (clippingToPadding ? 0 : getPaddingTop());
           if (viewTop <= 0) {
               if (viewThatShouldStick == null || viewTop >
                   (getTopForViewRelativeOnlyChild(viewThatShouldStick) - getScrollY() +
                       (clippingToPadding ? 0 : getPaddingTop()))) {
                   viewThatShouldStick = v;
               }
           } else {
               if (approachingView == null || viewTop <
                   (getTopForViewRelativeOnlyChild(approachingView) - getScrollY() +
                       (clippingToPadding ? 0 : getPaddingTop()))) {
                   approachingView = v;
               }
           }
       }
       if (viewThatShouldStick != null) {
           stickyViewTopOffset = approachingView == null
               ? 0
               : Math.min(0, getTopForViewRelativeOnlyChild(approachingView) - getScrollY() +
                   (clippingToPadding ? 0 : getPaddingTop()) - viewThatShouldStick.getHeight());
           if (viewThatShouldStick != currentlyStickingView) {
               if (currentlyStickingView != null) {
                   if (mOnViewStickyListeners != null) {
                       for (OnViewStickyListener onViewStickyListener : mOnViewStickyListeners)
                           onViewStickyListener.onUnSticky(currentlyStickingView);
                   }
                   stopStickingCurrentlyStickingView();
               }
               // only compute the left offset when we start sticking.
               stickyViewLeftOffset = getLeftForViewRelativeOnlyChild(viewThatShouldStick);
               startStickingView(viewThatShouldStick);
               if (mOnViewStickyListeners != null) {
                   for (OnViewStickyListener onViewStickyListener : mOnViewStickyListeners)
                       onViewStickyListener.onSticky(currentlyStickingView);
               }
           }
       } else if (currentlyStickingView != null) {
           if (mOnViewStickyListeners != null) {
               for (OnViewStickyListener onViewStickyListener : mOnViewStickyListeners)
                   onViewStickyListener.onUnSticky(currentlyStickingView);
           }
           stopStickingCurrentlyStickingView();
       }
   }

   private void startStickingView(View viewThatShouldStick) {
       Log.e("Sticky", "startStickingView");
       currentlyStickingView = viewThatShouldStick;
       if (currentlyStickingView != null) {
           if (getStringTagForView(currentlyStickingView).contains(FLAG_HASTRANSPARENCY)) {
               hideView(currentlyStickingView);
           }
           if (getStringTagForView(currentlyStickingView).contains(FLAG_NONCONSTANT)) {
               post(invalidateRunnable);
           }
       }
   }

   private void stopStickingCurrentlyStickingView() {
       Log.e("Sticky", "stopStickingCurrentlyStickingView");
       if (getStringTagForView(currentlyStickingView).contains(FLAG_HASTRANSPARENCY)) {
           showView(currentlyStickingView);
       }
       currentlyStickingView = null;
       removeCallbacks(invalidateRunnable);
   }

   @Override
   protected void onDetachedFromWindow() {
       Log.e("Sticky", "onDetachedFromWindow");
       removeCallbacks(invalidateRunnable);
       super.onDetachedFromWindow();
   }

   /**
    * Notify that the sticky attribute has been added or removed from one or more views in the View hierarchy
    */
   public void notifyStickyAttributeChanged() {
       notifyHierarchyChanged();
   }

   // 通知等级制度改变
   private void notifyHierarchyChanged() {
       Log.e("Sticky", "notifyHierarchyChanged");
       if (currentlyStickingView != null) {
           stopStickingCurrentlyStickingView();
       }
       stickyViews.clear();
       findStickyViews(getChildAt(0));
       doTheStickyThing();
       invalidate();
   }

   private void findStickyViews(View v) {
       Log.e("Sticky", "findStickyViews");
       if (!detainStickyView(v) && (v instanceof ViewGroup)) {
           ViewGroup vg = (ViewGroup)v;
           for (int i = 0; i < vg.getChildCount(); i++)
               findStickyViews(vg.getChildAt(i));
       }
   }

   // 扣留粘性视图
   private boolean detainStickyView(View view) {
       Log.e("Sticky", "detainStickyView");
       String tag = getStringTagForView(view);
       if (tag.contains(STICKY_TAG)) {
           stickyViews.add(view);
           return true;
       }
       return false;
   }

   private String getStringTagForView(View v) {
       Log.e("Sticky", "getStringTagForView");
       Object tagObject = v.getTag();
       return String.valueOf(tagObject);
   }

   private void hideView(View v) {
       v.setAlpha(0);
   }

   private void showView(View v) {
       v.setAlpha(1);
   }

}