package com.ebook;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ebook.bookpage.BookPageFactory;
import com.ebook.bookpage.PageWidget;
import com.sqlite.DbHelper;

public class EbookActivity extends Activity {

	public final static int OPENMARK = 0;
	public final static int SAVEMARK = 1;
	public final static int TEXTSET = 2;

	private PageWidget mPageWidget;
	private BookPageFactory pagefactory;
	DbHelper db;
	Bitmap mCurPageBitmap, mNextPageBitmap;
	Canvas mCurPageCanvas, mNextPageCanvas;
	Context mContext;
	Cursor mCursor;
	final String[] items = { "书签1 未使用", "书签2 未使用", "书签3 未使用", "书签4 未使用", "自动书签 未使用" };
	String fileName;
	String path;
	int curPostion;
	int screenWidth;
	int screenHeight;
	int bookMarkPos;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		initScreedWH();
		mPageWidget = new PageWidget(this);
		mPageWidget.setWidth(screenWidth);
		mPageWidget.setHeight(screenHeight);
		mContext = this;

		initBookMark();

		setContentView(mPageWidget);

		initBookPage();

		String pos = this.getIntent().getStringExtra("pos");

		if (!isViewIntent()) {
			path = this.getIntent().getStringExtra("pathes");

		} else {
			path = this.getIntent().getData().getPath();
		}

		try {
			Log.i("path", path);
			pagefactory.openBook(path);
			fileName = pagefactory.getFileName();
			if (pos != null) {
				pagefactory.setBeginPos(Integer.valueOf(pos));
				pagefactory.pageUp();
			}
			pagefactory.onDraw(mCurPageCanvas);
		} catch (IOException e1) {
			Toast.makeText(this, fileName + "不存在，请将文件放在SD卡根目录下,可以超过100M容量",
					Toast.LENGTH_LONG).show();
		}

		mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);

		mPageWidget.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent e) {
				// TODO Auto-generated method stub
				boolean ret = false;
				if (v == mPageWidget) {
					if (e.getAction() == MotionEvent.ACTION_DOWN) {
						mPageWidget.abortAnimation();
						mPageWidget.calcCornerXY(e.getX(), e.getY());
						pagefactory.onDraw(mCurPageCanvas);
						if (mPageWidget.DragToRight()) {
							try {
								pagefactory.prePage();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							if (pagefactory.isFirstPage())
								return false;
							pagefactory.onDraw(mNextPageCanvas);
						} else {
							try {
								pagefactory.nextPage();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							if (pagefactory.isLastPage()) {
								return false;
							}
							pagefactory.onDraw(mNextPageCanvas);
						}
						mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
					}
					ret = mPageWidget.doTouchEvent(e);

					return ret;
				}
				return false;
			}
		});
	}

	private void initScreedWH() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenHeight = dm.heightPixels;
		screenWidth = dm.widthPixels;
	}

	private boolean isViewIntent() {
		String action = getIntent().getAction();
		return Intent.ACTION_VIEW.equals(action);
	}

	private void initBookPage() {
		mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);

		mCurPageCanvas = new Canvas(mCurPageBitmap);
		mNextPageCanvas = new Canvas(mNextPageBitmap);
		pagefactory = new BookPageFactory(screenWidth, screenHeight);

		pagefactory.setBgBitmap(BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.shelf_bkg));
	}

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

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, fileName);
		menu.add(0, 2, 2, "书签");
		menu.add(0, 3, 3, "设置");
		menu.add(0, 4, 4, "退出");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Toast.makeText(this, "您正在观看： " + fileName, Toast.LENGTH_SHORT)
					.show();
			return true;
		case 2:
			showDialog(OPENMARK);
			return true;

		case 3:
			showDialog(TEXTSET);
			break;

		case 4:
			finish();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case OPENMARK:
			return new AlertDialog.Builder(mContext)
					.setTitle(R.string.bookmark)

					.setSingleChoiceItems(items, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									bookMarkPos = which;
								}
							})
					.setPositiveButton(R.string.load_bookmark,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Message msg = new Message();
									msg.what = OPENMARK;
									msg.arg1 = bookMarkPos;
									mhHandler.sendMessage(msg);
								}
							})
					.setNegativeButton(R.string.save_bookmark,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Message msg = new Message();
									msg.what = SAVEMARK;
									msg.arg1 = bookMarkPos;
									curPostion = pagefactory.getCurPostion();
									msg.arg2 = curPostion;
									mhHandler.sendMessage(msg);
								}
							}).create();
		case TEXTSET:
			String color[] = new String[] { "红色", "深灰色", "黄色", "蓝色", "黑色" };
			return new AlertDialog.Builder(mContext)
					.setTitle("字体颜色设置")
					.setSingleChoiceItems(color, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									bookMarkPos = which;
								}
							})
					.setPositiveButton("ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Message msg = new Message();
									msg.what = TEXTSET;
									switch (bookMarkPos) {
									case 0:
										msg.arg1 = Color.RED;
										break;
									case 1:
										msg.arg1 = Color.DKGRAY;
										break;
									case 2:
										msg.arg1 = Color.YELLOW;
										break;
									case 3:
										msg.arg1 = Color.BLUE;
										break;
									case 4:
										msg.arg1 = Color.BLACK;
										break;

									default:
										break;
									}
									mhHandler.sendMessage(msg);
								}
							})
					.setNegativeButton("cancle",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).create();
		default:
			break;
		}
		return null;

	}

	Handler mhHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case TEXTSET:
				pagefactory.changBackGround(msg.arg1);
				pagefactory.onDraw(mCurPageCanvas);
				mPageWidget.postInvalidate();
				break;

			case OPENMARK:
				try {
					mCursor = db.select();

				} catch (Exception e) {
					e.printStackTrace();
				}
				if (mCursor.getCount() > 0) {
					mCursor.moveToPosition(mCursor.getCount() - (msg.arg1 + 1));
					String pos = mCursor.getString(2);
					String tmp = mCursor.getString(1);
					
					if (fileName.equals(tmp.substring(tmp.lastIndexOf('/') + 1,
							tmp.length()))) {

						pagefactory.setBeginPos(Integer.valueOf(pos));
						try {
							pagefactory.prePage();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						pagefactory.onDraw(mNextPageCanvas);
						mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
						mPageWidget.invalidate();
						db.close();
					} else {
						Intent intent = new Intent(EbookActivity.this,
								EbookActivity.class);
						intent.putExtra("pathes", mCursor.getString(1));
						intent.putExtra("pos", mCursor.getString(2));
						db.close();
						startActivity(intent);
						finish();
					}
				}
				break;

			case SAVEMARK:
				try {
					mCursor = db.select();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (mCursor.getCount() > 0 && mCursor.getCount() > msg.arg1) {
					mCursor.moveToPosition(mCursor.getCount() - (msg.arg1 + 1));
					db.update(mCursor.getInt(0), path, String.valueOf(msg.arg2));
				} else {
					db.insert(path, String.valueOf(msg.arg2));
				}
				db.close();
				items[msg.arg1] = path.substring(path.lastIndexOf('/') + 1,
						path.length()) + ": " + String.valueOf(msg.arg2);
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
}