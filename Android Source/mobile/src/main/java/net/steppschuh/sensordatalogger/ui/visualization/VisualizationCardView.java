package net.steppschuh.sensordatalogger.ui.visualization;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.steppschuh.datalogger.data.Data;
import net.steppschuh.datalogger.data.DataBatch;
import net.steppschuh.sensordatalogger.R;
import net.steppschuh.sensordatalogger.ui.visualization.chart.ChartView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VisualizationCardView extends RelativeLayout {

    private VisualizationCardData data;

    boolean showDimensionValues = true;

    private ImageButton moreImageButton;

    private TextView headingTextView;
    private TextView subHeadingTextView;

    private TextView valueLeftTextView;
    private TextView valueCenterTextView;
    private TextView valueRightTextView;
    private TextView moreImageTextView;

    private ImageButton valueLeftButton;
    private ImageButton valueRightButton;

    private ChartView chartView;
    private String chartType = ChartView.TYPE_LINE;

    public VisualizationCardView(Context context) {
        super(context);
        initializeLayout(context);
    }

    public VisualizationCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeLayout(context);
    }

    private void initializeLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.visualization_card, this);

        moreImageButton = (ImageButton) findViewById(R.id.moreButton);

        headingTextView = (TextView) findViewById(R.id.headingText);
        subHeadingTextView = (TextView) findViewById(R.id.subHeadingText);

        //valueLeftTextView = (TextView) findViewById(R.id.valueLeftText);
        valueCenterTextView = (TextView) findViewById(R.id.valueCenterText);
        //valueRightTextView = (TextView) findViewById(R.id.valueRightText);

        //valueLeftButton = (ImageButton) findViewById(R.id.valueLeftButton);
        //valueRightButton = (ImageButton) findViewById(R.id.valueRightButton);

        /*OnClickListener previousDimensionClickedListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = chartView.getCurrentDataDimension();
                int breakIndex = 0;
                if (current == breakIndex && chartView.getDataDimension() != ChartView.DATA_DIMENSION_ALL) {
                    chartView.setDataDimension(ChartView.DATA_DIMENSION_ALL);
                } else {
                    chartView.setDataDimension(chartView.getPreviousDataDimension());
                }
            }
        };
        valueLeftTextView.setOnClickListener(previousDimensionClickedListener);
        valueLeftButton.setOnClickListener(previousDimensionClickedListener);

        OnClickListener nextDimensionClickedListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = chartView.getCurrentDataDimension();
                int breakIndex = data.getDataBatch().getNewestData().getValues().length - 1;
                if (current == breakIndex && chartView.getDataDimension() != ChartView.DATA_DIMENSION_ALL) {
                    chartView.setDataDimension(ChartView.DATA_DIMENSION_ALL);
                } else {
                    chartView.setDataDimension(chartView.getNextDataDimension());
                }
            }
        };
        valueRightTextView.setOnClickListener(nextDimensionClickedListener);
        valueRightButton.setOnClickListener(nextDimensionClickedListener);*/
// add by albert

        OnClickListener export = new OnClickListener() {
            @Override
            public void onClick(View v) {
                    DataBatch ExportBatch = new DataBatch(data.getDataBatch());
                    final int total_row = ExportBatch.getDataList().size();
                    Log.i("SensorLoggerAlbert", "total_row = " + total_row);
                    final String fileprefix = "export";
                    final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
                    final String filename = String.format("%s_%s.txt", fileprefix, date);

                  // final String directory = getContext().getApplicationContext().getFilesDir().getAbsolutePath();// + "/Albert";
                    final String directory = Environment.getExternalStorageDirectory().getAbsolutePath();// + "/sdcard";
                    final File logfile = new File(directory, filename);
                    final File logPath = logfile.getParentFile();

                    if (!logPath.isDirectory() && !logPath.mkdirs()) {
                        Log.e("SensorLoggerAlbert", "Could not create directory for log files");
                    }
/*
                int permissionCheck = ContextCompat.checkSelfPermission(getContext().getApplicationContext().getCurrentActivity(),
                        android.Manifest.permission.WRITE_CALENDAR);*/
                    try {
                        FileWriter filewriter = new FileWriter(logfile);
                        BufferedWriter bw = new BufferedWriter(filewriter);

                         // Write the string to the file
                        for (int i = 1; i < total_row; i++) {
                            StringBuffer sb = new StringBuffer(ExportBatch.getSource());
                            sb.append("\t");
                            sb.append(String.valueOf(ExportBatch.getDataList().get(i).getTimestamp()));
                            sb.append("\t");
                            sb.append(String.valueOf(ExportBatch.getDataList().get(i).getValues()[0]));
                            sb.append("\t");
                            sb.append(String.valueOf(ExportBatch.getDataList().get(i).getValues()[1]));
                            sb.append("\t");
                            sb.append(String.valueOf(ExportBatch.getDataList().get(i).getValues()[2]));
                            sb.append("\n");
                            bw.write(sb.toString());
                        }
                        bw.flush();
                        bw.close();
                        /*Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent.setType("**");

                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                                "SensorloggerAlbert data export");
                        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logfile));
                        getContext().startActivity(Intent.createChooser(emailIntent, "Send mail..."));

                        Log.i("SensorloggerAlbert", "export finished!");*/
                    } catch (IOException ioe) {
                        Log.e("SensorloggerAlbert", "IOException while writing Logfile");
                    }


                }
            };

        moreImageButton.setOnClickListener(export);

        //chartView = (ChartView) findViewById(R.id.chartView);
    }

    @Override
    public void requestLayout() {
        renderData();
        super.requestLayout();
    }

    public void renderData() {
        if (data == null || data.getDataBatch() == null) {
            return;
        }

        try {
            headingTextView.setText(data.getHeading());
            subHeadingTextView.setText(data.getSubHeading());

            if (data.getDataBatch().getNewestData() == null) {
                return;
            }

            if (showDimensionValues) {
                float[] latestValues = data.getDataBatch().getNewestData().getValues();
                String[] lastestReadableValues = new String[latestValues.length];
                for (int valueIndex = 0; valueIndex < latestValues.length; valueIndex++) {
                    lastestReadableValues[valueIndex] = String.format("%.02f", latestValues[valueIndex]);
                }
                    valueCenterTextView.setText(String.format(lastestReadableValues[0],lastestReadableValues[1],lastestReadableValues[2]));

               /* valueRightTextView.setText(lastestReadableValues[chartView.getNextDataDimension()]);
                valueLeftTextView.setText(lastestReadableValues[chartView.getPreviousDataDimension()]);*/
            } else {
                valueCenterTextView.setText("1");
               /* valueRightTextView.setText(ChartView.getDimensionName(chartView.getNextDataDimension()));
                valueLeftTextView.setText(ChartView.getDimensionName(chartView.getPreviousDataDimension()));*/
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

/*        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long minimumTimestamp = chartView.getStartTimestamp() - TimeUnit.SECONDS.toMillis(1);
                    DataBatch processedDataBatch = new DataBatch(data.getDataBatch());
                    List<Data> processedData = getProcessedDataList(data.getDataBatch().getDataSince(minimumTimestamp));
                    processedDataBatch.setDataList(processedData);
                    chartView.setDataBatch(processedDataBatch);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();*/
    }

    public static List<Data> getProcessedDataList(List<Data> unprocessedData) {
        List<Data> processedData = new ArrayList<>();

        int currentlySkippedDataCount = 0;
        Data lastAddedData = null;

        int maximumSkipCount = unprocessedData.size() / 5;

        for (int dataIndex = 0; dataIndex < unprocessedData.size(); dataIndex++) {
            Data currentData = unprocessedData.get(dataIndex);

            boolean forceAdding = false;
            if (dataIndex < maximumSkipCount) {
                forceAdding = true;
            } else if (dataIndex > unprocessedData.size() - maximumSkipCount) {
                forceAdding = true;
            } else if (currentlySkippedDataCount > maximumSkipCount) {
                forceAdding = true;
            }

            if (lastAddedData == null || forceAdding) {
                lastAddedData = currentData;
                processedData.add(lastAddedData);
                continue;
            }

            boolean hasEqualValues = true;
            for (int dimension = 0; dimension < currentData.getValues().length; dimension++) {
                float currentValue = currentData.getValues()[dimension];
                float lastAddedValue = lastAddedData.getValues()[dimension];
                float delta = Math.abs(currentValue - lastAddedValue);
                if (delta > 0.05) {
                    hasEqualValues = false;
                    break;
                }
            }

            if (hasEqualValues) {
                // continue skipping
                currentlySkippedDataCount++;
                continue;
            } else {
                if (currentlySkippedDataCount > 0) {
                    // stop skipping and add last data point
                    currentlySkippedDataCount = 0;
                    processedData.add(unprocessedData.get(dataIndex - 1));
                }
                // add current data point
                lastAddedData = currentData;
                processedData.add(lastAddedData);
            }
        }
        return processedData;
    }

    public VisualizationCardData getData() {
        return data;
    }

    public void setData(VisualizationCardData data) {
        this.data = data;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }
}
