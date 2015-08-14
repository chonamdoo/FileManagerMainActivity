
package com.cloudMinds.clear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudMinds.filemanager.R;

import java.util.ArrayList;

public class ClearDetialActivity extends Activity implements OnClickListener {

    private Button detialStart;
    private Button detialStop;
    private ArrayList<ClearInfo> infoList = null;
    public CheckBox selectAll;
    private ClearDetialAdapter adapter;
    private int type = -1; // 判断垃圾类型
    private ProgressDialog dialog = null;
    private ArrayList<ClearInfo> clear = null;
    private final int CLEAR_CACHE = 0;
    private final int CLEAR_FILES = 1;
    private final int CLEAR_UPDATE = 2;
    private ListView listView = null;
    private TextView clearTotal;
    private final String ACTION_ADD = "android.intent.action.PACKAGE_ADDED";
    private final String ACTION_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    private final String ACTION_DATA = "android.intent.action.PACKAGE_DATA_CLEARED";
    public int CACHE_POSITION = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.clear_detial);
        initUI();
    }

    private void initUI() {
        selectAll = (CheckBox) findViewById(R.id.detial_select_all);
        clearTotal = (TextView) findViewById(R.id.detial_total);
        detialStart = (Button) findViewById(R.id.detial_start);
        detialStop = (Button) findViewById(R.id.detial_stop);
        TextView titleView = (TextView) findViewById(R.id.detial_title);
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(Constant.CLEAR_TITLE);
        titleView.setText(title);
        type = bundle.getInt(Constant.CLEAR_TYPE, -1);
        // infoList = (ArrayList<ClearInfo>)
        // bundle.getSerializable(Constant.DATA);
        boolean isCheckAll = bundle.getBoolean(Constant.CHECK, false);
        BaseApplication app = (BaseApplication) getApplication();
        if (type == Constant.TYPE_CACHE) {
            infoList = app.getCacheInfo();
            selectAll.setVisibility(View.INVISIBLE);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DATA);
            filter.addDataScheme("package");
            registerReceiver(apkRecevier, filter);
        } else if (type == Constant.TYPE_TEMP_FILE) {
            infoList = app.getTempFileInfo();
        } else if (type == Constant.TYPE_EMPTY_FOLDER) {
            infoList = app.getEmptyFolderInfo();
        } else if (type == Constant.TYPE_THUMB) {
            infoList = app.getThumbInfo();
        } else if (type == Constant.TYPE_SOFTWARE) {
            infoList = app.getSoftwareInfo();
        } else if (type == Constant.TYPE_APK) {
            infoList = app.getApkInfo();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_ADD);
            filter.addAction(ACTION_REMOVED);
            filter.addDataScheme("package");
            registerReceiver(apkRecevier, filter);
        } else if (type == Constant.TYPE_BIG_FILE) {
            infoList = app.getBigFileInfo();
        }

        listView = (ListView) findViewById(R.id.detial_listview);
        adapter = new ClearDetialAdapter(this, infoList);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);

        selectAll.setChecked(isCheckAll);
        if (infoList == null || infoList.size() == 0) {
            selectAll.setVisibility(View.GONE);
            detialStart.setVisibility(View.GONE);
            detialStop.setVisibility(View.GONE);
        } else {
            int size = infoList.size();
            boolean isAllSelected = true;
            for (int i = 0; i < size; i++) {
                isAllSelected &= infoList.get(i).isSelected();
            }
            selectAll.setChecked(isAllSelected);
        }
        detialStop.setOnClickListener(this);
        detialStart.setOnClickListener(this);
        selectAll.setOnClickListener(this);
        updateClearSize();
    }

    private void updateClearSize() {
        int count = infoList.size();
        long length = 0;
        if (infoList != null) {
            for (int i = 0; i < count; i++) {
                length += infoList.get(i).getSize();
            }
        }
        clearTotal.setText(getString(R.string.title_total) + count + getString(R.string.title_file) + ","
                + getString(R.string.title_used) + Util.convertStorage(length) + getString(R.string.title_room));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.detial_start) {
            clear();
        } else if (v.getId() == R.id.detial_stop) {
            onDestroy();
        } else if (v.getId() == R.id.detial_select_all) {
            int size = infoList.size();
            boolean isChecked = selectAll.isChecked();
            for (int i = 0; i < size; i++) {
                infoList.get(i).setSelected(isChecked);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void clear() {
        clear = new ArrayList<ClearInfo>();
        if (infoList == null || infoList.size() == 0) {
            return;
        }
        int size = infoList.size();
        ClearInfo cInfo = null;
        for (int i = 0; i < size; i++) {
            cInfo = infoList.get(i);
            if (cInfo.isSelected()) {
                clear.add(cInfo);
            }
        }
        if (clear == null || clear.size() == 0) {
            Toast.makeText(this, getString(R.string.clear_select), Toast.LENGTH_SHORT).show();
            return;
        }
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.clear_clear));
        dialog.setCancelable(false);
        dialog.show();
        detialStart.setVisibility(View.GONE);
        detialStop.setVisibility(View.VISIBLE);
        detialStop.setText(getString(R.string.stop_clear));
        if (type == Constant.TYPE_CACHE) {
            new ClearCache().clearCache(this, new Callback() {
                @Override
                public void onFinished(boolean successed) {
                    infoList = new ArrayList<ClearInfo>();
                    clearHandler.sendEmptyMessage(CLEAR_CACHE);
                }
            });
        } else {
            Util.delFiles(clear, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    clearHandler.sendEmptyMessage(CLEAR_FILES);
                }
            });
        }
    }

    private Handler clearHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CLEAR_CACHE:
                case CLEAR_FILES:
                    if (infoList != null) {
                        for (int i = 0; i < clear.size(); i++) {
                            infoList.remove(clear.get(i));
                        }
                    }
                    listView = (ListView) findViewById(R.id.detial_listview);
                    adapter = new ClearDetialAdapter(ClearDetialActivity.this, infoList);
                    listView.setAdapter(adapter);
                    listView.setItemsCanFocus(true);
                    if (infoList != null && infoList.size() > 0) {
                        detialStart.setVisibility(View.VISIBLE);
                        detialStop.setVisibility(View.GONE);
                    } else {
                        detialStop.setText(getString(R.string.clear_over));
                    }
                    updateClearSize();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    break;
                case CLEAR_UPDATE:
                    adapter.notifyDataSetChanged();
                    break;
            }
            Toast.makeText(ClearDetialActivity.this, getString(R.string.clear_success), Toast.LENGTH_SHORT).show();
        };
    };

    private BroadcastReceiver apkRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (infoList == null || infoList.size() == 0) {
                return;
            }
            String packageName = intent.getDataString();
            packageName = packageName.substring(packageName.indexOf(":") + 1);
            ClearInfo info = null;
            // 接收安装广播
            if (intent.getAction().equals(ACTION_ADD)) {
                for (int i = 0; i < infoList.size(); i++) {
                    info = infoList.get(i);
                    if (info.getPackageName().equalsIgnoreCase(packageName)) {
                        if (info.getState() == Constant.UNINSTALLED) {
                            info.setState(Constant.INSTALLED);
                        }
                    }
                }
                Toast.makeText(ClearDetialActivity.this, getString(R.string.install_success), Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(ACTION_REMOVED)) { // 接收卸载广播
                for (int i = 0; i < infoList.size(); i++) {
                    info = infoList.get(i);
                    if (info.getPackageName().equalsIgnoreCase(packageName)) {
                        if (info.getState() == Constant.INSTALLED) {
                            info.setState(Constant.UNINSTALLED);
                        }
                    }
                }
            } else if (intent.getAction().equals(ACTION_DATA)) {
                for (int i = 0; i < infoList.size(); i++) {
                    info = infoList.get(i);
                    if (info.getPackageName().equalsIgnoreCase(packageName)) {
                        infoList.remove(info);
                        CACHE_POSITION = -1;
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onDestroy() {
        BaseApplication bapp = (BaseApplication) getApplication();
        if (type == Constant.TYPE_CACHE) {
            bapp.setCacheInfo(infoList);
            try {
                unregisterReceiver(apkRecevier);
            } catch (Exception e) {
            }
        } else if (type == Constant.TYPE_TEMP_FILE) {
            bapp.setTempFileInfo(infoList);
        } else if (type == Constant.TYPE_EMPTY_FOLDER) {
            bapp.setEmptyFolderInfo(infoList);
        } else if (type == Constant.TYPE_THUMB) {
            bapp.setThumbInfo(infoList);
        } else if (type == Constant.TYPE_SOFTWARE) {
            bapp.setSoftwareInfo(infoList);
        } else if (type == Constant.TYPE_APK) {
            bapp.setApkInfo(infoList);
            try {
                unregisterReceiver(apkRecevier);
            } catch (Exception e) {
            }
        } else if (type == Constant.TYPE_BIG_FILE) {
            bapp.setBigFileInfo(infoList);
        }
        Intent intent = new Intent();
        intent.putExtra(Constant.CLEAR_TYPE, type);
        // intent.putParcelableArrayListExtra(Constant.DATA, infoList);
        setResult(Constant.ACTIVITY_RESULT, intent);
        finish();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CACHE_FOR_RESULT) {
            if (CACHE_POSITION == -1 || CACHE_POSITION >= infoList.size()) {
                return;
            }
            new CacheInfo(this).checkCache(infoList.get(CACHE_POSITION).getPackageName(),
                    new Callback() {
                        @Override
                        public void onFinished(boolean successed) {
                            if (successed) {
                                infoList.remove(CACHE_POSITION);
                                CACHE_POSITION = -1;
                                clearHandler.sendEmptyMessage(CLEAR_UPDATE);
                            }
                        }
                    });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onDestroy();
        }
        return false;
    };
}
