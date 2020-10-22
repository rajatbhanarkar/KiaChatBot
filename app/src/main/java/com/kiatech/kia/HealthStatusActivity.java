package com.kiatech.kia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;


// This activity will contain a graph, showing the mental health of the user

public class HealthStatusActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    LineChart lineChart;
    ImageView Info;
    ImageView Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_status);

        // UI Stuff, making nav bar and status bar color same as background

        getWindow().setNavigationBarColor(getResources().getColor(R.color.darkBackground));
        getWindow().setStatusBarColor(getResources().getColor(R.color.darkBackground));

        // Declaring all IDs and linking to XML

        lineChart = (LineChart)findViewById(R.id.lineChart);
        Info = (ImageView) findViewById(R.id.tvinfo);
        Back = (ImageView)findViewById(R.id.ivback);

        // OnClick show the health labels 1-7

        Info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(HealthStatusActivity.this);
                dialog.setContentView(R.layout.health_info_layout);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(800, 1200);
                dialog.show();
            }
        });

        // Back Button

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // LineChart Initialization, to plot graph

        lineChart.setOnChartGestureListener(this);
        lineChart.setOnChartValueSelectedListener(this);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // Adding dummy values to arraylist to plot points

        ArrayList<Entry> valueList = new ArrayList<>();
        valueList.add(new Entry(0, 1));
        valueList.add(new Entry(1, 4));
        valueList.add(new Entry(2, 2));
        valueList.add(new Entry(3, 5));
        valueList.add(new Entry(4, 3));
        valueList.add(new Entry(5, 6));
        valueList.add(new Entry(6, 7));


        // Init dataset

        LineDataSet set1 = new LineDataSet(valueList, "");
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        set1.setColor(getResources().getColor(R.color.yellow));
        set1.setValueTextColor(getResources().getColor(R.color.white));
        set1.setValueTextSize(0f);
        set1.setLineWidth(2f);
        set1.setDrawCircles(false);

        // X-Axis labels

        ArrayList<String> AxisLabels = new ArrayList<>(Arrays.asList(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));

        // X-axis init

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(AxisLabels));
        xAxis.setTextColor(getResources().getColor(R.color.white));
        xAxis.setTypeface(Typeface.MONOSPACE);
        xAxis.setTextSize(12f);
        xAxis.setAxisLineColor(getResources().getColor(R.color.colorPrimary));
        xAxis.setGridColor(getResources().getColor(R.color.colorPrimary));
        xAxis.setGridLineWidth(0.5f);
        xAxis.setAxisLineWidth(2f);

        // Left Y-axis init

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.white));
        leftAxis.setAxisLineColor(getResources().getColor(R.color.colorPrimary));
        leftAxis.setGridColor(getResources().getColor(R.color.colorPrimary));
        leftAxis.setTypeface(Typeface.MONOSPACE);
        leftAxis.setTextSize(12f);
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setAxisLineWidth(2f);

        // Right Y-axis init

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setTextColor(getResources().getColor(R.color.white));
        rightAxis.setAxisLineColor(getResources().getColor(R.color.colorPrimary));
        rightAxis.setGridColor(getResources().getColor(R.color.colorPrimary));
        rightAxis.setGridLineWidth(0.5f);
        rightAxis.setTypeface(Typeface.MONOSPACE);
        rightAxis.setTextSize(12f);
        rightAxis.setAxisLineWidth(2f);

        // Plotting the dataset

        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(set1);
        lineChart.setData(new LineData(dataSet));
        lineChart.setBorderColor(getResources().getColor(R.color.colorPrimary));
        lineChart.getLegend().setEnabled(false);
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }

    @Override
    public void onChartLongPressed(MotionEvent me) { }

    @Override
    public void onChartDoubleTapped(MotionEvent me) { }

    @Override
    public void onChartSingleTapped(MotionEvent me) { }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) { }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) { }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) { }

    @Override
    public void onValueSelected(Entry e, Highlight h) { }

    @Override
    public void onNothingSelected() { }
}