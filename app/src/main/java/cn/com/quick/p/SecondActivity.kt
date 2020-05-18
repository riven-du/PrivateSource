package cn.com.quick.p

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_second.*
import java.io.File

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        quickLogE("SecondActivity  ${Process.myPid()}")

        btn_sec.setOnClickListener {
//            startActivity(Intent(it.context, ThirdActivity::class.java))
//            setResult(0, Intent().putExtra("args", "0"))
//            finish()

            File("${externalCacheDir}${File.separator}a.txt").apply {
                deleteOnExit()
                createNewFile()
                writeText("123")
            }

//            getSharedPreferences("sp", Context.MODE_PRIVATE).edit().apply {
//                putInt("key", 300)
//                apply()
//            }
        }
    }
}
