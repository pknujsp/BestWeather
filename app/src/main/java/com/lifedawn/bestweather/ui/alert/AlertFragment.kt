package com.lifedawn.bestweather.ui.alert

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.views.HeaderbarStyle
import com.lifedawn.bestweather.commons.views.HeaderbarStyle.setStyle
import com.lifedawn.bestweather.databinding.FragmentAlertBinding

class AlertFragment : Fragment() {
    private var binding: FragmentAlertBinding? = null
    private var btnObjList: List<BtnObj>? = null
    private var bundle: Bundle? = null
    private var menuOnClickListener: View.OnClickListener? = null
    fun setMenuOnClickListener(menuOnClickListener: View.OnClickListener?): AlertFragment {
        this.menuOnClickListener = menuOnClickListener
        return this
    }

    enum class Constant {
        DRAWABLE_ID, MESSAGE
    }

    fun setBtnObjList(btnObjList: List<BtnObj>?) {
        this.btnObjList = btnObjList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        setStyle(HeaderbarStyle.Style.Black, activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        binding!!.mainToolbar.gps.visibility = View.GONE
        binding!!.mainToolbar.find.visibility = View.GONE
        binding!!.mainToolbar.refresh.visibility = View.GONE
        val statusBarHeight = MyApplication.getStatusBarHeight()
        val layoutParams = binding!!.mainToolbar.root.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = statusBarHeight
        binding!!.mainToolbar.root.layoutParams = layoutParams
        binding!!.mainToolbar.openNavigationDrawer.setOnClickListener(menuOnClickListener)
        return binding!!.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val drawableId = bundle!!.getInt(Constant.DRAWABLE_ID.name)
        val message = bundle!!.getString(Constant.MESSAGE.name)
        binding!!.alertImageView.setImageResource(drawableId)
        binding!!.textView.text = message
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val context = requireContext().applicationContext
        for (btnObj in btnObjList!!) {
            val button = Button(context)
            button.background = ContextCompat.getDrawable(context, R.drawable.rounded_btn_background)
            button.setTextColor(Color.WHITE)
            button.layoutParams = layoutParams
            button.text = btnObj.text
            button.setOnClickListener(btnObj.onClickListener)
            binding!!.btnList.addView(button)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    class BtnObj(val onClickListener: View.OnClickListener, val text: String)
}