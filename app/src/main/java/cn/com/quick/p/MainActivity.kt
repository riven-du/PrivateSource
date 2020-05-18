package cn.com.quick.p

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Process
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        quickLogE("MainActivity  ${Process.myPid()}")
        btn_main.setOnClickListener {

            getSharedPreferences("sp", Context.MODE_PRIVATE).apply {
                quickLogE("data value:    ${getInt("key", -1)}")
            }

            startActivityForResult(Intent(it.context, SecondActivity::class.java), 2)
        }
        btn_main.movementMethod = LinkMovementMethod.getInstance()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            data?.apply {
                val extra = getStringExtra("args")
                quickLogE("args:  $extra")
            }
        }
    }
}
