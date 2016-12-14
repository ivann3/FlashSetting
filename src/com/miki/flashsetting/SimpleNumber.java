package com.miki.flashsetting;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Shenl 2016.11.11 V03简化版
 */
public class SimpleNumber extends Activity {

	private final static String TAG = "SHEN_LEI";
	
	@SuppressWarnings("unused")
	private Button clean, end, delete, number, autoCopy;
	private TextView filePath, pro_tv, tv_free, progress_tv, progress_tv1,
			current;
	private Context mContext;
	private ProgressBar mProgressBar;
	private int fileCont = 0; // 控制复制文件后缀名，避免重复
	private int freeSum = 0;
	private final String DATABASE_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath();
	private File path = Environment.getExternalStorageDirectory();
	private File cleanFile;

	private Handler stepTimeHandler;
	private Runnable mTicker;
	private int mk = 0;
	private int dataTime = 0;
	private long startTime = 0;
	private float fileTotalSpace = 0;
	private int currentNum = 0;

	private EditText editText;
	private String counts, spendTime, timeSecond, fileSize, copyVelocity,
			completeNumber;
	private TextView tv_spendTime, tv_dataTime, tv_fileSize, tv_copyVelocity;

	private boolean isStop = false;
	private long x, p, blockSize, freeBlocks, allBlocks;
	private float xFloat, pFloat, freeSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.simple);

		initView();
//		filePath.setText(""+cleanFile.getPath());
	}

	public void initView() {
		mContext = this;
//		filePath = (TextView) findViewById(R.id.filePath);
		pro_tv = (TextView) findViewById(R.id.textView);
		tv_free = (TextView) findViewById(R.id.text_free);
		progress_tv = (TextView) findViewById(R.id.progress_tv);
		progress_tv1 = (TextView) findViewById(R.id.progress_tv1);
		current = (TextView) findViewById(R.id.current);
		mProgressBar = (ProgressBar) findViewById(R.id.progress);

		clean = (Button) findViewById(R.id.clean);
		end = (Button) findViewById(R.id.end);
		delete = (Button) findViewById(R.id.delete);
		number = (Button) findViewById(R.id.number);
		autoCopy = (Button) findViewById(R.id.autoCopy);

		editText = (EditText) findViewById(R.id.edt);
		tv_spendTime = (TextView) findViewById(R.id.tv_spendTime);
		tv_copyVelocity = (TextView) findViewById(R.id.tv_copyVelocity);
		tv_dataTime = (TextView) findViewById(R.id.tv_dataTime);
		tv_fileSize = (TextView) findViewById(R.id.tv_fileSize);

		tv_free.setText("剩余内存：" + getSDFreeSize() + "M");
		this.fileTotalSpace = (float) (getResources().openRawResourceFd(
				R.raw.txt).getLength() * 1.0 / 1024.f) / 1024.f;
	}

	public void onStart(View v) {
		switch (v.getId()) {
		case R.id.clean:
			isStop = false;
			deleteSD();
			cleanFile();
			Toast.makeText(mContext, "清除数据完毕", Toast.LENGTH_SHORT).show();
			break;
		case R.id.number:
			currentNum = 0;
			try {
				mk = Integer.parseInt(editText.getText().toString().trim());
			} catch (NumberFormatException e) {
				mk = 0;
			}
			if (mk == 0) {
				Toast.makeText(this, "默认复制100次", Toast.LENGTH_SHORT)
						.show();
			}else{
				Toast.makeText(this, "设置" + mk + "次成功", Toast.LENGTH_SHORT)
				.show();
			}
			break;
		case R.id.autoCopy:
			isStop = true;
			Toast.makeText(mContext, "开始复制", Toast.LENGTH_SHORT).show();
			cleanFile();
			if (mk > 0) {
				timeRun();
				new MyAsyncTask().execute();
			} else {
				mk = 100;
				timeRun();
				new MyAsyncTask().execute();
			}
			break;
		}
	}

	public void timeRun() {
		counts = editText.getText().toString();
		stepTimeHandler = new Handler();
		startTime = System.currentTimeMillis();
		mTicker = new Runnable() {
			public void run() {
				String content = showTimeCount(System.currentTimeMillis()
						- startTime);
				tv_spendTime.setText(content);

				long now = SystemClock.uptimeMillis();
				long next = now + (1000 - now % 1000);
				stepTimeHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}

	public void cleanFile(){
//		current.setText("");
//		tv_copyVelocity.setText("");
//		pro_tv.setText("");
		cleanFile = new File(path,"data.txt");
		try {
			if(!cleanFile.exists()){
				Log.i(TAG, "autoCopy !!exists");
			}else {
				cleanFile.delete();
				Log.i(TAG, "delete data.txt file...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("ShowToast")
	public void deleteSD() {
		freeSum = 0;
		spendTime = tv_spendTime.getText().toString();
		completeNumber = pro_tv.getText().toString();

		File tmp = null;
		File[] fileList = path.listFiles();
		for (int d = 0; d < fileList.length; d++) {
			tmp = fileList[d];
			if (tmp.getName().endsWith(".rar")) {
				tmp.delete();
			}
		}
		Toast.makeText(mContext, "删除成功！", 1000).show();
		tv_free.setText("剩余内存：" + getSDFreeSize() + "M");
		currentNum++;

		UtilsSDCard.SaveUserInfo(currentNum, spendTime, counts, timeSecond,
				fileSize, copyVelocity, completeNumber);
	}

	class MyAsyncTask extends AsyncTask<Integer, Integer, Integer> {
		private static final int FINISH_COPY = 0;

		@Override
		protected void onPreExecute() {}

		@Override
		protected Integer doInBackground(Integer... fileID) {
			if (isStop == true) {
				try {
					fileCont++;
					int sum = 0;
					sum = sum + fileCont;
					String databaseFilename = DATABASE_PATH + "/" + "LS" + sum
							+ ".rar";
					File dir = new File(DATABASE_PATH);
					publishProgress(fileCont);

					if (!dir.exists())
						dir.mkdir();
					if (!(new File(databaseFilename)).exists()) {
						InputStream is = getResources().openRawResource(
								R.raw.txt);
						FileOutputStream fos = new FileOutputStream(
								databaseFilename);
						byte[] buffer = new byte[8192];
						int count = 0;
						while ((count = is.read(buffer)) > 0) {
							fos.write(buffer, 0, count);
						}
						fos.close();
						is.close();

						freeSum++;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return FINISH_COPY;
				}
			}
			return null;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Integer result) {
			StatFs sf = new StatFs(path.getPath());
			blockSize = sf.getBlockSize();
			freeBlocks = sf.getAvailableBlocks();
			if ((freeBlocks * blockSize) == 0 || freeSize==0.0) {
//			if (x==0) {
				stepTimeHandler.removeCallbacks(mTicker);
				deleteSD();
//				if (FINISH_COPY == result) {
//					tv_free.setText("剩余内存：" + getSDFreeSize() + "M");
//				}
				if (mk > 0) {
					if (mk > currentNum) {
						timeRun();
						new MyAsyncTask().execute();
					} else {
						mk = 0;
						Toast.makeText(mContext, "已经全部测试完成", Toast.LENGTH_SHORT).show();
						isStop = false;
					}
				}
			} else {
				new MyAsyncTask().execute();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			tv_free.setText("剩余内存：" + getSDFreeSize() + " M");
			tv_fileSize.setText(freeSum * fileTotalSpace + " M ");
			pro_tv.setText("" + freeSum);
			if (dataTime == 0) {
				tv_copyVelocity.setText("");
			} else {
				tv_copyVelocity.setText("" + (freeSum * fileTotalSpace)
						/ dataTime + " M/s");
			}
		}
	}

	@SuppressWarnings("deprecation")
	public long getSDFreeSize() {
		StatFs sf = new StatFs(path.getPath());
		blockSize = sf.getBlockSize();
		freeBlocks = sf.getAvailableBlocks();
		allBlocks = sf.getBlockCount();

		xFloat = (long)((freeBlocks*blockSize)/1024/1024);
		pFloat = (long)((allBlocks*blockSize)/1024/1024);
		
		fileSize = tv_fileSize.getText().toString();
		freeSize = (xFloat * 100) / pFloat;
		p = (long) (100 - freeSize);
		
		mProgressBar.setProgress((int) p);
		progress_tv1.setText("已用" + p + "%");

		return (freeBlocks*blockSize)/1024/1024; // 单位MB
	}

	public String showTimeCount(long time) {
		if (time >= 360000000) {
			return "00:00:00";
		}
		String timeCount = "";
		long hourc = time / 3600000;
		String hour = "0" + hourc;
		hour = hour.substring(hour.length() - 2, hour.length());

		long minuec = (time - hourc * 3600000) / (60000);
		String minue = "0" + minuec;
		minue = minue.substring(minue.length() - 2, minue.length());

		long secc = (time - hourc * 3600000 - minuec * 60000) / 1000;
		String sec = "0" + secc;
		sec = sec.substring(sec.length() - 2, sec.length());
		timeCount = hour + ":" + minue + ":" + sec;

		dataTime = (int) (minuec * 60 + secc);
		copyVelocity = tv_copyVelocity.getText().toString();

		tv_dataTime.setText(dataTime + "");
		timeSecond = tv_dataTime.getText().toString();
		return timeCount;
	}
}