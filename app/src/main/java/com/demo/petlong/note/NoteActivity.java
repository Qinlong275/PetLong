package com.demo.petlong.note;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.demo.petlong.R;

public class NoteActivity extends Activity implements OnClickListener {

	private Button textbtn, imgbtn, video;
	private ListView lv;
	private Intent i;
	private MyAdapter adapter;
	private NotesDB notesDB;
	private SQLiteDatabase dbReader;
	private Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		initView();
	}

	public void initView() {
		lv = (ListView) findViewById(R.id.list);
		textbtn = (Button) findViewById(R.id.text);
		imgbtn = (Button) findViewById(R.id.img);
		video = (Button) findViewById(R.id.video);
		textbtn.setOnClickListener(this);
		imgbtn.setOnClickListener(this);
		video.setOnClickListener(this);
		notesDB = new NotesDB(this);// ʵ����
		dbReader = notesDB.getReadableDatabase();
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				cursor.moveToPosition(position);
				Intent i = new Intent(NoteActivity.this, SelectAct.class);
				i.putExtra(NotesDB.ID,
						cursor.getInt(cursor.getColumnIndex(NotesDB.ID)));
				i.putExtra(NotesDB.CONTENT, cursor.getString(cursor
						.getColumnIndex(NotesDB.CONTENT)));
				i.putExtra(NotesDB.TIME,
						cursor.getString(cursor.getColumnIndex(NotesDB.TIME)));
				startActivity(i);
			}
		});
	}

	@Override
	public void onClick(View v) {
		i = new Intent(this, AddContent.class);
		switch (v.getId()) {
		case R.id.text:
			i.putExtra("flag", "1");
			startActivity(i);
			break;
		}
	}

	public void selectDB() {
		cursor = dbReader.query(NotesDB.TABLE_NAME, null, null, null, null,
				null, null);
		adapter = new MyAdapter(this, cursor);
		lv.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		selectDB();
	}
}
