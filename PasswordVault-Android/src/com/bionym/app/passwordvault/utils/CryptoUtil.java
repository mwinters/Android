package com.bionym.app.passwordvault.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.util.Base64;
import android.util.Log;

/**
 * Provide password encryption/decryption
 * 
 * * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 */
public class CryptoUtil {

	private static final CryptoUtil cryptoUtil = new CryptoUtil();

	public static CryptoUtil getCryptoUtil() {
		return cryptoUtil;
	}

	private static final String LOG_TAG = CryptoUtil.class.getName();

	private byte[] salt = { (byte) 0x38, (byte) 0x4f, (byte) 0x7a, (byte) 0x9d, (byte) 0x4b, (byte) 0x52, (byte) 0x0d, (byte) 0xc4 };

	private char[] cryptoPassword = "24A008E9294C238BC5B2DE0907C224887427820784BEBCC695897E9E29B4D0AF".toCharArray();

	private static String ALGORITHM = "PBEWithMD5AndDES";
	private SecretKey pbeKey;

	private static int count = 20;

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public void setCryptoPassword(char[] cryptoPassword) {
		this.cryptoPassword = cryptoPassword;
	}

	private boolean initializeKey() {
		if (pbeKey == null) {
			try {
				// Iteration count
				PBEKeySpec pbeKeySpec = new PBEKeySpec(cryptoPassword, salt, count);
				SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);

				pbeKey = keyFac.generateSecret(pbeKeySpec);
				return true;
			} catch (Exception e) {
				Log.e(LOG_TAG, "Exception initializing crypto algorithm!", e);
				return false;
			}
		}
		return true;
	}

	public String encrypt(String input) {
		try {
			if (initializeKey()) {
				Cipher cipher = Cipher.getInstance(ALGORITHM);
				cipher.init(Cipher.ENCRYPT_MODE, pbeKey);
				byte[] inputBytes = input.getBytes();
				return Base64.encodeToString(cipher.doFinal(inputBytes), 0);
			} else {
				// return the input text without any encryption
				return input;
			}
		} catch (Exception e) { // the algorithm does not exist!
			Log.e(LOG_TAG, "Exception encrypting password!", e);
			// return input
			return input;
		}
	}

	public String decrypt(String encryptedText) {
		try {
			initializeKey();
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, pbeKey);
			byte[] encryptedBytes = Base64.decode(encryptedText, 0);
			byte[] originalBytes = cipher.doFinal(encryptedBytes);
			String password = new String(originalBytes);
			return password;
		} catch (Exception e) { // the algorithm does not exist, or the
								// encryption is invalid!
			Log.e(LOG_TAG, "Exception decrypting password!", e);
			// return encryped string
			return encryptedText;
		}
	}
}
