
package com.cloudMinds.clear;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudMinds.filemanager.R;

import java.io.File;
import java.util.List;

public class ClearDetialAdapter extends BaseAdapter {
    private List<ClearInfo> infoList;
    private Context mContext;

    public ClearDetialAdapter(Context _context, List<ClearInfo> infos) {
        this.mContext = _context;
        this.infoList = infos;
    }

    @Override
    public int getCount() {
        if (infoList != null && infoList.size() > 0) {
            return infoList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (infoList != null && infoList.size() > 0) {
            return infoList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.clear_detial_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.detial_item_icon);
            holder.name = (TextView) convertView.findViewById(R.id.detial_item_name);
            holder.size = (TextView) convertView.findViewById(R.id.detial_item_size);
            holder.state = (TextView) convertView.findViewById(R.id.detial_item_state);
            holder.version = (TextView) convertView.findViewById(R.id.detial_item_version);
            holder.selected = (CheckBox) convertView.findViewById(R.id.detial_item_selected);
            holder.layout = (LinearLayout) convertView.findViewById(R.id.detial_item_layout);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        ClearInfo info = infoList.get(position);
        if (info == null) {
            return null;
        }
        if (holder.icon != null) {
            if (info.getIcon() != null) {
                holder.icon.setImageBitmap(info.getIcon());
            } else {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.file_icon_default));
            }
        }
        if (holder.name != null) {
            if (!TextUtils.isEmpty(info.getName())) {
                holder.name.setText(info.getName());
            } else {
                holder.name.setText(mContext.getString(R.string.unknown));
            }
        }
        if (holder.size != null) {
            holder.size.setText(Util.convertStorage(info.getSize()));
        }

        if (holder.selected != null) {
            if (info.getState() != Constant.CACHE_STATE) {
                holder.selected.setChecked(info.isSelected());
                holder.selected.setOnClickListener(new CheckBoxClick(holder.selected, position));
            } else {
                holder.selected.setVisibility(View.GONE);
            }
        }

        if (holder.state != null) {
            if ((info.getState() == Constant.INSTALLED || info.getState() == Constant.UNINSTALLED
                    || info.getState() == Constant.INSTALLED_UPDATE || info.getState() == Constant.UNKNOWN)) {
                holder.state.setVisibility(View.VISIBLE);
                if (info.getState() == Constant.INSTALLED) {
                    holder.state.setText(mContext.getString(R.string.installed));
                } else if (info.getState() == Constant.UNINSTALLED) {
                    holder.state.setText(mContext.getString(R.string.uninstalled));
                } else if (info.getState() == Constant.INSTALLED_UPDATE) {
                    holder.state.setText(mContext.getString(R.string.update));
                } else {
                    holder.state.setText(mContext.getString(R.string.unknown));
                }
            } else {
                holder.state.setVisibility(View.GONE);
            }
        }
        if (holder.version != null) {
            if (!TextUtils.isEmpty(info.getVersion())) {
                holder.version.setVisibility(View.VISIBLE);
                holder.version.setText(info.getVersion());
            } else {
                holder.version.setVisibility(View.GONE);
            }
        }

        if (holder.layout != null) {
            holder.layout.setOnClickListener(new LayoutClick(position));
        }
        return convertView;
    }

    private class Holder {
        ImageView icon;
        TextView name;
        TextView size;
        TextView state;
        TextView version;
        CheckBox selected;
        LinearLayout layout;
    }

    private class LayoutClick implements OnClickListener {
        private int position;

        public LayoutClick(int pos) {
            this.position = pos;
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setMessage(infoList.get(position).getPath());
            final int value = infoList.get(position).getState();
            if (value != -1) {
                String show = null;
                if (value == Constant.CACHE_STATE) {
                    dialog.setMessage(mContext.getString(R.string.clear_cache_prompt));
                    show = mContext.getString(R.string.clear_cache);
                } else if (value == Constant.INSTALLED) {
                    show = mContext.getString(R.string.uninstall);
                } else if (value == Constant.UNINSTALLED) {
                    show = mContext.getString(R.string.install);
                } else if (value == Constant.INSTALLED_UPDATE) {
                    show = mContext.getString(R.string.update);
                }
                if (show != null) {
                    dialog.setNegativeButton(show, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (value == Constant.CACHE_STATE) {
                                clearCache(infoList.get(position).getPackageName(), position);
                            } else if (value == Constant.INSTALLED) {
                                unInstallAPK(infoList.get(position).getPackageName());
                            } else if (value == Constant.UNINSTALLED) {
                                installAPK(infoList.get(position).getPath());
                            } else if (value == Constant.INSTALLED_UPDATE) {
                                installAPK(infoList.get(position).getPath());
                            }
                        }
                    });
                } 
            }
            dialog.setPositiveButton(mContext.getString(R.string.cancel), null);
            dialog.show();
        }
    }

    private void clearCache(String packageName, int pos) {
        Intent intent = new
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + packageName);
        intent.setData(uri);
        ((ClearDetialActivity) mContext).startActivityForResult(intent, Constant.CACHE_FOR_RESULT);
        ((ClearDetialActivity) mContext).CACHE_POSITION = pos;

    }

    private void unInstallAPK(String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        mContext.startActivity(uninstallIntent);
    }

    private void installAPK(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private class CheckBoxClick implements OnClickListener {
        private CheckBox cbox;
        private int position = -1;

        public CheckBoxClick(CheckBox cb, int pos) {
            this.cbox = cb;
            this.position = pos;
        }

        @Override
        public void onClick(View v) {
            boolean isChecked = cbox.isChecked();
            infoList.get(position).setSelected(isChecked);
            if (isChecked) {
                boolean isAllChecked = true;
                int size = infoList.size();
                for (int i = 0; i < size; i++) {
                    isAllChecked &= infoList.get(i).isSelected();
                }
                ((ClearDetialActivity) mContext).selectAll.setChecked(isAllChecked);
            } else {
                ((ClearDetialActivity) mContext).selectAll.setChecked(false);
            }

        }
    }
}
