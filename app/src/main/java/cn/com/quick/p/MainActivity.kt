package cn.com.quick.p

import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import cn.com.quick.media.QuickMediaQuery
import cn.com.quick.utils.QuickKeyboardUtils
import cn.com.quick.utils.quickLogE
import kotlin.concurrent.thread


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
        viewSpace = View(this)
        viewSpace.setBackgroundColor(Color.BLUE)
        view.addView(viewSpace, 1080, 0)

        val btn = Button(this)
        btn.setOnClickListener {
        }
        view.addView(btn)
        scrollView.addView(view)
        return@lazy scrollView
    }


    private val projectionPhotos = arrayOf(
        MediaStore.Files.FileColumns.TITLE,
        MediaStore.Files.FileColumns._ID
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rootView)
        QuickKeyboardUtils.init(this)
        QuickKeyboardUtils.startObserver(this, keyboardHeightBlock)

        val quickMediaQuery = QuickMediaQuery(this)
        thread {
            quickMediaQuery.queryAllDoc(this)
        }
    }

    private val keyboardHeightBlock:(Int, Int) -> Unit =  { a, b ->
        quickLogE("a: $a------------------------------b: $b")
    }
}
