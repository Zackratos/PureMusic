package org.zack.music.main.list

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 *  正方形的 ImageView
 * Created by zackratos on 18-5-11.
 */
class SquareImageView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val length = Math.min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(length, length)
    }
}