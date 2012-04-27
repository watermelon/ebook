package com.ebook.scansd;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;

public class BookMark {

	Context mContext;
	String fileName;

	public BookMark(Context context, String filename) {
		mContext = context;
		this.fileName = filename.substring(0, filename.lastIndexOf(".") + 1) + "mark.txt";
	}

	public BookMark(String filename) {
		// TODO Auto-generated constructor stub
		this.fileName = filename.substring(0, filename.lastIndexOf(".") + 1) + "mark.txt";
	}

	public void setContext(Context context) {
		mContext = context;
	}
 
	final static String DATA_URL = "/data/data";

	public void saveFile(String string) {
//		File file = new File(DATA_URL + FILE_NAME);
		try {
			// FileOutputStream outputStream = new FileOutputStream(file);
			FileOutputStream outputStream = mContext.openFileOutput(fileName,
					Context.MODE_WORLD_READABLE);
			outputStream.write(string.getBytes());
			outputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public String loadFile() {

		try {
//			File file = new File(DATA_URL + FILE_NAME);
			FileInputStream inputStream = mContext.openFileInput(fileName);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inputStream.read(buffer)) != -1) {
				stream.write(buffer, 0, length);
			}
			stream.close();
			inputStream.close();
			return stream.toString();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
}
