package com.lifedawn.bestweather.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.databinding.ViewgroupDrawableWithTextBinding

class DrawableTextViewGroup : ViewGroup {
    /*
	        <attr name="drawableSize" format="dimension" />
        <attr name="src" format="reference" />
        <attr name="drawableTint" format="color" />
        <attr name="text" format="string" />
        <attr name="textColor" format="color" />
        <attr name="textSize" format="dimension" />
	 */
    private var binding: ViewgroupDrawableWithTextBinding? = null
    private var drawableSize = 0f
    private var drawableSrc = 0
    private var drawableTint = 0
    private var text: String? = null
    private var textColor = 0

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(attrs)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
    private fun init(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.DrawableTextViewGroup, 0, 0)
        try {
            drawableSize = typedArray.getDimension(
                R.styleable.DrawableTextViewGroup_drawableSize,
                resources.getDimension(R.dimen.defaultImageSizeInDrawableWithText)
            )
            drawableSrc = typedArray.getResourceId(R.styleable.DrawableTextViewGroup_drawableSrc, R.drawable.temp_icon)
            drawableTint = typedArray.getColor(R.styleable.DrawableTextViewGroup_drawableColor, 0)
            text = typedArray.getString(R.styleable.DrawableTextViewGroup_text)
            textColor = typedArray.getColor(
                R.styleable.DrawableTextViewGroup_textColor, ContextCompat.getColor(
                    context,
                    R.color.defaultTextColorInDrawableWithText
                )
            )
        } finally {
            typedArray.recycle()
        }
        binding = ViewgroupDrawableWithTextBinding.inflate(LayoutInflater.from(context), this, true)
        val imgLayoutParams = binding!!.drawable.layoutParams as LinearLayout.LayoutParams
        imgLayoutParams.width = drawableSize.toInt()
        imgLayoutParams.height = drawableSize.toInt()
        binding!!.drawable.setImageResource(drawableSrc)
        if (drawableTint != 0) {
            binding!!.drawable.imageTintList = ColorStateList.valueOf(drawableTint)
        }
        binding!!.text.text = text
        binding!!.text.setTextColor(textColor)
    }
}