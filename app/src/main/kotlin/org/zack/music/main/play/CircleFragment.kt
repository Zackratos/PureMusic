package org.zack.music.main.play

import kotlinx.android.synthetic.main.fragment_circle.*
import org.zack.music.CircularSeekBar
import org.zack.music.R
import org.zack.music.bean.SongInfo
import org.zack.music.event.Status

/**
 * @Author  Zackratos
 * @Data    18-5-16
 * @Email   869649339@qq.com
 */
class CircleFragment: SeekBarFragment() {

    companion object {
        fun newInstance(): SeekBarFragment {
            return CircleFragment()
        }
    }

    override fun layoutId(): Int {
        return R.layout.fragment_circle
    }

    override fun initEventAndData() {
        super.initEventAndData()

        val parent = parentFragment as PlayFragment

        fl_root.setOnClickListener {
//            val parent = parentFragment as PlayFragment
            parent.switchSeekBar()
        }



        csb_main.setOnSeekBarChangeListener(object : CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(circularSeekBar: CircularSeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {
                receiveProgress = true
                parent.setPlayProgress(seekBar?.progress ?: 0)
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {
                receiveProgress = false
            }
        })
    }

    override fun onStatus(status: Status) {
        // 先设置最大进度，再设置当前进度，防止当前进度比最大进度大
        val position = status.config.position
        csb_main.max = when(position) {
            -1 -> 100
            else -> status.songs[position].duration.toInt()
        }
        csb_main.progress = status.config.progress
    }


    override fun onPlaySong(songInfo: SongInfo, newPlay: Boolean) {
        // 先将当前进度设为0,再设置最大进度，防止当前进度比最大进度大
        if (newPlay) csb_main.progress = 0
        csb_main.max = songInfo.song.duration.toInt()
    }

    override fun onProgressChange(progress: Int) {
        csb_main.progress = progress
    }

    override fun onSeekBarChange(color: Int) {
        csb_main.circleColor = color
    }



}