package com.demo.petlong.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesDB extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "notes";
	public static final String CONTENT = "content";
	public static final String PATH = "path";// ͼƬ
	public static final String VIDEO = "video";
	public static final String ID = "_id";
	public static final String TIME = "time";

	public NotesDB(Context context) {
		super(context, "notes", null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + CONTENT
				+ " TEXT NOT NULL," + PATH + " TEXT," + VIDEO + " TEXT," + TIME
				+ " TEXT NOT NULL )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
