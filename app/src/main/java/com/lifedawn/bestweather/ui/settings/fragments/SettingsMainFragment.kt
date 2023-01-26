package com.lifedawn.bestweather.ui.settings.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.interfaces.IAppbarTitle
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.databinding.FragmentSettingsMainBinding

class SettingsMainFragment : BaseFragment<FragmentSettingsMainBinding>(R.layout.fragment_settings_main), IAppbarTitle {
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!childFragmentManager.popBackStackImmediate()) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsFragment = SettingsFragment(this)
        childFragmentManager.beginTransaction().add(binding.fragmentContainer.getId(), settingsFragment).commitNow()
        binding.toolbar.backBtn.setOnClickListener { view1 -> onBackPressedCallback.handleOnBackPressed() }
        binding.toolbar.fragmentTitle.setText(R.string.settings)
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
    }

    override fun setAppbarTitle(title: String?) {
        binding.toolbar.fragmentTitle.setText(title)
    }
}