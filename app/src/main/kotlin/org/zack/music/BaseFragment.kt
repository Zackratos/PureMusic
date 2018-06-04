package org.zack.music

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


/**
 *
 * Created by zackratos on 18-5-11.
 */
abstract class BaseFragment: Fragment() {

    protected var rootView: View? = null

    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutId = layoutId()
        return when (layoutId) {
            0 -> super.onCreateView(inflater, container, savedInstanceState)
            else -> inflater?.inflate(layoutId, container, false)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initEventAndData()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        val refWatcher = PureApp.getRefWatcher(activity)
        refWatcher.watch(this)
    }


    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    protected fun removeDisposable(disposable: Disposable?) {
        if (disposable != null)
            compositeDisposable.remove(disposable)
    }


    @LayoutRes
    protected abstract fun layoutId(): Int

    protected abstract fun initEventAndData()

}