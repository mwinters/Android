package com.bionym.app.passwordvault.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.adapter.ExpandableListAdapter;
import com.bionym.app.passwordvault.controller.Nymi;
import com.bionym.app.passwordvault.controller.NymiBand;
import com.bionym.app.passwordvault.controller.Nymulator;
import com.bionym.app.passwordvault.model.Credentials;
import com.bionym.app.passwordvault.utils.Constants;
import com.bionym.app.passwordvault.utils.FileManager;
import com.bionym.app.passwordvault.utils.SystemUtils;

/**
 * 
 * This is the main activity which the user enters once provision and validation
 * is done
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 */
@SuppressLint("InflateParams")
public class HomeActivity extends Activity {

	private DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private RelativeLayout mainlayout;
	private List<String> listDataHeader;
	private ExpandableListAdapter listAdapter;
	private TextView addNew;
	private LinearLayout allpwds;
	private LinearLayout starredPwds;
	private TextView allpwdsTextView;
	private TextView starredPwdsTextView;

	private HashMap<String, List<String>> listDataChild;

	private ArrayList<Credentials> credentialsArr;
	private Context mContext;

	private Resources appResources;
	private Nymi nymi;
	private int handle;
	private DisplayMetrics metrics;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		appResources = getResources();

		handle = getIntent().getIntExtra("handle", 0);
		metrics = appResources.getDisplayMetrics();

		// hide the app icon from status bar
		getActionBar().setIcon(new ColorDrawable(appResources.getColor(android.R.color.transparent)));

		mContext = this;

		credentialsArr = new ArrayList<Credentials>();

		// load the password text file in memory
		credentialsArr = FileManager.readPasswordFile(mContext);

		// Initialize layout params
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
		mainlayout = (RelativeLayout) findViewById(R.id.left_drawer_view);
		addNew = (TextView) findViewById(R.id.addnew_textview);
		addNew.setTypeface(SystemUtils.getButtonTypeface(this));
		allpwds = (LinearLayout) findViewById(R.id.all_pwd_layout);
		starredPwds = (LinearLayout) findViewById(R.id.starred_pwd_layout);
		allpwdsTextView = (TextView) findViewById(R.id.all_drawer_text_banner);
		allpwdsTextView.setTypeface(SystemUtils.getButtonTypeface(this));
		starredPwdsTextView = (TextView) findViewById(R.id.starred_drawer_text_banner);
		starredPwdsTextView.setTypeface(SystemUtils.getButtonTypeface(this));
		TextView footerText = (TextView) findViewById(R.id.text_banner1);
		footerText.setTypeface(SystemUtils.getButtonTypeface(this));

		// if phone then only portrait mode, in case of tablet both landscape
		// and
		// portrait mode
		if (!SystemUtils.isTablet(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		// preparing list data
		prepareListData();

		listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

		if (SystemUtils.isTablet(HomeActivity.this)) {

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				int height = (int) (metrics.heightPixels - getStatusBarHeight());
				LinearLayout.LayoutParams rel_btn = new LinearLayout.LayoutParams(appResources.getDimensionPixelOffset(R.dimen.left_drawer_width), height);
				mainlayout.setLayoutParams(rel_btn);
			}
		}


		if (mDrawerList != null) {
			// setting list adapter
			mDrawerList.setAdapter(listAdapter);

			mDrawerList.setOnChildClickListener(new OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

					ListDialog dialog = new ListDialog(mContext, android.R.style.Theme_Holo_Dialog, childPosition);
					dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
					// if in portrait mode
					if (mDrawerLayout != null) {
						mDrawerLayout.closeDrawer(mainlayout);
					}

					return false;
				}
			});
		}

		if (mDrawerLayout != null) {

			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, // nav
					// menu
					// toggle
					// icon

					R.string.app_name, // nav drawer open - description for
										// accessibility
					R.string.app_name // nav drawer close - description for
										// accessibility
			) {
				@Override
				public void onDrawerClosed(View view) {
					// calling onPrepareOptionsMenu() to show action bar icons
					invalidateOptionsMenu();

				}

				@Override
				public void onDrawerOpened(View drawerView) {

					// calling onPrepareOptionsMenu() to hide action bar icons
					invalidateOptionsMenu();
				}

				@Override
				public void onDrawerSlide(View drawerView, float slideOffset) {
					super.onDrawerSlide(drawerView, slideOffset);
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);

		}
		
		//customize action bar
		ActionBar actionBar = getActionBar();
		ShapeDrawable mBackgroundActionBar = new ShapeDrawable();
		mBackgroundActionBar.getPaint().setColor(appResources.getColor(R.color.dark_navy));
		mBackgroundActionBar.setBounds(30, 0, 0, 0);
		actionBar.setBackgroundDrawable(mBackgroundActionBar);
		
		// customize action bar title layout
		int titleId = appResources.getIdentifier("action_bar_title", "id", "android");
		TextView actionBartextView = (TextView) findViewById(titleId);
		actionBartextView.setTypeface(SystemUtils.getButtonTypeface(this));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		params.setMargins(50, 0, 0, 0);
		actionBartextView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		actionBartextView.setLayoutParams(params);

		// display fragment dynamically
		if (savedInstanceState == null) {
			if (credentialsArr.size() == 0) {
				displayView(Constants.MENU_OPTIONS.NEW_PWD);
			} else {
				displayView(Constants.MENU_OPTIONS.ALL_PWD);

			}
		}
		// show add new credential view
		addNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				displayView(Constants.MENU_OPTIONS.NEW_PWD);

			}
		});

		// show all credential view
		allpwds.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				displayView(Constants.MENU_OPTIONS.ALL_PWD);

			}
		});
		// show starred credential view
		starredPwds.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				displayView(Constants.MENU_OPTIONS.STARRED_PWD);

			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null) {
			// Pass the event to ActionBarDrawerToggle, if it returns
			// true, then it has handled the app icon touch event
			if (mDrawerToggle.onOptionsItemSelected(item)) {
				return true;
			}
			// Handle your other action bar items...
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * This method calculates the screen status bar height
	 * 
	 * @return height of the the status bar
	 */
	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = appResources.getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/*
	 * Preparing the list data
	 */
	private void prepareListData() {
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();

		// Adding child data
		listDataHeader.add(appResources.getString(R.string.options));

		// Adding child data
		List<String> menuitems = new ArrayList<String>();
		menuitems.add(appResources.getString(R.string.clear_pwd));
		menuitems.add(appResources.getString(R.string.unauthorize));

		listDataChild.put(listDataHeader.get(0), menuitems); // Header, Child
																// data
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerLayout != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (SystemUtils.isTablet(this)) {

			if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				Log.e("On Config Change", "LANDSCAPE");
			} else {
				Log.e("On Config Change", "PORTRAIT");
				if (mDrawerLayout != null) {
					mDrawerLayout.closeDrawer(mainlayout);
				}
			}
		} else {
			// Pass any configuration change to the drawer toggles
			mDrawerToggle.onConfigurationChanged(newConfig);
			if (mDrawerLayout != null) {

				mDrawerLayout.closeDrawer(mainlayout);
			}

		}
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(Constants.MENU_OPTIONS position) {

		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {

		case ALL_PWD:
			setTitle(appResources.getString(R.string.all));
			fragment = new CredentailsViewFragment(position.ordinal());
			break;
		case STARRED_PWD:
			setTitle(appResources.getString(R.string.starred));
			fragment = new CredentailsViewFragment(position.ordinal());
			break;
		case NEW_PWD:
			setTitle(appResources.getString(R.string.new_pwd));
			fragment = new NewPasswordFragment();
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

			if (mDrawerList != null) {

				// update selected item and title, then close the drawer
				mDrawerList.setItemChecked(position.ordinal(), true);
				mDrawerList.setSelection(position.ordinal());
			}
			if (mDrawerLayout != null) {
				mDrawerLayout.closeDrawer(mainlayout);
			}
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	/**
	 * This is a custom dialog for displaying unauthorized/clear passwords ui
	 * and take appropriate actions
	 * 
	 */
	private class ListDialog extends Dialog {

		private int index;

		/**
		 * 
		 * @param context
		 *            context of the activity on which popup is displayed
		 * @param theme
		 * @param position
		 *            the action to be taken
		 */
		public ListDialog(Context context, int theme, int position) {
			super(context, theme);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);

			index = position;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			Context context = getContext();

			// create custom dialog main layout and inflate view in that.
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.custom_dialog, null);

			// initialize dialog ui components
			final TextView headerSubText = (TextView) layout.findViewById(R.id.remove_header_text);
			TextView headerText = (TextView) layout.findViewById(R.id.popupHeader);
			// show the header on the screen
			headerSubText.setVisibility(View.VISIBLE);
			final Button cancel = (Button) layout.findViewById(R.id.cancelBtn);
			cancel.setTypeface(SystemUtils.getButtonTypeface(context));

			final Button clear = (Button) layout.findViewById(R.id.saveBtn);
			clear.setTypeface(SystemUtils.getButtonTypeface(context));
			clear.setSelected(true);

			cancel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();

				}
			});
			final Constants.RESET_OPTIONS optionsEnum = Constants.RESET_OPTIONS.values()[index];

			// change ui on the screen dynamically based on option selected
			switch (optionsEnum) {

			case CLEAR_PASSWORDS:
				// set the header text
				headerText.setText(appResources.getString(R.string.clear_pwd));
				headerSubText.setText(appResources.getString(R.string.clear_text));
				// change the button names on the screen dynamically based on
				// option selected
				clear.setText(appResources.getString(R.string.clear));
				break;
			case UNAUTHORIZE:
				// set the header text
				headerText.setText(appResources.getString(R.string.unauthorize_header));
				headerSubText.setText(appResources.getString(R.string.unauthorize_text));
				// change the button names on the screen dynamically based on
				// option selected
				clear.setText(appResources.getString(R.string.unauthorizeButton));

				break;
			}

			clear.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (optionsEnum) {
					case CLEAR_PASSWORDS:
						FileManager.deleteFile(Constants.PWDFILENAME, mContext);
						dismiss();
						// display alert after action is performed
						CustomAlertDialog clearAlert = new CustomAlertDialog(mContext, android.R.style.Theme_Holo_Dialog, index);
						clearAlert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						clearAlert.setCanceledOnTouchOutside(true);
						clearAlert.show();
						break;
					case UNAUTHORIZE:
						if (Constants.ISDEVICE) {
							nymi = NymiBand.getInstance();

						} else {
							nymi = Nymulator.getInstance();
						}
						if (nymi != null && handle != 0) {
							nymi.disconnect(handle);
						}
						FileManager.deleteFile(Constants.PWDFILENAME, mContext);
						FileManager.deleteFile(Constants.PROVFILENAME, mContext);

						dismiss();
						// display alert after action is performed
						CustomAlertDialog unauthorizeAlert = new CustomAlertDialog(mContext, android.R.style.Theme_Holo_Dialog, index);
						unauthorizeAlert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						unauthorizeAlert.setCanceledOnTouchOutside(true);
						unauthorizeAlert.show();
						break;

					}

				}
			});

			setContentView(layout);

		}
	}

	/**
	 * This is a custom alert dialog displayed when unauthorized/clear actions
	 * are taken
	 * 
	 */
	private class CustomAlertDialog extends Dialog {

		private int index;

		/**
		 * 
		 * @param context
		 *            context of the activity on which popup is displayed
		 * @param theme
		 * @param position
		 *            the action to be taken
		 */
		public CustomAlertDialog(Context context, int theme, int position) {
			super(context, theme);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);

			index = position;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			Context context = getContext();

			// create main layout and inflate view in that.
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.custom_alert_dialog, null);

			// initialize dialog ui components
			TextView headerText = (TextView) layout.findViewById(R.id.popupHeader);
			TextView alertText = (TextView) layout.findViewById(R.id.alert_text);
			alertText.setTypeface(SystemUtils.getButtonTypeface(context));
			alertText.setMinHeight((int) (metrics.heightPixels * 0.10));
			alertText.setMaxHeight((int) (metrics.heightPixels * 0.20));
			final Constants.RESET_OPTIONS optionsEnum = Constants.RESET_OPTIONS.values()[index];

			this.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					switch (optionsEnum) {
					case UNAUTHORIZE:
						startActivity(new Intent(HomeActivity.this, SplashScreenActivity.class));
						finish();
						break;

					case CLEAR_PASSWORDS:
						displayView(Constants.MENU_OPTIONS.NEW_PWD);
						break;

					}

				}
			});

			this.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					switch (optionsEnum) {
					case UNAUTHORIZE:
						startActivity(new Intent(HomeActivity.this, SplashScreenActivity.class));
						finish();
						break;

					case CLEAR_PASSWORDS:
						displayView(Constants.MENU_OPTIONS.NEW_PWD);
						break;

					}

				}
			});

			// change ui on the screen dynamically based on option selected
			switch (optionsEnum) {
			case CLEAR_PASSWORDS:
				// set the header text
				headerText.setText(appResources.getString(R.string.clear_pwd));
				alertText.setText(appResources.getString(R.string.alert_text_pwdclear));
				break;
			case UNAUTHORIZE:
				// set the header text
				headerText.setText(appResources.getString(R.string.unauthorize_header));
				alertText.setText(appResources.getString(R.string.alert_text_unauthorize));
				break;

			}

			setContentView(layout);

			// set dialog height and width
			int screenWidth = 0;
			int screenheight = 0;
			if (SystemUtils.isTablet(HomeActivity.this)) {

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					screenWidth = (int) (metrics.widthPixels * 0.30);
					screenheight = LayoutParams.WRAP_CONTENT;

				} else {
					screenWidth = (int) (metrics.widthPixels * 0.30);
					screenheight = LayoutParams.WRAP_CONTENT;

				}

			} else {
				screenWidth = (int) (metrics.widthPixels * 0.80);
				screenheight = LayoutParams.WRAP_CONTENT;

			}
			getWindow().setLayout(screenWidth, screenheight);

		}
	}
}
