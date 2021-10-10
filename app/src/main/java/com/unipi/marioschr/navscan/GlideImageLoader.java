package com.unipi.marioschr.navscan;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import lv.chi.photopicker.loader.ImageLoader;

public class GlideImageLoader implements ImageLoader {

	@Override
	public void loadImage(@NonNull Context context, @NonNull ImageView imageView, @NonNull Uri uri) {
		Glide.with(context).asBitmap()
				.load(uri)
				.placeholder(R.drawable.world_travel)
				.centerCrop()
				.into(imageView);
	}
}
