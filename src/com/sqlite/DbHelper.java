package com.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "sec_db";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_NAME = "book_mark";
	public final static String FIELD_ID = "_id";
	public final static String FIELD_FILENAME = "filename";
	public final static String FIELD_BOOKMARK = "bookmark";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		StringBuffer sqlCreateCountTb = new StringBuffer();
		sqlCreateCountTb.append("create table ").append(TABLE_NAME)
		   .append("(_id integer primary key autoincrement,")		   
		   .append(" filename text,")    
		   .append(" bookmark text);");   
		db.execSQL(sqlCreateCountTb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		String sql = " DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);

	}

	public Cursor select() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null,
				" _id desc");
		return cursor;
	}

	public long insert(String Title) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FIELD_BOOKMARK, Title);
		long row = db.insert(TABLE_NAME, null, cv);
		return row;
	}
	
	public long insert(String filename, String bookmark) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FIELD_FILENAME, filename);
		cv.put(FIELD_BOOKMARK, bookmark);
		long row = db.insert(TABLE_NAME, null, cv);
		return row;
	}
	
	public void delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);
	}

	public void update(int id, String filename, String bookmark) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Integer.toString(id) };
		ContentValues cv = new ContentValues();
		cv.put(FIELD_FILENAME, filename);
		cv.put(FIELD_BOOKMARK, bookmark);
		db.update(TABLE_NAME, cv, where, whereValue);
	}
}
