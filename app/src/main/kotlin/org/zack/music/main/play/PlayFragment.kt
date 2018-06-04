package org.zack.music.main.play

import android.support.v4.app.Fragment
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.fragment_play.*
import org.zack.music.BaseFragment
import org.zack.music.R
import org.zack.music.config.Config
import org.zack.music.event.*
import org.zack.music.tools.RxBus
import org.zack.music.main.MainFragment

/**
 * @Author  Zackratos
 * @Data    18-5-13
 * @Email   869649339@qq.com
 */
class PlayFragment: BaseFragment() {


    companion object {

        fun newInstance(): PlayFragment {
            return PlayFragment()
        }
    }


    private val circleFragment: SeekBarFragment by lazy {
        CircleFragment()
    }

    private val horizontalFragment: SeekBarFragment by lazy {
        HorizontalFragment()
    }


    private var lastSeekBar: Fragment? = null

    fun switchSeekBar() {
        val fragment = when (lastSeekBar) {
            circleFragment -> horizontalFragment
            else -> circleFragment
        }
        childFragmentManager.beginTransaction()
                .hide(lastSeekBar)
                .show(fragment)
                .runOnCommit { lastSeekBar = fragment }
                .commit()
/*        childFragmentManager.beginTransaction()
                .hide(fragment)
                .tran(lastSeekBar)
                .runOnCommit { lastSeekBar = fragment }
                .commit()*/
    }


    override fun layoutId(): Int {
        return R.layout.fragment_play
    }

    override fun initEventAndData() {
        childFragmentManager.beginTransaction()
                .add(R.id.fl_seek_bar_container, circleFragment)
                .add(R.id.fl_seek_bar_container, horizontalFragment)
                .hide(horizontalFragment)
                .runOnCommit { lastSeekBar = circleFragment}
                .commit()

        val parent = parentFragment as MainFragment

        iv_play.setOnClickListener {

            parent.playOrPause()
        }

        iv_next.setOnClickListener {
            parent.cutSong(true)
        }

        iv_previous.setOnClickListener {
            parent.cutSong(false)
        }

        iv_cycle.setOnClickListener {
            parent.cycle()
        }

        iv_random.setOnClickListener {
            parent.random()
        }

/*        val statusDisposable = RxBus.getInstance().toObservable(Status::class.java)
                .subscribe {
                    iv_play.setImageResource(when {
                        it.playing -> R.drawable.ic_pause
                        else -> R.drawable.ic_play_arrow
                    })

                    iv_cycle.setImageResource(
                            when(it.config.cycle) {
                                Config.ORDER -> R.drawable.ic_replay_no
                                Config.ALL -> R.drawable.ic_replay_all
                                Config.SINGLE -> R.drawable.ic_replay_single
                                else -> R.drawable.ic_replay_no
                            }
                    )

                    iv_random.setImageResource(
                            when {
                                it.config.random -> R.drawable.ic_shuffle_on
                                else -> R.drawable.ic_shuffle_off
                            }
                    )
                }
        addDisposable(statusDisposable)*/

        val statusDisposable = RxBus.getInstance().toObservable(Status::class.java)
                .subscribe {
                    iv_play.setImageResource(when {
                        it.playing -> R.drawable.ic_pause
                        else -> R.drawable.ic_play_arrow
                    })

                    iv_cycle.setImageResource(when (it.config.cycle) {
                        Config.ORDER -> R.drawable.ic_replay_no
                        Config.ALL -> R.drawable.ic_replay_all
                        Config.SINGLE -> R.drawable.ic_replay_single
                        else -> R.drawable.ic_replay_no
                    })

                    iv_random.setImageResource(when {
                        it.config.random -> R.drawable.ic_shuffle_on
                        else -> R.drawable.ic_shuffle_off
                    })
                }
        addDisposable(statusDisposable)

        val pauseDisposable = RxBus.getInstance().toObservable(PlayOrPause::class.java)
                .subscribe {
                    onPlayOrPause(it.play)
                }
        addDisposable(pauseDisposable)

        val playDisposable = RxBus.getInstance().toObservable(PlaySong::class.java)
                .subscribe {
                    iv_play.setImageResource(R.drawable.ic_pause)
                }

        addDisposable(playDisposable)

        val cycleDisposable = RxBus.getInstance().toObservable(Cycle::class.java)
                .subscribe {
                    iv_cycle.setImageResource(
                            when(it.cycle) {
                                Config.ORDER -> R.drawable.ic_replay_no
                                Config.ALL -> R.drawable.ic_replay_all
                                Config.SINGLE -> R.drawable.ic_replay_single
                                else -> R.drawable.ic_replay_no
                            }
                    )
                }
        addDisposable(cycleDisposable)

        val randomDisposable = RxBus.getInstance().toObservable(IsRandom::class.java)
                .subscribe {
                    iv_random.setImageResource(
                            when {
                                it.random -> R.drawable.ic_shuffle_on
                                else -> R.drawable.ic_shuffle_off
                            }
                    )
                }
        addDisposable(randomDisposable)

    }


    private fun onPlayOrPause(play: Boolean) {
        iv_play.setImageResource(
                when {
                    play -> R.drawable.ic_pause
                    else -> R.drawable.ic_play_arrow
                }
        )
    }


    fun setPlayProgress(progress: Int) {
        val parent = parentFragment as MainFragment
        parent.setPlayProgress(progress)
    }

}