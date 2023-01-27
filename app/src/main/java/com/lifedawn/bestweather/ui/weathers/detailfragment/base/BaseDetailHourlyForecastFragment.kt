package com.lifedawn.bestweather.ui.weathers.detailfragment.base

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.ui.weathers.detailfragment.base.dialogfragments.DetailHourlyForecastDialogFragment

open class BaseDetailHourlyForecastFragment constructor() : BaseDetailForecastFragment() {
    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.toolbar.extraBtn.setText(R.string.show_data_type)
        binding!!.toolbar.extraBtn.setVisibility(View.VISIBLE)
        binding!!.toolbar.extraBtn.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                val items: Array<CharSequence> = getResources().getTextArray(R.array.DetailHourlyForecastShowDataType)
                val sharedPreferences: SharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext())
                val selectedItem: Int = sharedPreferences.getInt(getString(R.string.pref_key_detail_hourly_forecast_show_data), 0)
                MaterialAlertDialogBuilder((getActivity())!!)
                    .setSingleChoiceItems(items, selectedItem, object : DialogInterface.OnClickListener {
                        public override fun onClick(dialog: DialogInterface, which: Int) {
                            sharedPreferences.edit().putInt(getString(R.string.pref_key_detail_hourly_forecast_show_data), which).commit()
                            if (selectedItem != which) {
                                setDataViewsByList()
                            }
                            dialog.dismiss()
                        }
                    }).setTitle(R.string.show_data_type).create().show()
            }
        })
    }

    public override fun onClickedItem(position: Int) {
        if (clickableItem) {
            clickableItem = false
            val bundle: Bundle = Bundle()
            bundle.putInt("FirstSelectedPosition", position)
            val detailHourlyForecastDialogFragment: DetailHourlyForecastDialogFragment = DetailHourlyForecastDialogFragment()
            DetailHourlyForecastDialogFragment.Companion.setHourlyForecastDtoList(hourlyForecastDtoList)
            detailHourlyForecastDialogFragment.setArguments(bundle)
            detailHourlyForecastDialogFragment.show(
                getChildFragmentManager(),
                DetailHourlyForecastDialogFragment::class.java.getName()
            )
        }
    }

    override fun onFragmentStarted(fragment: Fragment?) {
        if (fragment is DetailHourlyForecastDialogFragment) {
            super.onFragmentStarted(fragment)
        }
    }

    override fun setDataViewsByList() {}
    override fun setDataViewsByTable() {}

    companion object {
        protected var hourlyForecastDtoList: List<HourlyForecastDto>? = null
        @JvmStatic
        fun setHourlyForecastDtoList(hourlyForecastDtoList: List<HourlyForecastDto>?) {
            Companion.hourlyForecastDtoList = hourlyForecastDtoList
        }
    }
}