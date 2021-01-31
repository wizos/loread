package com.reginald.editspinner;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;

/**
 * EditSpinner
 * modified from {@link android.widget.AutoCompleteTextView}
 *
 * @author xyxyLiu tonyreginald@gmail.com
 */
public class EditSpinner extends EditText {

    private static final boolean DEBUG = BuildConfig.DEBUG_LOG;
    private static final String TAG = "EditSpinner";

    private static final long TIMEOUT_POPUP_DISMISS = 200L;

    private ListPopupWindow mPopup;
    private ListAdapter mAdapter;
    private int mDropDownAnchorId;
    private int selected = -1;

    private Drawable mDropDownDrawable;

    private PopupWindow.OnDismissListener mOnDismissListener;
    private OnShowListener mOnShowListener;
    private AdapterView.OnItemClickListener mItemClickListener;
    private AdapterView.OnItemSelectedListener mItemSelectedListener;
    private ItemConverter mItemConverter;

    private boolean mDropDownDismissedOnCompletion = true;

    private long mLastDismissTime = 0L;
    private boolean mDropDownTouchedDown = false;
    private boolean mOpenBefore;

    private boolean mIsEditable = true;
    private KeyListener mKeyListener;

    public EditSpinner(Context context) {
        super(context, null);
        initFromAttributes(context, null, 0, 0);
    }

    public EditSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs, 0, 0);
    }

    public EditSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EditSpinner, defStyleAttr, defStyleRes);

        mPopup = new ListPopupWindow(context, attrs);
        mPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mPopup.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);

        Drawable selector = a.getDrawable(R.styleable.EditSpinner_dropDownSelector);
        if (selector != null) {
            mPopup.setListSelector(selector);
        }

        int dropDownAnimStyleResId = a.getResourceId(R.styleable.EditSpinner_dropDownAnimStyle, -1);
        if (dropDownAnimStyleResId > 0) {
            setDropDownAnimationStyle(dropDownAnimStyleResId);
        }

        mDropDownDrawable = a.getDrawable(R.styleable.EditSpinner_dropDownDrawable);
        int dropDownDrawableSpacing = a.getDimensionPixelOffset(R.styleable.EditSpinner_dropDownDrawableSpacing, 0);

        if (mDropDownDrawable != null) {
            int dropDownDrawableWidth = a.getDimensionPixelOffset(R.styleable.EditSpinner_dropDownDrawableWidth, -1);
            int dropDownDrawableHeight = a.getDimensionPixelOffset(R.styleable.EditSpinner_dropDownDrawableHeight, -1);
            setDropDownDrawable(mDropDownDrawable, dropDownDrawableWidth, dropDownDrawableHeight);
            setDropDownDrawableSpacing(dropDownDrawableSpacing);
        }

        // Get the anchor's id now, but the view won't be ready, so wait to actually get the
        // view and store it in mDropDownAnchorView lazily in getDropDownAnchorView later.
        // Defaults to NO_ID, in which case the getDropDownAnchorView method will simply return
        // this TextView, as a default anchoring point.
        mDropDownAnchorId = a.getResourceId(R.styleable.EditSpinner_dropDownAnchor,
                View.NO_ID);


        // For dropdown width, the developer can specify a specific width, or MATCH_PARENT
        // (for full screen width) or WRAP_CONTENT (to match the width of the anchored view).
        mPopup.setWidth(a.getLayoutDimension(R.styleable.EditSpinner_dropDownWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mPopup.setHeight(a.getLayoutDimension(R.styleable.EditSpinner_dropDownHeight,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        mPopup.setOnItemClickListener(new DropDownItemClickListener());
        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mLastDismissTime = SystemClock.elapsedRealtime();
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss();
                }
            }
        });
        a.recycle();

        mIsEditable = getKeyListener() != null;

        setFocusable(true);
        addTextChangedListener(new MyWatcher());

        Log.d(TAG, "mIsEditable = " + mIsEditable);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        boolean isEdit =  super.onCheckIsTextEditor();
        Log.d(TAG, "onCheckIsTextEditor = " + isEdit);
        return isEdit;
    }

    /**
     * set whether it can be edited
     * @param isEditable isEditable
     */
    public void setEditable(boolean isEditable) {
        if (mIsEditable == isEditable) return;
        mIsEditable = isEditable;
        if (isEditable) {
            if (mKeyListener != null) {
                setKeyListener(mKeyListener);
            }
        } else {
            mKeyListener = getKeyListener();
            setKeyListener(null);
        }
    }

    public int getDropDownWidth() {
        return mPopup.getWidth();
    }

    /**
     * <p>Sets the current width for the auto-complete drop down list. This can
     * be a fixed width, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill the screen, or
     * {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the width of its anchor view.</p>
     * @param width the width to use
     */
    public void setDropDownWidth(int width) {
        mPopup.setWidth(width);
    }

    public int getDropDownHeight() {
        return mPopup.getHeight();
    }

    /**
     * <p>Sets the current height for the auto-complete drop down list. This can
     * be a fixed height, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill
     * the screen, or {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the height
     * of the drop down's content.</p>
     * @param height the height to use
     */
    public void setDropDownHeight(int height) {
        mPopup.setHeight(height);
    }

    /**
     * <p>Returns the id for the view that the auto-complete drop down list is anchored to.</p>
     * @return the view's id, or {@link View#NO_ID} if none specified
     */
    public int getDropDownAnchor() {
        return mDropDownAnchorId;
    }

    /**
     * <p>Sets the view to which the auto-complete drop down list should anchor. The view
     * corresponding to this id will not be loaded until the next time it is needed to avoid
     * loading a view which is not yet instantiated.</p>
     * @param id the id to anchor the drop down list view to
     */
    public void setDropDownAnchor(int id) {
        mDropDownAnchorId = id;
        mPopup.setAnchorView(null);
    }

    public Drawable getDropDownBackground() {
        return mPopup.getBackground();
    }

    /**
     * <p>Sets the background of the auto-complete drop-down list.</p>
     * @param d the drawable to set as the background
     */
    public void setDropDownBackgroundDrawable(Drawable d) {
        mPopup.setBackgroundDrawable(d);
    }

    /**
     * <p>Sets the background of the auto-complete drop-down list.</p>
     * @param id the id of the drawable to set as the background
     */
    public void setDropDownBackgroundResource(int id) {
        mPopup.setBackgroundDrawable(getContext().getResources().getDrawable(id));
    }

    public int getDropDownVerticalOffset() {
        return mPopup.getVerticalOffset();
    }

    /**
     * <p>Sets the vertical offset used for the auto-complete drop-down list.</p>
     * @param offset the vertical offset
     */
    public void setDropDownVerticalOffset(int offset) {
        mPopup.setVerticalOffset(offset);
    }

    public int getDropDownHorizontalOffset() {
        return mPopup.getHorizontalOffset();
    }

    /**
     * <p>Sets the horizontal offset used for the auto-complete drop-down list.</p>
     * @param offset the horizontal offset
     */
    public void setDropDownHorizontalOffset(int offset) {
        mPopup.setHorizontalOffset(offset);
    }

    public int getDropDownAnimationStyle() {
        return mPopup.getAnimationStyle();
    }

    /**
     * <p>Sets the animation style of the auto-complete drop-down list.</p>
     *
     * <p>If the drop-down is showing, calling this method will take effect only
     * the next time the drop-down is shown.</p>
     * @param animationStyle animation style to use when the drop-down appears
     * and disappears.  Set to -1 for the default animation, 0 for no
     * animation, or a resource identifier for an explicit animation.
     */
    public void setDropDownAnimationStyle(int animationStyle) {
        mPopup.setAnimationStyle(animationStyle);
    }

    /**
     * Checks whether the drop-down is dismissed when a item is clicked.
     * @return isDropDownDismissedOnCompletion
     */
    public boolean isDropDownDismissedOnCompletion() {
        return mDropDownDismissedOnCompletion;
    }

    /**
     * Sets whether the drop-down is dismissed when a suggestion is clicked. This is
     * true by default.
     * @param dropDownDismissedOnCompletion Whether to dismiss the drop-down.
     */
    public void setDropDownDismissedOnCompletion(boolean dropDownDismissedOnCompletion) {
        mDropDownDismissedOnCompletion = dropDownDismissedOnCompletion;
    }

    public void clearListSelection() {
        mPopup.clearListSelection();
    }

    public int getListSelection() {
        return mPopup.getSelectedItemPosition();
    }

    public void setListSelection(int position) {
        mPopup.setSelection(position);
    }

    public int getSelected(){
        return selected;
    }
    public void selectItem(int index) {
        if (mAdapter != null && index >= 0 && index < mAdapter.getCount()) {
            selectItem(mAdapter.getItem(index));
            selected = index;
        }else {
            selected = -1;
        }
    }

    private void selectItem(Object selectedItem) {
        if (selectedItem != null) {
            replaceText(convertSelectionToString(selectedItem));
        }

    }

    public void setDropDownDrawable(Drawable drawable) {
        setDropDownDrawable(drawable, -1, -1);
    }

    public void setDropDownDrawable(Drawable drawable, int width, int height) {
        mDropDownDrawable = drawable;
        if (width >= 0 && height >= 0) {
            drawable.setBounds(new Rect(0, 0, width, height));
            setCompoundDrawables(null, null, drawable, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    public boolean isPopupShowing() {
        return mPopup.isShowing();
    }

    public void showDropDown() {
        if (mPopup.getAnchorView() == null) {
            if (mDropDownAnchorId != View.NO_ID) {
                mPopup.setAnchorView(getRootView().findViewById(mDropDownAnchorId));
            } else {
                mPopup.setAnchorView(this);
            }
        }
        if (!isPopupShowing()) {
            // Make sure the list does not obscure the IME when shown for the first time.
            mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        }

        requestFocus();
        mPopup.show();
        mPopup.getListView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        if (mOnShowListener != null) {
            mOnShowListener.onShow();
        }
    }

    public void dismissDropDown() {
        mPopup.dismiss();
    }

    public void setAdapter(ListAdapter adapter) {
        mAdapter = adapter;
        mPopup.setAdapter(mAdapter);
    }

    public void setDropDownDrawableSpacing(int spacing) {
        setCompoundDrawablePadding(spacing);
    }

    public int getDropDownDrawableSpacing() {
        return getCompoundDrawablePadding();
    }

    public void setOnDismissListener(final PopupWindow.OnDismissListener dismissListener) {
        mOnDismissListener = dismissListener;
    }

    public void setOnShowListener(final OnShowListener showListener) {
        mOnShowListener = showListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        mItemClickListener = l;
    }

    public void setItemConverter(ItemConverter itemConverter) {
        mItemConverter = itemConverter;
    }

    protected void performCompletion() {
        performCompletion(null, -1, -1);
    }

    protected void performCompletion(View selectedView, int position, long id) {
        if (isPopupShowing()) {
            Object selectedItem;
            if (position < 0) {
                selectedItem = mPopup.getSelectedItem();
            } else {
                selectedItem = mAdapter.getItem(position);
            }
            if (selectedItem == null) {
                if (DEBUG) {
                    Log.w(TAG, "performCompletion: no selected item");
                }
                return;
            }

            // 自己添加的
            selected = position;
            selectItem(selectedItem);

            if (mItemClickListener != null) {
                final ListPopupWindow list = mPopup;

                if (selectedView == null || position < 0) {
                    selectedView = list.getSelectedView();
                    position = list.getSelectedItemPosition();
                    id = list.getSelectedItemId();
                }
                mItemClickListener.onItemClick(list.getListView(), selectedView, position, id);
            }
        }

        if (mDropDownDismissedOnCompletion) {
            dismissDropDown();
        }
    }

    protected void replaceText(CharSequence text) {
        clearComposingText();

        setText(text);
        // make sure we keep the caret at the end of the text view
        Editable spannable = getText();
        Selection.setSelection(spannable, spannable.length());
    }

    protected CharSequence convertSelectionToString(Object selectedItem) {
        if (mItemConverter != null) {
            return mItemConverter.convertItemToString(selectedItem);
        } else {
            return selectedItem.toString();
        }
    }

    @Override
    protected void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
        switch (hint) {
            case INVISIBLE:
                dismissDropDown();
                break;
            case View.GONE:
            case View.VISIBLE:
                break;
        }
    }

    @Override
    protected boolean setFrame(final int l, int t, final int r, int b) {
        boolean result = super.setFrame(l, t, r, b);

        if (isPopupShowing()) {
            showDropDown();
        }

        return result;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused) {
            dismissDropDown();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        dismissDropDown();
        super.onDetachedFromWindow();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (DEBUG) {
            Log.d(TAG, "onTouchEvent() event = " + event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (isInDropDownClickArea(event)) {
                    mDropDownTouchedDown = true;
                    return true;
                } else {
                    mDropDownTouchedDown = false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mDropDownTouchedDown && isInDropDownClickArea(event)) {
                    if (SystemClock.elapsedRealtime() - mLastDismissTime > TIMEOUT_POPUP_DISMISS) {
                        clearFocus();
                        showDropDown();
                        return true;
                    } else {
                        dismissDropDown();
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }

    private boolean isInDropDownClickArea(MotionEvent event) {
        int areaLeft = mIsEditable ? getWidth() - getCompoundPaddingRight() : 0;
        int areaRight = getWidth();
        int areaTop = 0;
        int areaBottom = getHeight();

        if (DEBUG) {
            Log.d(TAG, String.format("x = %d, y = %d, areaLeft = %d, areaRight = %d, areaTop = %d, areaBottom = %d",
                    (int) event.getX(), (int) event.getY(), areaLeft, areaRight, areaTop, areaBottom));
        }
        if (event.getX() > areaLeft && event.getX() < areaRight && event.getY() > areaTop && event.getY() < areaBottom) {
            return true;
        } else {
            return false;
        }
    }


    void doBeforeTextChanged() {

        // when text is changed, inserted or deleted, we attempt to show
        // the drop down
        mOpenBefore = isPopupShowing();
        if (DEBUG) {
            Log.v(TAG, "before text changed: open=" + mOpenBefore);
        }
    }

    void doAfterTextChanged() {

        // if the list was open before the keystroke, but closed afterwards,
        // then something in the keystroke processing (an input filter perhaps)
        // called performCompletion() and we shouldn't do any more processing.
        if (DEBUG) {
            Log.v(TAG, "after text changed: openBefore=" + mOpenBefore
                    + " open=" + isPopupShowing());
        }
        if (mOpenBefore && !isPopupShowing()) {
            return;
        }

        if (isPopupShowing()) {
            dismissDropDown();
        }
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing()) {
            // special case for the back key, we do not even try to send it
            // to the drop down list but instead, consume it immediately
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    dismissDropDown();
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean consumed = mPopup.onKeyUp(keyCode, event);
        if (consumed) {
            switch (keyCode) {
                // if the list accepts the key events and the key event
                // was a click, the text view gets the selected item
                // from the drop down as its content
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_TAB:
                    if (event.hasNoModifiers()) {
                        performCompletion();
                    }
                    return true;
            }
        }

        if (isPopupShowing() && keyCode == KeyEvent.KEYCODE_TAB && event.hasNoModifiers()) {
            performCompletion();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPopup.onKeyDown(keyCode, event)) {
            return true;
        }

        if (isPopupShowing() && keyCode == KeyEvent.KEYCODE_TAB && event.hasNoModifiers()) {
            return true;
        }


        boolean handled = super.onKeyDown(keyCode, event);

        if (handled && isPopupShowing()) {
            clearListSelection();
        }

        return handled;
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, mDropDownDrawable != null ? mDropDownDrawable : right, bottom);
    }

    /**
     * Convert list item to string, which will be showed in EditText
     */
    public interface ItemConverter {
        String convertItemToString(Object selectedItem);
    }

    /**
     * Callback when popup window showed
     */
    public interface OnShowListener {
        void onShow();
    }

    /**
     * This is used to watch for edits to the text view.
     */
    private class MyWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            doAfterTextChanged();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            doBeforeTextChanged();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private class DropDownItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            performCompletion(v, position, id);
        }
    }
}
