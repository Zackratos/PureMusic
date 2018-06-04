package org.zack.music.update

import android.support.v4.app.FragmentManager
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.zack.music.Constants
import org.zack.music.PureApp
import org.zack.music.http.ApiService
import org.zack.music.http.RxUtils
import org.zack.music.tools.AppUtils
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/31
 */
object UpdateUtils {

    private const val NEWEST = "已经是最新版"

    fun checkUpdate(fm: FragmentManager, showMsg: Boolean = false, showProgress: Boolean = false): Disposable {
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ApiService::class.java)
                .checkUpdate()
                .subscribeOn(Schedulers.io())
                .compose(RxUtils.resultData())
                .map { it.check }
                .flatMap {
                    when {
                        AppUtils.getAppVersionCode() < it.versionCode -> Observable.just(it)
                        else -> Observable.error(Throwable(NEWEST))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    if (showProgress) showProgressDialog(fm)
                }
                .doAfterNext {
                    if (showProgress) dismissProgressDialog(fm)
                }
                .subscribe({ showUpdateDialog(fm, it) }, {
                    if (showMsg) {
                        Toast.makeText(PureApp.app, it.message, Toast.LENGTH_SHORT).show()
                    }
                    if (showProgress) dismissProgressDialog(fm)
                })
    }


    private const val TAG_PROGRESS = "progress"
    private const val TAG_UPDATE = "update"

    private fun showUpdateDialog(fm: FragmentManager, check: Update.Check) {
        val dialog = UpdateDialog.newInstance(check)
        dialog.isCancelable = false
        dialog.show(fm, TAG_UPDATE)
    }


    private fun showProgressDialog(fm: FragmentManager) {
        val dialog = ProgressDialog.newInstance()
        dialog.isCancelable = true
        dialog.show(fm, TAG_PROGRESS)
    }

    private fun dismissProgressDialog(fm: FragmentManager) {
        val dialog = fm.findFragmentByTag(TAG_PROGRESS)
        if (dialog != null) {
            dialog as ProgressDialog
            dialog.dismiss()
        }
    }

}