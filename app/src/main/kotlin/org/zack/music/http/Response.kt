package org.zack.music.http

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/29
 */
data class Response<out T>(
        val data: T,
        val status: String,
        val code: Int)