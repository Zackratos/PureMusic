package org.zack.music.http

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/29
 */
data class Girl(val bg: MutableList<Bg>) {

    data class Bg(val url: String)
}