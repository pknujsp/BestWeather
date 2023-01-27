package com.lifedawn.bestweather.ui.weathers.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.lifedawn.bestweather.ui.weathers.FragmentType

class DoubleWeatherIconView(
    context: Context?,
    private val fragmentType: FragmentType,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val columnWidth: Int
) : View(context), ICleaner {
    private val imgSize: Int
    private val singleImgSize: Int
    private val margin: Int
    private val dividerWidth: Int
    private val leftImgRect = Rect()
    private val rightImgRect = Rect()
    private val singleImgRect = Rect()
    private val dividerRect = Rect()
    private val dividerPaint: Paint
    private var weatherIconObjList: MutableList<WeatherIconObj> = ArrayList()

    init {
        dividerWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics).toInt()
        var tempMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
        singleImgSize = viewHeight - tempMargin * 2
        var tempImgSize = columnWidth / 2 - tempMargin * 2
        if (tempImgSize > viewHeight) {
            tempImgSize = tempImgSize - (tempImgSize - viewHeight)
            tempMargin = if (columnWidth / 2 - tempImgSize > 0) {
                (columnWidth / 2 - tempImgSize) / 2
            } else {
                0
            }
        }
        imgSize = tempImgSize
        margin = tempMargin
        dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (fragmentType) {
            FragmentType.Simple -> dividerPaint.color = Color.WHITE
            else -> dividerPaint.color = Color.BLACK
        }
        isClickable = true
        isFocusable = true
        setOnTouchListener(object : OnTouchListener {
            var actionDownMillis: Long = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    actionDownMillis = System.currentTimeMillis()
                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - actionDownMillis < 500 && weatherIconObjList.size > 0) {
                        val index = (event.x / columnWidth).toInt()
                        val isSingle = !weatherIconObjList[index].isDouble
                        var text: String? = null
                        text = if (isSingle) {
                            weatherIconObjList[index].singleDescription
                        } else {
                            weatherIconObjList[index].leftDescription + " / " + weatherIconObjList[index].rightDescription
                        }
                        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show()
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
        singleImgRect[columnWidth / 2 - singleImgSize / 2, margin, columnWidth / 2 + singleImgSize / 2] = height - margin
        leftImgRect[margin, height / 2 - imgSize / 2, margin + imgSize] = height / 2 + imgSize / 2
        rightImgRect[leftImgRect.right + margin * 2, leftImgRect.top, leftImgRect.right + margin * 2 + imgSize] = leftImgRect.bottom
        dividerRect[leftImgRect.right + margin - dividerWidth / 2, leftImgRect.top, leftImgRect.right + margin + dividerWidth / 2] =
            leftImgRect.bottom
        for (weatherIconObj in weatherIconObjList) {
            if (weatherIconObj.isDouble) {
                weatherIconObj.leftImg!!.bounds = leftImgRect
                weatherIconObj.rightImg!!.bounds = rightImgRect
                weatherIconObj.leftImg!!.draw(canvas)
                weatherIconObj.rightImg!!.draw(canvas)
                canvas.drawRect(dividerRect, dividerPaint)
            } else {
                weatherIconObj.singleImg!!.bounds = singleImgRect
                weatherIconObj.singleImg!!.draw(canvas)
            }
            singleImgRect.offset(columnWidth, 0)
            leftImgRect.offset(columnWidth, 0)
            rightImgRect.offset(columnWidth, 0)
            dividerRect.offset(columnWidth, 0)
        }
    }

    fun setIcons(weatherIconObjList: MutableList<WeatherIconObj>) {
        this.weatherIconObjList = weatherIconObjList
    }

    override fun clear() {
        weatherIconObjList.clear()
    }

    class WeatherIconObj {
        val isDouble: Boolean
        var leftImg: Drawable? = null
        var rightImg: Drawable? = null
        var singleImg: Drawable? = null
        var leftDescription: String? = null
        var rightDescription: String? = null
        var singleDescription: String? = null

        constructor(leftDrawable: Drawable?, rightDrawable: Drawable?, leftDescription: String?, rightDescription: String?) {
            leftImg = leftDrawable
            rightImg = rightDrawable
            this.leftDescription = leftDescription
            this.rightDescription = rightDescription
            isDouble = true
        }

        constructor(drawable: Drawable?, singleDescription: String?) {
            singleImg = drawable
            this.singleDescription = singleDescription
            isDouble = false
        }
    }
}