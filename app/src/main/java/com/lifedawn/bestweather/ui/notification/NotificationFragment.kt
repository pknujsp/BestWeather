package com.lifedawn.bestweather.ui.notification

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.IntentUtil
import com.lifedawn.bestweather.commons.classes.MainThreadWorker
import com.lifedawn.bestweather.commons.views.BaseFragment
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback
import com.lifedawn.bestweather.databinding.FragmentNotificationBinding
import com.lifedawn.bestweather.ui.notification.daily.fragment.DailyPushNotificationListFragment
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationHelper
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationSettingsFragment
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationViewModel

class NotificationFragment constructor() : BaseFragment<FragmentNotificationBinding>(R.layout.fragment_notification) {
    private var ongoingNotificationViewModel: OngoingNotificationViewModel? = null
    private var initializing: Boolean = true
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ongoingNotificationViewModel = ViewModelProvider(requireActivity()).get<OngoingNotificationViewModel>(
            OngoingNotificationViewModel::class.java
        )
    }


    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.ongoing.title.setText(R.string.always_notification)
        binding!!.ongoing.editBtn.setText(R.string.settings)
        binding!!.toolbar.fragmentTitle.setText(R.string.notification)
        binding!!.toolbar.backBtn.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                getParentFragmentManager().popBackStack()
            }
        })
        binding!!.ongoing.editBtn.setOnClickListener(View.OnClickListener({ v: View? -> addOngoingNotificationSettingsFragment() }))
        binding!!.ongoing.funcStateSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            public override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                binding!!.ongoing.editBtn.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
                if (initializing) {
                    initializing = false
                    return
                }
                if (isChecked) {
                    ongoingNotificationViewModel.getOngoingNotificationDto(object : DbQueryCallback<OngoingNotificationDto?>() {
                        fun onResultSuccessful(ongoingNotificationDto: OngoingNotificationDto) {
                            MainThreadWorker.runOnUiThread(Runnable({

                                // 저장된 알림 데이터가 있으면 알림 표시
                                val context: Context = requireContext().getApplicationContext()
                                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                                    ongoingNotificationDto.setOn(true)
                                    val helper: OngoingNotificationHelper = OngoingNotificationHelper(context)
                                    val pendingIntent: PendingIntent = helper.createManualPendingIntent(
                                        getString(R.string.com_lifedawn_bestweather_action_REFRESH),
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )
                                    try {
                                        pendingIntent.send()
                                    } catch (e: PendingIntent.CanceledException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    initializing = true
                                    binding!!.ongoing.funcStateSwitch.setChecked(false)
                                    ongoingNotificationDto.setOn(false)
                                    Toast.makeText(getContext(), R.string.disabledNotification, Toast.LENGTH_SHORT).show()
                                    startActivity(IntentUtil.getNotificationSettingsIntent(getActivity()))
                                }
                                ongoingNotificationViewModel.save(ongoingNotificationDto, null)
                            }))
                        }

                        fun onResultNoData() {
                            // 저장된 알림 데이터가 없으면 알림 세부설정 화면 열기
                            MainThreadWorker.runOnUiThread(Runnable({ addOngoingNotificationSettingsFragment() }))
                        }
                    })
                } else {
                    val notificationHelper: NotificationHelper = NotificationHelper(requireContext().getApplicationContext())
                    notificationHelper.cancelNotification(NotificationType.Ongoing.getNotificationId())
                    ongoingNotificationViewModel.getOngoingNotificationDto(object : DbQueryCallback<OngoingNotificationDto?>() {
                        fun onResultSuccessful(result: OngoingNotificationDto) {
                            result.setOn(false)
                            ongoingNotificationViewModel.save(result, null)
                        }

                        fun onResultNoData() {}
                    })
                }
            }
        })
        binding!!.daily.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                val listFragment: DailyPushNotificationListFragment = DailyPushNotificationListFragment()
                val tag: String = DailyPushNotificationListFragment::class.java.getName()
                getParentFragmentManager().beginTransaction().hide(this@NotificationFragment).add(
                    R.id.fragment_container,
                    listFragment, tag
                )
                    .addToBackStack(tag).commitAllowingStateLoss()
            }
        })
        loadOngoingNotificationState()
    }

    public override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadOngoingNotificationState()
        }
    }

    private fun loadOngoingNotificationState() {
        initializing = true
        ongoingNotificationViewModel.getOngoingNotificationDto(object : DbQueryCallback<OngoingNotificationDto?>() {
            fun onResultSuccessful(result: OngoingNotificationDto) {
                MainThreadWorker.runOnUiThread(Runnable({
                    binding!!.ongoing.funcStateSwitch.setChecked(result.isOn())
                    initializing = false
                }))
            }

            fun onResultNoData() {
                MainThreadWorker.runOnUiThread(Runnable({
                    binding!!.ongoing.funcStateSwitch.setChecked(false)
                    initializing = false
                }))
            }
        })
    }

    private fun addOngoingNotificationSettingsFragment() {
        val ongoingNotificationSettingsFragment: OngoingNotificationSettingsFragment = OngoingNotificationSettingsFragment()
        val tag: String = OngoingNotificationSettingsFragment::class.java.getName()
        getParentFragmentManager().beginTransaction().hide(this@NotificationFragment).add(
            R.id.fragment_container,
            ongoingNotificationSettingsFragment,
            tag
        ).addToBackStack(tag).commitAllowingStateLoss()
    }
}