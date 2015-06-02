package com.bionym.app.passwordvault.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.utils.SystemUtils;

/**
 * This is a custom alert dialog displayed when view password option is clicked
 * or change/copy/remove password actions are taken
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */
public class ErrorDialog extends Dialog {

	private Context context;
	private String errorMsg;
	private String headerText;

	/**
	 * 
	 * @param ctx
	 *            context of the activity on which popup is displayed
	 * @param theme
	 * @param errorText
	 *            error message to be displayed
	 */
	public ErrorDialog(Context ctx, int theme, String errorText) {
		super(ctx, theme);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		errorMsg = errorText;
		context = ctx;
	}

	/**
	 * 
	 * @param ctx
	 *            context of the activity on which popup is displayed
	 * @param theme
	 * @param errorText
	 *            error message to be displayed
	 * @param hText
	 *            error message header text to be displayed
	 */
	public ErrorDialog(Context ctx, int theme, String errorText, String hText) {
		super(ctx, theme);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		errorMsg = errorText;
		headerText = hText;
		context = ctx;
	}

	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create main layout and inflate view in that.
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.error_dialog, null);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();

		// initialize dialog ui components
		if (headerText != null) {
			TextView header = (TextView) layout.findViewById(R.id.popupHeader);
			header.setText(headerText);
		}
		TextView alertText = (TextView) layout.findViewById(R.id.alert_text);
		alertText.setMinHeight((int) (metrics.heightPixels * 0.10));
		alertText.setMaxHeight((int) (metrics.heightPixels * 0.20));
		Button ok = (Button) layout.findViewById(R.id.okayBtn);
		// set the error text
		alertText.setText(errorMsg);

		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

			}

		});

		setContentView(layout);

		// set dialog height and width
		int screenWidth = 0;
		int screenheight = 0;
		if (SystemUtils.isTablet(context)) {

			if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				screenWidth = (int) (metrics.widthPixels * 0.40);
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
