package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.weathers.FragmentType

class DoubleWindDirectionView(
    context: Context?,
    private val fragmentType: FragmentType,
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val columnWidth: Int
) : LinearLayout(context), ICleaner {
    private val imgSize: Int
    private val margin: Int
    private val dividerWidth: Int
    private var windDirectionObjList: MutableList<WindDirectionObj> = ArrayList()

    init {
        dividerWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics).toInt()
        var tempMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
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
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setIcons(windDirectionObjList: MutableList<WindDirectionObj>) {
        this.windDirectionObjList = windDirectionObjList
        val directionContainerViewGroupLayoutParams = LayoutParams(
            columnWidth, viewHeight
        )
        directionContainerViewGroupLayoutParams.gravity = Gravity.CENTER
        val directionViewLayoutParams = LayoutParams(imgSize, imgSize)
        directionViewLayoutParams.gravity = Gravity.CENTER
        val dividerLayoutParams = LayoutParams(dividerWidth, ViewGroup.LayoutParams.MATCH_PARENT)
        dividerLayoutParams.gravity = Gravity.CENTER
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics).toInt()
        dividerLayoutParams.leftMargin = margin
        dividerLayoutParams.rightMargin = margin
        val context = context
        val drawable = ContextCompat.getDrawable(context, R.drawable.arrow)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
        for (windDirectionObj in windDirectionObjList) {
            val leftDirectionView = ImageView(context)
            leftDirectionView.setImageDrawable(drawable)
            leftDirectionView.setPadding(padding, padding, padding, padding)
            leftDirectionView.rotation = (windDirectionObj.leftDirectionDegree + 180).toFloat()
            leftDirectionView.scaleType = ImageView.ScaleType.FIT_CENTER
            val rightDirectionView = ImageView(context)
            rightDirectionView.setImageDrawable(drawable)
            rightDirectionView.setPadding(padding, padding, padding, padding)
            rightDirectionView.rotation = (windDirectionObj.rightDirectionDegree + 180).toFloat()
            rightDirectionView.scaleType = ImageView.ScaleType.FIT_CENTER
            val dividerView = View(context)
            dividerView.setBackgroundColor(Color.GRAY)
            val container = LinearLayout(context)
            container.setPadding(padding, padding, padding, padding)
            container.addView(leftDirectionView, directionViewLayoutParams)
            container.addView(dividerView, dividerLayoutParams)
            container.addView(rightDirectionView, directionViewLayoutParams)
            addView(container, directionContainerViewGroupLayoutParams)
        }
    }

    override fun clear() {
        windDirectionObjList.clear()
    }

    class WindDirectionObj(val leftDirectionDegree: Int, val rightDirectionDegree: Int)
}