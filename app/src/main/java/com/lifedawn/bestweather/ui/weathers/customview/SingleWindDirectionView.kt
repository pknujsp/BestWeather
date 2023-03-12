package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.weathers.enums.WeatherDataType

class SingleWindDirectionView(
    context: Context?,
    private val weatherDataType: WeatherDataType,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val columnWidth: Int
) : LinearLayout(context), ICleaner {
    private val iconSize: Int
    private var windDirectionObjList: MutableList<Int>? = null

    init {
        val tempImgSize = viewHeight
        iconSize = tempImgSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setWindDirectionObjList(windDirectionObjList: MutableList<Int>) {
        this.windDirectionObjList = windDirectionObjList
        val layoutParams = LayoutParams(columnWidth, viewHeight)
        layoutParams.gravity = Gravity.CENTER
        val context = context
        val drawable = ContextCompat.getDrawable(context, R.drawable.arrow)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics).toInt()
        for (degree in windDirectionObjList) {
            val directionView = ImageView(context)
            directionView.setImageDrawable(drawable)
            directionView.setPadding(padding, padding, padding, padding)
            directionView.rotation = (degree + 180).toFloat()
            addView(directionView, layoutParams)
        }
    }

    override fun clear() {
        if (windDirectionObjList != null) windDirectionObjList!!.clear()
    }
}