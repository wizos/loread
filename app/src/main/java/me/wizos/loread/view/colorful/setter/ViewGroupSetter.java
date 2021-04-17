package me.wizos.loread.view.colorful.setter;

import android.content.res.Resources.Theme;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * ViewGroup类型的Setter,用于修改ListView、RecyclerView等ViewGroup类型的Item
 * View,核心思想为遍历每个Item View中的子控件,然后根据用户绑定的view
 * id与属性来将View修改为当前Theme下的最新属性值，达到ViewGroup控件的换肤效果。
 * <p>
 * TODO : Color与Drawable的设计问题,是否需要修改为桥接模式 {@see ViewBackgroundColorSetter}、
 * {@see ViewBackgroundDrawableSetter}
 *
 * @author mrsimple
 */
public class ViewGroupSetter extends ViewSetter {

    /**
     * ListView的子试图的Setter
     */
    protected Set<ViewSetter> mItemViewSetters = new HashSet<ViewSetter>();

    /**
     * @param targetView
     * @param resId
     */
    public ViewGroupSetter(ViewGroup targetView, int resId) {
        super(targetView, resId);
    }

    public ViewGroupSetter(ViewGroup targetView) {
        super(targetView, 0);
    }

    /**
     * 设置View的背景色
     *
     * @param viewId
     * @param colorId
     * @return
     */
    public ViewGroupSetter childViewBgColor(int viewId, int colorId) {
        mItemViewSetters.add(new ViewBackgroundColorSetter(viewId, colorId));
        return this;
    }

    /**
     * 设置View的drawable背景
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public ViewGroupSetter childViewBgDrawable(int viewId, int drawableId) {
        mItemViewSetters.add(new ViewBackgroundDrawableSetter(viewId, drawableId));
        return this;
    }

    public ViewGroupSetter childImageSrcResource(int viewId, int resourceId) {
        mItemViewSetters.add(new ImageSrcResourceSetter(viewId, resourceId));
        return this;
    }

    /**
     * 设置文本颜色,因此View的类型必须为TextView或者其子类
     *
     * @param viewId
     * @param colorId
     * @return
     */
    public ViewGroupSetter childViewTextColor(int viewId, int colorId) {
        mItemViewSetters.add(new TextColorSetter(viewId, colorId));
        return this;
    }

    @Override
    public void setValue(Theme newTheme, int themeId) {
        int alpha = 255;
        if (mView == null) {
            return;
        }
        if (mView.getBackground() != null) {
            alpha = mView.getBackground().getAlpha();// 自加。保留透明度信息。
        }
        // XLog.i("需要修改底色为：" + getColor(newTheme) + " 透明度：" + alpha);
        mView.setBackgroundColor(getColor(newTheme));
        mView.getBackground().setAlpha(alpha);// 自加。保留透明度信息。
        // 清空AbsListView的元素
        // clearListViewRecyclerBin(mView);
        // 清空RecyclerView
        clearRecyclerViewRecyclerBin(mView);
        // 修改所有子元素的相关属性
        changeChildenAttrs((ViewGroup) mView, newTheme, themeId);
    }

    /**
     * @param viewId
     * @return
     */
    private View findViewById(View rootView, int viewId) {
		// Log.d("", "### viewgroup find view : " + targetView);
        return rootView.findViewById(viewId);
    }

    /**
     * 修改子视图的对应属性
     *
     * @param viewGroup
     * @param newTheme
     * @param themeId
     */
    private void changeChildenAttrs(ViewGroup viewGroup, Theme newTheme, int themeId) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = viewGroup.getChildAt(i);
            // 深度遍历
            if (childView instanceof ViewGroup) {
                changeChildenAttrs((ViewGroup) childView, newTheme, themeId);
            }

            // 遍历子元素与要修改的属性,如果相同那么则修改子View的属性
            for (ViewSetter setter : mItemViewSetters) {
                // 每次都要从ViewGroup中查找数据
                setter.mView = findViewById(viewGroup, setter.mViewId);

				// Log.e("", "### childView : " + childView + ", id = "
				// 		+ childView.getId());
				// Log.e("", "### setter view : " + setter.mView + ", id = "
				// 		+ setter.getViewId());
                if (childView.getId() == setter.getViewId()) {
                    setter.setValue(newTheme, themeId);
					// Log.e("", "@@@ 修改新的属性: " + childView);
                }
            }
        }
    }

    private void clearListViewRecyclerBin(View rootView) {
        if (rootView instanceof AbsListView) {
            try {
                Field localField = AbsListView.class.getDeclaredField("mRecycler");
                localField.setAccessible(true);
                Method localMethod = Class.forName("android.widget.AbsListView$RecycleBin").getDeclaredMethod("clear");
                localMethod.setAccessible(true);
                localMethod.invoke(localField.get(rootView));
				// Log.e("", "### 清空AbsListView的RecycerBin ");
            } catch (NoSuchFieldException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e2) {
                e2.printStackTrace();
            } catch (NoSuchMethodException e3) {
                e3.printStackTrace();
            } catch (IllegalAccessException e4) {
                e4.printStackTrace();
            } catch (InvocationTargetException e5) {
                e5.printStackTrace();
            }
        }
    }

    private void clearRecyclerViewRecyclerBin(View rootView) {
        if (rootView instanceof RecyclerView) {
            try {
                Field declaredField = RecyclerView.class.getDeclaredField("mRecycler");
                declaredField.setAccessible(true);
                Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("clear");
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(declaredField.get(rootView));
                ((RecyclerView) rootView).getRecycledViewPool().clear();
            } catch (NoSuchFieldException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                XLog.e("错误：" + e);
            }

            // XLog.i("### 准备 清空RecyclerView的Recycer ");
            // RecyclerView.RecycledViewPool recycledViewPool = ((RecyclerView) rootView).getRecycledViewPool();
            // recycledViewPool.clear();
            // try {
            //     ((RecyclerView) rootView).getRecycledViewPool().clear();
            //     Field localField = RecyclerView.class.getDeclaredField("mRecycler");
            //     localField.setAccessible(true);
            //     Method localMethod = Class.forName(
            //             "androidx.recyclerview.widget.RecyclerView$Recycler")
            //             .getDeclaredMethod("clear", new Class[0]);
            //     localMethod.setAccessible(true);
            //     localMethod.invoke(localField.get(rootView), new Object[0]);
            //     rootView.invalidate();
            //     ((RecyclerView) rootView).getRecycledViewPool().clear();
            //
            //     // XLog.i("", "### 清空RecyclerView的Recycer ");
            // } catch (NoSuchFieldException e1) {
            //     e1.printStackTrace();
            // } catch (ClassNotFoundException e2) {
            //     e2.printStackTrace();
            // } catch (NoSuchMethodException e3) {
            //     e3.printStackTrace();
            // } catch (IllegalAccessException e4) {
            //     e4.printStackTrace();
            // } catch (InvocationTargetException e5) {
            //     e5.printStackTrace();
            // }



            // Class<RecyclerView> recyclerViewClass = RecyclerView.class;
            // try {
            //     Field declaredField = recyclerViewClass.getDeclaredField("mRecycler" );
            //     declaredField.setAccessible(true);
            //     Method declaredMethod = Class.forName(RecyclerView.Recycler. class.getName()).getDeclaredMethod("clear", new Class[0]);
            //     declaredMethod.setAccessible(true);
            //     declaredMethod.invoke(declaredField.get(((RecyclerView) rootView)), new Object[0]);
            //     RecyclerView.RecycledViewPool recycledViewPool = ((RecyclerView) rootView).getRecycledViewPool();
            //     recycledViewPool.clear();
            // } catch (Exception e) {
            //     XLog.e("错误：" + e);
            // }

        }
    }

}
