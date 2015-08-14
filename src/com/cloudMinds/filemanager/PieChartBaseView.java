
package com.cloudMinds.filemanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.View;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartRenderingInfo;
import org.afree.graphics.geom.Dimension;
import org.afree.graphics.geom.RectShape;
import org.afree.ui.RectangleInsets;

public class PieChartBaseView extends View {

    private int PIC_SIZE = 350;    //饼图的大小。宽和高一致

    public PieChartBaseView(Context context) {
        super(context);
        this.initialize();
    }

    /**
     * initialize parameters
     */
    private void initialize() {
        this.info = new ChartRenderingInfo();
        this.maximumDrawWidth = PIC_SIZE;
        this.minimumDrawHeight = PIC_SIZE;
        this.maximumDrawWidth = PIC_SIZE;
        this.maximumDrawHeight = PIC_SIZE;
        /*
         * this.minimumDrawWidth = DEFAULT_MINIMUM_DRAW_WIDTH;
         * this.minimumDrawHeight = DEFAULT_MINIMUM_DRAW_HEIGHT;
         * this.maximumDrawWidth = DEFAULT_MAXIMUM_DRAW_WIDTH;
         * this.maximumDrawHeight = DEFAULT_MAXIMUM_DRAW_HEIGHT;
         */
        // new SolidColor(Color.argb(0, 100, 100, 100));
    }

    /**
     * Default setting for buffer usage. The default has been changed to
     * <code>true</code> from version 1.0.13 onwards, because of a severe
     * performance problem with drawing the zoom RectShape using XOR (which now
     * happens only when the buffer is NOT used).
     */
 //   public final boolean DEFAULT_BUFFER_USED = true;

    /** The default panel width. */
    // public static final int DEFAULT_WIDTH = 200;

    /** The default panel height. */
    // public static final int DEFAULT_HEIGHT = 200;

    /** The default limit below which chart scaling kicks in. */
    // public static final int DEFAULT_MINIMUM_DRAW_WIDTH = 200;

    /** The default limit below which chart scaling kicks in. */
    // public static final int DEFAULT_MINIMUM_DRAW_HEIGHT = 200;

    /** The default limit above which chart scaling kicks in. */
    // public static final int DEFAULT_MAXIMUM_DRAW_WIDTH = 200;

    /** The default limit above which chart scaling kicks in. */
    // public static final int DEFAULT_MAXIMUM_DRAW_HEIGHT = 200;

    /** The chart that is displayed in the panel. */
    private AFreeChart chart;

    /** The drawing info collected the last time the chart was drawn. */
    private ChartRenderingInfo info;

    /**
     * The zoom RectShape starting point (selected by the user with touch). This
     * is a point on the screen, not the chart (which may have been scaled up or
     * down to fit the panel).
     */

    private RectangleInsets insets = null;

    /**
     * The minimum width for drawing a chart (uses scaling for smaller widths).
     */
    private int minimumDrawWidth;

    /**
     * The minimum height for drawing a chart (uses scaling for smaller
     * heights).
     */
    private int minimumDrawHeight;

    /**
     * The maximum width for drawing a chart (uses scaling for bigger widths).
     */
    private int maximumDrawWidth;

    /**
     * The maximum height for drawing a chart (uses scaling for bigger heights).
     */
    private int maximumDrawHeight;

    private Dimension size = null;

    /** The chart anchor point. */
    private PointF anchor;

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.insets = new RectangleInsets(0, 0, 0, 0);
        this.size = new Dimension(w, h);
    }

    private RectangleInsets getInsets() {
        return this.insets;
    }

    public void setChart(AFreeChart chart) {
        this.chart = chart;
    }

    public int getMinimumDrawWidth() {
        return this.minimumDrawWidth;
    }

    public ChartRenderingInfo getChartRenderingInfo() {
        return this.info;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paintComponent(canvas);
    }

    /**
     * Paints the component by drawing the chart to fill the entire component,
     * but allowing for the insets (which will be non-zero if a border has been
     * set for this component). To increase performance (at the expense of
     * memory), an off-screen buffer image can be used.
     * 
     * @param canvas the graphics device for drawing on.
     */
    public void paintComponent(Canvas canvas) {
        // first determine the size of the chart rendering area...
        Dimension size = getSize();
        RectangleInsets insets = getInsets();
        RectShape available = new RectShape(insets.getLeft(), insets.getTop(),
                size.getWidth() - insets.getLeft() - insets.getRight(),
                size.getHeight() - insets.getTop() - insets.getBottom());

        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();

        if (drawWidth < this.minimumDrawWidth) {
            drawWidth = this.minimumDrawWidth;
        }
        else if (drawWidth > this.maximumDrawWidth) {
            drawWidth = this.maximumDrawWidth;
        }

        if (drawHeight < this.minimumDrawHeight) {
            drawHeight = this.minimumDrawHeight;
        }
        else if (drawHeight > this.maximumDrawHeight) {
            drawHeight = this.maximumDrawHeight;
        }
        RectShape chartArea = new RectShape(0.0, 0.0, drawWidth,
                drawHeight);
        this.chart.draw(canvas, chartArea, this.anchor, this.info);
        this.anchor = null;
    }

    public Dimension getSize() {
        return this.size;
    }

    public PointF getAnchor() {
        return this.anchor;
    }

    public ChartRenderingInfo getInfo() {
        return info;
    }

    protected void setAnchor(PointF anchor) {
        this.anchor = anchor;
    }
}
