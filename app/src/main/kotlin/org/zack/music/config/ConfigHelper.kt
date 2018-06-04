package org.zack.music.config

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.zack.music.PureApp

/**
 * @Author  Zackratos
 * @Data    18-5-19
 * @Email   869649339@qq.com
 */
class ConfigHelper private constructor() {

    private val sp: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(PureApp.app)
    }

    companion object {
        fun getInstance(): ConfigHelper {
            return Holder.INSTANCE
        }

        private const val CYCLE = "cycle"
        private const val RANDOM = "random"
        private const val POSITION = "position"
        private const val PROGRESS = "progress"

        private const val BG_TYPE = "bg_type"
        private const val SEEK_TRAN = "seek_tran"

        private const val CUSTOM_PATH = "custom_path"
    }

    fun getCycle(): Int {
        return sp.getInt(CYCLE, Config.ORDER)
    }

    fun putCycle(cycle: Int) {
        sp.edit().putInt(CYCLE, cycle).apply()
    }

    fun putPosition(position: Int) {
        sp.edit().putInt(POSITION, position).apply()
    }

    fun getPosition(): Int {
        return sp.getInt(POSITION, -1)
    }

    fun putProgress(progress: Int) {
        sp.edit().putInt(PROGRESS, progress).apply()
    }

    fun getProgress(): Int {
        return sp.getInt(PROGRESS, 0)
    }

    fun putRandom(random: Boolean) {
        sp.edit().putBoolean(RANDOM, random).apply()
    }

    fun getRandom(): Boolean {
        return sp.getBoolean(RANDOM, false)
    }


    fun getConfig(): Config {
        return Config().apply {
            cycle = sp.getInt(CYCLE, Config.ORDER)
            random = sp.getBoolean(RANDOM, false)
            position = sp.getInt(POSITION, -1)
            progress = sp.getInt(PROGRESS, 0)
        }
    }

    fun putConfig(config: Config) {
        sp.edit().putInt(CYCLE, config.cycle)
                .putBoolean(RANDOM, config.random)
                .putInt(POSITION, config.position)
                .putInt(PROGRESS, config.progress)
                .apply()
    }

    fun getBgType(): Int {
        return sp.getInt(BG_TYPE, Config.TRANS)
    }

    fun putBgType(type: Int) {
        sp.edit().putInt(BG_TYPE, type).apply()
    }

    fun isSeekTran(): Boolean {
        return sp.getBoolean(SEEK_TRAN, false)
    }

    fun putSeekTran(seekTran: Boolean) {
        sp.edit().putBoolean(SEEK_TRAN, seekTran).apply()
    }

    fun getCustomBgPath(): String? {
        return sp.getString(CUSTOM_PATH, null)
    }

    fun putCustomBgPath(path: String?) {
        sp.edit().putString(CUSTOM_PATH, path).apply()
    }




    private object Holder {
        val INSTANCE = ConfigHelper()
    }
}