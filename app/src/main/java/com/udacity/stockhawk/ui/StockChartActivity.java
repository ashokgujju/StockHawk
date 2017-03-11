package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockChartActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int STOCK_HISTORY_LOADER = 1;
    public static final String SYMBOL_KEY = "symbol";
    private String symbol;

    @BindView(R.id.chart)
    LineChart mLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_chart);
        ButterKnife.bind(this);

        symbol = getIntent().getExtras().getString(SYMBOL_KEY);
        mLineChart.setDrawBorders(false);
        mLineChart.setBackgroundColor(Color.parseColor("#FFFFFF"));
        mLineChart.setDrawGridBackground(false);
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
            ArrayList<Entry> values = new ArrayList<Entry>();
            int i = 0;
            while (data.moveToPosition(i)) {
                String history = data.getString(Contract.Quote.POSITION_HISTORY);
                String[] quotes = history.split(QuoteSyncJob.QUOTE_DIVIDER);
                for (String quote : quotes) {
                    String[] quoteInfo = quote.split(QuoteSyncJob.TIME_VALUE_DIVIDER);
                    values.add(new Entry(i++, Float.parseFloat(quoteInfo[1])));
                }

            }

            LineDataSet lineDataSet = new LineDataSet(values, symbol);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);
            lineDataSet.setColor(Color.parseColor("#0000FF"));
            LineData lineData = new LineData(lineDataSet);
            mLineChart.setData(lineData);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
