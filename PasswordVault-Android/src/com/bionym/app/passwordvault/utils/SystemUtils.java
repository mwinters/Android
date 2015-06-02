package com.bionym.app.passwordvault.utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Various utilities functions for NCA
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */
public class SystemUtils {
	public static final int NCL_PROVISION_MAX_NAME_SIZE = 18;
	protected static ScheduledThreadPoolExecutor executor;

	/**
	 * 
	 * @return true if the calling code is running under the mail looper
	 */
	public static boolean isRunByMainLopper() {
		Looper looper = Looper.getMainLooper();

		if (looper != null) {
			return looper.getThread() == Thread.currentThread();
		}

		return false;
	}

	/**
	 * Run the runnable on the main looper thread
	 * 
	 * @param runnable
	 *            the runnable
	 */
	public static void runOnMainLooper(Runnable runnable) {
		if (isRunByMainLopper()) {
			runnable.run();
		} else {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(runnable);
		}
	}

	/**
	 * Run the runnable on the main looper thread
	 * 
	 * @param runnable
	 *            the runnable
	 * @param delay
	 *            the milliseconds to delay
	 */
	public static void runOnMainLooper(Runnable runnable, long delay) {
		if (delay <= 0) {
			runOnMainLooper(runnable);
		} else {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(runnable, delay);
		}
	}

	/**
	 * Run the runnable on a non-UI thread
	 * 
	 * @param runnable
	 *            the runnable
	 * @return a ScheduledFuture to managing the task
	 */
	public static ScheduledTask runTask(Runnable runnable) {
		return runTaskAfterMillies(runnable, 0);
	}

	/**
	 * Run a task on a non-UI thread after the delay
	 * 
	 * @param runnable
	 *            the runnable
	 * @param delay
	 *            the delay to run the task, it is in milliseconds
	 * @return a ScheduledFuture to managing the task
	 */
	public static ScheduledTask runTaskAfterMillies(Runnable runnable, long delay) {
		if (executor == null) {
			synchronized (SystemUtils.class) {
				if (executor == null) {
					executor = new ScheduledThreadPoolExecutor(2);
				}
			}
		}

		if (delay > 0) {
			return new ScheduledTask(executor.schedule(runnable, delay, TimeUnit.MILLISECONDS), runnable);
		} else {
			executor.execute(runnable);
			return null;
		}
	}

	/**
	 * Vibrate the device for the given time
	 * 
	 * @param context
	 *            the context
	 * @param pattern
	 *            the vibrate pattern
	 * @param repeatIndex
	 *            the index to start the repeat pattern, -1 for no repeat
	 */
	public static void vibrate(Context context, long[] pattern, int repeatIndex) {
		try {
			Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			if (v != null && v.hasVibrator()) {
				v.vibrate(pattern, repeatIndex);
			}
		} catch (Exception e) {
			Log.e("Nymi", "Unable to vibrate!", e);
		}
	}

	/**
	 * Stop vibrate
	 * 
	 * @param context
	 *            the context
	 */
	public static void stopVibrate(Context context) {
		try {
			Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			if (v != null && v.hasVibrator()) {
				v.cancel();
			}
		} catch (Exception e) {
			Log.e("Nymi", "Unable to stop vibrate!", e);
		}
	}

	/**
	 * Create a byte array from a char array
	 * 
	 * @param chars
	 *            the char array
	 * @return a byte array
	 */
	public static byte[] charArrayToByteArray(char[] chars) {
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) chars[i];
		}
		return bytes;
	}

	/**
	 * Create a char array from a byte array
	 * 
	 * @param bytes
	 *            the byte array
	 * @return a char array
	 */
	public static char[] byteArrayToCharArray(byte[] bytes) {
		char[] result = new char[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			result[i] = (char) bytes[i];
		}
		return result;
	}

	/**
	 * 
	 * @param bytes
	 * @return Hex string representation of the bytes
	 */
	public static String toHexString(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if (i > 0) {
				builder.append(' ');
			}
			String b = Integer.toHexString(bytes[i]);
			if (b.length() == 0) {
				builder.append("00");
			} else if (b.length() == 1) {
				builder.append("0");
				builder.append(b);
			} else if (b.length() == 2) {
				builder.append(b);
			} else {
				builder.append(b.substring(b.length() - 2, b.length()));
			}
		}

		return builder.toString();
	}

	/**
	 * @param maxLength
	 *            the maximal length of the device name
	 * @return the device name
	 */
	public static String getNEAName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		String name;
		if (model.startsWith(manufacturer)) {
			name = model;
		} else {
			name = manufacturer + "-" + model;
		}

		if (name.length() == 0) {
			return "Android";
		}

		if (name.length() > NCL_PROVISION_MAX_NAME_SIZE) {
			name = name.substring(0, NCL_PROVISION_MAX_NAME_SIZE);
		}

		char[] chars = name.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] > 0x7E) {
				chars[i] = '.';
			} else if (chars[i] < 0x20) {
				chars[i] = '.';
			}
		}

		if (Character.isLowerCase(chars[0])) {
			chars[0] = Character.toUpperCase(chars[0]);
		}
		return new String(chars);
	}

	/**
	 * 
	 * @param act
	 *            activity object
	 * 
	 * @return if app is running on tablet or phone
	 */
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Hide soft keyboard
	 * 
	 * @param edittext
	 *            edittext for which keyboard should be visible
	 * @param ctx
	 *            context of the activity
	 */
	public static void hideKeyboard(EditText edittext, Context ctx) {
		InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
	}

	/**
	 * Show soft keyboard for the view
	 * 
	 * @param edittext
	 *            edittext for which keyboard should be hidden
	 * @param ctx
	 *            context of the activity
	 */
	public static void showKeyboard(EditText edittext, Context ctx) {
		InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(edittext, InputMethodManager.SHOW_IMPLICIT);
	}

	/**
	 * Get type face
	 * 
	 * @param aContext
	 *            context of the activity
	 * @return typeface
	 */
	public static Typeface getTextViewTypeface(Context aContext) {
		return Typeface.createFromAsset(aContext.getAssets(), "OpenSans-Regular.ttf");
	}

	/**
	 * Get type face
	 * 
	 * @param aContext
	 *            context of the activity
	 * @return typeface
	 */
	public static Typeface getButtonTypeface(Context aContext) {
		return Typeface.createFromAsset(aContext.getAssets(), "Montserrat-Bold.ttf");
	}

}
