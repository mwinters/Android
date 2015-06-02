package com.bionym.app.passwordvault.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.adapter.CredentialsAdapter;
import com.bionym.app.passwordvault.model.Credentials;
import com.bionym.app.passwordvault.utils.Constants;
import com.bionym.app.passwordvault.utils.CryptoUtil;
import com.bionym.app.passwordvault.utils.FileManager;
import com.bionym.app.passwordvault.utils.SystemUtils;

/**
 * 
 * This UI Fragment displays the list of credentials
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 * 
 */
@SuppressLint("InflateParams")
public class CredentailsViewFragment extends Fragment {
	private final String LOG_TAG = CredentailsViewFragment.class.getName();

	private ListView listView;
	private CredentialsAdapter customAdapter;
	private ArrayList<Credentials> credentialsArr;
	private ArrayList<Credentials> credArr;
	private int list_mode;
	private Resources appResources;

	/**
	 * @param mode
	 *            starred passwords to be displayed or all passwords
	 */
	public CredentailsViewFragment(int mode) {
		list_mode = mode;
	}

	/**
	 * All subclasses of Fragment must include a public empty constructor
	 */
	public CredentailsViewFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.activity_credentialslist, container, false);
		return rootView;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onStart();

		if (!SystemUtils.isTablet(getActivity())) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		credArr = new ArrayList<Credentials>();
		appResources = getActivity().getResources();

		// initialize ui components
		listView = (ListView) getActivity().findViewById(R.id.credlist);
		ViewGroup footer = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.credentials_list_footer, listView, false);
		listView.addFooterView(footer);
		TextView headertext = (TextView) footer.findViewById(R.id.footer);
		headertext.setTypeface(SystemUtils.getButtonTypeface(getActivity()));

		Constants.MENU_OPTIONS optionsEnum = Constants.MENU_OPTIONS.values()[list_mode];

		if (optionsEnum == Constants.MENU_OPTIONS.STARRED_PWD) {
			setTitle(appResources.getString(R.string.starred));
		} else {
			setTitle(appResources.getString(R.string.all));

		}

		// get the credentials list
		credentialsArr = FileManager.readPasswordFile(getActivity());

		// if only starred passwords are to be listed
		if (optionsEnum == Constants.MENU_OPTIONS.STARRED_PWD) {
			for (int i = 0; i < credentialsArr.size(); i++) {
				if (credentialsArr.get(i).isFavourite()) {
					credArr.add(credentialsArr.get(i));
				}
			}
		} else {
			// copy all credentials in starred list
			for (int i = 0; i < credentialsArr.size(); i++) {
				credArr.add(credentialsArr.get(i));
			}
		}

		// show list of credentials in ascending alphabetical order
		Collections.sort(credArr, new Comparator<Credentials>() {
			public int compare(Credentials c1, Credentials c2) {
				return c1.getTag().compareTo(c2.getTag());
			}
		});
		// get data from the file by the CredentialsAdapter
		customAdapter = new CredentialsAdapter(getActivity(), R.layout.itemlistrow, credArr);
		// Assign adapter to ListView
		listView.setAdapter(customAdapter);

		// ListView Item Click Listener
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// add new footer is clicked
				if (position == credArr.size()) {

					Fragment fragment = new NewPasswordFragment();
					FragmentManager fragmentManager = getFragmentManager();
					fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
					setTitle(appResources.getString(R.string.new_pwd));

				} else {
					// ListView Clicked item value
					Credentials itemValue = (Credentials) listView.getItemAtPosition(position);
					// if website url is available then show launch Website
					// option else not
					if (itemValue.getWebsite() == null) {
						showPopUp(itemValue, false);
					} else {
						showPopUp(itemValue, true);

					}
				}
			}

		});

	}

	/**
	 * Copy EditCopy text to the ClipBoard
	 * 
	 * @param text
	 *            text which is to be copied to the clipboard
	 */
	private void copyToClipBoard(String text) {

		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		android.content.ClipData clip = android.content.ClipData.newPlainText(appResources.getString(R.string.copy), text);
		clipboard.setPrimaryClip(clip);
	}

	/**
	 * This method displays the list of options which the user can perform on a
	 * particular credential
	 * 
	 * @param selectedPasswordObj
	 *            The selected credential from the list view
	 */
	private void showPopUp(final Credentials selectedPasswordObj, final boolean isUrlAvailable) {

		// create a alert dialog builder
		final AlertDialog.Builder optionsPopup = new AlertDialog.Builder(getActivity());

		// inflate the popup view in the dialog builder
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View PopupLayout = inflater.inflate(R.layout.list_dialog, null);
		optionsPopup.setView(PopupLayout);

		// display the options alert dialog
		final AlertDialog optionsDialog = optionsPopup.create();
		optionsDialog.show();
		optionsDialog.setCanceledOnTouchOutside(true);

		// initialize the popup ui components
		ListView optionsListview = (ListView) PopupLayout.findViewById(R.id.menu);
		// add a header to the options list view
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.dialogheader, optionsListview, false);
		optionsListview.addHeaderView(header);
		// remove divider
		optionsListview.setDivider(new ColorDrawable(appResources.getColor(android.R.color.transparent)));
		TextView headertext = (TextView) header.findViewById(R.id.header);
		headertext.setText(selectedPasswordObj.getTag());
		headertext.setTypeface(SystemUtils.getButtonTypeface(getActivity()));

		// populate the menu options list and set the adapter
		optionsListview.setAdapter(prepareListData(selectedPasswordObj.getTag(), isUrlAvailable));

		optionsListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Constants.OPTIONS optionsEnum = Constants.OPTIONS.values()[position];
				switch (optionsEnum) {
				case COPY:
				case CHANGE:
				case REMOVE:
					ListDialog dialog = new ListDialog(getActivity(), android.R.style.Theme_Holo_Dialog, selectedPasswordObj, position);
					dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
					break;
				case VIEW:
					ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, CryptoUtil.getCryptoUtil().decrypt(
							selectedPasswordObj.getPassword()), selectedPasswordObj.getTag());
					alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					alert.setCanceledOnTouchOutside(true);
					alert.show();
					break;
				case LAUNCH_WEBSITE:
					// launch URL will act as remove if url is not available
					if (isUrlAvailable) {
						String url = selectedPasswordObj.getWebsite();
						// copy to clipboard
						copyToClipBoard(CryptoUtil.getCryptoUtil().decrypt(selectedPasswordObj.getPassword()));
						if (!url.startsWith("https://") && !url.startsWith("http://")) {

							url = "http://" + url;
						}
						Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						startActivity(openUrlIntent);
					} else {
						ListDialog removeDialog = new ListDialog(getActivity(), android.R.style.Theme_Holo_Dialog, selectedPasswordObj, position);
						removeDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						removeDialog.setCanceledOnTouchOutside(true);
						removeDialog.show();
					}
					break;
				default:
					Log.i(LOG_TAG, "Default Selected");
				}
				optionsDialog.cancel();

			}

		});
	}

	/**
	 * This method creates menu option list to be displayed when user selects a
	 * particular credential. This list with contain an icon and below options:
	 * <ul>
	 * <li>Copy to Clipboard</li>
	 * <li>View Password</li>
	 * <li>Change Password</li>
	 * <li>Remove Password</li>
	 * <li>Launch Website</li>
	 * </ul>
	 * 
	 * @param headerName
	 *            name of the credential selected
	 * @return Adapter containing actions and their respective images
	 */
	private SimpleAdapter prepareListData(String headerName, boolean isUrlAvailable) {
		// Each row in the list stores option name and icon
		List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
		String[] menuitems = null;
		int[] icons = null;
		if (isUrlAvailable) {
			// Array of integers points to images stored in /res/drawable
			icons = new int[] { R.drawable.eyeoff, R.drawable.eyeon, R.drawable.passwordedit, R.drawable.passwordgo, R.drawable.passworddelete };

			// Array of strings to store options
			menuitems = new String[] { appResources.getString(R.string.copytoclipboard), appResources.getString(R.string.view_pwd),
					appResources.getString(R.string.change_pwd), appResources.getString(R.string.launchwebsite_option),
					appResources.getString(R.string.remove_option) };

		} else {
			// Array of integers points to images stored in /res/drawable
			icons = new int[] { R.drawable.eyeoff, R.drawable.eyeon, R.drawable.passwordedit, R.drawable.passworddelete };

			// Array of strings to store options
			menuitems = new String[] { appResources.getString(R.string.copytoclipboard), appResources.getString(R.string.view_pwd),
					appResources.getString(R.string.change_pwd), appResources.getString(R.string.remove_option) };

		}
		for (int i = 0; i < menuitems.length; i++) {
			HashMap<String, String> hm = new HashMap<String, String>();
			// dynamically add credential tag in the options
			if (i == menuitems.length - 1) {
				hm.put(appResources.getString(R.string.menuname), new StringBuilder(menuitems[i]).append(" ").append(headerName).toString());
			} else {
				hm.put(appResources.getString(R.string.menuname), menuitems[i]);
			}
			hm.put(appResources.getString(R.string.icon), Integer.toString(icons[i]));
			aList.add(hm);
		}
		// Keys used in Hashmap
		String[] from = { appResources.getString(R.string.icon), appResources.getString(R.string.menuname) };

		// Ids of views in listview_layout
		int[] to = { R.id.starImg, R.id.pwdTag };

		SimpleAdapter sd = new SimpleAdapter(getActivity(), aList, R.layout.itemlistrow, from, to);

		return sd;
	}

	/**
	 * This is a custom dialog for displaying copy/change/remove passwords ui
	 * and take appropriate actions
	 * 
	 */
	private class ListDialog extends Dialog {

		private Credentials selectedCredObj;
		private int index;
		private Context context;

		/**
		 * 
		 * @param ctx
		 *            context of the activity on which popup is displayed
		 * @param theme
		 * @param credObj
		 *            selected credentials object from the list displayed
		 * @param position
		 *            the action to be taken on the selected credential
		 */
		public ListDialog(Context ctx, int theme, Credentials credObj, int position) {
			super(ctx, theme);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);

			selectedCredObj = credObj;
			index = position;
			context = ctx;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// create custom dialog main layout and inflate view in that.
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.custom_dialog, null);

			DisplayMetrics metrics = appResources.getDisplayMetrics();

			final EditText url = (EditText) layout.findViewById(R.id.url);
			url.setTypeface(SystemUtils.getTextViewTypeface(context));

			// initialize dialog ui components
			final EditText passwordText = (EditText) layout.findViewById(R.id.pwd);
			passwordText.setTypeface(SystemUtils.getTextViewTypeface(context));
			passwordText.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView arg0, int keycode, KeyEvent event) {
					if (keycode == EditorInfo.IME_ACTION_NEXT) {
						url.requestFocus();
						return true;
					}
					return false;

				}

			});
			final TextView headerSubText = (TextView) layout.findViewById(R.id.remove_header_text);
			TextView headerText = (TextView) layout.findViewById(R.id.popupHeader);
			headerSubText.setMinHeight((int) (metrics.heightPixels * 0.10));
			headerSubText.setMaxHeight((int) (metrics.heightPixels * 0.20));
			final Button cancel = (Button) layout.findViewById(R.id.cancelBtn);
			cancel.setTypeface(SystemUtils.getButtonTypeface(context));

			final Button save = (Button) layout.findViewById(R.id.saveBtn);
			save.setTypeface(SystemUtils.getButtonTypeface(context));
			save.setSelected(true);

			// set the selected credentials tag name as header text
			headerText.setText(selectedCredObj.getTag());

			cancel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();

				}
			});
			final Constants.OPTIONS optionsEnum = Constants.OPTIONS.values()[index];

			// change ui on the screen dynamically based on option selected
			switch (optionsEnum) {
			case COPY:
				// show the password on the screen
				headerSubText.setText(CryptoUtil.getCryptoUtil().decrypt(selectedCredObj.getPassword()));
				headerSubText.setVisibility(View.VISIBLE);

				// change the button names on the screen dynamically based on
				// option selected
				cancel.setText(appResources.getString(R.string.okay));
				save.setText(appResources.getString(R.string.copy));

				break;

			case CHANGE:
				// show the enter password edit box on the screen
				passwordText.setVisibility(View.VISIBLE);
				url.setVisibility(View.VISIBLE);
				passwordText.requestFocus();
				if (selectedCredObj.getWebsite() != null) {
					url.setText(selectedCredObj.getWebsite());
				}
				this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

				break;

			case REMOVE:
			case LAUNCH_WEBSITE:
				// make remove confirmation text visible
				headerSubText.setVisibility(View.VISIBLE);
				// change the button names on the screen dynamically based on
				// option selected
				save.setText(appResources.getString(R.string.remove));
				break;
			default:
				Log.i(LOG_TAG, "Default Selected");
			}

			save.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (optionsEnum) {
					case COPY:
						// copy password
						copyToClipBoard(CryptoUtil.getCryptoUtil().decrypt(selectedCredObj.getPassword()));
						dismiss();
						// display alert after action is performed
						CustomAlertDialog copyAlert = new CustomAlertDialog(getActivity(), android.R.style.Theme_Holo_Dialog, selectedCredObj, index);
						copyAlert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						copyAlert.setCanceledOnTouchOutside(true);
						copyAlert.show();
						break;
					case CHANGE:
						if (!passwordText.getText().toString().isEmpty()) {

							if (passwordText.getText().toString().length() > Constants.CREDENTIAL_LENGTH) {
								ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, appResources
										.getString(R.string.error_pwdlengthlimit));
								alert.setCanceledOnTouchOutside(true);
								alert.show();
								return;
							}
							// remove the selected credential from credentials
							// list
							credentialsArr.remove(selectedCredObj);
							credArr.remove(selectedCredObj);

							// encrypt user entered password
							String encryptedPwd = CryptoUtil.getCryptoUtil().encrypt(passwordText.getText().toString().trim());
							Credentials obj = new Credentials();
							obj.setPassword(encryptedPwd);
							// set website url
							if (!url.getText().toString().isEmpty()) {
								obj.setWebsite(url.getText().toString());
							} else {
								obj.setWebsite(selectedCredObj.getWebsite());

							}
							obj.setTag(selectedCredObj.getTag());
							obj.setFavourite(selectedCredObj.isFavourite());

							// add updated credential to credentials list
							credentialsArr.add(obj);
							credArr.add(obj);

							// show list of credentials in ascending
							// alphabetical order
							Collections.sort(credArr, new Comparator<Credentials>() {
								public int compare(Credentials c1, Credentials c2) {
									return c1.getTag().compareTo(c2.getTag());
								}
							});
							customAdapter.notifyDataSetChanged();

							// write new credential to file
							FileManager.writePasswordFile(credentialsArr, context);

							dismiss();
							SystemUtils.hideKeyboard(passwordText, context);

							// display alert after action is performed
							CustomAlertDialog changeAlert = new CustomAlertDialog(getActivity(), android.R.style.Theme_Holo_Dialog, selectedCredObj, index);
							changeAlert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
							changeAlert.setCanceledOnTouchOutside(true);
							changeAlert.show();
						} else {
							ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, appResources
									.getString(R.string.error_emptypwd));
							alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
							alert.setCanceledOnTouchOutside(true);
							alert.show();
							return;
						}

						break;
					case REMOVE:
					case LAUNCH_WEBSITE:

						// remove the selected credential from credentials list
						credentialsArr.remove(selectedCredObj);
						credArr.remove(selectedCredObj);
						customAdapter.notifyDataSetChanged();
						// write new credential to file
						FileManager.writePasswordFile(credentialsArr, context);

						dismiss();
						// display alert after action is performed
						CustomAlertDialog removeAlert = new CustomAlertDialog(getActivity(), android.R.style.Theme_Holo_Dialog, selectedCredObj, index);
						removeAlert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						removeAlert.setCanceledOnTouchOutside(true);
						removeAlert.show();
						break;
					default:
						Log.i(LOG_TAG, "Default Selected");

					}

				}
			});

			// set dialog height and width

			int screenWidth = (int) (metrics.widthPixels * 0.80);
			setContentView(layout);

			if (!SystemUtils.isTablet(getActivity())) {
				getWindow().setLayout(screenWidth, LayoutParams.WRAP_CONTENT);
			} else {
				getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			}

		}
	}

	/**
	 * This is a custom alert dialog displayed when view password option is
	 * clicked or change/copy/remove password actions are taken
	 * 
	 */
	private class CustomAlertDialog extends Dialog {

		private Credentials selectedCredObj;
		private int index;
		private Context context;

		/**
		 * 
		 * @param ctx
		 *            context of the activity on which popup is displayed
		 * @param theme
		 * @param credObj
		 *            selected credentials object from the list displayed
		 * @param position
		 *            the action to be taken on the selected credential
		 */
		public CustomAlertDialog(Context ctx, int theme, Credentials credObj, int position) {
			super(ctx, theme);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);

			selectedCredObj = credObj;
			index = position;
			context = ctx;

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			DisplayMetrics metrics = appResources.getDisplayMetrics();

			// create main layout and inflate view in that.
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.custom_alert_dialog, null);

			// initialize dialog ui components
			TextView headerText = (TextView) layout.findViewById(R.id.popupHeader);
			TextView alertText = (TextView) layout.findViewById(R.id.alert_text);
			alertText.setMinHeight((int) (metrics.heightPixels * 0.20));
			alertText.setMaxHeight(LayoutParams.WRAP_CONTENT);
			// set the selected credentials tag name as header text
			headerText.setText(selectedCredObj.getTag());
			final Constants.OPTIONS optionsEnum = Constants.OPTIONS.values()[index];

			// change ui on the screen dynamically based on option selected
			switch (optionsEnum) {
			case COPY:
				alertText.setText(appResources.getString(R.string.alert_text_copied));
				alertText.setTypeface(SystemUtils.getButtonTypeface(context));
				break;
			case CHANGE:
				alertText.setText(appResources.getString(R.string.alert_text_pwdchanged));
				alertText.setTypeface(SystemUtils.getButtonTypeface(context));
				break;
			case REMOVE:
				alertText.setText(appResources.getString(R.string.alert_text_pwdremoved));
				alertText.setTypeface(SystemUtils.getButtonTypeface(context));
				break;
			default:
				Log.i(LOG_TAG, "Default Selected");
				break;

			}
			setContentView(layout);

			// set dialog height and width
			int screenWidth = 0;
			int screenheight = 0;
			if (SystemUtils.isTablet(getActivity())) {

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					screenWidth = (int) (metrics.widthPixels * 0.30);
					screenheight = LayoutParams.WRAP_CONTENT;

				} else {
					screenWidth = (int) (metrics.widthPixels * 0.20);
					screenheight = LayoutParams.WRAP_CONTENT;

				}

			} else {
				screenWidth = (int) (metrics.widthPixels * 0.80);
				screenheight = LayoutParams.WRAP_CONTENT;

			}
			getWindow().setLayout(screenWidth, screenheight);

		}
	}

	/**
	 * Set title in action bar
	 * 
	 * @param title
	 *            title to be set on the action bar
	 */
	public void setTitle(CharSequence title) {
		/*
		 * final TextView statusbar = (TextView)
		 * getActivity().findViewById(R.id.popupHeader);
		 * statusbar.setText(title);
		 * statusbar.setTypeface(SystemUtils.getButtonTypeface(getActivity()));
		 */
		getActivity().getActionBar().setTitle(title);

	}

}
