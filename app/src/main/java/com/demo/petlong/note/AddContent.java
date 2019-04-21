package com.demo.petlong.note;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import com.demo.petlong.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddContent extends Activity implements OnClickListener {

	private String val;
	private Button savebtn, deletebtn;
	private EditText ettext;

	private NotesDB notesDB;
	private SQLiteDatabase dbWriter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addcontent);
		val = getIntent().getStringExtra("flag");
		savebtn = (Button) findViewById(R.id.save);
		deletebtn = (Button) findViewById(R.id.delete);
		ettext = (EditText) findViewById(R.id.ettext);
		savebtn.setOnClickListener(this);
		deletebtn.setOnClickListener(this);

		notesDB = new NotesDB(this);
		dbWriter = notesDB.getWritableDatabase();
		initView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			addDB();
			finish();
			break;
		case R.id.delete:
			finish();
			break;
		}
	}


	public void addDB() {
		ContentValues cv = new ContentValues();
		cv.put(NotesDB.CONTENT, ettext.getText().toString());
		cv.put(NotesDB.TIME, getTime());
		dbWriter.insert(NotesDB.TABLE_NAME, null, cv);
	}

	private String getTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");
		Date date = new Date();
		String str = format.format(date);
		return str;
	}

	public void initView() {
	}

}
