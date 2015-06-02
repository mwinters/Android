package com.bionym.app.passwordvault.view;

import com.bionym.app.passwordvault.R;
import com.bionym.app.passwordvault.utils.SystemUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SetupActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// Remove title bar
				this.requestWindowFeature(Window.FEATURE_NO_TITLE);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

				if (!SystemUtils.isTablet(this)) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				setContentView(R.layout.validation_activity);	
				
				TextView splashtext = (TextView) findViewById(R.id.splashtext1);
				splashtext.setTypeface(SystemUtils.getButtonTypeface(SetupActivity.this));

				Button buttonadd = (Button) findViewById(R.id.splashbtn);
				buttonadd.setTypeface(SystemUtils.getButtonTypeface(SetupActivity.this));
				buttonadd.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// if first time launch proceed to provisioning
						startActivity(new Intent(SetupActivity.this, ProvisioningActivity.class));
						finish();
					}
				});
	}
}
