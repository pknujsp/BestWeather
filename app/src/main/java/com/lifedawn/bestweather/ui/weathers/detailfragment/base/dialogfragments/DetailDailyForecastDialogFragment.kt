package com.lifedawn.bestweather.ui.weathers.detailfragment.base.dialogfragments

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.databinding.TabForecastItemBinding
import com.lifedawn.bestweather.ui.weathers.detailfragment.adapters.DetailDailyForecastViewPagerAdapter
import java.time.format.DateTimeFormatter

class DetailDailyForecastDialogFragment : BaseDetailDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DetailDailyForecastViewPagerAdapter()
        adapter.setDailyForecastDtoList(dailyForecastDtoList)
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
        val dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E")
        val divider = " / "
        var index = 0
        var tabItemBinding: TabForecastItemBinding? = null
        for ((date, valuesList, minTemp, maxTemp) in dailyForecastDtoList!!) {
            tabItemBinding = TabForecastItemBinding.inflate(layoutInflater, binding!!.tabLayout, false)
            tabItemBinding.dateTime.text = date.format(dateTimeFormatter)
            tabItemBinding.temp.setText(String(minTemp + divider + maxTemp))
            if (valuesList.size == 1) {
                Glide.with(tabItemBinding.leftWeatherIcon).load(valuesList[0].weatherIcon).into(tabItemBinding.leftWeatherIcon)
                tabItemBinding.rightWeatherIcon.visibility = View.GONE
            } else if (valuesList.size == 2) {
                Glide.with(tabItemBinding.leftWeatherIcon).load(valuesList[0].weatherIcon).into(tabItemBinding.leftWeatherIcon)
                Glide.with(tabItemBinding.rightWeatherIcon).load(valuesList[1].weatherIcon).into(tabItemBinding.rightWeatherIcon)
            } else if (valuesList.size == 4) {
                Glide.with(tabItemBinding.leftWeatherIcon).load(valuesList[1].weatherIcon).into(tabItemBinding.leftWeatherIcon)
                Glide.with(tabItemBinding.rightWeatherIcon).load(valuesList[2].weatherIcon).into(tabItemBinding.rightWeatherIcon)
            }
            binding!!.tabLayout.getTabAt(index++)!!.customView = tabItemBinding.root
        }
        binding!!.tabLayout.selectTab(binding!!.tabLayout.getTabAt(firstSelectedPosition))
    }

    companion object {
        private var dailyForecastDtoList: List<DailyForecastDto>? = null
        fun setDailyForecastDtoList(dailyForecastDtoList: List<DailyForecastDto>?) {
            Companion.dailyForecastDtoList = dailyForecastDtoList
        }
    }
}