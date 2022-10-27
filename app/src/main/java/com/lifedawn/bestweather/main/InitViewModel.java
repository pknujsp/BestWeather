package com.lifedawn.bestweather.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class InitViewModel extends AndroidViewModel {
	public boolean ready;

	public InitViewModel(@NonNull Application application) {
		super(application);
	}
}
