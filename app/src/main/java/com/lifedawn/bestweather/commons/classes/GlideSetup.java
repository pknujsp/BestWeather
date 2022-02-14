package com.lifedawn.bestweather.commons.classes;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@GlideModule
public class GlideSetup extends AppGlideModule {

	@Override
	public void registerComponents(Context context, Glide glide, Registry registry) {
		final OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS)
				.writeTimeout(5, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS).build();

		OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(okHttpClient);
		glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
	}

}
