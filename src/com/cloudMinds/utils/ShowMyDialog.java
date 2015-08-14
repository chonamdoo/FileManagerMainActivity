
package com.cloudMinds.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public class ShowMyDialog {
    Context mContext;
    String mTitle;
    String mMsg;
    OnFinishListener mOnFinisher;
    MyDialog myDialog;

    public interface OnFinishListener {
        boolean onFinish(String txt);
    }

    public ShowMyDialog(Context mContext, String mTitle, String mMsg,
            OnFinishListener onFinishListener) {
        super();
        this.mContext = mContext;
        this.mTitle = mTitle;
        this.mMsg = mMsg;
        this.mOnFinisher = onFinishListener;
        myDialog = new MyDialog(mContext);
    }

    public void show() {
        myDialog.setDialogTitle(mTitle);
        myDialog.setMessage(mMsg);
        myDialog.setPositiveButton(mContext.getString(android.R.string.ok), new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(myDialog.getInputMessage())
                        && myDialog.getInputMessage().length() <= 85) {

                    if (mOnFinisher.onFinish(myDialog.getInputMessage())) {
                        myDialog.dismiss();
                    }
                }
            }
        });
        myDialog.setNegativeButton(mContext.getString(android.R.string.cancel),
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        myDialog.dismiss();

                    }
                });
    }
}
