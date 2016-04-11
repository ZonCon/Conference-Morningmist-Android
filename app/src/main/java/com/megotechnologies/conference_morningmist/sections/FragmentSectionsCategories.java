package com.megotechnologies.conference_morningmist.sections;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megotechnologies.conference_morningmist.FragmentMeta;
import com.megotechnologies.conference_morningmist.MainActivity;
import com.megotechnologies.conference_morningmist.R;
import com.megotechnologies.conference_morningmist.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.conference_morningmist.interfaces.ZCRunnable;
import com.megotechnologies.conference_morningmist.utilities.ImageProcessingFunctions;
import com.megotechnologies.conference_morningmist.utilities.MLog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentSectionsCategories extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	int IV_ID_PREFIX = 4000;

	Boolean RUN_FLAG = false;
	Thread thPictDownload;

	LinearLayout llContainer;

	Timer timer;

	int bannerCounter = 0, bannerMax = 0;
	ArrayList<HashMap<String, String>> recordsBannerItems;
	TextView tvBannerTitle, tvBannerSub;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		v =  inflater.inflate(R.layout.fragment_section_categories, container, false);
		activity.lastCreatedActivity = MainActivity.SCREEN_SECTION_CATEGORIES;
		activity.showHeaderFooter();

		storeClassVariables();
		initUIHandles();
		initUIListeners();
		formatUI();
		
		try {
			loadFromLocalDB();
		} catch (IllegalStateException e) {
			
		}

		return v;

	}

	Handler timerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			bannerCounter++;
			if(bannerCounter == bannerMax) {
				bannerCounter = 0;
			}

			HashMap<String, String> mapItem = recordsBannerItems.get(bannerCounter);
			String _idItem = mapItem.get(MainActivity.DB_COL_ID);
			String title = mapItem.get(MainActivity.DB_COL_TITLE);
			String sub = mapItem.get(MainActivity.DB_COL_SUB);
			tvBannerTitle.setText(title);
			tvBannerSub.setText(sub);
			bannerMax = recordsBannerItems.size();

			HashMap<String, String> mapPictures = new HashMap<String, String>();
			mapPictures.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_PICTURE);
			mapPictures.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);
			ArrayList<HashMap<String, String>> recordsPictures = null;
			if (dbC.isOpen()) {
				dbC.isAvailale();
				recordsPictures = dbC.retrieveRecords(mapPictures);
			}

			MLog.log("Pictures found = " + recordsPictures.size());

			String picture = "";
			if (recordsPictures.size() > 0) {
				mapPictures = recordsPictures.get(0);
				picture = mapPictures.get(MainActivity.DB_COL_PATH_PROC);
			}

			if (picture.length() > 0) {

				if (activity.checkIfExistsInExternalStorage(picture)) {

					String filePath = MainActivity.STORAGE_PATH + "/" + picture;
					Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromFile(filePath, MainActivity.IMG_TH_MAX_SIZE, MainActivity.IMG_TH_MAX_SIZE);
					Message msg1 = new Message();
					msg1.obj = bmp;
					msg1.what = (IV_ID_PREFIX + MainActivity.NUM_INIT_STREAMS);
					MLog.log("Sending message to " + msg1.what);
					threadHandler.sendMessage(msg1);

				} else {

					thPictDownload = new Thread(FragmentSectionsCategories.this);
					thPictDownload.setName(picture + ";" + (IV_ID_PREFIX + MainActivity.NUM_INIT_STREAMS));
					thPictDownload.start();

				}

			} else {

				Message msg1 = new Message();
				msg1.obj = null;
				msg1.what = (IV_ID_PREFIX + MainActivity.NUM_INIT_STREAMS);
				MLog.log("Sending message to " + msg1.what);
				threadHandler.sendMessage(msg1);

			}

		}
	};

	TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {

			timerHandler.sendEmptyMessage(0);

		}
	};

	void loadFromLocalDB() {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);

		ArrayList<HashMap<String, String>> records = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			records = dbC.retrieveRecords(map);
		}

		if(records.size() < MainActivity.NUM_INIT_STREAMS) {
			return;
		}

		llContainer.removeAllViews();
		llContainer.setPadding(0, 0, 0, 2);

		for(int i = MainActivity.NUM_INIT_STREAMS; i < records.size();) {

			LinearLayout llRowC = new LinearLayout(activity.context);
			llRowC.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			paramsLL.bottomMargin = 2;
			llRowC.setLayoutParams(paramsLL);
			llContainer.addView(llRowC);

			if(i == MainActivity.NUM_INIT_STREAMS) {

				map = records.get(i);

				//Last min test case
				if (map == null) {
					break;
				}

				final int idStreamLeft = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));

				int rowHeight = (int) (MainActivity.SCREEN_WIDTH / 2);
				int rowHeightLarge = (int) (MainActivity.SCREEN_WIDTH * 0.7);

				RelativeLayout rlRow = new RelativeLayout(activity.context);
				paramsLL = new LayoutParams(0, rowHeightLarge);
				paramsLL.weight = 2;
				paramsLL.setMargins(2, 2, 2, 0);
				rlRow.setLayoutParams(paramsLL);
				rlRow.setBackgroundColor(activity.getResources().getColor(R.color.tile_bg));
				llRowC.addView(rlRow);
				rlRow.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						if (!activity.IS_CLICKED) {

							activity.IS_CLICKED = true;

							FragmentSectionItemsList frag = new FragmentSectionItemsList();
							frag.idStream = idStreamLeft;
							activity.fragMgr.beginTransaction()
									.add(((ViewGroup) getView().getParent()).getId(), frag, MainActivity.SCREEN_SECTION_ITEM_LIST)
									.addToBackStack(MainActivity.SCREEN_SECTION_ITEM_LIST)
									.commit();

						}


					}

				});

				MLog.log("Name = " + map.get(MainActivity.DB_COL_NAME));

				ImageView iv = new ImageView(activity.context);
				RelativeLayout.LayoutParams rParamsIv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
						(rowHeightLarge - (rowHeight / 5)));
				rParamsIv.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				iv.setLayoutParams(rParamsIv);
				iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
				iv.setScaleType(ScaleType.CENTER_CROP);
				iv.setId(IV_ID_PREFIX + i);
				//iv.setPadding(3, 3, 3, 0);
				iv.setCropToPadding(true);
				rlRow.addView(iv);

				TextView tv = new TextView(activity.context);
				RelativeLayout.LayoutParams rParamsTv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, rowHeight / 5);
				rParamsTv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
				tv.setLayoutParams(rParamsTv);
				tv.setGravity(Gravity.CENTER | Gravity.LEFT);
				tv.setBackgroundColor(getResources().getColor(R.color.black));
				tv.setText(map.get(MainActivity.DB_COL_NAME).toUpperCase());
				tv.setTextColor(getResources().getColor(R.color.white));
				tv.setPadding(MainActivity.SPACING, 0, 0, 0);
				tv.setTextSize(rowHeight / 15);
				tv.setLineSpacing(0.0f, 1.2f);
				rlRow.addView(tv);
				tvBannerTitle = tv;

				TextView tvSub = new TextView(activity.context);
				rParamsTv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, rowHeight / 5);
				rParamsTv.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				tvSub.setLayoutParams(rParamsTv);
				tvSub.setGravity(Gravity.CENTER | Gravity.LEFT);
				tvSub.setText(map.get(MainActivity.DB_COL_NAME).toUpperCase());
				tvSub.setTextColor(getResources().getColor(R.color.white));
				tvSub.setPadding(MainActivity.SPACING, 0, 0, 0);
				tvSub.setTextSize(rowHeight / 15);
				tvSub.setShadowLayer(1, 1, 1, getResources().getColor(R.color.black));
				tvSub.setLineSpacing(0.0f, 1.2f);
				rlRow.addView(tvSub);
				tvBannerSub = tvSub;

				HashMap<String, String> mapItems = new HashMap<String, String>();
				mapItems.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
				mapItems.put(MainActivity.DB_COL_FOREIGN_KEY, map.get(MainActivity.DB_COL_ID));
				String _idItem = null;
				if (dbC.isOpen()) {
					dbC.isAvailale();
					_idItem = dbC.retrieveId(mapItems);
					recordsBannerItems = dbC.retrieveRecords(mapItems);
					if(recordsBannerItems.size() > 0) {
						HashMap<String, String> mapItem = recordsBannerItems.get(0);
						String title = mapItem.get(MainActivity.DB_COL_TITLE);
						String desc = mapItem.get(MainActivity.DB_COL_SUB);
						tv.setText(title);
						tvSub.setText(desc);
						bannerMax = recordsBannerItems.size();
						bannerCounter = 0;

						timer = new Timer();
						timer.schedule(timerTask, 0, 5000);

					}
				}

				MLog.log("Item Id = " + _idItem);

				HashMap<String, String> mapPictures = new HashMap<String, String>();
				mapPictures.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_PICTURE);
				mapPictures.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);
				ArrayList<HashMap<String, String>> recordsPictures = null;
				if (dbC.isOpen()) {
					dbC.isAvailale();
					recordsPictures = dbC.retrieveRecords(mapPictures);
				}

				MLog.log("Pictures found = " + recordsPictures.size());

				String picture = "";
				if (recordsPictures.size() > 0) {
					mapPictures = recordsPictures.get(0);
					picture = mapPictures.get(MainActivity.DB_COL_PATH_PROC);
				}

				if (picture.length() > 0) {

					if (activity.checkIfExistsInExternalStorage(picture)) {

						String filePath = MainActivity.STORAGE_PATH + "/" + picture;
						Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromFile(filePath, MainActivity.IMG_TH_MAX_SIZE, MainActivity.IMG_TH_MAX_SIZE);
						Message msg = new Message();
						msg.obj = bmp;
						msg.what = (IV_ID_PREFIX + i);
						MLog.log("Sending message to " + msg.what);
						threadHandler.sendMessage(msg);

					} else {

						thPictDownload = new Thread(this);
						thPictDownload.setName(picture + ";" + (IV_ID_PREFIX + i));
						thPictDownload.start();

					}

				} else {

					Message msg = new Message();
					msg.obj = null;
					msg.what = (IV_ID_PREFIX + i);
					MLog.log("Sending message to " + msg.what);
					threadHandler.sendMessage(msg);

				}
				i++;

			} else {


				for (int j = i; j < records.size() && j < i + 3; j++) {

					map = records.get(j);

					//Last min test case
					if (map == null) {
						break;
					}

					final int idStreamLeft = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));

					int rowHeight = (int) (MainActivity.SCREEN_WIDTH / 3);

					RelativeLayout rlRow = new RelativeLayout(activity.context);
					paramsLL = new LayoutParams(0, rowHeight);
					paramsLL.weight = 2;
					MLog.log("parity = " + (j % 3) + "");
					if (j % 3 == 1) {
						paramsLL.setMargins(2, 0, 1, 0);
					} else if (j % 3 == 2) {
						paramsLL.setMargins(1, 0, 1, 0);
					}else {
						paramsLL.setMargins(1, 0, 2, 0);
					}
					rlRow.setLayoutParams(paramsLL);
					rlRow.setBackgroundColor(activity.getResources().getColor(R.color.tile_bg));
					llRowC.addView(rlRow);
					rlRow.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							if (!activity.IS_CLICKED) {

								activity.IS_CLICKED = true;

								FragmentSectionItemsList frag = new FragmentSectionItemsList();
								frag.idStream = idStreamLeft;
								activity.fragMgr.beginTransaction()
										.add(((ViewGroup) getView().getParent()).getId(), frag, MainActivity.SCREEN_SECTION_ITEM_LIST)
										.addToBackStack(MainActivity.SCREEN_SECTION_ITEM_LIST)
										.commit();

							}


						}

					});

					MLog.log("Name = " + map.get(MainActivity.DB_COL_NAME));

					ImageView iv = new ImageView(activity.context);
					RelativeLayout.LayoutParams rParamsIv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
							(rowHeight - (rowHeight / 5)));
					rParamsIv.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
					iv.setLayoutParams(rParamsIv);
					iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
					iv.setScaleType(ScaleType.FIT_CENTER);
					iv.setId(IV_ID_PREFIX + j);
					iv.setCropToPadding(true);
					iv.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
					iv.setCropToPadding(true);
					rlRow.addView(iv);

					TextView tv = new TextView(activity.context);
					RelativeLayout.LayoutParams rParamsTv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, rowHeight / 5);
					rParamsTv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
					tv.setLayoutParams(rParamsTv);
					tv.setGravity(Gravity.CENTER);
					tv.setText(map.get(MainActivity.DB_COL_NAME).toUpperCase());
					tv.setTextColor(getResources().getColor(R.color.text_color));
					tv.setTextSize(rowHeight / 15);
					tv.setLineSpacing(0.0f, 1.2f);
					rlRow.addView(tv);

					HashMap<String, String> mapItems = new HashMap<String, String>();
					mapItems.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
					mapItems.put(MainActivity.DB_COL_FOREIGN_KEY, map.get(MainActivity.DB_COL_ID));
					String _idItem = null;
					if (dbC.isOpen()) {
						dbC.isAvailale();
						_idItem = dbC.retrieveId(mapItems);
					}

					MLog.log("Item Id = " + _idItem);

					HashMap<String, String> mapPictures = new HashMap<String, String>();
					mapPictures.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_PICTURE);
					mapPictures.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);
					ArrayList<HashMap<String, String>> recordsPictures = null;
					if (dbC.isOpen()) {
						dbC.isAvailale();
						recordsPictures = dbC.retrieveRecords(mapPictures);
					}

					MLog.log("Pictures found = " + recordsPictures.size());

					String picture = "";
					if (recordsPictures.size() > 0) {
						mapPictures = recordsPictures.get(0);
						picture = mapPictures.get(MainActivity.DB_COL_PATH_ORIG);
					}

					if (picture.length() > 0) {

						if (activity.checkIfExistsInExternalStorage(picture)) {

							String filePath = MainActivity.STORAGE_PATH + "/" + picture;
							Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromFile(filePath, MainActivity.IMG_TH_MAX_SIZE, MainActivity.IMG_TH_MAX_SIZE);
							Message msg = new Message();
							msg.obj = bmp;
							msg.what = (IV_ID_PREFIX + j);
							MLog.log("Sending message to " + msg.what);
							threadHandler.sendMessage(msg);

						} else {

							thPictDownload = new Thread(this);
							thPictDownload.setName(picture + ";" + (IV_ID_PREFIX + j));
							thPictDownload.start();

						}

					} else {

						Message msg = new Message();
						msg.obj = null;
						msg.what = (IV_ID_PREFIX + j);
						MLog.log("Sending message to " + msg.what);
						threadHandler.sendMessage(msg);

					}

				}

				i+=3;

			}

		}
		
	}
	
	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub

		llContainer = (LinearLayout)v.findViewById(R.id.ll_container);
		
	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
		activity.app.ENABLE_SYNC = true;
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			MLog.log("Displaying = " + msg.what);

			try {

				Bitmap bmp = (Bitmap) msg.obj;
				if (bmp != null) {

					ImageView iv = (ImageView) v.findViewById(msg.what);
					iv.setImageBitmap(bmp);

				} else {

					ImageView iv = (ImageView) v.findViewById(msg.what);
					iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

				}

			} catch (IllegalStateException e) {

			}


		}

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub

		Looper.prepare();

		//activity.handlerLoading.sendEmptyMessage(1);

		Thread t = Thread.currentThread();
		String tName = t.getName();
		String[] strArr = tName.split(";");

		String url = MainActivity.UPLOADS + "/" + strArr[0];
		int id = Integer.parseInt(strArr[1]);
		Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromStream(url, MainActivity.IMG_TH_MAX_SIZE, MainActivity.IMG_TH_MAX_SIZE);
		Message msg = new Message();
		msg.obj = bmp;
		msg.what = id;
		MLog.log("Sending message to " + id);
		threadHandler.sendMessage(msg);

		String filePath = MainActivity.STORAGE_PATH + "/" + strArr[0];
		File file = new File(filePath);
		if(file.exists()) {
			file.delete();
		}

		try {

			FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		//activity.handlerLoading.sendEmptyMessage(0);

	}


	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub
		RUN_FLAG = value;
	}



}
