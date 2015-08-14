
package com.cloudMinds.filemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cloudMinds.filemanager.R;
import com.cloudMinds.filemanager.FileCategoryHelper.FileCategory;
import com.cloudMinds.utils.CategoryInfo;
import com.cloudMinds.utils.SDInfoUtil;
import com.cloudMinds.utils.Util;

public class FileSDCardInfoFragment extends Fragment {

    private final int RECT_SIDE = 18;
    private static final int SHOW_PROGRESSBAR = 1;
    private static final int CLOSE_PROGRESSBAR = 0;
    private static final int ADD_VIEW = 2;
    private static final int SHOW_ROM_INFO = 3;
    private LinearLayout sdLayout;
    private LinearLayout internalLayout;
    private View sdView;
    private View mRootView;
    private SDInfoUtil infoUtil;
    private boolean isAddView = false;
    private ScrollView sdInfo;
    private LinearLayout progress;
    private Thread sdInfoThread;
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.info, container, false);
        sdInfo = (ScrollView) mRootView.findViewById(R.id.sd_detial);
        progress = (LinearLayout) mRootView.findViewById(R.id.sd_progress);
        sdLayout = (LinearLayout) mRootView.findViewById(R.id.sd_layout);
        infoUtil = new SDInfoUtil(getActivity());
        handler.sendEmptyMessage(SHOW_PROGRESSBAR);
        sdInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileSettingsHelper settingsHelper = FileSettingsHelper.getInstance(getActivity());
                    FileOperationHelper operationHelper = FileOperationHelper.getInstance(getActivity());
                    FileSDCardHelper cardHelper = FileSDCardHelper.getInstance(getActivity(), settingsHelper,
                            operationHelper);
                    HashMap<FileCategory, CategoryInfo> mCategoryInfo = null;

                    ArrayList<SDCardInfo> cardInfo = cardHelper.getAllRoot();
                    if (cardInfo != null && cardInfo.size() > 0) {
                        for (int i = 0; i < cardInfo.size(); i++) {
                            sdView = LayoutInflater.from(getActivity()).inflate(R.layout.sd_info, null);
                            SDCardInfo info = cardInfo.get(i);
                            mCategoryInfo = infoUtil.getCategoryInfo(info.path);
                           // internalLayout = (LinearLayout) mRootView.findViewById(R.id.internal_piechart);// 饼图
                            long[] data = infoUtil.getDirectoryStorage(info.path);
                            showSDCardInfo(sdView, data, info.type);
                            //DrawPieChart(sdView, mCategoryInfo, data);
                            setSDInfo(sdView, mCategoryInfo, data);
                            sdView.invalidate();
                            handler.sendEmptyMessage(ADD_VIEW);
                            while (!isAddView) {
                                Thread.sleep(100);
                            }
                        }
                        handler.sendEmptyMessage(SHOW_ROM_INFO);

                    }
                    handler.sendEmptyMessage(CLOSE_PROGRESSBAR);
                } catch (Exception e) {
                }
            }
        });
        sdInfoThread.start();
        return mRootView;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESSBAR:
                    // ((FileSDCardMainActivity)
                    // mContext).showProgress(mContext.getString(
                    // R.string.message_data_to_large));
                    break;

                case CLOSE_PROGRESSBAR:
                    // ((FileSDCardMainActivity) mContext).closeProgress();
                    sdInfo.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    break;
                case ADD_VIEW:
                    ImageView dividing = new ImageView(getActivity());
                    dividing.setImageResource(R.drawable.line);
                    dividing.setPadding(0, 2, 0, 2);
                    sdLayout.addView(dividing);
                    sdLayout.addView(sdView);
                    isAddView = true;
                    break;
                case SHOW_ROM_INFO:
                    showROMInfo(mRootView, infoUtil.getDataStorage());
                    showSystemInfo(mRootView);
                    break;

            }
        };
    };

    private void showROMInfo(View view, long[] data) {
       // internalLayout.addView(new PieChartRomView(getActivity(), data));
        TextView totalView = (TextView) view.findViewById(R.id.internal_total);
        TextView usedView = (TextView) view.findViewById(R.id.internal_used);
        TextView lastView = (TextView) view.findViewById(R.id.internal_last);
        long last = data[0];
        long total = data[1];
        long used = total - last;
        lastView.setText(Util.convertSDStorage(last));
        usedView.setText(Util.convertSDStorage(used));
        totalView.setText(Util.convertSDStorage(total));
    }

    private void showSDCardInfo(View view, long[] data, int type) {
        TextView titleView = (TextView) view.findViewById(R.id.sd_title);
        TextView totalView = (TextView) view.findViewById(R.id.sd_total);
        TextView usedView = (TextView) view.findViewById(R.id.sd_used);
        TextView lastView = (TextView) view.findViewById(R.id.sd_last);
        if (type == 0) {
            titleView.setText(R.string.title_internal_sdcard);
        } else {
            titleView.setText(R.string.title_external_sdcard);
        }
        long last = data[0];
        long total = data[1];
        long used = total - last;
        lastView.setText(Util.convertSDStorage(last));
        usedView.setText(Util.convertSDStorage(used));
        totalView.setText(Util.convertSDStorage(total));
    }

    private void showSystemInfo(View view) {
        TextView typeView = (TextView) view.findViewById(R.id.phone_type);
        TextView systemView = (TextView) view.findViewById(R.id.phone_system);
        TextView resolutionView = (TextView) view.findViewById(R.id.phone_resolution);
        TextView macVIew = (TextView) view.findViewById(R.id.phone_mac_addres);
        typeView.setText(getPhoneType());
        systemView.setText(getSystemInfo());
        resolutionView.setText(getResolution());
        String macAddress = getMacAddress();
        if (macAddress == null || getMacAddress().equals("")) {
            LinearLayout macLayout = (LinearLayout) view.findViewById(R.id.mac_layout);
            macLayout.setVisibility(View.GONE);
        } else {
            macVIew.setText(macAddress);
        }
    }

    private void DrawPieChart(View view, HashMap<FileCategory, CategoryInfo> mCategoryInfo, long[] data) {
       // LinearLayout layout = (LinearLayout) view.findViewById(R.id.info_piechart);
       // PieChartView pie = new PieChartView(getActivity(), mCategoryInfo, data);
        //layout.addView(pie);
    }

    private void DrawRect(ImageView iv, int color) {
        Bitmap bmp = Bitmap.createBitmap(RECT_SIDE, RECT_SIDE, Config.ARGB_4444);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(new RectF(0, 0, RECT_SIDE, RECT_SIDE), paint);
        iv.setImageBitmap(bmp);
    }

    private void setSDInfo(View view, HashMap<FileCategory, CategoryInfo> mCategoryInfo, long[] data) {
        ImageView pictureIcon = (ImageView) view.findViewById(R.id.icon_picture);
        TextView pictureNum = (TextView) view.findViewById(R.id.picture_num);
        TextView pictureSize = (TextView) view.findViewById(R.id.picture_size);
        DrawRect(pictureIcon, getResources().getColor(R.color.sdinfo_pic_color));
        pictureNum.setText("(" + mCategoryInfo.get(FileCategory.Picture).count + ")");
        pictureSize.setText(Util.convertSDStorage(mCategoryInfo.get(FileCategory.Picture).size) + "");

        ImageView videoIcon = (ImageView) view.findViewById(R.id.icon_video);
        TextView videoNum = (TextView) view.findViewById(R.id.video_num);
        TextView videoSize = (TextView) view.findViewById(R.id.video_size);
        DrawRect(videoIcon, getResources().getColor(R.color.sdinfo_video_color));
        videoNum.setText("(" + mCategoryInfo.get(FileCategory.Video).count + ")");
        videoSize.setText(Util.convertSDStorage(mCategoryInfo.get(FileCategory.Video).size) + "");

        ImageView musicIcon = (ImageView) view.findViewById(R.id.icon_music);
        TextView musicNum = (TextView) view.findViewById(R.id.music_num);
        TextView musicSize = (TextView) view.findViewById(R.id.music_size);
        DrawRect(musicIcon, getResources().getColor(R.color.sdinfo_music_color));
        musicNum.setText("(" + mCategoryInfo.get(FileCategory.Music).count + ")");
        musicSize.setText(Util.convertSDStorage(mCategoryInfo.get(FileCategory.Music).size) + "");

        ImageView themeIcon = (ImageView) view.findViewById(R.id.icon_theme);
        TextView themeNum = (TextView) view.findViewById(R.id.theme_num);
        TextView themeSize = (TextView) view.findViewById(R.id.theme_size);
        DrawRect(themeIcon, getResources().getColor(R.color.sdinfo_theme_color));
        themeNum.setText("(" + mCategoryInfo.get(FileCategory.Theme).count + ")");
        themeSize.setText(Util.convertSDStorage(mCategoryInfo.get(FileCategory.Theme).size) + "");

        ImageView docIcon = (ImageView) view.findViewById(R.id.icon_doc);
        TextView docNum = (TextView) view.findViewById(R.id.doc_num);
        TextView docSize = (TextView) view.findViewById(R.id.doc_size);
        DrawRect(docIcon, getResources().getColor(R.color.sdinfo_doc_color));
        docNum.setText("(" + mCategoryInfo.get(FileCategory.Doc).count + ")");
        docSize.setText(Util.convertSDStorage(mCategoryInfo.get(FileCategory.Doc).size) + "");

        ImageView zipIcon = (ImageView) view.findViewById(R.id.icon_zip);
        TextView zipNum = (TextView) view.findViewById(R.id.zip_num);
        TextView zipSize = (TextView) view.findViewById(R.id.zip_size);
        DrawRect(zipIcon, getResources().getColor(R.color.sdinfo_zip_color));
        zipNum.setText("(" + mCategoryInfo.get(FileCategory.Zip).count + ")");
        zipSize.setText(Util.convertSDStorage(mCategoryInfo.get(FileCategory.Zip).size) + "");

        ImageView othersIcon = (ImageView) view.findViewById(R.id.icon_others);
        TextView othersNum = (TextView) view.findViewById(R.id.others_num);
        TextView othersSize = (TextView) view.findViewById(R.id.others_size);
        DrawRect(othersIcon, getResources().getColor(R.color.sdinfo_other_color));
        long[] other = getOtherInfo(mCategoryInfo, data);
        othersNum.setText("(" + other[0] + ")");
        othersSize.setText(Util.convertSDStorage(other[1]) + "");
    }

    private long[] getOtherInfo(HashMap<FileCategory, CategoryInfo> mCategoryInfo, long[] data) {
        long[] other = new long[2];
        long totalSize = 0;
        long totalCount = 0;
        CategoryInfo info = null;
        for (Map.Entry<FileCategory, CategoryInfo> obj : mCategoryInfo.entrySet()) {
            info = obj.getValue();
            totalCount += info.count;
            totalSize += info.size;
        }
        long allSize = mCategoryInfo.get(FileCategory.All).size;
        long allCount = mCategoryInfo.get(FileCategory.All).count;
        other[0] = allCount * 2 - totalCount;
        other[1] = data[1] - data[0] - totalSize + allSize;
        return other;
    }

    private String getResolution() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels + "*" + dm.heightPixels;
    }

    private String getMacAddress() {
        WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
        if (null != info) {
            return info.getMacAddress();
        } else {
            return "";
        }
    }

    private String getPhoneType() {
        return android.os.Build.MODEL + "";
    }

    private String getSystemInfo() {
        return android.os.Build.VERSION.RELEASE + "";
    }

    @Override
    public void onDestroy() {
        handler.removeMessages(CLOSE_PROGRESSBAR);
        handler.removeMessages(ADD_VIEW);
        handler.removeMessages(SHOW_ROM_INFO);
        handler = null;
        super.onDestroy();
    }
}
