package com.bionym.app.passwordvault.view;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.controller.Nymi;
import com.bionym.app.passwordvault.controller.NymiBand;
import com.bionym.app.passwordvault.controller.Nymulator;
import com.bionym.app.passwordvault.controller.ValidationController;
import com.bionym.app.passwordvault.utils.Constants;
import com.bionym.app.passwordvault.utils.FileManager;
import com.bionym.app.passwordvault.utils.SystemUtils;
import com.bionym.ncl.NclProvision;

/**
 * 
 * This is the launching activity of the application. User can provision new
 * Nymi Band, validate existing Nymi band, view list of existing provisions and
 * also delete existing provisions in this screen
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */

public class ValidationActivity extends Activity implements ValidationController.ValidationProcessListener {

	private final String LOG_TAG = ValidationActivity.class.getName();

	private ValidationController validationController;

	private NclProvision provision;

	private ProgressBar progress;
	private TextView textView;
	private ImageView image;
	private Button retry;

	private Context mContext;
	private Nymi nymi;

	private Resources appResources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.validation_activity);

		appResources = getResources();

		if (!SystemUtils.isTablet(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		mContext = this;

		progress = (ProgressBar) findViewById(R.id.myProgress);
		textView = (TextView) findViewById(R.id.myTextProgress);
		image = (ImageView) findViewById(R.id.tickImage);
		retry = (Button) findViewById(R.id.retry);
		TextView splashtext = (TextView) findViewById(R.id.splashtext1);
		splashtext.setTypeface(SystemUtils.getButtonTypeface(this));
		findViewById(R.id.splashbtn).setVisibility(View.GONE);

		// load provision in memory from provision text file
		provision = FileManager.loadProvision(mContext);

		startValidation();

		retry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				retry.setVisibility(View.GONE);
				image.setVisibility(View.GONE);
				image.setImageResource(R.drawable.tickimage);
				startValidation();

			}
		});

	}

	/**
	 * This method initialize the Nymi and starts validation of the Nymi band to
	 * check authorized user
	 */
	private void startValidation() {
		if (provision != null) {
			// start validation
			textView.setVisibility(View.VISIBLE);
			progress.setVisibility(View.VISIBLE);
			initializeNcl();
			validationController = new ValidationController(ValidationActivity.this);
			validationController.startValidation(ValidationActivity.this, provision);
		}
	}

	/**
	 * Initialize the NCL library
	 */
	protected void initializeNcl() {

		if (nymi == null) { // Create the NclCallback object
			if (Constants.ISDEVICE) {
				nymi = NymiBand.startNclSession(this, appResources.getString(R.string.app_name), null);

			} else {
				nymi = Nymulator.startNclSession(this, appResources.getString(R.string.app_name), null);
			}
		}
	}

	@Override
	public void onStartProcess(ValidationController controller) {
		Log.i(LOG_TAG, new StringBuilder("Nymi start validation for: ").append(Arrays.toString(provision.id.v)).toString());

	}

	@Override
	public void onFound(ValidationController controller) {

		final String provisionId = SystemUtils.toHexString(controller.getProvision().key.v);
		Log.i(LOG_TAG, new StringBuilder("Nymi validation found Nymi on: ").append(provisionId).toString());
	}

	@Override
	public void onValidated(final ValidationController controller) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progress.setVisibility(View.GONE);
				textView.setText(appResources.getString(R.string.progress_successvalidation_text));
				image.setVisibility(View.VISIBLE);
				// goto main activity
				Intent intent = new Intent(mContext, HomeActivity.class);
				intent.putExtra("handle", controller.getNymiHandle());
				startActivity(intent);
				finish();
			}
		});

	}

	@Override
	public void onFailure(ValidationController controller) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progress.setVisibility(View.GONE);
				textView.setText(appResources.getString(R.string.progress_failvalidation_text));
				image.setImageResource(R.drawable.crossimage);
				image.setVisibility(View.VISIBLE);
				retry.setVisibility(View.VISIBLE);

			}
		});
	}

	@Override
	public void onDisconnected(ValidationController controller) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progress.setVisibility(View.GONE);
				textView.setText(appResources.getString(R.string.progress_failvalidation_text));
				image.setImageResource(R.drawable.crossimage);
				image.setVisibility(View.VISIBLE);
				retry.setVisibility(View.VISIBLE);
			}
		});
	}
}
