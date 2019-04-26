package com.jju.howe.howeassistant.action;

import android.content.Intent;

import com.jju.howe.howeassistant.activity.TopActivity;

public class CallView {

    private TopActivity mActivity;

    public CallView(TopActivity activity) {
        mActivity = activity;
    }

    public void start() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL_BUTTON);
        mActivity.startActivity(intent);
    }
}
