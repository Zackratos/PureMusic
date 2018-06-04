package org.zack.music.tools

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * @Author  Zackratos
 * @Data    18-5-13
 * @Email   869649339@qq.com
 */
class RxBus private constructor(private val bus: Subject<Any>) {

/*    private val bus: Subject<Any> by lazy {
        PublishSubject.create<Any>().toSerialized()
    }*/

    companion object {
        fun getInstance(): RxBus {
            return Holder.INSTANCE
        }
    }

    // 提供了一个新的事件
    fun post(o: Any) {
        bus.onNext(o)
    }

    // 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
    fun <T> toObservable(eventType: Class<T>): Observable<T> {
        return bus.ofType(eventType)
    }

    private object Holder {
        val INSTANCE = RxBus(PublishSubject.create<Any>().toSerialized())
    }

}