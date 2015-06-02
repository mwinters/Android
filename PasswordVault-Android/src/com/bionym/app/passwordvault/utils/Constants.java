package com.bionym.app.passwordvault.utils;

/**
 * 
 * This class contains constant variables used in the application
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 */
public class Constants {

	public static final String PWDFILENAME = "pwdfile.txt";
	public static final String PROVFILENAME = "provfile.txt";

	public static final boolean ISDEVICE = false;

	public static final String IP = "10.0.1.105";

	/**
	 * This enum is used when different operations like copy,view ,change and
	 * remove,launch website are done on selected credential
	 * 
	 */
	public static enum OPTIONS {
		DEFAULT, COPY, VIEW, CHANGE, LAUNCH_WEBSITE, REMOVE;

	}
	
	/**
	 * This enum is used when different operations like copy,view,change and
	 * remove,launch website are done on selected credential
	 * 
	 */
	public static enum OPTIONS_1 {
		DEFAULT, COPY, VIEW, CHANGE,REMOVE;

	}

	/**
	 * This enum is used when user wants to view different UI views i.e. All
	 * password screen, starred passwords screen or all password screen
	 * 
	 */
	public static enum MENU_OPTIONS {
		ALL_PWD, STARRED_PWD, NEW_PWD;
	}

	/**
	 * This enum is used when user wants clicks options in sliding menu
	 * 
	 */
	public static enum RESET_OPTIONS {
		CLEAR_PASSWORDS, UNAUTHORIZE;
	}

	public static final int CREDENTIAL_LENGTH = 50;

}
