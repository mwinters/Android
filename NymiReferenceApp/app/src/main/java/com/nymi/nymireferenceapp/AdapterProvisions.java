package com.nymi.nymireferenceapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nymi.api.NymiDevice;

import java.util.ArrayList;

/**
 * Created by dgomon on 14-12-16.
 */
public class AdapterProvisions extends BaseAdapter {
    
    private final Activity mActivity;
    private ArrayList<NymiDevice> mDevices;

    public AdapterProvisions(Activity activity) {
        mActivity = activity;
        mDevices = new ArrayList<NymiDevice>();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.layout_provision_row, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.provision = (TextView) rowView.findViewById(R.id.layout_provision_row_provision);
            rowView.setTag(viewHolder);
        }

        // fill data
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.provision.setText(mDevices.get(position).getId());
        
        return rowView;
    }
    
    public void setDevices(ArrayList<NymiDevice> devices) {
        if (devices != null) {
            mDevices = devices;
            notifyDataSetInvalidated();
        }
    }
    
    public void addDevice(NymiDevice device) {
        mDevices.add(device);
        notifyDataSetInvalidated();
    }
    
    static class ViewHolder {
        public TextView provision;
    }
}
