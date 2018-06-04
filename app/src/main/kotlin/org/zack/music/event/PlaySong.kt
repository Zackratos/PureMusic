package org.zack.music.event

import org.zack.music.bean.SongInfo
import org.zack.music.config.Config

/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/17
 */
data class PlaySong(
        val songInfo: SongInfo,
        val lastPosition: Int = 0,
        val newPlay: Boolean = true)