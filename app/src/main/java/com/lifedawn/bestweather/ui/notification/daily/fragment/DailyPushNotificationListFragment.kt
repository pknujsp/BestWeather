package com.lifedawn.bestweather.ui.notification.daily.fragment

import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.RecyclerViewItemDecoration
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.LocationType
import com.lifedawn.bestweather.commons.interfaces.OnCheckedSwitchInListListener
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener
import com.lifedawn.bestweather.commons.interfaces.OnClickedPopupMenuItemListener
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.data.local.room.dto.DailyPushNotificationDto
import com.lifedawn.bestweather.data.local.room.repository.DailyPushNotificationRepository
import com.lifedawn.bestweather.databinding.FragmentDailyPushNotificationListBinding
import com.lifedawn.bestweather.databinding.ViewDailyPushNotificationItemBinding
import com.lifedawn.bestweather.ui.notification.daily.DailyNotificationHelper
import com.lifedawn.bestweather.ui.notification.daily.DailyPushNotificationType
import com.lifedawn.bestweather.ui.notification.daily.viewmodel.DailyNotificationViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class DailyPushNotificationListFragment : Fragment() {
    private var binding: FragmentDailyPushNotificationListBinding? = null
    private var adapter: NotificationListAdapter? = null
    private val hoursFormatter = DateTimeFormatter.ofPattern("a h:mm")
    private var repository: DailyPushNotificationRepository? = null
    private var dailyNotificationHelper: DailyNotificationHelper? = null
    private var dailyNotificationViewModel: DailyNotificationViewModel? = null
    private val sortComparator =
        Comparator<DailyPushNotificationDto> { o1, o2 -> LocalTime.parse(o1.alarmClock).compareTo(LocalTime.parse(o2.alarmClock)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = DailyPushNotificationRepository.getINSTANCE()
        dailyNotificationHelper = DailyNotificationHelper(requireContext().applicationContext)
        dailyNotificationViewModel = ViewModelProvider(this).get(DailyNotificationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDailyPushNotificationListBinding.inflate(inflater, container, false)
        val layoutParams = binding!!.toolbar.root.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = MyApplication.getStatusBarHeight()
        binding!!.toolbar.root.layoutParams = layoutParams
        binding!!.progressResultView.setContentView(binding!!.notificationList)
        binding!!.toolbar.fragmentTitle.setText(R.string.daily_notification)
        binding!!.toolbar.backBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        binding!!.notificationList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding!!.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                binding!!.notificationList.addItemDecoration(
                    RecyclerViewItemDecoration(
                        requireContext().applicationContext, true,
                        binding!!.root.height - binding!!.addBtn.top - binding!!.addBtn.height / 2
                    )
                )
            }
        })
        adapter = NotificationListAdapter(object : OnClickedListViewItemListener<DailyPushNotificationDto?> {
            override fun onClickedItem(e: DailyPushNotificationDto) {
                val settingsFragment = DailyNotificationSettingsFragment()
                val tag = DailyNotificationSettingsFragment::class.java.name
                val bundle = Bundle()
                bundle.putSerializable("dto", e)
                bundle.putBoolean(BundleKey.NewSession.name, false)
                settingsFragment.arguments = bundle
                parentFragmentManager.beginTransaction().hide(this@DailyPushNotificationListFragment).add(
                    R.id.fragment_container,
                    settingsFragment, tag
                ).addToBackStack(tag).setPrimaryNavigationFragment(settingsFragment).commitAllowingStateLoss()
            }
        }, object : OnCheckedSwitchInListListener<DailyPushNotificationDto?> {
            override fun onCheckedSwitch(dailyPushNotificationDto: DailyPushNotificationDto, isChecked: Boolean) {
                dailyPushNotificationDto.isEnabled = isChecked
                repository!!.update(dailyPushNotificationDto, null)
                dailyNotificationHelper!!.modifyPushNotification(dailyPushNotificationDto)
                var text = (LocalTime.parse(dailyPushNotificationDto.alarmClock).format(hoursFormatter)
                        + ", ")
                text += if (isChecked) {
                    getString(R.string.registeredDailyNotification)
                } else {
                    getString(R.string.unregisteredDailyNotification)
                }
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
        }, object : OnClickedPopupMenuItemListener<DailyPushNotificationDto?> {
            override fun onClickedItem(e: DailyPushNotificationDto, position: Int) {
                when (position) {
                    0 -> MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.removeNotification)
                        .setMessage(R.string.will_you_delete_the_notification)
                        .setPositiveButton(R.string.remove) { dialog, which ->
                            dailyNotificationHelper!!.disablePushNotification(e)
                            repository!!.delete(e, null)
                            dialog.dismiss()
                        }
                        .setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }.create().show()
                }
            }
        })
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (adapter!!.itemCount > 0) {
                    binding!!.progressResultView.onSuccessful()
                } else {
                    binding!!.progressResultView.onFailed(getString(R.string.empty_daily_notifications))
                }
            }
        })
        binding!!.notificationList.adapter = adapter
        binding!!.addBtn.setOnClickListener {
            val settingsFragment = DailyNotificationSettingsFragment()
            val tag = DailyNotificationSettingsFragment::class.java.name
            val bundle = Bundle()
            bundle.putBoolean(BundleKey.NewSession.name, true)
            settingsFragment.arguments = bundle
            parentFragmentManager.beginTransaction().hide(this@DailyPushNotificationListFragment).add(
                R.id.fragment_container,
                settingsFragment, tag
            ).addToBackStack(tag).commit()
        }
        dailyNotificationViewModel!!.listLiveData.observe(viewLifecycleOwner) { result ->
            Collections.sort(result, sortComparator)
            adapter!!.setNotificationList(result)
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    class NotificationListAdapter(
        private val onClickedItemListener: OnClickedListViewItemListener<DailyPushNotificationDto>,
        private val onCheckedSwitchInListListener: OnCheckedSwitchInListListener<DailyPushNotificationDto>,
        private val onClickedPopupMenuItemListener: OnClickedPopupMenuItemListener<DailyPushNotificationDto>
    ) : RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {
        private var notificationList: List<DailyPushNotificationDto> = ArrayList()
        private val hoursFormatter = DateTimeFormatter.ofPattern("a hh:mm")
        fun setNotificationList(notificationList: List<DailyPushNotificationDto>) {
            this.notificationList = notificationList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_daily_push_notification_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind()
        }

        override fun getItemCount(): Int {
            return notificationList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val binding: ViewDailyPushNotificationItemBinding
            private var init = true

            init {
                binding = ViewDailyPushNotificationItemBinding.bind(itemView)
                binding.notiSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                    if (init) {
                        return@OnCheckedChangeListener
                    }
                    onCheckedSwitchInListListener.onCheckedSwitch(notificationList[bindingAdapterPosition], isChecked)
                })
                binding.root.setOnClickListener { v: View? -> onClickedItemListener.onClickedItem(notificationList[bindingAdapterPosition]) }
                binding.control.setOnClickListener { v: View? ->
                    val popupMenu = PopupMenu(binding.root.context, binding.control, Gravity.BOTTOM)
                    popupMenu.menuInflater.inflate(R.menu.menu_of_daily_notification_item, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            else -> onClickedPopupMenuItemListener.onClickedItem(
                                notificationList[bindingAdapterPosition],
                                bindingAdapterPosition
                            )
                        }
                        return@setOnClickListener true
                    })
                    popupMenu.show()
                }
            }

            fun onBind() {
                init = true
                val pos = bindingAdapterPosition
                val dto = notificationList[pos]
                binding.hours.text = LocalTime.parse(dto.alarmClock).format(hoursFormatter)
                binding.notiSwitch.isChecked = dto.isEnabled
                binding.notificationType.setText(
                    DailyPushNotificationType.Companion.getNotificationName(
                        dto.notificationType,
                        binding.root.context.applicationContext
                    )
                )
                val addressName =
                    if (dto.locationType === LocationType.SelectedAddress) dto.addressName else binding.root.context.getString(
                        R.string.current_location
                    )
                binding.location.text = addressName
                init = false
            }
        }
    }
}