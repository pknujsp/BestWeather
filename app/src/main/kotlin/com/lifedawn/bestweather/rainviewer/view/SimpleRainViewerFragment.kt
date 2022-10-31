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

        binding.weatherCardViewHeader.forecastName.setText(R.string.radar)
        binding.weatherCardViewHeader.compareForecast.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rainViewerFragment = RainViewerFragment()
        rainViewerFragment.arguments = bundle

        childFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, rainViewerFragment).commit()

        binding.weatherCardViewHeader.detailForecast.setOnClickListener {
            val detailRainViewerFragment = RainViewerFragment()
            detailRainViewerFragment.arguments = Bundle().apply {
                putAll(bundle)
                putBoolean("simpleMode", false)
            }

            val fragmentManager = requireParentFragment().parentFragmentManager
            fragmentManager.beginTransaction().hide(fragmentManager.primaryNavigationFragment!!)
                    .add(R.id.fragment_container, detailRainViewerFragment, RainViewerFragment::class.simpleName).addToBackStack(
                            RainViewerFragment::class.simpleName).commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }
}