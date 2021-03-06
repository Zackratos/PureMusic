package org.zack.music.http

import io.reactivex.Observable
import org.zack.music.update.Update
import retrofit2.http.GET

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/29
 */
interface ApiService {

    @GET("PureMusic/v2.x/internet/background/config.json")
    fun getBgConfig(): Observable<Response<Girl>>

    @GET("PureMusic/v2.x/internet/update/update.json")
    fun checkUpdate(): Observable<Response<Update>>
}