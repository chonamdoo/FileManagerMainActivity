package com.cloudMinds.filemanager;

import android.content.Context;

import com.cloudMinds.filemanager.R;
import com.cloudMinds.filemanager.FileCategoryHelper.FileCategory;
import com.cloudMinds.utils.CategoryInfo;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.labels.StandardPieSectionLabelGenerator;
import org.afree.chart.plot.PiePlot;
import org.afree.data.general.DefaultPieDataset;
import org.afree.data.general.PieDataset;
import org.afree.graphics.SolidColor;

import java.util.HashMap;
import java.util.Map;

public class PieChartView extends PieChartBaseView {
    private Context mContext = null;
    private long[] data = null;
    private HashMap<FileCategory, CategoryInfo> mCategoryInfo = null;
    HashMap<String, SolidColor> showColor = new HashMap<String, SolidColor>();

    public PieChartView(Context paramContext, HashMap<FileCategory, CategoryInfo> info, long[] data) {
        super(paramContext);
        this.mContext = paramContext;
        this.mCategoryInfo = info;
        this.data = data;
        setChart(createChart());
    }

    public enum Colors {
    }

    private AFreeChart createChart() {

        AFreeChart localAFreeChart = ChartFactory.createPieChart("", createDataset(), false, false, false);
        PiePlot localPiePlot = (PiePlot) localAFreeChart.getPlot();
        if (showColor == null || showColor.size() == 0) {
            return localAFreeChart;
        }
        for (Map.Entry<String, SolidColor> obj : showColor.entrySet()) {
            localPiePlot.setSectionPaintType(obj.getKey(), obj.getValue());
        }
        localPiePlot.setNoDataMessage(mContext.getString(R.string.sd_not_available));
        localAFreeChart.setBorderVisible(true);
        localAFreeChart.setBorderStroke(0.1f);
        localAFreeChart.setBorderPaintType(new SolidColor(mContext.getResources().getColor(R.color.sdinfo_pic_border)));
        localAFreeChart.setBackgroundPaintType(null);
        
        localAFreeChart.setBorderEffect(null);
        // 设置背景透明度
        localPiePlot.setBackgroundAlpha(255);
        localPiePlot.setBackgroundImage(null);
        // 设置前景透明度
        localPiePlot.setForegroundAlpha(255);
        // 指定饼图轮廓线的颜色
        localPiePlot.setBaseSectionOutlineEffect(null);
        localPiePlot.setAutoPopulateSectionOutlinePaint(false);
        localPiePlot.setBaseSectionOutlineStroke(0.0f);
        String last = mContext.getString(R.string.sdcard_last);
        if (showColor.containsKey(last)) {
            localPiePlot.setExplodePercent(last, 0.1D);
        }
        localPiePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}"));
        localPiePlot.setBackgroundPaintType(null);
    //    localPiePlot.setLabelBackgroundPaintType(new SolidColor(Color.argb(100, 100, 100, 100)));
        localPiePlot.setLabelBackgroundPaintType(null);
        localPiePlot.setLegendLabelToolTipGenerator(null);
        localPiePlot.setSimpleLabels(true);
        localPiePlot.setInteriorGap(0.0D);
        return localAFreeChart;
    }

    private HashMap<String, Long> getShowPicChart() {
        HashMap<String, Long> showInfo = new HashMap<String, Long>();
        long pictureSize = mCategoryInfo.get(FileCategory.Picture).size;
        long videoSize = mCategoryInfo.get(FileCategory.Video).size;
        long musicSize = mCategoryInfo.get(FileCategory.Music).size;
        long themeSize = mCategoryInfo.get(FileCategory.Theme).size;
        long docSize = mCategoryInfo.get(FileCategory.Doc).size;
        long zipSize = mCategoryInfo.get(FileCategory.Zip).size;
        long lastSize = data[0];
        long otherSize = data[1] - data[0] - pictureSize - videoSize - musicSize - themeSize - docSize - zipSize;
        long showSize = data[1] / 100;
        if (pictureSize >= showSize) {
            String name = mContext.getString(R.string.category_picture);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_pic_color)));
            showInfo.put(name, pictureSize);
        }
        if (videoSize >= showSize) {
            String name = mContext.getString(R.string.category_video);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_video_color)));
            showInfo.put(name, videoSize);
        }
        if (musicSize >= showSize) {
            String name = mContext.getString(R.string.category_music);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_music_color)));
            showInfo.put(name, musicSize);
        }
        if (themeSize >= showSize) {
            String name = mContext.getString(R.string.category_theme);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_theme_color)));
            showInfo.put(name, themeSize);
        }
        if (docSize >= showSize) {
            String name = mContext.getString(R.string.category_document);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_doc_color)));
            showInfo.put(name, docSize);
        }
        if (zipSize >= showSize) {
            String name = mContext.getString(R.string.category_zip);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_zip_color)));
            showInfo.put(name, zipSize);
        }
        if (otherSize > showSize) {
            String name = mContext.getString(R.string.category_other);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_other_color)));
            showInfo.put(name, otherSize);
        }
        if (lastSize >= showSize) {
            String name = mContext.getString(R.string.sdcard_last);
            showColor.put(name, new SolidColor(mContext.getResources().getColor(R.color.sdinfo_last_color)));
            showInfo.put(name, lastSize);
        }
        return showInfo;
    }

    private PieDataset createDataset() {
        DefaultPieDataset localDefaultPieDataset = new DefaultPieDataset();
        HashMap<String, Long> showInfo = getShowPicChart();
        if (showInfo == null || showInfo.size() == 0) {
            return localDefaultPieDataset;
        }
        for (Map.Entry<String, Long> obj : showInfo.entrySet()) {
            localDefaultPieDataset.setValue(obj.getKey(), obj.getValue());
        }
        return localDefaultPieDataset;
    }
}
