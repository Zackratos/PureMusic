package org.zack.music.tools

import android.util.Log
import org.zack.music.BuildConfig

/**
 * @Author  Zackratos
 * @Data    18-5-21
 * @Email   869649339@qq.com
 */
object LogUtil {

    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg)
    }

    fun d(msg: String) {
        d("pure", msg)
    }

}