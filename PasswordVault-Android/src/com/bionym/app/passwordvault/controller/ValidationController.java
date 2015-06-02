package com.bionym.app.passwordvault.controller;

import android.content.Context;
import android.util.Log;

import com.bionym.app.passwordvault.utils.Constants;
import com.bionym.app.passwordvault.utils.ScheduledTask;
import com.bionym.app.passwordvault.utils.SystemUtils;
import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventDetection;
import com.bionym.ncl.NclEventDisconnection;
import com.bionym.ncl.NclEventError;
import com.bionym.ncl.NclEventFind;
import com.bionym.ncl.NclEventValidation;
import com.bionym.ncl.NclProvision;

/**
 * This is a reusable controller for validating a provisioned Nymi. To listen on a validation process's progress, one should pass a 
 * {@link ValidationProcessListener} to the controller when calling {@link #startValidation(ValidationProcessListener, NclProvision)
 * to start the validation process.
 * <P>
 * ValidationController will not initialize Ncl library, the Ncl library should be initialized before invoking the controller.
 * Neither will it disconnect a connected connection.
 *
 * * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */
public class ValidationController {
	// Constants
	protected static final String LOG_TAG = ValidationController.class.getName();
	protected static final int VALIDATION_TIMEOUT_MILLIES = 30000; // controlling
																	// how long
																	// we should
																	// give up
																	// finding
																	// process
	protected static final int MAX_VALIDATION_RETRY = 2; // controlling how many
															// times we should
															// retry validation
															// should we fail to
															// validate after
															// found
	protected static final int RSSI_SAMPLING_WINDOW_WIDTH = 1; // The number of
																// RSSI samples
																// required to
																// filter RSSI
																// noise
	protected static final int DEFAULT_RSSI_THRESHOLD = -75; // the minimal RSSI
																// for accepting
																// a Nymi, this
																// seems to be
																// more
																// reasonable
																// for one
																// sample due to
																// fluctuation
	protected static final int RETRY_WAIT_TIME = 1500; // the wait time before
														// we retry

	protected static ScheduledTask waitTask; // wait for Nymi validation
												// timeout, and end the
												// validation process

	protected long startFindingTime = 0L;
	protected long validationTimeOut = VALIDATION_TIMEOUT_MILLIES;

	// the current nymi handle
	protected int nymiHandle = Ncl.NYMI_HANDLE_ANY;

	// the current provision that has been made
	protected NclProvision provision;

	protected State state;

	protected Context context;
	protected ValidationProcessListener listener;
	protected String firmwareVersion;
	protected Nymi nymi;
	protected int rssiThreshold = DEFAULT_RSSI_THRESHOLD;
	protected int retryCount = MAX_VALIDATION_RETRY;
	protected int validationRetryCount;

	// NCL event callbacks
	protected NclCallback nclCallback;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the context
	 */
	public ValidationController(Context context) {
		this.context = context;
	}

	/**
	 * 
	 * @return the current validation process listener
	 */
	public ValidationProcessListener getListener() {
		return listener;
	}

	/**
	 * Set the validation process listener
	 * 
	 * @param listener
	 *            the listener
	 */
	public void setListener(ValidationProcessListener listener) {
		this.listener = listener;
	}

	/**
	 * 
	 * @return the RSSI threshold to accept a Nymi
	 */
	public int getRssiThreshold() {
		return rssiThreshold;
	}

	/**
	 * Set the Nymi RSSI acceptance threshold
	 * 
	 * @param rssiThreshold
	 *            the threshold
	 */
	public void setRssiThreshold(int rssiThreshold) {
		this.rssiThreshold = rssiThreshold;
	}

	/**
	 * 
	 * @return how many time the control should retry validation before
	 *         reporting failure.
	 */
	public int getRetryCount() {
		return retryCount;
	}

	/**
	 * Set how many time the control should retry validation before reporting
	 * failure.
	 * 
	 * @param retryCount
	 *            the new value
	 */
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	/**
	 * Get the connected Nymi handler
	 * 
	 * @return
	 */
	public int getNymiHandle() {
		return nymiHandle;
	}

	/**
	 * 
	 * @return true if validation process is in progress
	 */
	public boolean isValidating() {
		return state == State.FINDING || state == State.VALIDATING || state == State.RETRIEVE_AUTHENTICATION_STATUS || state == State.RETRIEVE_FIRMWARE_VERSION;
	}

	/**
	 * 
	 * @return the provisioned provision
	 */
	public NclProvision getProvision() {
		return provision;
	}

	/**
	 * 
	 * @return the current state
	 */
	public State getState() {
		return state;
	}

	/**
	 * 
	 * @return the connected NYmi's firmware version if available
	 */
	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	/**
	 * Start the validation process
	 * 
	 * @param listener
	 *            the listener
	 * @param provision
	 *            the provision
	 * @return true if the process is started successfully
	 */
	public boolean startValidation(final ValidationProcessListener listener, final NclProvision provision) {
		return startValidation(listener, provision, VALIDATION_TIMEOUT_MILLIES);
	}

	/**
	 * Start the validation process
	 * 
	 * @param listener
	 *            the listener
	 * @param provision
	 *            the provision
	 * @param timeout
	 *            the time in millies to wait for the process to complete
	 * @return true if the process is started successfully
	 */
	public synchronized boolean startValidation(final ValidationProcessListener listener, final NclProvision provision, long timeout) {
		if (isValidating()) {
			return false;
		}

		nymiHandle = Ncl.NYMI_HANDLE_ANY;
		validationTimeOut = timeout;
		this.listener = listener;

		if (Constants.ISDEVICE) {
			nymi = NymiBand.getInstance();
		} else {
			nymi = Nymulator.getInstance();
		}

		if (nymi == null) { // failed to initialize NCL
			Log.d(LOG_TAG, "BLE failed to initialize!");

			return false;
		}

		if (nclCallback == null) {
			nclCallback = createNclCallback();
		}

		nymi.addCallback(nclCallback);
		this.provision = provision;
		SystemUtils.runTask(new Runnable() {
			@Override
			public void run() {
				validationRetryCount = retryCount;
				startFinding();
			}
		});

		return true;
	}

	/**
	 * Return a new instanceof NclCallback
	 */
	protected NclCallback createNclCallback() {
		return new MyNclCallback();
	}

	/**
	 * Start the provisioned Nymi finding process
	 */
	protected void startFinding() {
		if (!isValidating()) {
			cancelWait();
			Log.d(LOG_TAG, "start scan");
			int bleStatus = nymi.enableBle();

			if (bleStatus == Nymi.BLE_STATUS_OK) {
				nymiHandle = Ncl.NYMI_HANDLE_ANY;
				Log.d(LOG_TAG, "Start finding provision: " + SystemUtils.toHexString(provision.key.v));
				if (nymi.startFinding(provision)) { // Ncl.startFinding(provisions,
													// 1, NclBool.NCL_FALSE)) {
					state = State.FINDING;

					startFindingTime = System.currentTimeMillis();

					waitTask = SystemUtils.runTaskAfterMillies(new Runnable() {
						@Override
						public void run() { // validation timeout, end the
											// finding process, and release wake
											// lock
							if (waitTask != null && waitTask.getRunnable() == this) {
								waitTask = null;
								Log.d(LOG_TAG, "Finding Nymi timeout, stop scan");
								validationRetryCount = 0;
								if (state == State.FINDING || state == State.VALIDATING) {
									stopValidation();
									state = State.NO_DEVICE;
									if (listener != null) {
										listener.onFailure(ValidationController.this);
									}
								}
							}
						}
					}, validationTimeOut);
				} else { // unable to start
					state = State.FAILED;
					if (listener != null) {
						listener.onFailure(ValidationController.this);
					}
				}
			} else { // Error state
				final int status = bleStatus;

				if (status == Nymi.BLE_STATUS_DISABLED) {
					state = State.BLE_DISABLED;
					if (listener != null) {
						listener.onFailure(ValidationController.this);
					}
				} else if (status == Nymi.BLE_STATUS_DISABLED_AIRPLANE_MODE) {
					state = State.AIRPLANE_MODE;
					if (listener != null) {
						listener.onFailure(ValidationController.this);
					}
				} else if (status == Nymi.BLE_STATUS_NO_BLE) {
					state = State.NO_BLE;
					if (listener != null) {
						listener.onFailure(ValidationController.this);
					}
				} else {
					state = State.NO_DEVICE;
					if (listener != null) {
						listener.onFailure(ValidationController.this);
					}
				}
			}
		}
	}

	/**
	 * Stop the finding process
	 */
	protected void stopFinding() {
		if (state == State.FINDING) {
			boolean b = nymi.stopScan();
			Log.d(LOG_TAG, "stop scan: " + b);
		}
	}

	/**
	 * Called to stop the process, but leave NCL connection around
	 */
	public void stopValidation() {
		cancelWait();
		stopFinding();
		if (nymi != null) {
			if (nclCallback != null) {
				nymi.removeCallback(nclCallback);
				nclCallback = null;
			}
		}
		nymiHandle = Ncl.NYMI_HANDLE_ANY;
		state = null;
	}

	/**
	 * Cancel validation wait timeout
	 */
	protected void cancelWait() {
		if (waitTask != null) {
			waitTask.cancel(true);
			waitTask = null;
		}
	}

	/**
	 * Handle NclEventValidation
	 * 
	 * @param event
	 *            the NclEventValidation event
	 */
	protected void handleValidationEvent(NclEventValidation event) {
		cancelWait();
		if (state == State.VALIDATING) {
			Log.d(LOG_TAG, "NCL_EVENT_VALIDATION Validated in (millies): " + (System.currentTimeMillis() - startFindingTime));
			nymiHandle = event.nymiHandle;
			stopFinding();
			state = State.VALIDATED;
			if (listener != null) {
				listener.onValidated(ValidationController.this);
			}
		}
	}

	/**
	 * Handle NclEventDisconnection
	 * 
	 * @param event
	 *            the NclEventDisconnection event
	 */
	protected void handleDisconnectionEvent(NclEventDisconnection event) {
		if (nymiHandle == Ncl.NYMI_HANDLE_ANY || nymiHandle == event.nymiHandle) { // nymiHandle
																					// may
																					// be
																					// -1
																					// if
																					// disconnect
																					// during
																					// connection
			// Nymi got disconnected, this might be normal case, just make sure
			// we cleanup Nymi, and release wake lock
			// However, it can also occur when Nymi connection has failed for
			// whatever reason
			Log.d(LOG_TAG, "NCL_EVENT_DISCONNECTION validated: " + (state == State.VALIDATED));
			if (state == State.FINDING || state == State.VALIDATING || state == State.RETRIEVE_AUTHENTICATION_STATUS
					|| state == State.RETRIEVE_FIRMWARE_VERSION) {
				stopValidation();
				if (validationRetryCount-- > 0) {
					Log.d(LOG_TAG, "Restart finding ...");
					if (nclCallback == null) {
						nclCallback = createNclCallback();
					}
					nymi.addCallback(nclCallback);
					SystemUtils.runTaskAfterMillies(new Runnable() {
						@Override
						public void run() {
							startFinding();
						}
					}, RETRY_WAIT_TIME);
				} else {
					if (listener != null) {
						listener.onFailure(ValidationController.this);
					}
					state = State.FAILED;
				}
			} else {
				stopValidation();
				if (listener != null) {
					listener.onDisconnected(ValidationController.this);
				}
			}
		}
	}

	/**
	 * Handle Ncl error
	 */
	protected void handleErrorEvent() {
		Log.d(LOG_TAG, "NCL_EVENT_ERROR Nymi Error !");
		if (isValidating()) { // failure during validation
			if (validationRetryCount-- > 0) {// failed, retry
				Log.d(LOG_TAG, "Restart finding ...");
				stopValidation();
				if (nclCallback == null) {
					nclCallback = createNclCallback();
				}
				nymi.addCallback(nclCallback);
				SystemUtils.runTaskAfterMillies(new Runnable() {
					@Override
					public void run() {
						startFinding();
					}
				}, 200);
			} else {
				state = State.FAILED;
				stopValidation();
				if (listener != null) {
					listener.onFailure(ValidationController.this);
				}
			}
		}
		// Don't care
	}

	/**
	 * Handle the NclEventFound/NclEventDetection
	 */
	protected void handleFoundEvent(int handle, int rssi) {
		if (state == State.FINDING) { // finding in progress
			Log.d(LOG_TAG, "NCL_EVENT_FIND Handle: " + handle + " RSSI: " + rssi + " in millies: " + (System.currentTimeMillis() - startFindingTime));
			if (rssi < rssiThreshold) { // too far
				return;
			}

			stopFinding();
			if (listener != null) {
				listener.onFound(ValidationController.this);
			}

			boolean b = nymi.validate(handle);
			if (!b) { // cannot validate?
				nymiHandle = Ncl.NYMI_HANDLE_ANY;
				if (validationRetryCount-- > 0) {
					Log.d(LOG_TAG, "Restart finding ...");
					state = null;
					startFinding();
				} else {
					stopValidation();
					state = State.FAILED;
					if (listener != null) {
						listener.onFailure(ValidationController.this);
					}
				}
				Log.d(LOG_TAG, "NCL_EVENT_FIND Validate returned: " + b + " rssi: " + rssi);
			} else {
				state = State.VALIDATING;
			}
		}
	}

	protected class MyNclCallback implements NclCallback {
		public void call(NclEvent event, Object userData) {
			Log.d(LOG_TAG, this.toString() + ": " + event.getClass().getName());

			if (event instanceof NclEventFind) {
				int handle = ((NclEventFind) event).nymiHandle;
				int rssi = ((NclEventFind) event).rssi;
				handleFoundEvent(handle, rssi);
			} else if (event instanceof NclEventDetection) {
				int handle = ((NclEventDetection) event).nymiHandle;
				int rssi = ((NclEventDetection) event).rssi;
				handleFoundEvent(handle, rssi);
			} else if (event instanceof NclEventValidation) { // Nymi is
																// validated,
																// end the
																// finding
																// process,
																// disconnect
																// Nymi, and
																// unlock the
																// screen
				handleValidationEvent((NclEventValidation) event);
			} else if (event instanceof NclEventDisconnection) {
				handleDisconnectionEvent((NclEventDisconnection) event);
			} else if (event instanceof NclEventError) { // We got an error,
															// make sure we
															// cleanup Nymi, and
															// release wake lock
				handleErrorEvent();
			}
		}

	}

	/**
	 * Interface for listening on the provision process
	 * 
	 */
	public interface ValidationProcessListener {
		/**
		 * Called when the provision process is started
		 * 
		 * @param controller
		 *            the ValidationController performing the validation
		 */
		public void onStartProcess(ValidationController controller);

		/**
		 * Called when the provisioned Nymi is found
		 * 
		 * @param controller
		 *            the ValidationController performing the validation
		 */
		public void onFound(ValidationController controller);

		/**
		 * Called when the Nymi is validated
		 * 
		 * @param controller
		 *            the ValidationController performing the validation
		 */
		public void onValidated(ValidationController controller);

		/**
		 * Called when the provision process failed
		 * 
		 * @param controller
		 *            the ValidationController performing the validation
		 */
		public void onFailure(ValidationController controller);

		/**
		 * Called when the connected Nymi during the provision process is
		 * disconnected
		 * 
		 * @param controller
		 *            the ValidationController performing the validation
		 */
		public void onDisconnected(ValidationController controller);
	}

	public enum State {
		/**
		 * Finding in progress
		 */
		FINDING,

		/**
		 * Validating in progress
		 */
		VALIDATING,

		/**
		 * The provisioned Nymi is validated
		 */
		VALIDATED,

		/**
		 * Retrieving firmware version
		 */
		RETRIEVE_FIRMWARE_VERSION,

		/**
		 * Retrieving authentication status
		 */
		RETRIEVE_AUTHENTICATION_STATUS,

		/**
		 * There is no Nymi Band
		 */
		NO_DEVICE,

		/**
		 * The validation failed
		 */
		FAILED,

		/**
		 * The device has no BLE
		 */
		NO_BLE,

		/**
		 * The device's BLE is disabled
		 */
		BLE_DISABLED,

		/**
		 * The device's BLE is disabled as it is in airplane mode
		 */
		AIRPLANE_MODE
	}
}
