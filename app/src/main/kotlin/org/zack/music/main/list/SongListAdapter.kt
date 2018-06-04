package org.zack.music.main.list

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.zack.music.R
import org.zack.music.bean.Song
import org.zack.music.bean.SongInfo

/**
 *
 * Created by zackratos on 18-5-11.
 */
class SongListAdapter(
        val fragment: SongListFragment,
        private val songs: MutableList<Song>): RecyclerView.Adapter<SongListAdapter.ViewHolder>() {

    // 当前播放的音乐位置
//    private var currentPosition = -1
    var currentPosition = -1
    var lastPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(LayoutInflater.from(fragment.activity).inflate(R.layout.item_song_list, parent, false))
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val song = songs[position]
//            currentPosition = position
            fragment.startPlaySong(SongInfo(song, position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val song = songs[position]
        holder?.tvTitle?.text = song.title
        holder?.tvArtist?.text = String.format(fragment.getString(R.string.music_list_item_artist),
                song.artistName, song.albumName)

        holder?.ivPlaying?.visibility = when (currentPosition) {
            position -> View.VISIBLE
            else -> View.INVISIBLE
        }

        Observable.create<Uri> {
            it.onNext(song.getCoverUri())
            it.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Glide.with(fragment).loadFromMediaStore(it)
                            .placeholder(R.drawable.ic_album)
                            .error(R.drawable.ic_album)
                            .into(holder?.ivCover)
                }, {})
    }

    override fun getItemCount(): Int {
        return songs.size
    }


    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView?.findViewById<TextView>(R.id.tv_title)
        val ivCover = itemView?.findViewById<SquareImageView>(R.id.iv_cover)
        val tvArtist = itemView?.findViewById<TextView>(R.id.tv_artist)
        val ivPlaying = itemView?.findViewById<ImageView>(R.id.iv_playing)
    }
}