package com.lifedawn.bestweather.ui.weathers.detail.base.dialogfragments

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto
import com.lifedawn.bestweather.databinding.TabForecastItemBinding
import com.lifedawn.bestweather.ui.weathers.detail.adapters.DetailHourlyForecastViewPagerAdapter
import java.time.format.DateTimeFormatter

class DetailHourlyForecastDialogFragment : BaseDetailDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DetailHourlyForecastViewPagerAdapter()
        adapter.setHourlyForecastDtoList(hourlyForecastDtoList)
        binding!!.detailForecastViewPager.adapter = adapter
        binding!!.detailForecastViewPager.setCurrentItem(firstSelectedPosition, false)
        binding!!.detailForecastViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
        setTabCustomView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun setTabCustomView() {
        super.setTabCustomView()
        val layoutInflater = layoutInflater
        val hour0Formatter = DateTimeFormatter.ofPattern("E H")
        var index = 0
        var tabItemBinding: TabForecastItemBinding? = null
        for ((hours, weatherIcon, _, _, temp) in hourlyForecastDtoList!!) {
            tabItemBinding = TabForecastItemBinding.inflate(layoutInflater, binding!!.tabLayout, false)
            tabItemBinding.rightWeatherIcon.visibility = View.GONE
            tabItemBinding.dateTime.text = hours.format(hour0Formatter)
            Glide.with(tabItemBinding.leftWeatherIcon).load(weatherIcon).into(tabItemBinding.leftWeatherIcon)
            tabItemBinding.temp.text = temp
            binding!!.tabLayout.getTabAt(index++)!!.customView = tabItemBinding.root
        }
        binding!!.tabLayout.selectTab(binding!!.tabLayout.getTabAt(firstSelectedPosition))
    }

    companion object {
        private var hourlyForecastDtoList: List<HourlyForecastDto>? = null
        fun setHourlyForecastDtoList(hourlyForecastDtoList: List<HourlyForecastDto>?) {
            Companion.hourlyForecastDtoList = hourlyForecastDtoList
        }
    }
}