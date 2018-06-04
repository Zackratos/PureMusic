package org.zack.music.config

/**
 * @Author  Zackratos
 * @Data    18-5-19
 * @Email   869649339@qq.com
 */
data class Config(
        var cycle: Int = ORDER,
        var random: Boolean = false,
        var position: Int = -1,
        var progress: Int = 0) {

    companion object {
        // 顺序播放
        const val ORDER = 1
        // 单曲循环
        const val SINGLE = 2
        // 全部循环
        const val ALL = 3
        // 透明背景
        const val TRANS = 1
        // 内置图片
        const val SONGS = 2
        // 妹子图片
        const val GIRL = 3

        const val CUSTOM = 4
    }

}