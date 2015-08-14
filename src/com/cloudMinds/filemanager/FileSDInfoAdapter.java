
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

import java.util.List;

public class FileSDInfoAdapter extends ArrayAdapter<FileInfo> {
    private LayoutInflater mInflater;
    private Context mContext;

    public FileSDInfoAdapter(Context context, List<FileInfo> objects) {
        super(context, 0, objects);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileInfo fileInfo = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.sd_file_info_item, null);
            holder.sd_file_image = (ImageView) convertView.findViewById(R.id.sd_file_image);
            holder.sd_file_name = (TextView) convertView.findViewById(R.id.sd_file_name);
            holder.sd_file_size = (TextView) convertView.findViewById(R.id.sd_file_size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (fileInfo != null) {
            if (fileInfo.isDir) {
                holder.sd_file_image.setImageResource(R.drawable.folder);
            } else {
                FileIconHelper.setSDCardInfoIcon(fileInfo, holder.sd_file_image);
            }
            holder.sd_file_name.setText(fileInfo.fileName);
            holder.sd_file_size.setText(Util.convertStorage(fileInfo.fileSize));
        }
        return convertView;
    }

    class ViewHolder {
        ImageView sd_file_image;
        TextView sd_file_name;
        TextView sd_file_size;

    }
}
