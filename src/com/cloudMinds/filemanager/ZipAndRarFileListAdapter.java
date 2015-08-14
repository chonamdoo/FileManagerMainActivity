
package com.cloudMinds.filemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudMinds.filemanager.R;
import com.cloudMinds.utils.Util;
import com.cloudMinds.utils.ZipAndRARUtil;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.util.List;

public class ZipAndRarFileListAdapter extends ArrayAdapter<FileObject> {
    private LayoutInflater mInflater;
    private Context mContext;

    public ZipAndRarFileListAdapter(Context context, List<FileObject> objects) {
        super(context, 0, objects);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileObject object = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.file_list_item, null);
            holder.fileImageFrame = (ImageView) convertView.findViewById(R.id.file_image_frame);
            holder.fileImage = (ImageView) convertView.findViewById(R.id.file_image);
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.fileCount = (TextView) convertView.findViewById(R.id.file_count);
            holder.fileModifiedDate = (TextView) convertView.findViewById(R.id.modified_time);
            holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            holder.fileCheckBoxArea = convertView.findViewById(R.id.file_checkbox_area);
            holder.fileCheckBox = (ImageView) convertView.findViewById(R.id.file_checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (object != null) {
            holder.fileCheckBox.setVisibility(View.GONE);
            holder.fileCheckBoxArea.setVisibility(View.GONE);
            holder.fileCount.setVisibility(View.GONE);
            holder.fileImageFrame.setVisibility(View.GONE);
            holder.fileModifiedDate.setVisibility(View.VISIBLE);
            long time = 0;
            long size = 0;
            try {
                time = object.getContent().getLastModifiedTime();
                if (object.getType() == FileType.FILE)
                    size = object.getContent().getSize();
            } catch (FileSystemException e1) {
                e1.printStackTrace();
            }
            holder.fileModifiedDate.setText(Util.formatDateString(mContext, time));
            holder.fileName.setText(Util.getNameFromFilepath(object.getName().toString()));
            try {
                if (object.getType() == FileType.FOLDER) {
                    holder.fileImage.setImageResource(R.drawable.folder);
                    holder.fileSize.setVisibility(View.GONE);
                } else {
                    ZipAndRARUtil.setIcon(object.getName().toString(), holder.fileImage);
                    holder.fileSize.setVisibility(View.VISIBLE);
                    holder.fileSize.setText(Util.convertStorage(size));
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView fileImageFrame;
        ImageView fileImage;
        TextView fileName;
        TextView fileCount;
        TextView fileModifiedDate;
        TextView fileSize;
        View fileCheckBoxArea;
        ImageView fileCheckBox;
    }
}
