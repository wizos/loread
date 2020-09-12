package me.wizos.loread.view.slideback.callback;

public abstract class SlideCallBack implements SlideBackCallBack {
    private SlideBackCallBack callBack;

    public SlideCallBack() {
    }

    public SlideCallBack(SlideBackCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onSlideBack() {
        if (null != callBack) {
            callBack.onSlideBack();
        }
    }
//    @Override
//    public void onViewSlideUpdate(int offset) {
//        if (null != callBack) {
//            callBack.onViewSlideUpdate(offset);
//        }
//    }

    /**
     * 滑动来源： <br>
     * EDGE_LEFT    左侧侧滑 <br>
     * EDGE_RIGHT   右侧侧滑 <br>
     */
    public abstract void onSlide(int edgeFrom);

    public abstract void onViewSlide(int edgeFrom, int offset);
}