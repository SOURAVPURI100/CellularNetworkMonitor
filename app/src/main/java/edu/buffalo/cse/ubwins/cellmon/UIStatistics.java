package edu.buffalo.cse.ubwins.cellmon;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

// Ins Begin of ++ spu

/**
 * Created by Sourav Puri on 7/25/17.
 */

class TechnolgiesData {
    String tech;
    int count;
    int percentage;

    public TechnolgiesData(String tech) {
        this.tech = tech;
        this.count = 0;
        this.percentage = 0;
    }
}

public class UIStatistics extends Fragment implements DateSelectedListener,
        NetworkDialogListener {
    public final String TAG = "[CELNETMON-MAPFRAG]";

    // Ins Begin of ++ spu
    public ArrayList<Entry> entryListGlobal;
    private final String _all_ = "all";
    private final String _2G_ = "_2G";
    private final String _2_5G_ = "_2_5G";
    private final String _3G_ = "_3G";
    private final String _4G_ = "_4G";
    private Map<String, Boolean> mapNetworkDialog = new HashMap<>();
    private Map<Integer, String> mapNetwork = new HashMap<>();
    private NetworkSelect netSelect;
    private TextView mapDate = null;
    private int mapView = 2; // For UI Statistics
    private String dateType = "";
    private String dateValue = "";
    private MinDateTask minDateTask;
    private GetJSONTask getJSONTask;
    private ProgressBar progressBar;
    private PieChart pieChart;
    private View rootView = null;
    private boolean progressFlag = false;
    FragmentTransaction fragmentTransaction;
    // Ins End of ++ spu

    public UIStatistics() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new MinDateTask().execute("start"); -- spu
        minDateTask = new MinDateTask(getActivity(), UIStatistics.this); //++ spu
        minDateTask.execute("start"); //++ spu
        setHasOptionsMenu(true);
        // Ins Begin of ++ spu
        buildNetworkMap(); //Build Map should be done before we call refresh map
        // Set Network Menu
        netSelect = NetworkSelect.newInstance("Network");
        netSelect.setTargetFragment(UIStatistics.this, 0);
        netSelect.setCancelable(true);
        // Add Progress Wheel on a new Dialog
        progressBar = ProgressBar.newInstance("Loading");
        progressBar.setTargetFragment(UIStatistics.this, 0);
//        progressBar.setCancelable(true);
        // Get the Map View Signal or Technology
        Bundle args = getArguments();
        mapView = args.getInt("index");
        // retain this fragment
        setRetainInstance(true);
        // Ins End of ++ spu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Add Layout for fragment in Activity Container

        if (savedInstanceState == null) {
            rootView = inflater.inflate(R.layout.ui_statistics, container, false);
            // Add pie chart view
            pieChart = (PieChart) rootView.findViewById(R.id.piechart);
            // Ins Begin of ++ spu
            //Adding legend text below
            mapDate = (TextView) rootView.findViewById(R.id.dayTitle);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
            mapDate.setText("Day " + df.format(c.getTime()));

        }

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.day_select:
                Log.d(TAG, "Day selected");
                if (!item.isChecked()) item.setChecked(true);
//                new MinDateTask().execute("day"); -- spu
                minDateTask = new MinDateTask(getActivity(), UIStatistics.this); //++ spu
                minDateTask.execute("day"); //++ spu

                return true;
            case R.id.week_select:
                Log.d(TAG, "Week selected");
                if (!item.isChecked()) item.setChecked(true);

//                new MinDateTask().execute("week"); -- spu
                minDateTask = new MinDateTask(getActivity(), UIStatistics.this); //++ spu
                minDateTask.execute("week"); //++ spu
                return true;
            case R.id.month_select:
                Log.d(TAG, "Month selected");
                if (!item.isChecked()) item.setChecked(true);
//                new MinDateTask().execute("month");
                minDateTask = new MinDateTask(getActivity(), UIStatistics.this); //++ spu
                minDateTask.execute("month"); //++ spu
                return true;

            // Ins Begin of ++ spu
            case R.id.network:
//                 FragmentManager fm =  getActivity().getFragmentManager();
                netSelect.show(getActivity().getFragmentManager(), "Networks");
                return true;
//                Window window = netSelect.getActivity().getWindow();
//                window.setLayout(30,70);
            // Ins End of ++ spu

            default:
                Log.d(TAG, "Other selected");
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFinishSelect(String type, String value) {
        Log.d(TAG, type + " " + value);
        Calendar minDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Ins Begin of ++ spu
        dateType = type;
        dateValue = value;
        // Ins End of ++ spu

        try {
            minDate.setTime(sdf.parse(value));
        } catch (ParseException e) {
            Log.d(TAG, "Parse exception thrown in onFinishSelect");
        }
        Log.d(TAG, "Time is: " + minDate.getTimeInMillis());
        value = String.valueOf(minDate.getTimeInMillis());
//        new GetJSONTask().execute(type, value); -- spu
//        Ins Begin of ++ spu
        getJSONTask = new GetJSONTask(getActivity(), UIStatistics.this);
        getJSONTask.execute(type, value, mapView + "");
        // Show Progress Wheel
        if (progressBar != null) {
            progressBar.show(getActivity().getFragmentManager(), "Loading");
//            fragmentTransaction = getFragmentManager().beginTransaction();
//            fragmentTransaction.add(progressBar, "Loading").commitAllowingStateLoss();
//            fragmentTransaction.show(progressBar);
            progressFlag = true;
        }
//        Ins End of ++ spu
//        refreshMap();
    }

    // Ins Begin of ++ spu
    @Override
    //
    public void onFinishNetworkDialog(boolean all, boolean _2G, boolean _2_5G, boolean _3G,
                                      boolean _4G) {
        // Update global network hashmap
        mapNetworkDialog.put(_all_, all);
        mapNetworkDialog.put(_2G_, _2G);
        mapNetworkDialog.put(_2_5G_, _2_5G);
        mapNetworkDialog.put(_3G_, _3G);
        mapNetworkDialog.put(_4G_, _4G);
        buildStatisticsView(this.entryListGlobal);

    }

    // Build UI Statistics View
    public void buildStatisticsView(ArrayList<Entry> entries) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
//      Filter entries based on Network hash map
        boolean allFilter = mapNetworkDialog.containsKey(_all_)
                && mapNetworkDialog.get(_all_);
        if (entries != null && allFilter == false) {
            entries = filterNetworkEntries(entries);
        }

        if (entries != null && entries.size() > 0) {
            // build map for technologies count Technolgies
            List<TechnolgiesData> techData = buildTechData(entries);

            for (int i = 0; i < techData.size(); i++) {

                TechnolgiesData techObj = techData.get(i);

                if (techObj.percentage > 0) {
                    pieEntries.add(new PieEntry(techObj.percentage, techObj.tech));
                }
            }
            // create pie data set
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "Technology Percentage");
            pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            PieData data = new PieData(pieDataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.WHITE);
            pieChart.setData(data);

            // undo all highlights
            pieChart.highlightValues(null);

            pieChart.invalidate();
            pieChart.setCenterText("Technologies");
            pieChart.setCenterTextColor(Color.GREEN);


            // Fill date on bottom map view
            if (dateType.equals("day")) {
                mapDate.setText("Day " + dateValue);
            } else if (dateType.equals("week")) {
                mapDate.setText("Week " + dateValue);
            } else if (dateType.equals("month")) {
                String str[] = dateValue.split("/");
                int month = Integer.parseInt(str[0]);
                mapDate.setText("Month " + new DateFormatSymbols().getMonths()[month - 1]);
            }

        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No data to display",
                    Toast.LENGTH_SHORT).show();
        }
        // Dismiss Progress Bar Dialog Box
        if (progressBar != null && progressFlag) {
            getFragmentManager().beginTransaction().remove(progressBar).commitAllowingStateLoss();
//            getFragmentManager().beginTransaction().detach(progressBar);
//            getFragmentManager().beginTransaction().hide(progressBar);
            progressBar.dismiss();
            progressFlag = false;
        }

    }

    //    Filter entries based on Network hash map
    public ArrayList<Entry> filterNetworkEntries(ArrayList<Entry> entries) {

        ArrayList<Entry> filterEntries = new ArrayList<>();
        // Create a deep copy of Entry List as per user selection on dialog box
        for (Entry entry : entries) {
            if (mapNetwork.containsKey(entry.network_type)) {
                String netValue = mapNetwork.get(entry.network_type);
                if (mapNetworkDialog.containsKey(netValue) && mapNetworkDialog.get(netValue)) {
                    Entry entryObj = (Entry) entry.clone();
                    filterEntries.add(entryObj);
                }
            }

        }
        return filterEntries;
    }

    // Build Network Map
    public void buildNetworkMap() {

        mapNetwork.put(1, _2_5G_);
        mapNetwork.put(2, _2_5G_);
        mapNetwork.put(3, _3G_);
        mapNetwork.put(4, _3G_);
        mapNetwork.put(5, _3G_);
        mapNetwork.put(6, _3G_);
        mapNetwork.put(7, _2G_);
        mapNetwork.put(8, _3G_);
        mapNetwork.put(9, _3G_);
        mapNetwork.put(10, _3G_);
        mapNetwork.put(11, _2G_);
        mapNetwork.put(12, _2G_);
        mapNetwork.put(13, _4G_);
        mapNetwork.put(14, _3G_);
        mapNetwork.put(15, _3G_);
        mapNetwork.put(16, _2G_);
        mapNetwork.put(17, _3G_);
        mapNetwork.put(18, _3G_);


        // Update global network hashmap to all set initially
        mapNetworkDialog.put(_all_, true);
        mapNetworkDialog.put(_2G_, true);
        mapNetworkDialog.put(_2_5G_, true);
        mapNetworkDialog.put(_3G_, true);
        mapNetworkDialog.put(_4G_, true);
    }

    // build Array List for technologies count Technologies
    public List<TechnolgiesData> buildTechData(ArrayList<Entry> entries) {
        List<TechnolgiesData> techData = new ArrayList<>();
        int unknownCount = 0;
        int totalEntries = entries.size();
        int totalPercent = 100;

//        build all technolgies objects
        TechnolgiesData obj4G = new TechnolgiesData("4G");
        TechnolgiesData obj3G = new TechnolgiesData("3G");
        TechnolgiesData obj2_5G = new TechnolgiesData("2.5G");
        TechnolgiesData obj2G = new TechnolgiesData("2G");
        techData.add(obj4G);
        techData.add(obj3G);
        techData.add(obj2_5G);
        techData.add(obj2G);

        for (Entry entry : entries) {
            int netType = entry.network_type;
            if (mapNetwork.containsKey(netType)) {
                String network = mapNetwork.get(netType);
                TechnolgiesData techObj = null;

                if (network.equals(_4G_)) {
                    techObj = techData.get(0);
                } else if (network.equals(_3G_)) {
                    techObj = techData.get(1);
                } else if (network.equals(_2_5G_)) {
                    techObj = techData.get(2);
                } else if (network.equals(_2G_)) {
                    techObj = techData.get(3);
                } else {
                    unknownCount++;
                }
                if (techObj != null) {
                    techObj.count += 1;
                }

            } else {
                unknownCount++;
            }

        }
        // Remove unknown Technologies count
        totalEntries = totalEntries - unknownCount;
        for (int i = 0; i < techData.size(); i++) {
            if (totalPercent <= 0) {
                break;
            }
            TechnolgiesData techObj = techData.get(i);
            if (techObj.count > 0) {
                int percent = (techObj.count * 100) / totalEntries;
                techObj.percentage = Math.max(1, percent);
            }
            totalPercent = totalPercent - techObj.percentage;
        }

        // Logic to adjust total added percentage to 100%
        int i = 0;
        while (totalPercent > 0) {
            int tech = i % 4;
            if (tech < techData.size() && techData.get(tech).percentage > 0) {
                techData.get(tech).percentage += 1;
                totalPercent -= 1;
            }
            i++;

        }
        i = 0;
        while (totalPercent < 0) {
            int tech = i % 4;
            if (tech < techData.size() && techData.get(tech).percentage > 1) {
                techData.get(tech).percentage -= 1;
                totalPercent += 1;
            }
            i++;

        }
        // Logic to adjust total added percentage to 100%

        return techData;
    }

    // Ins End of ++ spu
}