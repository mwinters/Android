package com.bionym.app.passwordvault.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.utils.FileManager;
import com.bionym.app.passwordvault.utils.SystemUtils;
import com.bionym.ncl.NclProvision;

/**
 * 
 * This activity is shown on first time launch User can provision this
 * application with their Nymi bands by clicking on Begin Setup button
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */
public class SplashScreenActivity extends Activity {
	// Splash screen timer
	private static int SPLASH_TIME_OUT = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if (!SystemUtils.isTablet(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		setContentView(R.layout.activity_splash);

		new Handler(getMainLooper()).postDelayed(new Runnable() {
			/*
			 * Showing splash screen with a timer.
			 */
			@Override
			public void run() {// load provision in memory from provision text
								// file
				NclProvision provision = FileManager.loadProvision(SplashScreenActivity.this);

				// if already provisioned move to validation else move to setup screen
				if (provision != null) {
					startActivity(new Intent(SplashScreenActivity.this, ValidationActivity.class));
				}else{
					startActivity(new Intent(SplashScreenActivity.this, SetupActivity.class));
				}
				finish();

			}
		}, SPLASH_TIME_OUT);

	}

}
