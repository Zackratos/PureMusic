package org.zack.music.event

import org.zack.music.bean.Song
import org.zack.music.config.Config

/**
 * @Author  Zackratos
 * @Data    18-5-27
 * @Email   869649339@qq.com
 */
data class Status(val songs: MutableList<Song>, val config: Config, val playing: Boolean)