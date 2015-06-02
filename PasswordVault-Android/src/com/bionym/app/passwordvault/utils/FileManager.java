package com.bionym.app.passwordvault.utils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.bionym.app.passwordvault.model.Credentials;
import com.bionym.ncl.NclProvision;
import com.bionym.ncl.NclProvisionId;
import com.bionym.ncl.NclProvisionKey;

/**
 * 
 * FileManager is responsible for reading/writing credentials and provisions to
 * a file.
 * 
 * @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 */
public class FileManager {
	private static final String LOG_TAG = FileManager.class.getName();

	/**
	 * Read credentials from flat file
	 * 
	 * @param aContext
	 *            context of the activity
	 * 
	 * @return data read from the file i.e list of credentials
	 *         <ul>
	 *         Credential Object contains:
	 *         <li>Tag/Label name of the credential</li>
	 *         <li>Password</li>
	 *         <li>isFavourite flag</li>
	 *         </ul>
	 * 
	 */

	@SuppressWarnings("unchecked")
	public static ArrayList<Credentials> readPasswordFile(Context aContext) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		ArrayList<Credentials> listInFile = new ArrayList<Credentials>();
		try {
			// if file does not exist create new file and open read stream
			fis = new FileInputStream(createFile(Constants.PWDFILENAME, aContext));
			ois = new ObjectInputStream(fis);

			// read file data from list
			listInFile = (ArrayList<Credentials>) ois.readObject();

		} catch (ClassNotFoundException e) {
			Log.e(LOG_TAG, new StringBuilder("ClassNotFoundException: ").append(e.toString()).toString());
		} catch (EOFException e) { // This exception will be caught when EOF is
									// reached
			Log.e(LOG_TAG, new StringBuilder("EOFException: ").append(e.toString()).toString());
		} catch (IOException e) {
			Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
		} finally {
			try {
				if (ois != null) {
					// close stream
					ois.close();
				}
				if (fis != null) {
					// close stream
					fis.close();
				}

			} catch (IOException e) {
				Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
			}
		}
		return listInFile;
	}

	/**
	 * Write list of credentials to the file
	 * <ul>
	 * Credential Object contains:
	 * <li>Tag/Label name of the credential</li>
	 * <li>Password</li>
	 * <li>isFavourite flag</li>
	 * </ul>
	 * 
	 * @param list
	 *            credentials list to be written to the file
	 * @param aContext
	 *            context of the activity
	 */
	public static void writePasswordFile(ArrayList<Credentials> list, Context aContext) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {

			fos = new FileOutputStream(createFile(Constants.PWDFILENAME, aContext));
			oos = new ObjectOutputStream(fos);

			// write file data to list
			oos.writeObject(list);
		} catch (IOException e) {
			Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
		} finally {
			try {
				if (oos != null && fos != null) {
					// close stream
					oos.flush();
					oos.close();
					fos.close();
				}
			} catch (IOException e) {
				Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
			}

		}
	}

	/**
	 * Write provisioned Nymi Id and key to flat file
	 * <ul>
	 * <li>Json Data:</li>
	 * <li>
	 * {"id":[-22,-82,62,79,-105,85,-87,-69,26,87,-102,-45,-110,-83 ,39
	 * ,-103],"key":[-44,75,-123,-11,95,-110,70,120,-40,120,3,-30,99
	 * ,64,-95,-21]}</li>
	 * 
	 * </ul>
	 * 
	 * @param provisions
	 *            provision Nymi Id and key in Json Format
	 * @param aContext
	 *            context of the activity
	 */
	private static void writeProvisionFile(String provisions, Context aContext) {

		// if file does not exist create new file
		createFile(Constants.PROVFILENAME, aContext);

		FileWriter file = null;
		try {
			file = new FileWriter(aContext.getFilesDir().getAbsolutePath() + File.separator + Constants.PROVFILENAME);

			// encrypt and write to file
			file.write(provisions);
		} catch (IOException e) {
			Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
		}

		finally {
			if (file != null) {
				try {
					file.flush();
					file.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
				}
			}
		}

	}

	/**
	 * Read provisioned Nymi Id from flat file
	 * <ul>
	 * <li>Json Data:</li>
	 * <li>
	 * {"id":[-22,-82,62,79,-105,85,-87,-69,26,87,-102,-45,-110,-83 ,39
	 * ,-103],"key":[-44,75,-123,-11,95,-110,70,120,-40,120,3,-30,99
	 * ,64,-95,-21]}</li>
	 * 
	 * </ul>
	 * 
	 * @param aContext
	 *            context of the activity
	 * @return data read from the file i.e NCLProvision list object
	 */
	private static String readProvisionFile(Context aContext) {
		// if file does not exist create new file
		createFile(Constants.PROVFILENAME, aContext);
		String ret = "";
		InputStream inputStream = null;

		try {
			inputStream = aContext.openFileInput(Constants.PROVFILENAME);

			if (inputStream != null) {
				// read file
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				// add file data to string buffer
				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString);
				}

				ret = stringBuilder.toString();

			}
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, new StringBuilder("FileNotFoundException: ").append(e.toString()).toString());
		} catch (IOException e) {
			Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
		} finally {
			try {
				if (inputStream != null) {
					// close stream
					inputStream.close();
				}
			} catch (IOException e) {
				Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
			}

		}

		return ret;

	}

	/**
	 * This method will create the password file in internal storage if it does
	 * not exist
	 * 
	 * @param filename
	 *            which needs to be created
	 * @param aContext
	 *            context of the activity
	 */
	private static File createFile(String filename, Context aContext) {
		// get file path
		String path = aContext.getFilesDir().getAbsolutePath() + File.separator + filename;

		File outputFile = new File(path);
		if (!outputFile.exists()) {
			try {
				outputFile.createNewFile();
			} catch (IOException e) {
				Log.e(LOG_TAG, new StringBuilder("IOException: ").append(e.toString()).toString());
			}
		}

		return outputFile;
	}

	/**
	 * This method will delete the file in internal storage if it exist
	 * 
	 * @param filename
	 *            which needs to be deleted
	 * @param aContext
	 *            context of the activity
	 */
	public static File deleteFile(String filename, Context aContext) {
		// get file path
		String path = aContext.getFilesDir().getAbsolutePath() + File.separator + filename;

		File outputFile = new File(path);
		if (outputFile.exists()) {
			// delete file
			outputFile.delete();

		}

		return outputFile;
	}

	/**
	 * This method saves the Nymi Provision Key,Id in Flat file
	 * <ul>
	 * <li>Json Format:</li>
	 * <li>
	 * {"id":[-22,-82,62,79,-105,85,-87,-69,26,87,-102,-45,-110,-83 ,39
	 * ,-103],"key":[-44,75,-123,-11,95,-110,70,120,-40,120,3,-30,99
	 * ,64,-95,-21]}</li>
	 * 
	 * </ul>
	 * 
	 * @param provision
	 *            provisioned Nymi object containing key and id
	 * @param aContext
	 *            context of the activity
	 * 
	 */
	public static void saveProvision(NclProvision provision, Context aContext) {

		JSONObject jPro = new JSONObject();

		JSONArray jKey = new JSONArray();
		JSONArray jId = new JSONArray();
		// add provision key byte array to jsonarray
		for (int i = 0; i < provision.key.v.length; i++) {
			jKey.put(provision.key.v[i]);
		}
		// add provision id byte array to jsonarray
		for (int i = 0; i < provision.id.v.length; i++) {
			jId.put(provision.id.v[i]);
		}

		try {

			// add provision key and id array to json object
			jPro.putOpt("key", jKey);
			jPro.putOpt("id", jId);
			Log.d(LOG_TAG, new StringBuilder("jPro: ").append(jPro.toString()).toString());

		} catch (JSONException e) {
			Log.e(LOG_TAG, new StringBuilder("JSONException: ").append(e.toString()).toString());
		}

		if (jPro != null) {
			writeProvisionFile(jPro.toString(), aContext);
		}
	}

	/**
	 * This method gets the saved Provision values
	 * <ul>
	 * <li>Json Format:</li>
	 * <li>
	 * {"id":[-22,-82,62,79,-105,85,-87,-69,26,87,-102,-45,-110,-83 ,39
	 * ,-103],"key":[-44,75,-123,-11,95,-110,70,120,-40,120,3,-30,99
	 * ,64,-95,-21]}</li>
	 * 
	 * </ul>
	 * 
	 * @param aContext
	 *            context of the activity
	 * @return provision objects
	 */
	public static NclProvision loadProvision(Context aContext) {

		String data = readProvisionFile(aContext);
		NclProvision provisonObj = null;
		try {

			if (data.length() > 0) {

				JSONObject jPro = new JSONObject(data);
				JSONArray jKey = new JSONArray();
				JSONArray jId = new JSONArray();

				// get provision key and value
				jKey = jPro.getJSONArray("key");
				jId = jPro.getJSONArray("id");

				provisonObj = new NclProvision();
				NclProvisionKey pKey = new NclProvisionKey();
				NclProvisionId pId = new NclProvisionId();

				// initialize provision key value byte array
				pKey.v = new byte[jKey.length()];
				pId.v = new byte[jId.length()];

				// copy provision key bytes to NclProvisionKey object
				for (int i = 0; i < jKey.length(); i++) {
					pKey.v[i] = (byte) jKey.getInt(i);
				}

				// copy provision key bytes to NclProvisionId object
				for (int i = 0; i < jId.length(); i++) {
					pId.v[i] = (byte) jId.getInt(i);
				}

				provisonObj.key = pKey;
				provisonObj.id = pId;

			}

		} catch (JSONException e) {
			Log.e(LOG_TAG, new StringBuilder("JSONException: ").append(e.toString()).toString());
		}

		return provisonObj;
	}
}
