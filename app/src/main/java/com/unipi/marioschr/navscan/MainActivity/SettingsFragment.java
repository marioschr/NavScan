package com.unipi.marioschr.navscan.MainActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.marioschr.navscan.Auth.AuthActivity;
import com.unipi.marioschr.navscan.utils.LocaleUtils;
import com.unipi.marioschr.navscan.R;

public class SettingsFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey);

		Preference btnSignOut = getPreferenceManager().findPreference("sign_out_key");
		if (btnSignOut != null) {
			btnSignOut.setOnPreferenceClickListener(preference -> {
				SignOut();
				return true;
			});
		}

		Preference btnEditProfile = getPreferenceManager().findPreference("edit_account_key");
		if (btnEditProfile != null) {
			btnEditProfile.setOnPreferenceClickListener(preference -> {
				NavigateToEditProfile();
				return true;
			});
		}

		Preference btnViewClaimedItems = getPreferenceManager().findPreference("claimed_items_key");
		if (btnViewClaimedItems != null) {
			btnViewClaimedItems.setOnPreferenceClickListener(preference -> {
				NavigateToViewClaimedItems();
				return true;
			});
		}

		Preference lang = getPreferenceManager().findPreference("language_key");
		if (lang != null) {
			lang.setOnPreferenceChangeListener((preference, newLang) -> {
				LocaleUtils.setLanguage(newLang.toString(), getContext());
				Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(getContext().getPackageName());
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				return true;
			});
		}
	}

	public void SignOut() {
		FirebaseAuth.getInstance().signOut();
		startActivity(new Intent(requireActivity(), AuthActivity.class));
		requireActivity().finish();
	}

	public void NavigateToEditProfile() {
		Navigation.findNavController(requireView()).navigate(R.id.action_navigation_settings_to_editProfileFragment);
	}

	public void NavigateToViewClaimedItems() {
		Navigation.findNavController(requireView()).navigate(R.id.action_navigation_settings_to_claimedItemsFragment);
	}
}