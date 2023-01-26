package com.lifedawn.bestweather.ui.notification.sunsetrise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.databinding.FragmentSunSetRiseNotificationBinding

class SunSetRiseNotificationFragment : Fragment() {
    private var binding: FragmentSunSetRiseNotificationBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSunSetRiseNotificationBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutParams = binding!!.toolbar.root.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding!!.toolbar.root.layoutParams = layoutParams
        binding!!.toolbar.backBtn.setOnClickListener { parentFragmentManager.popBackStackImmediate() }
        binding!!.toolbar.fragmentTitle.setText(R.string.sun_set_rise_notification)
        binding!!.sunRiseSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            var init = true
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                if (!init) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                    sharedPreferences.edit().putBoolean(getString(R.string.pref_key_sun_rise_notification), isChecked).commit()
                } else {
                    init = false
                }
            }
        })
        binding!!.sunSetSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            var init = true
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                if (!init) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
                    sharedPreferences.edit().putBoolean(getString(R.string.pref_key_sun_set_notification), isChecked).commit()
                } else {
                    init = false
                }
            }
        })
        binding!!.sunriseTime.setOnClickListener {
            showTimePicker(object : OnTimeListener {
                override fun onResult(value: Long) {}
            })
        }
        binding!!.sunsetTime.setOnClickListener {
            showTimePicker(object : OnTimeListener {
                override fun onResult(value: Long) {}
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun showTimePicker(onTimeListener: OnTimeListener) {}
    interface OnTimeListener {
        fun onResult(value: Long)
    }
}