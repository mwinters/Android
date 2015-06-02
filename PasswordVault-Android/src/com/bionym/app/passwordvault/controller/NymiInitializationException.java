package com.bionym.app.passwordvault.controller;

/**
 * Thrown when Nymi initialization fails
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 */
public class NymiInitializationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NymiInitializationException() {
		super();
	}

	public NymiInitializationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public NymiInitializationException(String detailMessage) {
		super(detailMessage);
	}

	public NymiInitializationException(Throwable throwable) {
		super(throwable);
	}
}
