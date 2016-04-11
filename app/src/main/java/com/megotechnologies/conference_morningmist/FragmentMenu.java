package com.megotechnologies.conference_morningmist;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.megotechnologies.conference_morningmist.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.conference_morningmist.interfaces.ZCRunnable;

public class FragmentMenu extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	TextView tvTitle;
	TextView tvLabShare, tvLabRate, tvLabPolicy, tvVersion, tvLabAlerts;
	LinearLayout llContainer;

	int opcode = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		activity.lastCreatedActivity = MainActivity.SCREEN_ACCOUNT;

		v =  inflater.inflate(R.layout.fragment_menu, container, false);

		storeClassVariables();
		initUIHandles();
		initUIListeners();
		formatUI();

		return v;
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub

		llContainer = (LinearLayout)v.findViewById(R.id.ll_container);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvLabShare = (TextView)v.findViewById(R.id.tv_share);
		tvLabPolicy = (TextView)v.findViewById(R.id.tv_policies);
		tvLabRate = (TextView)v.findViewById(R.id.tv_rate);
		tvLabAlerts = (TextView)v.findViewById(R.id.tv_alerts);
		tvVersion = (TextView)v.findViewById(R.id.tv_version);

	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		tvLabRate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Uri uri = Uri.parse(MainActivity.MARKET_URL_PREFIX_1 + activity.context.getPackageName());
				Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
				try {
					startActivity(goToMarket);
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.MARKET_URL_PREFIX_2 + activity.context.getPackageName())));
				}


			}

		});

		tvLabShare.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MainActivity.SHARE_SUBJECT);
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, MainActivity.SHARE_CONTENT + MainActivity.MARKET_URL_PREFIX_2 + activity.context.getPackageName());
				startActivity(Intent.createChooser(sharingIntent, "Share This App"));

			}

		});

		tvLabPolicy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				activity.loadPolicy();

			}

		});

		tvLabAlerts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				activity.loadAlerts();


			}
		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvTitle.setGravity(Gravity.CENTER);

		try {

			PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
			String version = pInfo.versionName;
			tvVersion.setText("Version " + version);
			tvVersion.setTypeface(activity.tf);
			tvVersion.setPadding(MainActivity.SPACING, MainActivity.SPACING*2, MainActivity.SPACING, 0);
			tvVersion.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE - 3));

		} catch (PackageManager.NameNotFoundException e) {

		}

		tvLabShare.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabShare.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabShare.setGravity(Gravity.LEFT);

		tvLabRate.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabRate.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabRate.setGravity(Gravity.LEFT);

		tvLabPolicy.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabPolicy.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabPolicy.setGravity(Gravity.LEFT);

		tvLabAlerts.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabAlerts.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabAlerts.setGravity(Gravity.LEFT);

		activity.app.ENABLE_SYNC = false;
		activity.app.PREVENT_CLOSE_AND_SYNC = true;

	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		activity.app.ENABLE_SYNC = true;
		activity.app.PREVENT_CLOSE_AND_SYNC = false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub


	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}


}
