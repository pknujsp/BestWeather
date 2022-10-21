package com.lifedawn.bestweather.rainviewer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.databinding.FragmentSimpleRainViewerBinding
import com.lifedawn.bestweather.weathers.WeatherFragment


class SimpleRainViewerFragment : Fragment() {
    private lateinit var binding: FragmentSimpleRainViewerBinding
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = (arguments ?: savedInstanceState) as Bundle
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSimpleRainViewerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.weatherCardViewHeader.forecastName.setText(R.string.radar)
        binding.weatherCardViewHeader.compareForecast.visibility = View.GONE

        val rainViewerFragment = RainViewerFragment()
        rainViewerFragment.arguments = bundle

        childFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, rainViewerFragment).commitAllowingStateLoss()

        binding.weatherCardViewHeader.detailForecast.setOnClickListener {
            val detailRainViewerFragment = RainViewerFragment()
            val argument = Bundle()
            argument.putAll(bundle)
            argument.putBoolean("simpleMode", false)
            detailRainViewerFragment.arguments = argument


            val fragmentManager = requireParentFragment().parentFragmentManager
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(WeatherFragment::class.java.name)!!)
                    .add(R.id.fragment_container, detailRainViewerFragment, RainViewerFragment::class.simpleName).addToBackStack(
                            RainViewerFragment::class.simpleName
                    ).commitAllowingStateLoss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }
}