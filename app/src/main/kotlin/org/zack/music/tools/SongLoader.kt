package org.zack.music.tools

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.*
import org.zack.music.bean.Song

/**
 * @Author  Zackratos
 * @Data    18-5-13
 * @Email   869649339@qq.com
 */
object SongLoader {

    private const val BASE_SELECTION = "$IS_MUSIC=1 AND $TITLE != '' AND $DURATION >= 10000"

    fun getAllSongs(context: Context): ArrayList<Song> {
        return getSongs(makeSongCursor(context, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER))
    }

    private fun getSongs(cursor: Cursor): ArrayList<Song> {
        val songs = ArrayList<Song>()
        if (cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return songs
    }

    private fun getSongFromCursor(cursor: Cursor): Song {
        return Song().apply {
            id = cursor.getInt(0)
            title = cursor.getString(1)
            trackNumber = cursor.getInt(2)
            year = cursor.getInt(3)
            duration = cursor.getLong(4)
            data = cursor.getString(5)
            dateModified = cursor.getLong(6)
            albumId = cursor.getInt(7)
            albumName = cursor.getString(8)
            artistId = cursor.getInt(9)
            artistName = cursor.getString(10)
        }
    }

    private fun makeSongCursor(context: Context, selection: String?, selectionValues: Array<String>?, sortOrder: String): Cursor {

//        var selectionValues = selectionValues
        val mySelection = when {
            selection != null && selection.trim { it <= ' ' } != "" -> """$BASE_SELECTION AND $selection"""
            else -> BASE_SELECTION
        }

//        try {
            return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(_ID, // 0
                            TITLE, // 1
                            TRACK, // 2
                            YEAR, // 3
                            DURATION, // 4
                            DATA, // 5
                            DATE_MODIFIED, // 6
                            ALBUM_ID, // 7
                            ALBUM, // 8
                            ARTIST_ID, // 9
                            ARTIST)// 10
                    , mySelection, selectionValues, sortOrder)
//        } catch (e: SecurityException) {
//            return null
//        }

    }
}