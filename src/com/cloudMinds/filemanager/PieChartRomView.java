package com.cloudMinds.filemanager;

import android.content.Context;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.labels.StandardPieSectionLabelGenerator;
import org.afree.chart.plot.PiePlot;
import org.afree.data.general.DefaultPieDataset;
import org.afree.data.general.PieDataset;
import org.afree.graphics.SolidColor;

import com.cloudMinds.filemanager.R;

public class PieChartRomView extends PieChartBaseView
{
    private long[] data;
    private Context mContext;

    public PieChartRomView(Context context, long[] data)
    {
        super(context);
        this.mContext = context;
        this.data = data;
        setChart(createChart());
    }

    private AFreeChart createChart()
    {

        AFreeChart localAFreeChart = ChartFactory.createPieChart("", createDataset(), false, false,
                false);
        PiePlot localPiePlot = (PiePlot) localAFreeChart.getPlot();
        SolidColor used = new SolidColor(mContext.getResources().getColor(R.color.sdinfo_used_color));
        SolidColor last = new SolidColor(mContext.getResources().getColor(R.color.sdinfo_last_color));
        localAFreeChart.setBorderEffect(null);
        localAFreeChart.setBorderVisible(true);
        localAFreeChart.setBorderStroke(0.1f);
        localAFreeChart.setBorderPaintType(new SolidColor(mContext.getResources().getColor(R.color.sdinfo_pic_border)));
        localAFreeChart.setBackgroundPaintType(null);

        localPiePlot.setSectionPaintType(mContext.getString(R.string.used1), used);
        localPiePlot.setSectionPaintType(mContext.getString(R.string.last1), last);
        localPiePlot.setNoDataMessage(mContext.getString(R.string.no_rom));
        // 设置背景透明度
        localPiePlot.setBackgroundAlpha(255);
        // 设置前景透明度
        localPiePlot.setForegroundAlpha(255);
        // 指定饼图轮廓线的颜色
        // localPiePlot.setBaseSectionOutlinePaint(Color.WHITE);
        // localPiePlot.setBaseSectionPaint(Color.BLACK);
        localPiePlot.setBaseSectionOutlineStroke(0.0f);
        localPiePlot.setExplodePercent(mContext.getString(R.string.used1), 0.1D);
        localPiePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}"));
        localPiePlot.setBackgroundPaintType(null);
        // localPiePlot.setLabelBackgroundPaintType(new SolidColor(Color.argb(0,
        // 0, 0, 0)));
        localPiePlot.setLabelBackgroundPaintType(null);
        // localPiePlot.setLegendLabelToolTipGenerator(new
        // StandardPieSectionLabelGenerator("Tooltip for legend item {0}"));
        localPiePlot.setLegendLabelToolTipGenerator(null);
        localPiePlot.setSimpleLabels(true);
        localPiePlot.setInteriorGap(0.0D);
        return localAFreeChart;
    }

    private PieDataset createDataset()
    {
        DefaultPieDataset localDefaultPieDataset = new DefaultPieDataset();
        localDefaultPieDataset.setValue(mContext.getString(R.string.used1), data[1] - data[0]);
        localDefaultPieDataset.setValue(mContext.getString(R.string.last1), data[0]);
        return localDefaultPieDataset;
    }
}
