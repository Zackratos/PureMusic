package org.zack.music.rxplayer

import android.media.MediaPlayer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import android.os.Looper
import org.zack.music.config.Config


/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/17
 */
class PlayerObservable(private val mp: MediaPlayer, private val data: String, private val cycle: Int = Config.ORDER): Observable<MediaPlayer>() {

    override fun subscribeActual(observer: Observer<in MediaPlayer>?) {
        if (observer == null) return
        if (!checkMainThread(observer)) return
        val listener = Listener(observer)
        observer.onSubscribe(listener)
        mp.reset()
        mp.isLooping = cycle == Config.SINGLE
        mp.setDataSource(data)
        mp.setOnPreparedListener(listener)
//        mp.setOnErrorListener(listener)
        mp.prepareAsync()
    }

    private fun checkMainThread(observer: Observer<in MediaPlayer>): Boolean {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            observer.onError(IllegalStateException(
                    "Expected to be called on the main thread but was " + Thread.currentThread().name))
            return false
        }
        return true
    }

    private class Listener(val observer: Observer<in MediaPlayer>):
            MainThreadDisposable(), MediaPlayer.OnPreparedListener {

        override fun onPrepared(mp: MediaPlayer?) {
            if (!isDisposed && mp != null) {
                observer.onNext(mp)
            }
        }

        override fun onDispose() {
        }

    }
}