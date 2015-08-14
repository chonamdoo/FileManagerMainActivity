
package com.cloudMinds.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cloudMinds.filemanager.R;

public class MyDialog extends Dialog {
    Context mContext;
    android.app.Dialog mDialog;   
    TextView titleView;
    EditText messageView;
    LinearLayout buttonLayout;

    public MyDialog(Context context) {
        super(context);
        this.mContext = context;
        mDialog = new android.app.Dialog(mContext);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        // 关键在下面的两行,使用window.setContentView,替换整个对话框窗口的布局
        Window window = mDialog.getWindow();
        window.setContentView(R.layout.alertdialog);
        titleView = (TextView) window.findViewById(R.id.title);
        titleView.setText(R.string.operation_rename_message);
        messageView = (EditText) window.findViewById(R.id.message);
        messageView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (messageView.getText().toString().length() > 85) {
                    messageView.setError(mContext.getString(R.string.maximumlength));
                }
                else if (TextUtils.isEmpty(messageView.getText()) || s.toString().length() <= 0) {
                    messageView.setError(mContext.getString(R.string.toast_filename_is_not_empty));
                } else {
                    messageView.setError(null);
                }
            }
        });
        buttonLayout = (LinearLayout) window.findViewById(R.id.buttonLayout);
    }

    public void setTitle(int resId)
    {
        titleView.setText(resId);
    }

    public void setMessage(int resId) {
        messageView.setText(resId);
    }

    public String getInputMessage() {
        return messageView.getText().toString();
    }

    public void setMessage(String message)
    {
        messageView.setText(message);
    }

    public EditText getEditText() {
        return messageView;
    }

    public void setDialogTitle(String title) {
        mDialog.setTitle(title);
    }

    /**
     * 设置按钮
     * 
     * @param text
     * @param listener
     */
    public void setPositiveButton(String text, final View.OnClickListener listener)
    {
        Button button = new Button(mContext);
        LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        button.setText(text);
        button.setLayoutParams(params);
        button.setBackgroundColor(Color.WHITE);
        button.setTextColor(Color.BLACK);
        button.setTextSize(18);
        button.setOnClickListener(listener);
        buttonLayout.addView(button);
        View view = new View(mContext);
        view.setBackgroundResource(R.color.mydialogcolor);
        LinearLayout.LayoutParams mParams = new LayoutParams(1, LayoutParams.FILL_PARENT);
        mParams.setMargins(40, 0, 0, 0);
        view.setLayoutParams(mParams);
        buttonLayout.addView(view, 1);
    }

    /**
     * 设置按钮
     * 
     * @param text
     * @param listener
     */
    public void setNegativeButton(String text, final View.OnClickListener listener)
    {
        Button button = new Button(mContext);
        LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);
        button.setText(text);
        button.setBackgroundColor(Color.WHITE);
        button.setTextColor(Color.BLACK);
        button.setTextSize(18);
        button.setOnClickListener(listener);
        if (buttonLayout.getChildCount() > 0)
        {
            params.setMargins(40, 0, 0, 0);
            button.setLayoutParams(params);
            buttonLayout.addView(button, 2);
        } else {
            button.setLayoutParams(params);
            buttonLayout.addView(button);
        }

    }

    /**
     * 关闭对话框
     */
    public void dismiss() {
        mDialog.dismiss();
    }

}
