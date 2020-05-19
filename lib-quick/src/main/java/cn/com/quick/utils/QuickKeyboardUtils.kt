package cn.com.quick.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Date :2020/5/19 14:40
 * Description:
 * History
 */
object QuickKeyboardUtils {

    /**
     * 为了避免错误，在合适的时机，比如说是闪屏页，初始化，
     * 这样可以保证每次拿到对应屏幕状态下的缓存键盘高度
     */
    fun init(activity: Activity) {
        val decorView = activity.window.decorView
        decorView.post {
            val rect = Rect()
            val rootView = decorView.rootView
            decorView.getWindowVisibleDisplayFrame(rect)
            if (rootView.height == rect.bottom) {
                saveWindowRootEquals(activity, true)
            } else if (rootView.height == rect.bottom + getNavBarHeight(activity)) {
                saveWindowRootEquals(activity, false)
            }
        }
    }

    /**
     * 页面需要则调用一次 activity 中 onCreate 中调用， fragment 中 onCreateView 中调用
     */
    fun startObserver(target: Any, keyboardHeightBlock: (Int, Int) -> Unit) {
        KeyboardPopup(target, keyboardHeightBlock)
    }

    /**
     * 隐藏键盘
     */
    fun hideSoftInput(activity: Activity) {
        try {
            activity.window.decorView.apply {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示键盘
     */
    fun showSoftInput(focusableView: EditText) {
        try {
            focusableView.isFocusable = true
            focusableView.isFocusableInTouchMode = true
            focusableView.requestFocus()
            val imm = focusableView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(focusableView, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示键盘
     */
    fun showSoftInput(context: Context) {
        try {
            val keyboardHeightOnLastCache = getKeyboardHeightOnLastCache(context)
            if (keyboardHeightOnLastCache != 0) {
                return
            }
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(0,0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取键盘高度
     */
    fun getKeyboardHeight(context: Context): Int {
        return if (getWindowRootEquals(context)) getKeyboardHeightOnWindowEqualRoot(context) else getKeyboardHeightOnWindowNotEqualRoot(context)
    }

    /**
     * 获取底部导航栏的高度
     */
    private fun getNavBarHeight(context: Context): Int {
        val res: Resources = context.resources
        val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            res.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    /**
     * 缓存当前可用 window 和 root 的高度是否相等，
     * 为了适配现在全面屏手机 全面屏和普通导航的切换，原因是 有些全面屏手机的键盘高度和普通导航的键盘高度不同
     */
    private fun saveWindowRootEquals(context: Context, windowRootEquals: Boolean) {
        getSp(context).edit().apply {
            putBoolean("WindowRootEquals", windowRootEquals)
            apply()
        }
    }

    /**
     * 获取缓存当前可用 window 和 root 的高度是否相等的值
     */
    private fun getWindowRootEquals(context: Context): Boolean {
        return getSp(context).getBoolean("WindowRootEquals", true)
    }

    /**
     * 实时记录每次键盘的高度
     */
    private fun saveKeyboardHeightOnLastCache(context: Context, keyboardHeight: Int) {
        getSp(context).edit().apply {
            putInt("KeyboardHeightOnLastCache", keyboardHeight)
            apply()
        }
    }

    private fun getKeyboardHeightOnLastCache(context: Context): Int {
        return getSp(context).getInt("KeyboardHeightOnLastCache", 0)
    }

    /**
     * 缓存 window 和 root 的高度不同时 键盘的高度
     */
    private fun saveKeyboardHeightOnWindowNotEqualRoot(context: Context, keyboardHeight: Int) {
        if (keyboardHeight == 0) {
            return
        }
        getSp(context).edit().apply {
            putInt("KeyboardHeightOnWindowNotEqualRoot", keyboardHeight)
            apply()
        }
    }

    private fun getKeyboardHeightOnWindowNotEqualRoot(context: Context): Int {
        return getSp(context).getInt("KeyboardHeightOnWindowNotEqualRoot", 0)
    }

    /**
     *  缓存 window 和 root 的高度相同时 键盘的高度
     */
    private fun saveKeyboardHeightOnWindowEqualRoot(context: Context, keyboardHeight: Int) {
        if (keyboardHeight == 0) {
            return
        }
        getSp(context).edit().apply {
            putInt("KeyboardHeightOnWindowEqualRoot", keyboardHeight)
            apply()
        }
    }

    private fun getKeyboardHeightOnWindowEqualRoot(context: Context): Int {
        return getSp(context).getInt("KeyboardHeightOnWindowEqualRoot", 0)
    }

    private fun getSp(context: Context): SharedPreferences {
        return context.getSharedPreferences("QuickKeyboardObserver", Context.MODE_PRIVATE)
    }

    /**
     * 帮助获取键盘高度的 KeyboardPopup
     */
    private class KeyboardPopup (target: Any, private val keyboardHeightBlock: (Int, Int) -> Unit):PopupWindow(), LifecycleObserver, OnGlobalLayoutListener  {

        private val activity:FragmentActivity
        private val activityOriginalSoftInputMode:Int
        private val popupLayout: FrameLayout
        private val decorView: View
        private val window: Window
        private val activityRect = Rect()
        private val popupRect = Rect()
        private val rootView: View

        init {
            when (target) {
                is FragmentActivity -> {
                    activity = target
                    target.lifecycle.addObserver(this)
                }
                is Fragment -> {
                    activity = target.activity!!
                    target.lifecycle.addObserver(this)
                }
                else -> {
                    throw IllegalArgumentException("target is not androidx.fragment.app.FragmentActivity or androidx.fragment.app.Fragment")
                }
            }
            window = activity.window
            decorView = window.decorView
            rootView = decorView.rootView
            activityOriginalSoftInputMode = window.attributes.softInputMode
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

            // 初始化 popupWindow
            popupLayout = FrameLayout(activity)
            popupLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            contentView = popupLayout
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            inputMethodMode = INPUT_METHOD_NEEDED
            width = 0
            height = WindowManager.LayoutParams.MATCH_PARENT
            popupLayout.viewTreeObserver.addOnGlobalLayoutListener(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            decorView.post {
                if (!isShowing && decorView.windowToken != null) {
                    setBackgroundDrawable(ColorDrawable(0))
                    decorView.getWindowVisibleDisplayFrame(activityRect)
                    showAtLocation(decorView, Gravity.NO_GRAVITY, activityRect.left, activityRect.top)
                }
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            hideSoftInput(activity)
            window.setSoftInputMode(activityOriginalSoftInputMode)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            popupLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            dismiss()
        }

        override fun onGlobalLayout() {
            try {
                popupLayout.getWindowVisibleDisplayFrame(popupRect)
                val rootHeight = rootView.height
                val keyboardHeight = activityRect.bottom - popupRect.bottom
                val keyboardHeightOnLastCache = getKeyboardHeightOnLastCache(activity)
                // keyboardHeight keyboardHeightOnLastCache 不相等时向下分发 键盘高度和当前window的可用高度
                if (keyboardHeight != keyboardHeightOnLastCache) {
                    if (rootHeight == activityRect.bottom) {
                        saveKeyboardHeightOnWindowEqualRoot(activity, keyboardHeight)
                        saveWindowRootEquals(activity, true)
                        keyboardHeightBlock.invoke(keyboardHeight, activityRect.bottom)
                    } else if (rootHeight == activityRect.bottom + getNavBarHeight(activity)) {
                        saveKeyboardHeightOnWindowNotEqualRoot(activity, keyboardHeight)
                        saveWindowRootEquals(activity, false)
                        keyboardHeightBlock.invoke(keyboardHeight, activityRect.bottom)
                    }
                }
                saveKeyboardHeightOnLastCache(activity, keyboardHeight)
            } catch (ignore: Exception) {}
        }
    }
}