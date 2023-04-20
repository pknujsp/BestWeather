package com.lifedawn.bestweather.ui.weathers.detail.base.dialogfragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.databinding.DialogFragmentDetailForecastBinding

abstract class BaseDetailDialogFragment : DialogFragment() {
    protected var binding: DialogFragmentDetailForecastBinding? = null
    protected var compositePageTransformer: CompositePageTransformer? = null
    protected var firstSelectedPosition = 0
    protected var bundle: Bundle? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = savedInstanceState ?: arguments
        firstSelectedPosition = bundle!!.getInt("FirstSelectedPosition")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.DialogTransparent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogFragmentDetailForecastBinding.inflate(inflater, container, false)
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
        binding!!.detailForecastViewPager.offscreenPageLimit = 2
        binding!!.detailForecastViewPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
        compositePageTransformer = CompositePageTransformer()
        compositePageTransformer!!.addTransformer(MarginPageTransformer(margin))
        compositePageTransformer!!.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.8f + r * 0.2f
        }
        binding!!.detailForecastViewPager.setPageTransformer(compositePageTransformer)
        binding!!.detailForecastViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    protected open fun setTabCustomView() {
        val tabLayoutMediator = TabLayoutMediator(
            binding!!.tabLayout, binding!!.detailForecastViewPager
        ) { tab, position -> }
        tabLayoutMediator.attach()
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}