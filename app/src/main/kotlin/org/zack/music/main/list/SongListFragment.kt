package org.zack.music.main.list

import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_song_list.*
import org.zack.music.BaseFragment
import org.zack.music.R
import org.zack.music.tools.RxBus
import org.zack.music.bean.SongInfo
import org.zack.music.event.PlaySong
import org.zack.music.event.Status
import org.zack.music.main.MainFragment


/**
 *
 * Created by zackratos on 18-5-11.
 */
class SongListFragment : BaseFragment() {

    companion object {
        fun newInstance(): SongListFragment {
            return SongListFragment()
        }
    }

    override fun layoutId(): Int {
        return R.layout.fragment_song_list
    }


    private var songListAdapter: SongListAdapter? = null

    override fun initEventAndData() {

        rv_song_list.layoutManager = LinearLayoutManager(activity)

        // 接收音乐加载完成事件
/*        val loadDisposable = RxBus.getInstance().toObservable(SongsLoaded::class.java)
                .subscribe {
                    songListAdapter = SongListAdapter(this, it.songs)
                    rv_song_list.adapter = songListAdapter
                    // 音乐加载完成接收成功后通知 service 发送状态事件
                    val parent = parentFragment as MainFragment
                    parent.sendStatus()
                }
        addDisposable(loadDisposable)

        val statusDisposable = RxBus.getInstance().toObservable(Status::class.java)
                .filter { it.config.position != -1 }
                .subscribe {
                    rv_song_list.scrollToPosition(it.config.position)
                    songListAdapter?.currentPosition = it.config.position
                    songListAdapter?.notifyItemChanged(it.config.position)
                }
        addDisposable(statusDisposable)*/

        val statusDisposable = RxBus.getInstance().toObservable(Status::class.java)
                .subscribe {
                    songListAdapter = SongListAdapter(this, it.songs)
                    rv_song_list.adapter = songListAdapter
                    val position = it.config.position
                    if (position != -1) {
                        rv_song_list.scrollToPosition(position)
                        songListAdapter?.currentPosition = position
                        songListAdapter?.notifyItemChanged(position)
                    }
                }
        addDisposable(statusDisposable)

        val playDisposable = RxBus.getInstance().toObservable(PlaySong::class.java)
                .subscribe {
                    rv_song_list.scrollToPosition(it.songInfo.position)
                    songListAdapter?.currentPosition = it.songInfo.position
                    songListAdapter?.lastPosition = it.lastPosition
                    songListAdapter?.notifyItemChanged(it.songInfo.position)
                    songListAdapter?.notifyItemChanged(it.lastPosition)
                }
        addDisposable(playDisposable)
    }


    fun startPlaySong(songInfo: SongInfo) {
        val parent = parentFragment as MainFragment
        parent.startPlaySong(songInfo, true)
    }

}