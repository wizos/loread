package me.wizos.loread.utils.colorful;

import android.app.Activity;
import android.content.res.Resources.Theme;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.socks.library.KLog;

import java.util.HashSet;
import java.util.Set;

import me.wizos.loread.R;
import me.wizos.loread.utils.colorful.setter.TextColorSetter;
import me.wizos.loread.utils.colorful.setter.ViewBackgroundColorSetter;
import me.wizos.loread.utils.colorful.setter.ViewBackgroundDrawableSetter;
import me.wizos.loread.utils.colorful.setter.ViewSetter;

/**
 * 主题切换控制类
 * 
 * @author mrsimple
 * 
 */
public final class Colorful {
	/**
	 * Colorful Builder
	 */
	Builder mBuilder;

	/**
	 * private constructor
	 * 
	 * @param builder
	 */
	private Colorful(Builder builder) {
		mBuilder = builder;
	}

	/**
	 * 设置新的主题
	 * 
	 * @param newTheme
	 */
	public void setTheme(int newTheme) {
		mBuilder.setTheme(newTheme);
	}

	/**
	 * 
	 * 构建Colorful的Builder对象
	 * 
	 * @author mrsimple
	 * 
	 */
	public static class Builder {
		/**
		 * 存储了视图和属性资源id的关系表
		 */
		Set<ViewSetter> mElements = new HashSet<ViewSetter>();
		/**
		 * 目标Activity
		 */
		Activity mActivity;

		/**
		 * @param activity
		 */
		public Builder(Activity activity) {
			mActivity = activity;
		}

		/**
		 * 
		 * @param fragment
		 */
		public Builder(Fragment fragment) {
			mActivity = fragment.getActivity();
		}

		private View findViewById(int viewId) {
			return mActivity.findViewById(viewId);
		}

		/**
		 * 将View id与存储该view背景色的属性进行绑定
		 * 
		 * @param viewId
		 *            控件id
		 * @param colorId
		 *            颜色属性id
		 * @return
		 */
		public Builder backgroundColor(int viewId, int colorId) {
			mElements.add(new ViewBackgroundColorSetter(findViewById(viewId),
					colorId));
			return this;
		}

		/**
		 * 将View id与存储该view背景Drawable的属性进行绑定
		 * 
		 * @param viewId
		 *            控件id
		 * @param drawableId
		 *            Drawable属性id
		 * @return
		 */
		public Builder backgroundDrawable(int viewId, int drawableId) {
			mElements.add(new ViewBackgroundDrawableSetter(
					findViewById(viewId), drawableId));
			return this;
		}

		/**
		 * 将TextView id与存储该TextView文本颜色的属性进行绑定
		 * 
		 * @param viewId
		 *            TextView或者TextView子类控件的id
		 * @param colorId
		 *            颜色属性id
		 * @return
		 */
		public Builder textColor(int viewId, int colorId) {
			TextView textView = (TextView) findViewById(viewId);
			mElements.add(new TextColorSetter(textView, colorId));
			return this;
		}

		/**
		 * 用户手动构造并且添加Setter
		 * 
		 * @param setter
		 *            用户自定义的Setter
		 * @return
		 */
		public Builder setter(ViewSetter setter) {
			mElements.add(setter);
			return this;
		}

		/**
		 * 设置新的主题
		 * 
		 * @param newTheme
		 */
		protected void setTheme(int newTheme) {
			mActivity.setTheme(newTheme);
			makeChange(newTheme);
            refreshStatusBar();
		}

		/**
		 * 修改各个视图绑定的属性
		 */
		private void makeChange(int themeId) {
			Theme curTheme = mActivity.getTheme();
			for (ViewSetter setter : mElements) {
				setter.setValue(curTheme, themeId);
			}
		}
		/**
		 * 我新加的 方法一
		 * 刷新 StatusBar
		 */
		private void refreshStatusBar() {
			TypedValue typedValue = new TypedValue();
			Theme theme = mActivity.getTheme();
			theme.resolveAttribute(R.attr.status_bar, typedValue, true);
			StatusBarUtil.setColorNoTranslucent(mActivity,mActivity.getResources().getColor(typedValue.resourceId ));
			KLog.d("【修改状态栏】" + typedValue.resourceId );

		}

//		/**
//		 * 我新加的 方法二
//		 * 刷新 StatusBar
//		 */
//		protected void initSystemBar(int colorId) {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				setTranslucentStatus(true);
//				SystemBarTintManager tintManager = new SystemBarTintManager(mActivity);
//				tintManager.setStatusBarTintColor(mActivity.getResources().getColor(R.color.main_grey_dark));
//				tintManager.setStatusBarTintEnabled(true);
//			}
//		}
//		private void setTranslucentStatus(boolean on) {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				Window win = mActivity.getWindow();
//				WindowManager.LayoutParams winParams = win.getAttributes();
//				final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//				if (on) {
//					winParams.flags |= bits;
//				} else {
//					winParams.flags &= ~bits;
//				}
//				win.setAttributes(winParams);
//			}
//		}
//
//
//		/**
//		 * 我新加的 方法三
//		 * 刷新 StatusBar
//		 * 变紫色
//		 */
//		/**
//		 * 设置状态栏颜色
//		 *
//		 * @param activity 需要设置的activity
//		 * @param color    状态栏颜色值
//		 */
//		private static void setStatusBarColor(Activity activity, int color) {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				// 设置状态栏透明
//				activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
////				 生成一个状态栏大小的矩形
//				View statusView = createStatusView(activity, color);
//				// 添加 statusView 到布局中
//				ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
//				decorView.addView(statusView);
//				// 设置根布局的参数
//				ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
//				rootView.setFitsSystemWindows(true);
//				rootView.setClipToPadding(true);
//			}
//		}
//		/**
//		 * 生成一个和状态栏大小相同的矩形条
//		 *
//		 * @param activity 需要设置的activity
//		 * @param color    状态栏颜色值
//		 * @return 状态栏矩形条
//		 */
//		private static View createStatusView(Activity activity, int color) {
//			// 获得状态栏高度
//			int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
//			int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
//
//			// 绘制一个和状态栏一样高的矩形
//			View statusView = new View(activity);
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//					statusBarHeight);
//			statusView.setLayoutParams(params);
//			statusView.setBackgroundColor(color);
//			return statusView;
//		}



		/**
		 * 创建Colorful对象
		 * 
		 * @return
		 */
		public Colorful create() {
			return new Colorful(this);
		}
	}



}
