/*
 * Copyright (C)  LeonDevLifeLog(https://github.com/Justson/AgentWeb)
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
package me.wizos.loread.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;

/**
 * 结合CoordinatorLayout可以与Toolbar联动的webview
 *
 * @author LeonDevLifeLog
 * @since 4.0.0
 */

public class NestedScrollWebView extends WebView implements NestedScrollingChild {
    /***
     * 方法一
     * https://github.com/fashare2015/NestedScrollWebView
     */
    public NestedScrollWebView(Context context) {
        super(context);
        initView();
        // 判断用户在进行滑动操作的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() + 100;
    }

    private static final int INVALID_POINTER = -1;

    private void initView() {
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }


    /**
     * Position of the last motion event.
     */
    private int mLastMotionY;

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Used during scrolling to retrieve the new offset within the window.
     */
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private NestedScrollingChildHelper mChildHelper;
    boolean mIsBeingDragged;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int actionMasked = ev.getActionMasked();

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;

                // Remember where the motion event started
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;

            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int deltaY = mLastMotionY - y;
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                }
                // Scroll to follow the motion event
                mLastMotionY = y - mScrollOffset[1];

                final int oldY = getScrollY();
                final int scrolledDeltaY = Math.max(0, oldY + deltaY) - oldY;
                final int unconsumedY = deltaY - scrolledDeltaY;
                if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
                    mLastMotionY -= mScrollOffset[1];
                }
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 作者：秋天的雨滴
     * 链接：https://www.jianshu.com/p/04d799608c2e
     * 解决该view上下滑动事件与子view左右滑动事件的冲突问题
     */
    private int mTouchSlop;
    private float mPrevX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(ev).getX();
                break;
            case MotionEvent.ACTION_MOVE:
                final float eventX = ev.getX();
                //获取水平移动距离
                float xDiff = Math.abs(eventX - mPrevX);
                //当水平移动距离大于滑动操作的最小距离的时候就认为进行了横向滑动
                //不进行事件拦截,并将这个事件交给子View处理
                if (xDiff > mTouchSlop) {
                    return false;
                }
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void endDrag() {
        mIsBeingDragged = false;
        stopNestedScroll();
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }


    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }


    /***
     * 方法二：但是在滚动滑出/滑入toolbar时，会卡顿一下
     * https://blog.csdn.net/m5314/article/details/68943869
     */
}
