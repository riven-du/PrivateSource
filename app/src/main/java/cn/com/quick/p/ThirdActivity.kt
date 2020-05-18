package cn.com.quick.p

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.util.Log

class ThirdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        Log.e("Activity", "ThirdActivity  ${Process.myPid()}")
    }
}
