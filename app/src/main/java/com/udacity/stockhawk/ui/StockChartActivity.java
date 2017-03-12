package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockChartActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int STOCK_HISTORY_LOADER = 1;
    public static final String SYMBOL_KEY = "symbol";
    private String symbol;
    private HashMap<Float, Long> xAxisLabels = new HashMap<>();

    @BindView(R.id.chart)
    LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_chart);
        ButterKnife.bind(this);

        symbol = getIntent().getExtras().getString(SYMBOL_KEY);

        setupLineChart();

        getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);
    }

    private void setupLineChart() {
        mChart.getXAxis().setLabelRotationAngle(-45f);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setTextColor(Color.WHITE);
        mChart.getAxisLeft().setTextColor(Color.WHITE);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setNoDataTextColor(Color.WHITE);
        mChart.setNoDataText(getString(R.string.chart_not_available));
        mChart.getDescription().setEnabled(false);
        mChart.setExtraRightOffset(25f);
        mChart.getLegend().setTextColor(Color.WHITE);
        mChart.getAxisRight().setEnabled(false);
        mChart.setMarker(new MyMarkerView(this, R.layout.marker_view));
        mChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return value + getString(R.string.currency_symbol);
            }
        });

        mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                if (xAxisLabels.get(value) != null) {
                    calendar.setTimeInMillis(xAxisLabels.get(value));
                    int mYear = calendar.get(Calendar.YEAR);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                    return mDay + "-" + mMonth + "-" + mYear;
                }
                return "";
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Contract.Quote.makeUriForStock(symbol),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.moveToFirst()) {
                String history = data.getString(Contract.Quote.POSITION_HISTORY);
                String[] quotes = history.split(QuoteSyncJob.QUOTE_DIVIDER);

                float index = 0;
                ArrayList<Entry> values = new ArrayList<Entry>();
                for (int i = quotes.length - 1; i >= 0; i--) {
                    String[] quoteInfo = quotes[i].split(QuoteSyncJob.TIME_VALUE_DIVIDER);
                    if (quoteInfo.length > 1) {
                        values.add(new Entry(index, Float.parseFloat(quoteInfo[1])));
                        xAxisLabels.put(index, Long.valueOf(quoteInfo[0]));
                        index++;
                    }
                }

                if (values.size() > 0) {
                    LineDataSet lineDataSet = new LineDataSet(values, symbol);
                    lineDataSet.setDrawCircles(false);
                    lineDataSet.setDrawValues(false);
                    lineDataSet.setColor(Color.WHITE);
                    lineDataSet.setDrawHighlightIndicators(true);
                    lineDataSet.setLineWidth(2f);
                    lineDataSet.setDrawHighlightIndicators(false);
                    lineDataSet.setFillColor(Color.RED);
                    LineData lineData = new LineData(lineDataSet);
                    mChart.setData(lineData);
                }
                setActionBarTitle(data.getString(Contract.Quote.POSITION_NAME));
            }

        }
    }

    private void setActionBarTitle(String companyName) {
        getSupportActionBar().setTitle(companyName);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class MyMarkerView extends MarkerView {

        private TextView mContent;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            mContent = (TextView) findViewById(R.id.content);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            super.refreshContent(e, highlight);
            mContent.setText(String.valueOf(e.getY()));
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}
