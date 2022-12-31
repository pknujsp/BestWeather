package com.lifedawn.bestweather.ui.notification.ongoing.work;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.LocationType;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.commons.classes.forremoteviews.RemoteViewsUtil;
import com.lifedawn.bestweather.ui.notification.NotificationHelper;
import com.lifedawn.bestweather.ui.notification.NotificationType;
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotiViewCreator;
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationHelper;
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationProcessor;
import com.lifedawn.bestweather.ui.notification.ongoing.OngoingNotificationRepository;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.commons.utils.DeviceUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OngoingNotificationListenableWorker extends ListenableWorker {
	private OngoingNotiViewCreator ongoingNotiViewCreator;
	private final String ACTION;

	public OngoingNotificationListenableWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		ACTION = workerParams.getInputData().getString("action");
	}

	@NonNull
	@Override
	public ListenableFuture<Result> startWork() {
		return CallbackToFutureAdapter.getFuture(completer -> {
			final BackgroundWorkCallback backgroundWorkCallback = () -> {
				if (!isStopped()) {
					completer.set(Result.success());
				}
			};

			OngoingNotificationRepository repository = OngoingNotificationRepository.getINSTANCE();
			repository.getOngoingNotificationDto(new DbQueryCallback<OngoingNotificationDto>() {
				@Override
				public void onResultSuccessful(OngoingNotificationDto ongoingNotificationDto) {
					if (ACTION.equals(Intent.ACTION_BOOT_COMPLETED) || ACTION.equals(Intent.ACTION_MY_PACKAGE_REPLACED)
							|| ACTION.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_RESTART))) {
						ongoingNotiViewCreator = new OngoingNotiViewCreator(getApplicationContext(), ongoingNotificationDto);
						reStartNotification(ongoingNotificationDto, backgroundWorkCallback);
					} else if (ACTION.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
						if (ongoingNotificationDto.getUpdateIntervalMillis() > 0) {
							OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());

							if (!ongoingNotificationHelper.isRepeating())
								ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotificationDto.getUpdateIntervalMillis());
						}

						if (DeviceUtils.Companion.isScreenOn(getApplicationContext())) {
							ongoingNotiViewCreator = new OngoingNotiViewCreator(getApplicationContext(), ongoingNotificationDto);
							createNotification(ongoingNotificationDto, backgroundWorkCallback);
						} else {
							backgroundWorkCallback.onFinished();
						}

					}
				}

				@Override
				public void onResultNoData() {
					NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
					notificationHelper.cancelNotification(NotificationType.Ongoing.getNotificationId());
					backgroundWorkCallback.onFinished();
				}
			});


			return backgroundWorkCallback;
		});
	}


	@NonNull
	@Override
	public ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
		final int notificationId = (int) System.currentTimeMillis();
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.ForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getApplicationContext().getString(R.string.updatingNotification)).setContentTitle(
						getApplicationContext().getString(R.string.updatingNotification))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		ForegroundInfo foregroundInfo = new ForegroundInfo(notificationId, notification);

		return new ListenableFuture<ForegroundInfo>() {
			@Override
			public void addListener(Runnable listener, Executor executor) {

			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public ForegroundInfo get() throws ExecutionException, InterruptedException {
				return foregroundInfo;
			}

			@Override
			public ForegroundInfo get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
				return foregroundInfo;
			}
		};
	}

	@Override
	public void onStopped() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		notificationHelper.cancelNotification(NotificationType.Ongoing.getNotificationId());
	}


	public void reStartNotification(OngoingNotificationDto ongoingNotificationDto, @NonNull BackgroundWorkCallback callback) {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());

		if (ongoingNotificationDto.getUpdateIntervalMillis() > 0 && !ongoingNotificationHelper.isRepeating()) {
			ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotificationDto.getUpdateIntervalMillis());
		}

		boolean active = notificationHelper.activeNotification(NotificationType.Ongoing.getNotificationId());
		if (active) {
			callback.onFinished();
		} else {
			createNotification(ongoingNotificationDto, callback);
		}
	}

	private void createNotification(OngoingNotificationDto ongoingNotificationDto, BackgroundWorkCallback callback) {
		RemoteViews[] remoteViews = ongoingNotiViewCreator.createRemoteViews(false);

		RemoteViews collapsedView = remoteViews[0];
		RemoteViews expandedView = remoteViews[1];

		RemoteViewsUtil.onBeginProcess(expandedView);
		RemoteViewsUtil.onBeginProcess(collapsedView);

		OngoingNotificationProcessor processor = OngoingNotificationProcessor.getINSTANCE();

		processor.makeNotification(getApplicationContext(), ongoingNotificationDto, collapsedView, expandedView, R.drawable.refresh, null,
				false, null);

		if (ongoingNotificationDto.getLocationType() == LocationType.CurrentLocation) {
			processor.loadCurrentLocation(getApplicationContext(), ongoingNotificationDto, collapsedView, expandedView, callback);
		} else {
			processor.loadWeatherData(getApplicationContext(), ongoingNotificationDto, collapsedView, expandedView, callback);
		}
	}

}
