package com.lifedawn.bestweather.rainviewer.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.databinding.FragmentSimpleRainViewerBinding
import com.lifedawn.bestweather.weathers.WeatherFragment
import com.lifedawn.bestweather.weathers.viewmodels.WeatherFragmentViewModel
import java.lang.ref.WeakReference


class SimpleRainViewerFragment : Fragment() {
    private var _binding: FragmentSimpleRainViewerBinding? = null
    private val binding get() = _binding!!
    private lateinit var bundle: Bundle
    private val weatherFragmentViewModel: WeatherFragmentViewModel by viewModels({ requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = (arguments ?: savedInstanceState) as Bundle
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentSimpleRainViewerBinding.inflate(inflater, container, false)

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

    override fun onResume() {
        super.onResume()
        weatherFragmentViewModel.onResumeWithAsync(this@SimpleRainViewerFragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}