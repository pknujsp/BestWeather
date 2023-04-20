package com.lifedawn.bestweather.commons.views

import android.R
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.util.*

class CustomEditText : AppCompatEditText, TextWatcher, View.OnTouchListener {
    private var closeDrawable: Drawable? = null
    private var onEditTextQueryListener: OnEditTextQueryListener? = null
    private var timer = Timer()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, R.attr.editTextStyle) {
        init()
    }

    private fun init() {
        inputType = InputType.TYPE_CLASS_TEXT
        closeDrawable = ContextCompat.getDrawable(context, com.lifedawn.bestweather.R.drawable.ic_baseline_close_24)
        DrawableCompat.setTintList(closeDrawable!!, hintTextColors)
        val btnSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics).toInt()
        closeDrawable!!.setBounds(0, 0, btnSize, btnSize)
        setClearBtnVisibility(false)
        addTextChangedListener(this)
        setOnTouchListener(this)
    }

    override fun setOnKeyListener(l: OnKeyListener) {
        super.setOnKeyListener(l)
    }

    private fun setClearBtnVisibility(visible: Boolean) {
        closeDrawable!!.setVisible(visible, false)
        setCompoundDrawables(null, null, if (visible) closeDrawable else null, null)
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        if (isFocused) {
            setClearBtnVisibility(charSequence.length > 0)
        }
    }

    private val DELAY = 500
    override fun afterTextChanged(editable: Editable) {
        timer.cancel()
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (onEditTextQueryListener != null) {
                    Log.e("afterTextChanged", editable.toString())
                    onEditTextQueryListener!!.onTextChange(editable.toString())
                }
            }
        }, DELAY.toLong())
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val x = motionEvent.x.toInt()
        if (closeDrawable!!.isVisible && x >= width - paddingRight - closeDrawable!!.intrinsicWidth) {
            setText("")
            error = null
        }
        return false
    }

    fun setOnEditTextQueryListener(onEditTextQueryListener: OnEditTextQueryListener?): CustomEditText {
        this.onEditTextQueryListener = onEditTextQueryListener
        return this
    }

    interface OnEditTextQueryListener {
        fun onTextChange(newText: String?)
        fun onTextSubmit(text: String?)
    }
}