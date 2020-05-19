package cn.com.quick.p

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import cn.com.quick.utils.QuickKeyboardUtils
import cn.com.quick.widget.vp.indicator.DotPagesView


class MainActivity : AppCompatActivity() {

    private lateinit var viewSpace: View

    private val rootView by lazy {
        val scrollView = ScrollView(this)
        val view = LinearLayout(this)

        val view1 = View(this)
        view.addView(view1, 1080, 300)
        view.orientation = LinearLayout.VERTICAL
        view.gravity = Gravity.BOTTOM
        val editText = EditText(this)
        editText.hint = "你好"
        view.addView(editText)
        val dot = DotPagesView(this, ViewPager(this), 6)
        view.addView(dot, 66*3, 5*3)
        viewSpace = View(this)
        viewSpace.setBackgroundColor(Color.BLUE)
        view.addView(viewSpace, 1080, 0)

        val btn = Button(this)
        btn.setOnClickListener {
            QuickKeyboardUtils.showSoftInput(editText)
        }
        view.addView(btn)
        scrollView.addView(view)
        return@lazy scrollView
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rootView)
        QuickKeyboardUtils.init(this)
        QuickKeyboardUtils.startObserver(this, keyboardHeightBlock)

        rootView.post {
            val keyboardHeight = QuickKeyboardUtils.getKeyboardHeight(this)
            quickLogE("keyboardHeight-> $keyboardHeight")
        }
    }

    private val keyboardHeightBlock:(Int, Int) -> Unit =  { a, b ->
        quickLogE("a: $a------------------------------b: $b")
    }
}
