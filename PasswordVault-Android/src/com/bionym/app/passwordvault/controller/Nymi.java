package com.bionym.app.passwordvault.controller;

import android.content.Context;

import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclProvision;

public abstract class Nymi {
	public static final int BLE_STATUS_DISABLED_AIRPLANE_MODE = 2;
	public static final int BLE_STATUS_OK = 0;
	public static final int NCL_ECG_SAMPLES_PER_EVENT = 5;
	public static final int BLE_STATUS_DISABLED = 1;
	public static final int BLE_STATUS_NO_BLE = 3;


	protected static final int CLOSE_BLE_CONNECTION_DELAY_MILLIES = 500; // time
																			// to
																			// wait
																			// after
																			// we
																			// close
																			// Nymi
																			// connection
																			// to
																			// close
																			// BLE
	protected static final int BLE_ENABLE_DISABLE_DELAY_MILLIES = 300; // time
																		// to
																		// wait
																		// after
																		// we
																		// enable/disable
	protected static final long CONNECTION_TIME_TO_LIVE_MILLIES = 900000; // Nymi
																			// connection's
																			// time
																			// to
																			// live

	protected abstract void startSession(Context context, String name, NclCallback sessionCallback);
	public abstract void endSession(final int nymiHandle);
	public abstract void removeAllCallbacks();
	public abstract boolean addCallback(NclCallback callback);
	public abstract boolean removeCallback(NclCallback callback);
	public abstract boolean startDiscovery();
	public abstract boolean startFinding(NclProvision provision);
	public abstract boolean startFinding(NclProvision[] provisions, boolean detect);
	public abstract boolean stopScan();
	public abstract boolean validate(int nymiHandle);
	public abstract int enableBle();
	public abstract boolean agree(int nymiHandle);
	public abstract boolean provision(int nymiHandle, boolean strong);
	public abstract boolean disconnect(int nymiHandle);
}
