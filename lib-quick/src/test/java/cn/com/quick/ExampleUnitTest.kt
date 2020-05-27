package cn.com.quick

import android.provider.MediaStore
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private val SELECTION = "${MediaStore.Files.FileColumns.MEDIA_TYPE} =? AND ${MediaStore.MediaColumns.SIZE} > 0 GROUP BY bucket_id"

    @Test
    fun addition_isCorrect() {
        //assertEquals(4, 2 + 2)

        println(SELECTION)
    }
}
