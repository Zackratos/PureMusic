package org.zack.music.http

import io.reactivex.Observable
import io.reactivex.ObservableTransformer

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/29
 */
object RxUtils {

    fun <T>resultData(): ObservableTransformer<Response<T>, T> {
        return ObservableTransformer { it.flatMap { Observable.just(it.data) } }
    }

}