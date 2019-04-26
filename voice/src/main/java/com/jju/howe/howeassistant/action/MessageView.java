package com.jju.howe.howeassistant.action;

import android.content.Intent;

import com.jju.howe.howeassistant.activity.TopActivity;


public class MessageView {
	private TopActivity mActivity;
	
	public MessageView(TopActivity activity){
		mActivity=activity;
	}
	
	public void start(){
		Intent intent=new Intent();
		intent.setClassName("com.android.mms","com.android.mms.ui.ConversationList");
		mActivity.startActivity(intent);
	}
}
