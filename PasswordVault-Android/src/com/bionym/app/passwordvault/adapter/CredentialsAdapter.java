package com.bionym.app.passwordvault.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.model.Credentials;
import com.bionym.app.passwordvault.utils.FileManager;

/**
 * 
 * This is a adapter class for populating user credentials on the screen in a
 * list
 * 
 *  @author sonal.agarwal
 * 
 * @Copyright (c) 2014 Nymi Inc. All rights reserved.
 * 
 */
public class CredentialsAdapter extends ArrayAdapter<Credentials> {

	private ArrayList<Credentials> credList;
	private Context mContext;
	private int resId;

	/**
	 * 
	 * @param context
	 *            Context of the activity
	 * @param resource
	 *            layout resource id of the row to be populated in the view
	 * @param items
	 *            data to be populated in the view
	 */
	public CredentialsAdapter(Context context, int resource, ArrayList<Credentials> items) {
		super(context, resource, items);
		this.credList = items;
		this.resId = resource;
		this.mContext = context;

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		CredentialsHolder holder = null;
		if (convertView == null) {

			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			convertView = vi.inflate(this.resId, parent, false);

			holder = new CredentialsHolder();
			holder.txtTag = (TextView) convertView.findViewById(R.id.pwdTag);
			holder.favImage = (ImageView) convertView.findViewById(R.id.starImg);

			convertView.setTag(holder);

		} else {
			holder = (CredentialsHolder) convertView.getTag();
		}

		final Credentials creds = this.credList.get(position);

		if (creds != null) {

			if (holder.txtTag != null) {
				holder.txtTag.setText(creds.getTag());
			}
			if (creds.isFavourite()) {
				holder.favImage.setImageResource(R.drawable.staron);
				holder.favImage.setTag(R.drawable.staron);
			} else {
				holder.favImage.setImageResource(R.drawable.staroff);
				holder.favImage.setTag(R.drawable.staroff);

			}
			final ImageView img = holder.favImage;

			holder.favImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					// unstar the password if starred
					if (img.getTag().equals((Integer) R.drawable.staron)) {
						img.setImageResource(R.drawable.staroff);
						creds.setFavourite(false);
					} else {
						// star the password if unstarred
						img.setImageResource(R.drawable.staron);
						creds.setFavourite(true);

					}
					// update the credential
					CredentialsAdapter.this.credList.remove(position);
					CredentialsAdapter.this.credList.add(position, creds);
					// update the credential list
					FileManager.writePasswordFile(CredentialsAdapter.this.credList, CredentialsAdapter.this.mContext);

					notifyDataSetChanged();

				}
			});

		}

		return convertView;

	}

	private class CredentialsHolder {
		TextView txtTag;
		ImageView favImage;
	}
}
