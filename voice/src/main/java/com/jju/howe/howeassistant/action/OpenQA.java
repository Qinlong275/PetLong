package com.jju.howe.howeassistant.action;


import com.jju.howe.howeassistant.activity.TopActivity;

public class OpenQA {

	private String mText;
	TopActivity mActivity;
	
	public OpenQA(String text, TopActivity activity){
		mText=text;
		mActivity=activity;
	}
	
	public void start(){
		mActivity.speakAnswer(mText);
	}
	
}
