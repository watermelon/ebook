package com.ebook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ebook.scansd.ScanSD;
import com.sqlite.DbHelper;

public class MainMenu extends Activity {

	public final static int OPENMARK = 0;
	ScanSD scanSD;
	Intent intent;
	Context mContext;
	RadioButton nextPage;
	RadioButton bmRadioButton;
	ListView ListView;
	TextView textView;
	ImageView storm;
	private DbHelper db;
	private int pos;
	final String[] items = { "书签1 未使用", "书签2 未使用", "书签3 未使用", "书签4 未使用", "自动书签 未使用" };
	Cursor mCursor;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.mainmenu);
		intent = new Intent();
		mContext = this;

		scanSD = new ScanSD();

		findView();
		initBookMark();
		initListView();
		textView.setOnClickListener(radButtonClicked);
		nextPage.setOnClickListener(radButtonClicked);
		bmRadioButton.setOnClickListener(radButtonClicked);
	}

	private void findView() {
		textView = (TextView) findViewById(R.id.textView1);
		bmRadioButton = (RadioButton) findViewById(R.id.bookmark);
		storm = (ImageView) findViewById(R.id.storm);
		nextPage = (RadioButton) findViewById(R.id.radio_next);
		ListView = (ListView) findViewById(R.id.listfile);
	}

	private OnClickListener radButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
			case R.id.bookmark:
				showDialog(0);
				break;

			case R.id.radio_next:
				if (nextPage.getText().equals("下一页")) {
					nextPage.setText("上一页");
				} else {
					nextPage.setText("下一页");
				}
				break;
			case R.id.textView1:
				Toast.makeText(MainMenu.this, "thanks", 500).show();
				finish();
				break;
			}
		}
	};

	private void initBookMark() {
		db = new DbHelper(mContext);
		try {
			mCursor = db.select();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCursor.getCount() > 0) {
			for (int i = 0; i < mCursor.getCount(); i++) {
				mCursor.moveToPosition(mCursor.getCount() - (i + 1));
				String str = mCursor.getString(1);
				str = str.substring(str.lastIndexOf('/') + 1, str.length());
				items[i] = str + ": " + mCursor.getString(2);
			}
		}
		db.close();
	}

	private void initListView() {
		ArrayList<File> list = new ArrayList<File>();
		list = scanSD.getFileList();

		final SimpleAdapter adapter = new SimpleAdapter(this,
				scanSD.getMapData(list), R.layout.relative,
				new String[] { "ItemName" }, new int[] { R.id.ItemText });

		ListView.setAdapter(adapter);
		ListView.setSelector(R.drawable.item_selector);
		ListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				String path;
				@SuppressWarnings("unchecked")
				HashMap<String, String> map = (HashMap<String, String>) ListView
						.getItemAtPosition(pos);
				// String name = map.get("ItemText");
				path = map.get("ItemPath");
				
				intent.setClass(mContext, EbookActivity.class);
				intent.putExtra("pathes", path);
				startActivity(intent);
				adapter.notifyDataSetChanged(); // 通知adapter刷新数据
			}
		});
	}

	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(mContext)
				.setTitle(R.string.bookmark)

				.setSingleChoiceItems(items, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								pos = which;
							}
						})
				.setPositiveButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNegativeButton(R.string.load_bookmark,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Message msg = new Message();
								msg.what = 0;
								msg.arg1 = pos;
								mhHandler.sendMessage(msg);
							}
						}).create();
	}

	Handler mhHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case OPENMARK:
				try {
					mCursor = db.select();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (mCursor.getCount() > 0) {
					mCursor.moveToPosition(mCursor.getCount() - (msg.arg1 + 1));
					Log.i("string", items[mCursor.getCount() - (msg.arg1 + 1)]);
					intent.setClass(mContext, EbookActivity.class);
					intent.putExtra("pathes", mCursor.getString(1));
					intent.putExtra("pos", mCursor.getString(2));
					startActivity(intent);
				}
				db.close();// 打开之后记得关闭数据库
				break;
				
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
}