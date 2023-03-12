package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.lifedawn.bestweather.ui.weathers.enums.WeatherDataType

class SingleWeatherIconView(
    context: Context?,
    private val weatherDataType: WeatherDataType,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val columnWidth: Int
) : View(context), ICleaner {
    private val imgSize: Int
    private var weatherIconObjList: MutableList<WeatherIconObj> = ArrayList()
    private val imgRect = Rect()

    init {
        val tempImgSize = viewHeight
        imgSize = tempImgSize
        isClickable = true
        isFocusable = true
        setOnTouchListener(object : OnTouchListener {
            var actionDownMillis: Long = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    actionDownMillis = System.currentTimeMillis()
                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - actionDownMillis < 500 && weatherIconObjList.size > 0) {
                        Toast.makeText(getContext(), weatherIconObjList[(event.x / columnWidth).toInt()].description, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                return true
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        imgRect[(columnWidth - imgSize) / 2, 0, (columnWidth - imgSize) / 2 + imgSize] = height
        for (weatherIconObj in weatherIconObjList) {
            weatherIconObj.img!!.bounds = imgRect
            weatherIconObj.img!!.draw(canvas)
            imgRect.offset(columnWidth, 0)
        }
    }

    fun setWeatherImgs(weatherIconObjList: MutableList<WeatherIconObj>) {
        this.weatherIconObjList = weatherIconObjList
    }

    override fun clear() {
        weatherIconObjList.clear()
    }

    class WeatherIconObj {
        var img: Drawable?
        var description: String? = null

        constructor(drawable: Drawable?) {
            img = drawable
        }

        constructor(img: Drawable?, description: String?) {
            this.img = img
            this.description = description
        }
    }
}