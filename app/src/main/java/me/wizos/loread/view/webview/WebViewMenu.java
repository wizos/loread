package me.wizos.loread.view.webview;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.LayoutRes;

/**
 * @author Wizos on 2020/1/12.
 */

public class WebViewMenu extends PopupWindow {
    private WebViewMenu(Context context) {
        super(context);
    }

    public static class Builder {
        private final Context context;
        private final View parent;
        private final int contentRes;

        private int width = 0;
        private int height = 0;
        private int x = 0;
        private int y = 0;
        private ClickListener clickListener;

        /**
         * 构造函数用于 传入必须有的属性，必须有的属性要用final修饰
         * @param context
         * @param parent
         * @param contentRes
         */
        public Builder(Context context, View parent, @LayoutRes int contentRes) {
            this.context = context;
            this.parent = parent;
            this.contentRes = contentRes;
        }
        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }
        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }
        public Builder setOffsetX(int x){
            this.x = x;
            return this;
        }
        public Builder setOffsetY(int y){
            this.y = y;
            return this;
        }
        public Builder setOnClickListener(ClickListener clickListener){
            this.clickListener = clickListener;
            return this;
        }
        public WebViewMenu show(){
            LayoutInflater inflater = LayoutInflater.from(context);
            View contentView = inflater.inflate(contentRes, null);
            WebViewMenu popWindow = new WebViewMenu(context);
            //设置默认选项
            popWindow.setWidth(width);
            popWindow.setHeight(height);
            popWindow.setContentView(contentView);
            popWindow.setOutsideTouchable(true);
            popWindow.setFocusable(true);
            clickListener.setOnClickListener(popWindow, contentView);
            // 显示
            popWindow.showAtLocation(parent, Gravity.TOP | Gravity.START, x, y);
            return popWindow;
        }
    }

    public interface ClickListener{
        void setOnClickListener(WebViewMenu webViewMenu, View popView);
    }
}
