package com.lifedawn.bestweather.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.databinding.FragmentIntroTransactionBinding

class IntroTransactionFragment constructor() : Fragment() {
    private var binding: FragmentIntroTransactionBinding? = null
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        public override fun handleOnBackPressed() {
            if (!getChildFragmentManager().popBackStackImmediate()) {
                getActivity()!!.finish()
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActivity()!!.getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback)
        getActivity()!!.getWindow().getDecorView().setSystemUiVisibility(
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        )
    }

    public override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntroTransactionBinding.inflate(inflater, container, false)
        return binding!!.getRoot()
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    public override fun onStart() {
        super.onStart()
        val introFragment: IntroFragment = IntroFragment()
        getChildFragmentManager().beginTransaction().add(
            binding!!.fragmentContainer.getId(), introFragment,
            getString(R.string.tag_intro_fragment)
        ).commitNow()
    }

    public override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
    }

    public override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}