package com.lifedawn.bestweather.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.lifedawn.bestweather.commons.interfaces.CheckSuccess
import com.lifedawn.bestweather.commons.interfaces.OnProgressViewListener
import com.lifedawn.bestweather.databinding.ViewProgressResultBinding
import java.util.*

class ProgressResultView : FrameLayout, OnProgressViewListener, CheckSuccess {
    private var binding: ViewProgressResultBinding? = null
    private val views: MutableList<View> = ArrayList()
    override var isSuccess = false
        private set
    private var btnEnabled = false

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    private fun init() {
        binding = ViewProgressResultBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setContentView(vararg contentView: View?) {
        views.addAll(Arrays.asList(*contentView))
        onSuccessful()
    }

    fun setBtnOnClickListener(onClickListener: OnClickListener?) {
        binding!!.btn.setOnClickListener(onClickListener)
        binding!!.btn.visibility = VISIBLE
        btnEnabled = true
    }

    override fun onSuccessful() {
        isSuccess = true
        for (v in views) v.visibility = VISIBLE
        visibility = GONE
    }

    override fun onFailed(text: String) {
        isSuccess = false
        for (v in views) v.visibility = VISIBLE
        binding!!.status.text = text
        binding!!.status.visibility = VISIBLE
        binding!!.progressbar.visibility = GONE
        binding!!.btn.visibility = if (btnEnabled) VISIBLE else GONE
        visibility = VISIBLE
    }

    override fun onStarted() {
        isSuccess = false
        for (v in views) v.visibility = GONE
        binding!!.btn.visibility = GONE
        binding!!.status.visibility = GONE
        binding!!.progressbar.visibility = VISIBLE
        visibility = VISIBLE
    }

    fun setTextColor(color: Int) {
        binding!!.status.setTextColor(color)
    }
}