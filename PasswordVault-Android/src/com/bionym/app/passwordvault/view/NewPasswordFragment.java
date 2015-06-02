package com.bionym.app.passwordvault.view;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.model.Credentials;
import com.bionym.app.passwordvault.utils.Constants;
import com.bionym.app.passwordvault.utils.CryptoUtil;
import com.bionym.app.passwordvault.utils.FileManager;
import com.bionym.app.passwordvault.utils.SystemUtils;

/**
 * 
 * This is a fragment container to display add new password UI
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */
public class NewPasswordFragment extends Fragment {
	private ArrayList<Credentials> credentialsArr;
	private Resources appResources;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.add_new_password, container, false);
		return rootView;
	}

	// All subclasses of Fragment must include a public empty constructor
	public NewPasswordFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (!SystemUtils.isTablet(getActivity())) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		credentialsArr = new ArrayList<Credentials>();
		appResources = getActivity().getResources();

		// load the password text file in memory
		credentialsArr = FileManager.readPasswordFile(getActivity());
		if (credentialsArr.size() == 0) {
			getActivity().findViewById(R.id.firstpassword_layout).setVisibility(View.VISIBLE);
		}
		// initialize ui components
		final Button buttonadd = (Button) getActivity().findViewById(R.id.addBtn);
		buttonadd.setSelected(true);
		buttonadd.setTypeface(SystemUtils.getButtonTypeface(getActivity()));

		final Button buttoncancel = (Button) getActivity().findViewById(R.id.cancelBtn);
		buttoncancel.setTypeface(SystemUtils.getButtonTypeface(getActivity()));

		final EditText url = (EditText) getActivity().findViewById(R.id.url);
		url.setTypeface(SystemUtils.getTextViewTypeface(getActivity()));

		final EditText pwd = (EditText) getActivity().findViewById(R.id.pwd);
		pwd.setTypeface(SystemUtils.getTextViewTypeface(getActivity()));
		pwd.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int keycode, KeyEvent event) {
				if (keycode == EditorInfo.IME_ACTION_NEXT) {
					url.requestFocus();
					return true;
				}
				return false;

			}

		});
		final EditText tag = (EditText) getActivity().findViewById(R.id.tag);
		tag.setTypeface(SystemUtils.getTextViewTypeface(getActivity()));
		tag.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int keycode, KeyEvent event) {
				if (keycode == EditorInfo.IME_ACTION_NEXT) {
					pwd.requestFocus();
					return true;
				}
				return false;

			}

		});
		setTitle(appResources.getString(R.string.new_pwd));
		// set focus on edit box and display keyboard
		tag.requestFocus();
		SystemUtils.showKeyboard(tag, getActivity());

		buttoncancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// go back to the list of passwords view if password list
				// exists
				CredentailsViewFragment fragment = new CredentailsViewFragment(Constants.MENU_OPTIONS.ALL_PWD.ordinal());
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
				setTitle(appResources.getString(R.string.all));
				SystemUtils.hideKeyboard(tag, getActivity());
			}
		});

		buttonadd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Credentials obj = new Credentials();
				// if tag is not empty
				if (!tag.getText().toString().isEmpty()) {
					for (int i = 0; i < credentialsArr.size(); i++) {
						// if tag entered already exists
						if (tag.getText().toString().equalsIgnoreCase(credentialsArr.get(i).getTag())) {
							ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, appResources
									.getString(R.string.error_tagexists1)
									+ " "
									+ tag.getText().toString()
									+ " "
									+ appResources.getString(R.string.error_tagexists2));
							alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
							alert.setCanceledOnTouchOutside(true);
							alert.show();
							return;
						}

					}
					// if tag entered length is more than 50 characters
					if (tag.getText().toString().length() > Constants.CREDENTIAL_LENGTH) {
						ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, appResources
								.getString(R.string.error_taglengthlimit));
						alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						alert.setCanceledOnTouchOutside(true);
						alert.show();
						return;
					}

				} else {
					ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, appResources.getString(R.string.error_emptytag));
					alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					alert.setCanceledOnTouchOutside(true);
					alert.show();
					return;
				}
				obj.setTag(tag.getText().toString());
				// if password is not empty
				if (!pwd.getText().toString().isEmpty()) {
					// if password entered length is more than 50 characters
					if (pwd.getText().toString().length() > Constants.CREDENTIAL_LENGTH) {
						ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, appResources
								.getString(R.string.error_pwdlengthlimit));
						alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						alert.setCanceledOnTouchOutside(true);
						alert.show();
						return;
					}
					// encrpyt user input password
					String encryptedPwd = CryptoUtil.getCryptoUtil().encrypt(pwd.getText().toString());
					obj.setPassword(encryptedPwd);
					// set website url
					if (!url.getText().toString().isEmpty()) {
						obj.setWebsite(url.getText().toString());
					}

					credentialsArr.add(obj);

					// write credentials in password file
					FileManager.writePasswordFile(credentialsArr, getActivity());
					CredentailsViewFragment fragment = new CredentailsViewFragment(Constants.MENU_OPTIONS.ALL_PWD.ordinal());
					FragmentManager fragmentManager = getFragmentManager();
					fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
					setTitle(appResources.getString(R.string.all));

				} else {
					ErrorDialog alert = new ErrorDialog(getActivity(), android.R.style.Theme_Holo_Dialog, appResources.getString(R.string.error_emptypwd));
					alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					alert.setCanceledOnTouchOutside(true);
					alert.show();
					return;
				}
				SystemUtils.hideKeyboard(tag, getActivity());
			}
		});
	}

	/**
	 * Set title in action bar
	 * 
	 * @param title
	 *            title to be set on the action bar
	 */
	private void setTitle(CharSequence title) {
		getActivity().getActionBar().setTitle(title);
	}
}
