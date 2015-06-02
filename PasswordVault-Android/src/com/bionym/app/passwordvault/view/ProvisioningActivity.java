package com.bionym.app.passwordvault.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.controller.Nymi;
import com.bionym.app.passwordvault.controller.NymiBand;
import com.bionym.app.passwordvault.controller.Nymulator;
import com.bionym.app.passwordvault.controller.ProvisionController;
import com.bionym.app.passwordvault.utils.Constants;
import com.bionym.app.passwordvault.utils.FileManager;
import com.bionym.app.passwordvault.utils.SystemUtils;
import com.bionym.ncl.NclProvision;

/**
 * 
 * This is the provisioning activity of the application. User can provision new
 * Nymi Band in this screen
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */

public class ProvisioningActivity extends Activity implements ProvisionController.ProvisionProcessListener {

	private final String LOG_TAG = ProvisioningActivity.class.getName();

	private ProvisionController provisionController;
	private ProvisionController mController;;

	private NclProvision provision;

	private ProgressBar progress;
	private TextView progressText;
	private LinearLayout provisionLayout;
	private LinearLayout agreeLayout;
	private Context mContext;
	private Button agreeBtn;
	private Button disagreeBtn;
	private Button retryBtn;
	private Nymi nymi;
	private boolean isSearching;
	private Resources appResources;
	final int sdk = android.os.Build.VERSION.SDK_INT;
	private AnimationDrawable loadingAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.home_view);

		appResources = getResources();

		// set to portrait mode is not a tablet
		if (!SystemUtils.isTablet(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		mContext = this;

		// initialize UI components
		progress = (ProgressBar) findViewById(R.id.myProgress);
		progressText = (TextView) findViewById(R.id.myTextProgress);
		progress.setVisibility(View.VISIBLE);
		progressText.setVisibility(View.VISIBLE);
		agreeBtn = (Button) findViewById(R.id.agreement_button1);
		agreeBtn.setSelected(true);
		agreeBtn.setTypeface(SystemUtils.getButtonTypeface(this));
		disagreeBtn = (Button) findViewById(R.id.agreement_button2);
		disagreeBtn.setTypeface(SystemUtils.getButtonTypeface(this));
		retryBtn = (Button) findViewById(R.id.retry);
		retryBtn.setTypeface(SystemUtils.getButtonTypeface(this));
		provisionLayout = (LinearLayout) findViewById(R.id.provisionlayout);
		agreeLayout = (LinearLayout) findViewById(R.id.agreelayout);

		// if orientation is changed restore previous state
		if (savedInstanceState != null) {
			isSearching = savedInstanceState.getBoolean("isSearching");
			provisionLayout.setVisibility(savedInstanceState.getInt("provisionLayout"));
			agreeLayout.setVisibility(savedInstanceState.getInt("agreeLayout"));
			provisionController = (ProvisionController) savedInstanceState.getSerializable("provisionController");
			mController = (ProvisionController) savedInstanceState.getSerializable("controller");
			if (agreeLayout.getVisibility() == View.VISIBLE) {
				agreementView(mController);
			}
		}

		if (!isSearching) {
			// get into provision mode.
			startSearch();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isSearching", isSearching);
		outState.putInt("provisionLayout", provisionLayout.getVisibility());
		outState.putInt("agreeLayout", agreeLayout.getVisibility());
		outState.putSerializable("provisionController", provisionController);
		outState.putSerializable("controller", mController);

	}

	/**
	 * Start the discovery process
	 */
	protected void startSearch() {

		isSearching = true;

		initializeNcl();

		if (provisionController != null) { // to have a clean start
			ProvisionController.stopProvision();
		} else {
			provisionController = new ProvisionController(ProvisioningActivity.this);
		}

		// start discovery
		if (!provisionController.startProvisionProcess(ProvisioningActivity.this)) {
			ProvisionController.stopProvision();
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);
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

	public void onWindowFocusChanged(boolean hasFocus) {
		loadingAnimation = (AnimationDrawable) findViewById(R.id.nymi_search).getBackground();
		if (hasFocus) {
			loadingAnimation.start();
		} else {
			loadingAnimation.stop();
		}
	}

	/**
	 * This method Shows the fail page UI
	 */

	private void failView() {
		runOnUiThread(new Runnable() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void run() {
				final TextView introText = (TextView) findViewById(R.id.intro_text1);
				if (loadingAnimation != null) {
					loadingAnimation.stop();
				}
				// if agree layout is not visible then make retry button visible
				if (findViewById(R.id.agreelayout).getVisibility() == View.VISIBLE) {
					provisionLayout.setVisibility(View.VISIBLE);
					agreeLayout.setVisibility(View.GONE);
					introText.setText(appResources.getString(R.string.agree_timeout_text));

				} else {
					introText.setText(appResources.getString(R.string.discover_failure_text));
				}
				if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					introText.setBackgroundDrawable(appResources.getDrawable(R.drawable.bg_textview));
				} else {
					introText.setBackground(appResources.getDrawable(R.drawable.bg_textview));
				}
				// set the fail view
				retryBtn.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
				progressText.setVisibility(View.GONE);
				retryBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// reset the view to discovery mode
						if (loadingAnimation != null) {
							loadingAnimation.start();
						}
						retryBtn.setVisibility(View.GONE);
						progress.setVisibility(View.VISIBLE);
						progressText.setVisibility(View.VISIBLE);
						introText.setText(appResources.getString(R.string.intro_text1));
						if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
							introText.setBackgroundDrawable(null);
						} else {
							introText.setBackground(null);
						}

						if (provisionController != null) {
							ProvisionController.stopProvision();
						}
						new Handler(getMainLooper()).postDelayed(new Runnable() {

							@Override
							public void run() {
								isSearching = false;

								// again start discovery
								startSearch();
							}
						}, 5000);
					}
				});
			}
		});

	}

	// Show the argeement View
	private void agreementView(final ProvisionController controller) {
		runOnUiThread(new Runnable() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void run() {
				// Find the relativelayout which is behind the Nymi Image
				RelativeLayout llLeds = (RelativeLayout) findViewById(R.id.llPattern);
				RadioButton ll;
				// Go through all the children(radiobuttons) that llLeds has
				// And set there color according to the LED pattern received
				// from the Nymi
				boolean[] leds = controller.getLedPatterns();
				if (leds != null) {
					for (int i = 0; i < leds.length; i++) {
						ll = (RadioButton) llLeds.getChildAt(i);
						ll.setClickable(false);
						if (leds[i]) {
							if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
								ll.setBackgroundDrawable(appResources.getDrawable(R.drawable.ledsingle));
							} else {
								ll.setBackground(appResources.getDrawable(R.drawable.ledsingle));
							}
						} else {
							// reset radio button image to defult
							ll.setBackgroundResource(0);

						}
					}

					provisionLayout.setVisibility(View.GONE);
					agreeLayout.setVisibility(View.VISIBLE);
					if (loadingAnimation != null) {
						loadingAnimation.stop();
					}
					progress.setVisibility(View.GONE);
					progressText.setVisibility(View.GONE);
				}

				disagreeBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						controller.rejectAgreement();
						if (provisionController != null) {
							ProvisionController.stopProvision();
						}
						provisionLayout.setVisibility(View.VISIBLE);
						agreeLayout.setVisibility(View.GONE);
						progress.setVisibility(View.VISIBLE);
						progressText.setVisibility(View.VISIBLE);
						if (loadingAnimation != null) {
							loadingAnimation.start();
						}
						new Handler(getMainLooper()).postDelayed(new Runnable() {

							@Override
							public void run() {
								isSearching = false;

								startSearch();
							}
						}, 5000);
					}

				});

				agreeBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						controller.acceptAgreement(true);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								agreeBtn.setVisibility(View.GONE);
								disagreeBtn.findViewById(R.id.agreement_button2).setVisibility(View.GONE);
								progress.setVisibility(View.VISIBLE);
								progressText.setVisibility(View.VISIBLE);
								progressText.setText(appResources.getString(R.string.prov_progress_text));
							}
						});

					}

				});
			}
		});
	}

	@Override
	public void onStartProcess(ProvisionController controller) {
		mController = controller;
		Log.i(LOG_TAG, "Nymi start provision ..");

	}

	@Override
	public void onAgreement(final ProvisionController controller) {
		mController = controller;
		agreementView(controller);
	}

	@Override
	public void onProvisioned(final ProvisionController controller) {
		mController = controller;
		provision = controller.getProvision();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				FileManager.saveProvision(provision, mContext);
				progress.setVisibility(View.GONE);
				progressText.setVisibility(View.GONE);
				findViewById(R.id.tickImage).setVisibility(View.VISIBLE);
				Intent intent = new Intent(mContext, HomeActivity.class);
				intent.putExtra("handle", controller.getNymiHandle());
				startActivity(intent);
				finish();

			}
		});

	}

	@Override
	public void onFailure(ProvisionController controller) {
		mController = controller;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				failView();
			}
		});
	}

	@Override
	public void onDisconnected(ProvisionController controller) {
		mController = controller;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progress.setVisibility(View.GONE);
				progressText.setVisibility(View.GONE);

			}
		});
	}

}
