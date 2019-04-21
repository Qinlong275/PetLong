package com.demo.petlong.note;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demo.petlong.R;

public class MyAdapter extends BaseAdapter {

	private Context context;
	private Cursor cursor;// ���ݿ��ѯ������Ϊcursor����
	private LinearLayout layout;

	public MyAdapter(Context context, Cursor cursor) {
		this.context = context;
		this.cursor = cursor;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return cursor.getPosition();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = LayoutInflater.from(context);// ������ͼ��Ȩ��
		layout = (LinearLayout) inflater.inflate(R.layout.cell, null);
		TextView contenttv = (TextView) layout.findViewById(R.id.list_content);
		TextView timetv = (TextView) layout.findViewById(R.id.list_time);
		ImageView imgiv = (ImageView) layout.findViewById(R.id.list_img);
		ImageView videoiv = (ImageView) layout.findViewById(R.id.list_video);// ��Ƶ�еĵ�һ��ͼ
		cursor.moveToPosition(position);
		String content = cursor.getString(cursor.getColumnIndex("content"));
		String time = cursor.getString(cursor.getColumnIndex("time"));
		String url = cursor.getString(cursor.getColumnIndex("path"));
		String urlvideo = cursor.getString(cursor.getColumnIndex("video"));
		contenttv.setText(content);
		timetv.setText(time);
		videoiv.setImageBitmap(getVideoThumbnail(urlvideo, 200, 200,
				MediaStore.Images.Thumbnails.MICRO_KIND));
		imgiv.setImageBitmap(getImageThumbnail(url, 200, 200));
		return layout;
	}

	public Bitmap getImageThumbnail(String uri, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(uri, options);
		options.inJustDecodeBounds = false;
		int beWidth = options.outWidth / width;
		int beHight = options.outHeight / height;
		int be = 1;
		if (beWidth < beHight) {
			be = beWidth;
		} else {
			be = beHight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(uri, options);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;

	}

	private Bitmap getVideoThumbnail(String uri, int width, int height, int kind) {
		Bitmap bitmap = null;
		bitmap = ThumbnailUtils.createVideoThumbnail(uri, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
}
