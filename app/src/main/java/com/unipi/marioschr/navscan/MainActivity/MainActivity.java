package com.unipi.marioschr.navscan.MainActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.unipi.marioschr.navscan.R;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;
import github.com.st235.lib_expandablebottombar.navigation.ExpandableBottomBarNavigationUI;

public class MainActivity extends AppCompatActivity {
	private NavController navController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ExpandableBottomBar bottomNavigationView = findViewById(R.id.expandable_bottom_bar);
		navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main2);
		ExpandableBottomBarNavigationUI.setupWithNavController(bottomNavigationView,navController);
	}

	@Override
	public boolean onSupportNavigateUp() {
		return navController.navigateUp() || super.onSupportNavigateUp();
	}
}