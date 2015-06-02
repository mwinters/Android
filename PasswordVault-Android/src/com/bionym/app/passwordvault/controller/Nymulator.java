package com.bionym.app.passwordvault.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.bionym.app.passwordvault.utils.Constants;
import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventDisconnection;
import com.bionym.ncl.NclEventError;
import com.bionym.ncl.NclEventValidation;
import com.bionym.ncl.NclMode;
import com.bionym.ncl.NclProvision;

/**
 * A high level NCL library including functionalities for managing BLE. It
 * executes event callbacks in a single thread. To better support
 * object-orientation and injection, Ncl's native methods are wrapped in
 * instance methods..
 * 
 * * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */
@SuppressLint("UseSparseArrays")
public class Nymulator extends Nymi {
	protected static final String LOG_TAG = Nymulator.class.getName();
	protected static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	protected static HashMap<Integer, Long> connectedHandles = new HashMap<Integer, Long>();

	// Indicating whether BLE was enabled or disabled before scanning
	protected static int originalBleStatus = BLE_STATUS_OK;

	protected static Nymulator instance;

	protected Context mContext;
	protected int nymiHandle;
	protected int errorCount = 0; // the number of consecutive errors
	protected NclCallbacks nclCallbacks = new NclCallbacks();

	/**
	 * Start Ncl session, it will initialize the NCL library if necessary
	 * 
	 * @param context
	 *            the context
	 * @param name
	 *            name of the device
	 * @param initializeCallback
	 *            callback for the initialization process, removed after
	 *            initialization
	 * @param sessionCallback
	 *            callback for the entire NCL session
	 * @return instance of Nymi
	 */
	public static synchronized Nymulator startNclSession(Context context, String name, NclCallback sessionCallback) {
		clearConnectedHandle(false);
		if (instance == null) { // Create the NclCallback object
			Nymulator nymi = new Nymulator();
			nymi.startSession(context, name, sessionCallback);
			instance = nymi;
		} else if (instance.getClass().equals(Nymulator.class)) {
			instance.removeAllCallbacks();
			if (sessionCallback != null) {
				instance.addCallback(sessionCallback);
			}
		} else {
			instance.removeAllCallbacks();
			instance = null;
			return startNclSession(context, name, sessionCallback);
		}
		return instance;
	}

	/**
	 * 
	 * @return the Nymi instance
	 */
	public static Nymulator getInstance() {
		return instance;
	}

	/**
	 * Start session
	 * 
	 * @param context
	 *            the context
	 * @param name
	 *            name to be used to identify provision
	 * @param sessionCallback
	 *            the session callback
	 */
	protected void startSession(Context context, String name, NclCallback sessionCallback) {
		mContext = context;
		Ncl.setIpAndPort(Constants.IP, 9089);
		boolean result = Ncl.init(nclCallbacks, null, name, NclMode.NCL_MODE_DEV, context);
		if (result) {
			Log.e(LOG_TAG, "Ncl initialization Success!");
		} else {
			Log.e(LOG_TAG, "Ncl initialization failed!");

		}

		if (sessionCallback != null) {
			addCallback(sessionCallback);
		}
	}

	/**
	 * End current Nymi session
	 */
	public void endSession(final int nymiHandle) {
		Log.d(LOG_TAG, "End NCL session ...");

		try {
			Ncl.stopScan();
		} catch (Throwable e) {
			Log.e(LOG_TAG, "Exception stoping scan", e);
		}

		if (nymiHandle >= 0) {
			Log.d(LOG_TAG, "Disconnecting Nymi: " + nymiHandle);
			try {
				disconnect(nymiHandle);
			} catch (Throwable e) {
				Log.e(LOG_TAG, "Failed to disconnect after unlock!", e);
			}
		}

		clearConnectedHandle(true);
		removeAllCallbacks();

		executor.schedule(new Runnable() { // wait for the connection to be
											// closed
					@Override
					public void run() {
						try { // clear scanned nymis
							Ncl.clearScannedNymis();
						} catch (Throwable e) {
							Log.e(LOG_TAG, "Failed to clear scanned nymis after unlock!", e);
						}

						disableBle(); // force connection to close
						executor.schedule(new Runnable() { // wait for the BLE
															// to be disable
															// before we restore
															// the state
									@Override
									public void run() {
										restoreBLEState();
									}
								}, BLE_ENABLE_DISABLE_DELAY_MILLIES, TimeUnit.MILLISECONDS);
					}
				}, CLOSE_BLE_CONNECTION_DELAY_MILLIES, TimeUnit.MILLISECONDS);
	}

	/**
	 * Remove all callbacks
	 */
	public void removeAllCallbacks() {
		synchronized (nclCallbacks) {
			nclCallbacks.clear();
		}
	}

	/**
	 * Add the given callback if it has not yet been added
	 * 
	 * @param callback
	 *            the callback
	 * @return true if the callback is added
	 */
	public boolean addCallback(NclCallback callback) {
		synchronized (nclCallbacks) {
			if (!nclCallbacks.contains(callback)) {
				nclCallbacks.add(callback);
				return true;
			}
		}

		return false;
	}

	/**
	 * Remove the callback
	 * 
	 * @param callback
	 *            the callback to remove
	 * @return true if the callback has been removed
	 */
	public boolean removeCallback(NclCallback callback) {
		synchronized (nclCallbacks) {
			return nclCallbacks.remove(callback);
		}
	}

	/**
	 * 
	 * @param callback
	 * @return true if callback has been added
	 */
	public boolean hasCallback(NclCallback callback) {
		synchronized (nclCallbacks) {
			return nclCallbacks.contains(callback);
		}
	}

	/**
	 * Wrapper for {@link Ncl.startDiscovery()}.
	 * 
	 * @return return value of {@link Ncl.startDiscovery()}
	 */
	public boolean startDiscovery() {
		return Ncl.startDiscovery();
	}

	/**
	 * Wrapper for {@link Ncl.startFinding()}.
	 * 
	 * @return return value of {@link Ncl.startFinding()}
	 */
	public boolean startFinding(NclProvision provision) {
		return Ncl.startFinding(new NclProvision[] { provision }, false);
	}

	/**
	 * Wrapper for {@link Ncl.startFinding()}.
	 * 
	 * @return return value of {@link Ncl.startFinding()}
	 */
	public boolean startFinding(NclProvision[] provisions, boolean detect) {
		return Ncl.startFinding(provisions, detect);
	}

	/**
	 * Wrapper for {@link Ncl.stopScan()}.
	 * 
	 * @return return value of {@link Ncl.stopScan()}
	 */
	public boolean stopScan() {
		return Ncl.stopScan();
	}

	/**
	 * Wrapper for {@link Ncl.agree(int)}.
	 * 
	 * @return return value of {@link Ncl.agree(int)}
	 */
	public boolean agree(int nymiHandle) {
		return Ncl.agree(nymiHandle);
	}

	/**
	 * Wrapper for {@link Ncl.provision(int, boolean)}.
	 * 
	 * @return return value of {@link Ncl.provision(int, boolean)}
	 */
	public boolean provision(int nymiHandle, boolean strong) {
		return Ncl.provision(nymiHandle, strong);
	}

	/**
	 * Wrapper for {@link Ncl.validate(int)}.
	 * 
	 * @return return value of {@link Ncl.validate(int)}
	 */
	public boolean validate(int nymiHandle) {
		return Ncl.validate(nymiHandle);
	}

	/**
	 * Wrapper for {@link Ncl.disconnect(int)}.
	 * 
	 * @return return value of {@link Ncl.disconnect(int)}
	 */
	public boolean disconnect(int nymiHandle) {
		if (nymiHandle >= 0) {
			synchronized (connectedHandles) {
				connectedHandles.remove(nymiHandle);
			}
			return Ncl.disconnect(nymiHandle);
		}
		return false;
	}

	/**
	 * 
	 * @param nymiHandle
	 * @return true if the nymiHandle is connected
	 */
	public boolean isConnected(int nymiHandle) {
		if (nymiHandle >= 0) {
			synchronized (connectedHandles) {
				Long timestamp = connectedHandles.get(nymiHandle);
				if (timestamp == null) {
					return false;
				} else {
					return System.currentTimeMillis() - timestamp < CONNECTION_TIME_TO_LIVE_MILLIES;
				}
			}
		}
		return false;
	}

	/**
	 * Wrapper for {@link Ncl.notify(int, boolean)}.
	 * 
	 * @return return value of {@link Ncl.notify(int, boolean)}
	 */
	public boolean notify(int nymiHandle, boolean good) {
		return Ncl.notify(nymiHandle, good);
	}

	/**
	 * Wrapper for {@link Ncl.startEcgStream(int)}.
	 * 
	 * @return return value of {@link Ncl.startEcgStream(int)}
	 */
	public boolean startEcgStream(int nymiHandle) {
		return Ncl.startEcgStream(nymiHandle);
	}

	/**
	 * Wrapper for {@link Ncl.stopEcgStream(int)}.
	 * 
	 * @return return value of {@link Ncl.stopEcgStream(int)}
	 */
	public boolean stopEcgStream(int nymiHandle) {
		return Ncl.stopEcgStream(nymiHandle);
	}

	/**
	 * Wrapper for {@link Ncl.prg(int)}.
	 * 
	 * @return return value of {@link Ncl.prg(int)}
	 */
	public boolean prg(int nymiHandle) {
		return Ncl.prg(nymiHandle);
	}

	/**
	 * Wrapper for {@link Ncl.getFirmwareVersion(int)}.
	 * 
	 * @return return value of {@link Ncl.getFirmwareVersion(int)}
	 */
	public boolean getFirmwareVersion(int nymiHandle) {
		return Ncl.getFirmwareVersion(nymiHandle);
	}

	/**
	 * 
	 * @return return if the device is in airplane mode. If this is the case
	 *         Bluetooth cannot be enabled
	 */
	@SuppressLint("NewApi")
	protected boolean isAirplaneModeOn() {
		return Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	}

	/**
	 * Enable BLE
	 * 
	 * @return the BLE status
	 */
	public int enableBle() {
		return BLE_STATUS_OK;
	}

	/**
	 * Disable BLE
	 * 
	 * @return the BLE status
	 */
	public int disableBle() {
		return BLE_STATUS_OK;
	}

	/**
	 * Clear connected Nymi handles
	 * 
	 * @param closeHandles
	 *            true to disconnect the handles
	 */
	protected static void clearConnectedHandle(boolean closeHandles) {
		synchronized (connectedHandles) {
			for (Entry<Integer, Long> entry : connectedHandles.entrySet()) {
				if (System.currentTimeMillis() - entry.getValue() < CONNECTION_TIME_TO_LIVE_MILLIES) {
					try {
						instance.disconnect(entry.getKey());
					} catch (Exception e) {
						Log.d(LOG_TAG, "Error closing handle: " + entry.getKey());
					}
				}
			}
			connectedHandles.clear();
		}
	}

	/**
	 * Restore BLE to whatever state it was before starting the session
	 */
	protected void restoreBLEState() {
		if (originalBleStatus == BLE_STATUS_DISABLED) { // BLE was disabled,
														// disable it again
			Log.d(LOG_TAG, "Restore BLE state, disable BLE");
			disableBle();
		} else if (originalBleStatus == BLE_STATUS_OK) { // BLE was enabled,
															// enable it again
			Log.d(LOG_TAG, "Restore BLE state, enable BLE");
			enableBle();
		}
	}

	/**
	 * Process the handle
	 * 
	 * @param handle
	 *            the handle
	 * @param connected
	 *            true if the handle is connected, false if it is disconnected
	 * @param nCallbacks
	 *            the number of current callbacks
	 */
	protected void processConnection(int handle, boolean connected, int nCallbacks) {
		if (handle >= 0) {
			if (connected) {
				nymiHandle = handle;
				if (nCallbacks > 0) { // somebody is listening
					synchronized (connectedHandles) {
						connectedHandles.put(handle, System.currentTimeMillis());
					}
				} else { // nobody is listening, must be a racing, disconnect
							// the connection
					disconnect(handle);
					return;
				}
			} else {
				if (nymiHandle < 0) { // we have not yet connected
					errorCount++;
				}
				nymiHandle = -1;
				synchronized (connectedHandles) {
					connectedHandles.remove(handle);
				}
			}
		}
	}

	/**
	 * Based on NCL's native callback mechanism but ensuring callbacks are
	 * invoked sequentially
	 * 
	 * @author Tony Lin
	 * 
	 */
	class NclCallbacks extends ArrayList<NclCallback> implements NclCallback {
		private static final long serialVersionUID = 1L;

		@Override
		public void call(final NclEvent event, final Object userData) {
			Log.e(LOG_TAG, "Dispatching event: " + event.getClass().toString());
			if (event instanceof NclEventError) {
				errorCount++;
			} else {
				errorCount = 0;
			}

			final ArrayList<NclCallback> snapshot = new ArrayList<NclCallback>();
			int handle = -1;
			boolean connected = false;
			if (event instanceof NclEventValidation) {
				handle = ((NclEventValidation) event).nymiHandle;
				connected = true;
			} else if (event instanceof NclEventDisconnection) {
				handle = ((NclEventDisconnection) event).nymiHandle;
				connected = false;
			}
			synchronized (this) {
				if (handle >= 0) {
					if (connected) {
						nymiHandle = handle;
						if (this.size() > 0) { // somebody is listening
							synchronized (connectedHandles) {
								connectedHandles.put(handle, System.currentTimeMillis());
							}
						} else { // nobody is listening, must be a racing,
									// disconnect the connection
							disconnect(handle);
							return;
						}
					} else {
						if (nymiHandle < 0) { // we have not yet connected
							errorCount++;
						}
						nymiHandle = -1;
						synchronized (connectedHandles) {
							connectedHandles.remove(handle);
						}
					}
				}

				snapshot.addAll(this);
				executor.execute(new Runnable() { // force single threaded event
													// NCL handling
					@Override
					public void run() {
						for (NclCallback callback : snapshot) {
							if (callback != null) {
								try {
									callback.call(event, userData);
								} catch (Throwable e) {
									Log.e(LOG_TAG, "Error invoking callback!", e);
								}
							}
						}
					}
				});
			}
		}
	}
}
