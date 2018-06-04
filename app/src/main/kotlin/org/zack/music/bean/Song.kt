package org.zack.music.bean

import android.content.ContentUris
import android.net.Uri
import java.io.File
import java.util.regex.Pattern

/**
 * @Author  Zackratos
 * @Data    18-5-13
 * @Email   869649339@qq.com
 */
data class Song(
        var id: Int,
        var title: String,
        var trackNumber: Int,
        var year: Int,
        var duration: Long,
        var data: String,
        var dateModified: Long,
        var albumId: Int,
        var albumName: String,
        var artistId: Int,
        var artistName: String) {

    constructor(): this(-1, "", -1, -1, -1, "", -1, -1, "", -1, "")


    fun getCoverUri(): Uri {
        val sArtworkUri = Uri
                .parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(sArtworkUri, albumId.toLong())
    }

    fun getLyricFile(): File? {
//        val path: String? = null

        val file = File(data)

        val dir = file.absoluteFile.parentFile

        if (dir != null && dir.exists() && dir.isDirectory) {
            val format = ".*%s.*\\.(lrc|txt)"
            val filename = Pattern.quote(stripExtension(file.name))

            val pattern = Pattern.compile(String.format(format, filename), Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
/*            val files = dir.listFiles( { f ->
                pattern.matcher(f.name).matches()
            })

            if (files != null && files.isNotEmpty()) {
                files.filterNotNull().forEach { return it }
            }*/

            dir.listFiles().forEach {
                if (pattern.matcher(it.name).matches()) return it
            }
        }
        return null
    }


    private fun stripExtension(str: String?): String? {
        if (str == null) return null
        val pos = str.lastIndexOf('.')
        return if (pos == -1) str else str.substring(0, pos)
    }

}