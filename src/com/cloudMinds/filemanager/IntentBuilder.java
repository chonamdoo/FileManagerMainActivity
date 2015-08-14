/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */

package com.cloudMinds.filemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.cloudMinds.filemanager.R;
import com.cloudMinds.utils.ZipAndRARUtil;

import java.io.File;
import java.util.ArrayList;

public class IntentBuilder {

    public static void viewFile(final Context context, final String filePath) {
        String type = getMimeType(filePath);
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            /* 设置intent的file与MimeType */
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.no_app_open, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {

            // unknown MimeType
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.dialog_select_type);

            CharSequence[] menuItemArray = new CharSequence[] {
                    context.getString(R.string.dialog_type_text),
                    context.getString(R.string.dialog_type_audio),
                    context.getString(R.string.dialog_type_video),
                    context.getString(R.string.dialog_type_image)
            };
            dialogBuilder.setItems(menuItemArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String selectType = "*/*";
                    switch (which) {
                        case 0:
                            selectType = "text/plain";
                            break;
                        case 1:
                            selectType = "audio/*";
                            break;
                        case 2:
                            selectType = "video/*";
                            break;
                        case 3:
                            selectType = "image/*";
                            break;
                    }
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(filePath)), selectType);
                    context.startActivity(intent);
                }
            });
            dialogBuilder.show();
        }
    }

    public static void viewZipOrRARFile(final Context mContext, final Handler mHandler,
            final String filePath) {
        String type = getMimeType(filePath);
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            /* 设置intent的file与MimeType */
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendMessage(ZipAndRARUtil.createMsg(ZipAndRARUtil.ERROR,
                        R.string.no_app_open));
                return;
            }
        } else {
            mHandler.sendMessage(ZipAndRARUtil.createMsg(ZipAndRARUtil.ERROR,
                    R.string.toast_no_support));
            return;
        }
    }

    public static Intent buildSendFile(ArrayList<FileInfo> files) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        String mimeType = "*/*";
        for (FileInfo file : files) {
            if (file.isDir)
                continue;

            File fileIn = new File(file.filePath);
            mimeType = getMimeType(file.fileName);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() == 0)
            return null;

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
                : android.content.Intent.ACTION_SEND);

        if (multiple) {
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }

        return intent;
    }

    public static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "*/*";

        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);

        return mimeType != null ? mimeType : "*/*";
    }
}
