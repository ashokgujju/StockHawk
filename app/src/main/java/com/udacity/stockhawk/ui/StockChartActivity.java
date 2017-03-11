package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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
    LineChart mLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_chart);
        ButterKnife.bind(this);

        symbol = getIntent().getExtras().getString(SYMBOL_KEY);

        mLineChart.getXAxis().setLabelRotationAngle(-45f);
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mLineChart.getXAxis().setTextColor(Color.WHITE);
        mLineChart.getAxisLeft().setTextColor(Color.WHITE);
        mLineChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return value + "$";
            }
        });

        mLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
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

        mLineChart.setDoubleTapToZoomEnabled(false);
        mLineChart.setNoDataTextColor(Color.WHITE);
        mLineChart.setNoDataText(getString(R.string.chart_not_available));
        mLineChart.setDescription(null);
        mLineChart.getAxisRight().setEnabled(false);
        getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);
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
                    lineDataSet.setLineWidth(3f);
                    LineData lineData = new LineData(lineDataSet);
                    mLineChart.setData(lineData);
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
}
