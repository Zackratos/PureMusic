package org.zack.music.main.play

import android.widget.SeekBar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_circle.*
import kotlinx.android.synthetic.main.fragment_horizontal.*
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import org.zack.music.R
import org.zack.music.bean.SongInfo
import org.zack.music.event.Status

/**
 * @Author  Zackratos
 * @Data    18-5-16
 * @Email   869649339@qq.com
 */
class HorizontalFragment: SeekBarFragment() {

    override fun layoutId(): Int {
        return R.layout.fragment_horizontal
    }

    override fun initEventAndData() {

        val parent = parentFragment as PlayFragment
        super.initEventAndData()
/*        lv_main.setOnClickListener {
            parent.switchSeekBar()
        }*/

        ll_root.setOnClickListener {
            parent.switchSeekBar()
        }

/*        sb_main.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tv_current.text = formatTime(seekBar?.progress ?: 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                receiveProgress = false

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                parent.setPlayProgress(seekBar?.progress ?: 0)
                receiveProgress = true
            }
        })*/

        dsb_main.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser) {
                    tv_current.text = formatTime(seekBar?.progress ?: 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
                receiveProgress = false
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                // 先允许接收时间，否则会接收不到 service 发出的进度改变事件
                receiveProgress = true
                parent.setPlayProgress(seekBar?.progress ?: 0)
            }
        })

    }

    override fun onStatus(status: Status) {
        val position = status.config.position
        val total = when(position) {
            -1 -> 100
            else -> {
                Observable.just(status.songs[position])
                        .map { it.getLyricFile() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            lv_main.setLyricFile(it)
                            lv_main.setCurrentTimeMillis(status.config.progress.toLong())
                        }, {lv_main.reset()})
                status.songs[position].duration.toInt()
            }
        }
        dsb_main.max = total
        tv_total.text = formatTime(total)
        dsb_main.progress = status.config.progress
        tv_current.text = formatTime(status.config.progress)
    }

    override fun onPlaySong(songInfo: SongInfo, newPlay: Boolean) {

        if (newPlay) {
            dsb_main.progress = 0
            tv_current.text = formatTime(0)
        }
        val total = songInfo.song.duration.toInt()
        dsb_main.max = total
        tv_total.text = formatTime(total)
//        lv_main.setLyricFile(File(songInfo.song.data.replace("mp3", "lrc")))

        Observable.just(songInfo)
                .map { it.song.getLyricFile() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    lv_main.setLyricFile(it)
                }, {lv_main.reset()})
    }

    override fun onProgressChange(progress: Int) {
        dsb_main.progress = progress
        tv_current.text = formatTime(progress)
        lv_main.setCurrentTimeMillis(progress.toLong())
    }

    override fun onSeekBarChange(color: Int) {
        dsb_main.setTrackColor(color)
    }

    private fun formatTime(progress: Int): String {
        val totalSecond = progress / 1000
        val min = totalSecond / 60
        val second = totalSecond % 60
        return "${formatNumber(min)}:${formatNumber(second)}"
    }

    private fun formatNumber(origin: Int): String {
        if (origin < 10) return "0$origin"
        return origin.toString()
    }

}