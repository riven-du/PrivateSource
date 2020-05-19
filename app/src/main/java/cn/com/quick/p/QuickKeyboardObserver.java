package cn.com.quick.p;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Date :2020/5/19 10:44
 * Description:
 * History
 */
public class QuickKeyboardObserver extends PopupWindow implements LifecycleObserver, ViewTreeObserver.OnGlobalLayoutListener {

    private FrameLayout mPopupView;
    private View mActivityContentView;
    private FragmentActivity mActivity;
    private KeyboardChangeListener mKeyboardChangeListener;
    public static void startObserver(@NonNull Object target, @NonNull KeyboardChangeListener keyboardChangeListener) {
        new QuickKeyboardObserver(target, keyboardChangeListener);
    }

    private QuickKeyboardObserver(@NonNull Object target, @NonNull KeyboardChangeListener keyboardChangeListener) {
        try {
            if (target instanceof FragmentActivity) {
                mActivity = (FragmentActivity) target;
                mActivity.getLifecycle().addObserver(this);
            } else if (target instanceof Fragment) {
                Fragment fragment = (Fragment) target;
                mActivity = fragment.getActivity();
                fragment.getLifecycle().addObserver(this);
            }
            mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            mKeyboardChangeListener = keyboardChangeListener;
            mPopupView = new FrameLayout(mActivity);
            mPopupView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setContentView(mPopupView);
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            mActivityContentView = mActivity.getWindow().getDecorView();
            setWidth(0);
            setHeight(WindowManager.LayoutParams.MATCH_PARENT);
            mPopupView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        mActivity.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if (!isShowing() && mActivityContentView.getWindowToken() != null) {
                    setBackgroundDrawable(new ColorDrawable(0));
                    mActivityContentView.getWindowVisibleDisplayFrame(actRect);
                    showAtLocation(mActivityContentView, Gravity.NO_GRAVITY, actRect.left, actRect.top);
                }
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        hideSoftInput(mActivity);
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mPopupView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        dismiss();
        mKeyboardChangeListener = null;
        mActivityContentView = null;
        mActivity = null;
        mPopupView = null;
    }

    private Rect popRect = new Rect();
    private Rect actRect = new Rect();


    @Override
    public void onGlobalLayout() {
        mPopupView.getWindowVisibleDisplayFrame(popRect);
        View rootView = mActivityContentView.getRootView();
        mActivityContentView.getWindowVisibleDisplayFrame(actRect);

        int rootHeight = rootView.getHeight();

        int keyboardHeight = actRect.bottom - popRect.bottom;

        int tmpKeyboardHeight = getKeyboardHeight2(mActivity);

        // 没有底部虚拟导航栏 或者 虚拟导航栏关闭状态
        if (rootHeight == actRect.bottom) {
            if (tmpKeyboardHeight != keyboardHeight) {
                saveKeyboardHeight(mActivity, keyboardHeight);
                notifyKeyboardHeightChanged(keyboardHeight, 0);
            }
        }
        // 虚拟导航栏展示状态
        else if (rootHeight == actRect.bottom + getNavBarHeight()) {
            if (tmpKeyboardHeight != keyboardHeight) {
                saveKeyboardHeight1(mActivity, keyboardHeight);
                notifyKeyboardHeightChanged(keyboardHeight, 0);
            }
        }
        saveKeyboardHeight2(mActivity, keyboardHeight);
    }

    private int getNavBarHeight() {
        Resources res = mActivity.getResources();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    private void notifyKeyboardHeightChanged(int height, int windowHeight) {
        if (mKeyboardChangeListener != null) {
            mKeyboardChangeListener.onKeyboardHeightChanged(height, windowHeight);
        }
    }

    /**
     * 显示软键盘
     */
    public static void showSoftInput(@NonNull Activity activity) {
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.toggleSoftInput(0, 0);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(@NonNull final Activity activity) {
        View view = activity.getWindow().getDecorView();
        if (view.getContext() == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveKeyboardHeight(@NonNull Context context, int keyboardHeight) {
        try {
            if (keyboardHeight == 0) {
                return;
            }
            SharedPreferences.Editor editor = getSp(context).edit().putInt("Keyboard_height", keyboardHeight);
            editor.apply();
        } catch (Exception ignore) {}
    }

    private void saveKeyboardHeight1(@NonNull Context context, int keyboardHeight) {
        try {
            if (keyboardHeight == 0) {
                return;
            }
            SharedPreferences.Editor editor = getSp(context).edit().putInt("Keyboard_height1", keyboardHeight);
            editor.apply();
        } catch (Exception ignore) {}
    }

    private void saveKeyboardHeight2(@NonNull Context context, int keyboardHeight) {
        try {
            SharedPreferences.Editor editor = getSp(context).edit().putInt("Keyboard_height2", keyboardHeight);
            editor.apply();
        } catch (Exception ignore) {}
    }

    private static int getKeyboardHeight(@NonNull Context context) {
        return getSp(context).getInt("Keyboard_height", 0);
    }

    private static int getKeyboardHeight1(@NonNull Context context) {
        return getSp(context).getInt("Keyboard_height1", 0);
    }

    private static int getKeyboardHeight2(@NonNull Context context) {
        return getSp(context).getInt("Keyboard_height2", 0);
    }

    private static SharedPreferences getSp(@NonNull Context context) {
        return context.getSharedPreferences("QuickKeyboardObserver", Context.MODE_PRIVATE);
    }

    public static void receiveKeyboardHeight(@NonNull final Activity context, final OnKeyboardHeightListener onKeyboardHeightListener) {
        if (onKeyboardHeightListener == null) {
            return;
        }
        final View decorView = context.getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                Resources res = context.getResources();
                int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
                int navHeight = 0;
                if (resourceId != 0) {
                    navHeight = res.getDimensionPixelSize(resourceId);
                }
                Rect outRect = new Rect();
                View rootView = decorView.getRootView();
                decorView.getWindowVisibleDisplayFrame(outRect);
                int rootHeight = rootView.getHeight();
                if (outRect.bottom == rootHeight) {
                    onKeyboardHeightListener.onKeyboardHeight(getKeyboardHeight(context));
                } else if ((outRect.bottom + navHeight) == rootHeight){
                    onKeyboardHeightListener.onKeyboardHeight(getKeyboardHeight1(context));
                }
            }
        });
        getSp(context).getInt("Keyboard_height", 0);
    }

    public interface OnKeyboardHeightListener {
        void onKeyboardHeight(int keyboardHeight);
    }

    public interface KeyboardChangeListener {
        void onKeyboardHeightChanged(int keyboardHeight, int windowHeight);
    }
}