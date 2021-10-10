package com.unipi.marioschr.navscan;

import android.app.Application;

import lv.chi.photopicker.ChiliPhotoPicker;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		ChiliPhotoPicker.INSTANCE.init(new GlideImageLoader(), BuildConfig.APPLICATION_ID+".fileprovider");
	}
}
