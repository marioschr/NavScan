package com.unipi.marioschr.navscan;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleUtils {

	public static void setLanguage(String language, Context context) {
		Locale locale = new Locale(language);
		Locale.setDefault(locale);
		Resources resources = context.getResources();
		Configuration configuration = resources.getConfiguration();
		configuration.locale = locale;
		resources.updateConfiguration(configuration, resources.getDisplayMetrics());
	}
}