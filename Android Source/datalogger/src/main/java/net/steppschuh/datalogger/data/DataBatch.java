package net.steppschuh.datalogger.data;

import android.app.Activity;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataBatch implements Serializable {

    public static final int CAPACITY_UNLIMITED = -1;
    public static final int CAPACITY_DEFAULT = 10;

    private int type;
    private String source;
    private List<Data> dataList;
    private int capacity = CAPACITY_DEFAULT;

    public DataBatch() {
        dataList = new ArrayList<>();
        capacity = CAPACITY_DEFAULT;
    }

    public DataBatch(DataBatch dataBatch) {
        type = dataBatch.getType();
        source = dataBatch.getSource();
        dataList = new ArrayList<>(dataBatch.getDataList().size());
        dataList.addAll(dataBatch.getDataList());
        capacity = dataBatch.capacity;
    }

    public DataBatch(List<Data> dataList) {
        this();
        this.dataList = dataList;
    }

    public DataBatch(String source) {
        this();
        this.source = source;
    }


    private void trimDataToCapacity(Boolean status) {
        // check if there's a capacity limit
       /*if (capacity == CAPACITY_UNLIMITED) {
            return;
        }*/

        // check if trimming is needed
        if (dataList == null || dataList.size() < capacity) {
            return;
        }

        // remove oldest data
        //Add albert
        while (dataList.size() > 10 && status == true) {
            final int total_row = dataList.size();
            Log.i("SensorLoggerAlbert", "total_row = " + total_row);
            final String fileprefix = "export";
            final String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
            final String filename = String.format("%s_%s.txt", source, fileprefix);

            // final String directory = getContext().getApplicationContext().getFilesDir().getAbsolutePath();// + "/Albert";
            final String directory = Environment.getExternalStorageDirectory().getAbsolutePath();// + "/sdcard";
            final String direc = "/storage/emulated/0";
            final File logfile = new File(directory, filename);
            final File logPath = logfile.getParentFile();

            if (!logPath.isDirectory() && !logPath.mkdirs()) {
                Log.e("SensorLoggerAlbert", "Could not create directory for log files");
            }
/*
                int permissionCheck = ContextCompat.checkSelfPermission(getContext().getApplicationContext().getCurrentActivity(),
                        android.Manifest.permission.WRITE_CALENDAR);*/
            try {
                FileWriter filewriter = new FileWriter(logfile, true);
                BufferedWriter bw = new BufferedWriter(filewriter);


                // Write the string to the file
                for (int i = 1; i < total_row; i++) {
                    StringBuffer sb = new StringBuffer(source);
                    sb.append("\t");
                    sb.append(String.valueOf(dataList.get(i).getTimestamp()));
                    sb.append("\t");
                    sb.append(String.valueOf(dataList.get(i).getValues()[0]));
                    sb.append("\t");
                    sb.append(String.valueOf(dataList.get(i).getValues()[1]));
                    sb.append("\t");
                    sb.append(String.valueOf(dataList.get(i).getValues()[2]));
                    sb.append("\n");
                    bw.append(sb.toString());
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
            dataList.clear();
        }
    }

    public void roundToDecimalPlaces(int decimalPlaces) {
        for (int dataIndex = 0; dataIndex < dataList.size(); dataIndex++) {
            Data data = dataList.get(dataIndex);
            for (int dimension = 0; dimension < data.getValues().length; dimension++) {
                data.getValues()[dimension] = roundToDecimalPlaces(data.getValues()[dimension], decimalPlaces);
            }
        }
    }

    public static float roundToDecimalPlaces(float value, int decimalPlaces) {
        double shift = Math.pow(10, decimalPlaces);
        return (float) (Math.round(value * shift) / shift);
    }

    public void addData(Data data) {
        dataList.add(data);
        trimDataToCapacity(true);
    }

    public void addData(List<Data> data) {
        dataList.addAll(data);
        trimDataToCapacity(true);
    }


    @JsonIgnore
    public Data getNewestData() {
        if (dataList == null || dataList.size() < 1) {
            return null;
        }
        return dataList.get(dataList.size() - 1);
    }

    @JsonIgnore
    public Data getOldestData() {
        if (dataList == null || dataList.size() < 1) {
            return null;
        }
        return dataList.get(0);
    }

    public List<Data> getDataSince(long timestamp) {
        List<Data> dataSince = new ArrayList<>();
        for (int i = dataList.size() - 1; i >= 0; i--) {
            if (dataList.get(i).getTimestamp() > timestamp) {
                dataSince.add(dataList.get(i));
            } else {
                break;
            }
        }
        Collections.reverse(dataSince);
        return dataSince;
    }

    public void removeDataBefore(long timestamp) {
        dataList = getDataSince(timestamp);
    }

    @JsonIgnore
    @Override
    public String toString() {
        return toJson();
    }

    @JsonIgnore
    public String toJson() {
        String jsonData = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            jsonData = mapper.writeValueAsString(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonData;
    }

    /**
     * Getter & Setter
     */
    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getCapacity() {
        return capacity;
    }

    //public int getsize() {return dataList.size();}

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
