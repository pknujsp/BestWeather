package com.lifedawn.bestweather.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View.OnKeyListener
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.utils.DeviceUtils.Companion.showKeyboard
import com.lifedawn.bestweather.commons.views.CustomEditText.OnEditTextQueryListener
import com.lifedawn.bestweather.databinding.ViewSearchBinding

class CustomSearchView : FrameLayout {
    private var binding: ViewSearchBinding? = null
    private var onEditTextQueryListener: OnEditTextQueryListener? = null
    var backBtnVisibility = 0
    var searchBtnVisibility = 0
    var enabled = false
    var showStroke = true
    var hint: String? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = getContext().theme.obtainStyledAttributes(attrs, R.styleable.CustomSearchView, 0, 0)
        try {
            backBtnVisibility = a.getInt(R.styleable.CustomSearchView_backBtnVisibility, VISIBLE)
            searchBtnVisibility = a.getInt(R.styleable.CustomSearchView_searchBtnVisibility, VISIBLE)
            hint = a.getString(R.styleable.CustomSearchView_hint)
            enabled = a.getBoolean(R.styleable.CustomSearchView_enabled, true)
            showStroke = a.getBoolean(R.styleable.CustomSearchView_showStroke, true)
        } finally {
            a.recycle()
        }
        binding = ViewSearchBinding.inflate(LayoutInflater.from(context), this, true)
        if (!showStroke) {
            binding!!.root.background = ContextCompat.getDrawable(context, R.drawable.searchview_background_no_stroke)
        }
        binding!!.back.visibility = backBtnVisibility
        binding!!.search.visibility = searchBtnVisibility
        binding!!.edittext.hint = hint
        binding!!.edittext.isEnabled = enabled
        if (!enabled) {
            binding!!.search.visibility = GONE
        }
        binding!!.search.setOnClickListener {
            if (onEditTextQueryListener != null) {
                onEditTextQueryListener!!.onTextSubmit(if (binding!!.edittext.text!!.length > 0) binding!!.edittext.text.toString() else "")
            }
        }
        binding!!.edittext.setOnKeyListener(OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                binding!!.search.callOnClick()
                return@OnKeyListener true
            }
            false
        })
    }

    fun requestFocusEditText() {
        binding!!.edittext.requestFocus()
        showKeyboard(context, binding!!.edittext)
    }

    fun clearFocusEditText() {
        binding!!.edittext.text = null
        binding!!.edittext.clearFocus()
    }

    fun setBackgroundTint(color: Int) {
        binding!!.root.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding!!.root.setOnClickListener(l)
    }

    fun setEditTextOnClickListener(l: OnClickListener?) {
        binding!!.edittext.setOnClickListener(l)
    }

    fun setEditTextOnFocusListener(onFocusListener: OnFocusChangeListener?) {
        binding!!.edittext.onFocusChangeListener = onFocusListener
    }

    fun callOnClickEditText() {
        binding!!.edittext.requestFocusFromTouch()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun setOnEditTextQueryListener(onEditTextQueryListener: OnEditTextQueryListener?) {
        this.onEditTextQueryListener = onEditTextQueryListener
        binding!!.edittext.setOnEditTextQueryListener(onEditTextQueryListener)
    }

    fun setOnBackClickListener(onBackClickListener: OnClickListener?) {
        binding!!.back.setOnClickListener(onBackClickListener)
    }

    fun setQuery(query: String?, submit: Boolean) {
        binding!!.edittext.setText(query)
        if (submit) {
            binding!!.search.callOnClick()
        }
    }

    val query: String
        get() = if (binding!!.edittext.text!!.length > 0) binding!!.edittext.text.toString() else ""
}