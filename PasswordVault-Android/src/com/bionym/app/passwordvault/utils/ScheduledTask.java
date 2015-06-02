package com.bionym.app.passwordvault.utils;

import java.util.concurrent.ScheduledFuture;

/**
 * Wrapper of ScheduledFuture to include the runnable, so we can correctly
 * identify a ScheduledTask from the running runnable
 * 
 * * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 */
public class ScheduledTask {
	ScheduledFuture<?> scheduledFuture;
	Runnable runnable;

	public ScheduledTask(ScheduledFuture<?> scheduledFuture, Runnable runnable) {
		this.scheduledFuture = scheduledFuture;
		this.runnable = runnable;
	}

	public boolean cancel(boolean interruptIfRunning) {
		return scheduledFuture.cancel(interruptIfRunning);
	}

	public ScheduledFuture<?> getScheduledFuture() {
		return scheduledFuture;
	}

	public Runnable getRunnable() {
		return runnable;
	}
}
