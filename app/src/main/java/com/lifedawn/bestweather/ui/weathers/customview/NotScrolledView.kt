package com.lifedawn.bestweather.ui.weathers.customview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.databinding.ViewNotScrolledBinding

class NotScrolledView : FrameLayout, ICleaner {
    private var binding: ViewNotScrolledBinding? = null

    constructor(context: Context?) : super(context!!) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        var iconVisibility = GONE
        var text = "ss"
        var iconSrc = R.drawable.temp_icon
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.NotScrolledView, 0, 0)
        try {
            iconVisibility = typedArray.getInt(R.styleable.NotScrolledView_iconVisibility, iconVisibility)
            text = typedArray.getString(R.styleable.NotScrolledView_android_text)
            iconSrc = typedArray.getResourceId(R.styleable.NotScrolledView_iconSrc, iconSrc)
        } catch (e: Exception) {
        } finally {
            typedArray.recycle()
        }
        binding = ViewNotScrolledBinding.inflate(LayoutInflater.from(context), this, true)
        binding!!.img.visibility = iconVisibility
        binding!!.img.setImageResource(iconSrc)
        binding!!.text.text = text
        binding!!.text.setTextColor(Color.GRAY)
    }

    fun setImg(id: Int) {
        binding!!.img.setImageResource(id)
        binding!!.img.visibility = VISIBLE
    }

    fun setText(text: String?) {
        binding!!.text.text = text
    }

    fun reDraw(newX: Int) {
        binding!!.root.x = newX.toFloat()
        invalidate()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    override fun clear() {
        binding!!.img.setImageDrawable(null)
        removeAllViewsInLayout()
        binding = null
    }
}