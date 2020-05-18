package cn.com.quick.p

import android.app.Application
import android.content.Context
import android.os.Process.myPid

/**
 * Date :2020/4/16 16:24
 * Description:
 * History
 */
class App: Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        quickLogE("pid:  ${myPid()}")
    }
}