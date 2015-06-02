package com.bionym.app.passwordvault.component;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * This is a custom TextView component. SetType method is overridden as to set
 * the custom font as OpenSans-Regular for all texts in textViews
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 */
public class CustomTextView extends TextView {

	/**
	 * 
	 * @param context
	 *            Context of the activity
	 * @param attrs
	 * @param defStyle
	 */
	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setType(context);
	}

	/**
	 * 
	 * @param context
	 *            Context of the activity
	 * @param attrs
	 */
	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setType(context);
	}

	/**
	 * 
	 * @param context
	 *            Context of the activity
	 */
	public CustomTextView(Context context) {
		super(context);
		setType(context);
	}

	/**
	 * This method sets the typeface for each textview as OpenSans-Regular
	 * 
	 * @param context
	 *            Context of the activity
	 */
	private void setType(Context context) {
		this.setTypeface(Typeface.createFromAsset(context.getAssets(), "OpenSans-Regular.ttf"));

	}
}