package cn.com.quick.p

import android.content.ContentValues
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import cn.com.quick.utils.QuickKeyboardUtils
import cn.com.quick.utils.quickLogE
import java.io.File
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

        rootView.post {
            val keyboardHeight = QuickKeyboardUtils.getKeyboardHeight(this)
            quickLogE("keyboardHeight-> $keyboardHeight")
        }
        thread {
            try {

//                contentResolver.query()

                val fis = assets.open("a.pdf")
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "a.pdf")

                val url = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, "file/a")
                    MediaStore.Files.getContentUri("external")
                } else {
                    Uri.fromFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + File.separator + "Camera"))
                }

                quickLogE("$url")

                val insertUrl = contentResolver.insert(
                    url,
                    contentValues
                )

                val os = contentResolver.openOutputStream(insertUrl!!)!!
                var read: Int

                try {
                    val buffer = ByteArray(1444)

                    while (fis.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                        os.flush()
                    }

                    os.close()
                    fis.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val keyboardHeightBlock:(Int, Int) -> Unit =  { a, b ->
        quickLogE("a: $a------------------------------b: $b")
    }
}
