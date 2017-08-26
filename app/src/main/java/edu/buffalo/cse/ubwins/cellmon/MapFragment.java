package edu.buffalo.cse.ubwins.cellmon;

import android.app.DatePickerDialog;
import android.app.Fragment;
//import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

// Ins Begin of ++ spu
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

// Ins End of ++ spu

import javax.crypto.BadPaddingException;

import static android.R.attr.data;
import static android.R.attr.fragment;
import static android.os.Build.VERSION_CODES.M;
import static com.facebook.stetho.inspector.network.PrettyPrinterDisplayType.JSON;

import static edu.buffalo.cse.ubwins.cellmon.LocationFinder.longitude;

/**
 * Created by pcoonan on 3/15/17.
 * Edited by Sourav Puri
 */

public class MapFragment extends Fragment implements DateSelectedListener,
        NetworkDialogListener, // ++spu
        ClusterManager.OnClusterClickListener<Entry>, // ++spu
        ClusterManager.OnClusterInfoWindowClickListener<Entry>, // ++spu
        ClusterManager.OnClusterItemClickListener<Entry>,  // ++spu
        ClusterManager.OnClusterItemInfoWindowClickListener<Entry> // ++spu
{
    public final String TAG = "[CELNETMON-MAPFRAG]";

    MapView mMapView;
    private GoogleMap googleMap;
//    private float[] colorMap = {0.0f, 30.0f, 60.0f, 90.0f, 120.0f}; -- spu
//    private String[] networkTypeMap = {"GSM", "CDMA", "LTE", "WCDMA"}; -- spu
//    private Long mindate; -- spu
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
    private ProgressBar progressBar;
    private SignalHeatMap signalHeatMap;
    private TextView mapDate = null;
    private int mapView = 0; // For technology // 1 for signal
    private String dateType = "";
    private String dateValue = "";
    private MinDateTask minDateTask;
    private GetJSONTask getJSONTask;
    private LatLng curLoc = null;
    private View rootView = null;
    // Ins End of ++ spu

    public MapFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new MinDateTask().execute("start"); -- spu
        minDateTask = new MinDateTask(getActivity(), MapFragment.this); //++ spu
        minDateTask.execute("start"); //++ spu
        setHasOptionsMenu(true);
        // Ins Begin of ++ spu
        buildNetworkMap(); //Build Map should be done before we call refresh map
        // Set Network Menu
        netSelect = NetworkSelect.newInstance("Network");
        netSelect.setTargetFragment(MapFragment.this, 0);
        netSelect.setCancelable(true);

        // Add Progress Wheel on a new Dialog
        progressBar = ProgressBar.newInstance("Loading");
        progressBar.setTargetFragment(MapFragment.this, 0);
//        progressBar.setCancelable(true);

        signalHeatMap = new SignalHeatMap();
        // Get the Map View Signal or Technology
        Bundle args = getArguments();
        mapView = args.getInt("index");
        // retain this fragment
        setRetainInstance(true);
        // Ins End of ++ spu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState == null){ // ++ spu
            rootView = inflater.inflate(R.layout.location_fragment, container, false);
            super.onSaveInstanceState(savedInstanceState); // ++ spu
            // Ins Begin of ++ spu
            // Adding legend text below
            mapDate = (TextView) rootView.findViewById(R.id.detailTitle);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
            mapDate.setText("Day " + df.format(c.getTime()));

            // Ins End of ++ spu
            mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);

            mMapView.onResume(); // needed to get the map to display immediately

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
//              refreshMap(null); -- spu
                    // Ins Begin of ++ spu
                    if (mapView == 0)
                        refreshMap(null);
                    else
                        refreshSignalMap(null);


                    // Ins End of ++ spu
                }
            });
//        Network int to type relationship

        }
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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
                minDateTask = new MinDateTask(getActivity(), MapFragment.this); //++ spu
                minDateTask.execute("day"); //++ spu

                return true;
            case R.id.week_select:
                Log.d(TAG, "Week selected");
                if (!item.isChecked()) item.setChecked(true);

//                new MinDateTask().execute("week"); -- spu
                minDateTask = new MinDateTask(getActivity(), MapFragment.this); //++ spu
                minDateTask.execute("week"); //++ spu
                return true;
            case R.id.month_select:
                Log.d(TAG, "Month selected");
                if (!item.isChecked()) item.setChecked(true);
//                new MinDateTask().execute("month");
                minDateTask = new MinDateTask(getActivity(), MapFragment.this); //++ spu
                minDateTask.execute("month"); //++ spu
                return true;

            // Ins Begin of ++ spu
            case R.id.network:
//                 FragmentManager fm =  getActivity().getFragmentManager();
                if(netSelect != null){
                    netSelect.show(getActivity().getFragmentManager(), "Networks");
                }

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
        getJSONTask = new GetJSONTask(getActivity(), MapFragment.this);
        getJSONTask.execute(type, value, mapView+"");

        // Show Progress Wheel
        if(progressBar != null){
            progressBar.show(getActivity().getFragmentManager(), "Loading");
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

        if (mapView == 0) {
            refreshMap(entryListGlobal);
        } else
            refreshSignalMap(entryListGlobal);

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

    // Ins End of ++ spu
//    private void refreshMap(ArrayList<Entry> entries){ // -- spu
    public void refreshMap(ArrayList<Entry> entries) { //++ spu added "all" filter
        googleMap.clear();

        // Ins Begin of ++ spu
        boolean allFilter = mapNetworkDialog.containsKey(_all_)
                && mapNetworkDialog.get(_all_);
//      Filter entries based on Network hash map
        if (entries != null && allFilter == false) {
            entries = filterNetworkEntries(entries);
        }
        // Ins End of ++spu

        // For showing a move to my location button
        googleMap.setMyLocationEnabled(true);
        ClusterManager<Entry> clusterManager =
                new ClusterManager<Entry>(getActivity().getApplicationContext(), googleMap);
        // For dropping a marker at a point on the Map
        double lat = (ForegroundService.FusedApiLatitude != null) ?
                ForegroundService.FusedApiLatitude : 42.953431;
        double lon = (ForegroundService.FusedApiLongitude != null) ?
                ForegroundService.FusedApiLongitude : -78.818229;
        curLoc = new LatLng(lat, lon);
        double latAvg = 0;
        double longAvg = 0;

        // Ins Begin of ++ spu
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterInfoWindowClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);
        clusterManager.setRenderer(new EntryRenderer(getActivity().getApplicationContext(),
                googleMap, clusterManager));

        // Ins End of ++ spu

        // Trying a few different approaches below. First, if there are no entries retrieved from
        // the server, we resort to using the locally stored data. This was implemented for
        // testing the map out, and I would recommend removing this feature to prevent any
        // confusion.

        // In the event that there are entries retrieved from the server, we display them. The most
        // basic approach to this is just to create standard markers and add some of the entry
        // information to them. Given the large amount of data presented, this is not realistic
        // for map responsiveness.

        // To fix the issue of displaying large amounts of data, we look to using clustering. There
        // are two approaches to this, custom and using the Google Maps Cluster Manager. So far, the
        // custom clustering approach is not complete, as it needs much better fine-grained tuning.
        // The Cluster Manager takes care of clustering the data points, but we need more control
        // over how these clusters are displayed. Currently each cluster shows how many markers are
        // contained within, but we want to display the average signal strength to the user.


        // TODO:
        //   - Determine if the Cluster Manager can be used to show the data we want
        //   - Split data between 3G and LTE (add toggle in UI)
        //   - Filter out stale entries
        //   - Adjust map zoom to contain all points/clusters (sometimes zooms in too much)

//        if(entries == null){ // -- spu
        if (entries == null || entries.size() == 0) { //++ spu
            if (entries != null && entries.size() == 0)
                Toast.makeText(getActivity().getApplicationContext(), "No Data Available", Toast.LENGTH_SHORT).show();
            // Del Begin of ++ spu
//            String rawQuery = "SELECT * FROM mapRecords";
//            DBHandler dbHandler = new DBHandler(getActivity().getApplicationContext());
//            SQLiteDatabase sqLiteDatabase = dbHandler.getWritableDatabase();
//            Cursor cur = sqLiteDatabase.rawQuery(rawQuery, null);
//
//
//            double count = DatabaseUtils.queryNumEntries(sqLiteDatabase, "mapRecords");
//
//            if(cur.moveToFirst()){
//                do{
//                    LatLng temp = new LatLng(cur.getDouble(1), cur.getDouble(2));
//                    latAvg += (cur.getDouble(1) / count);
//                    longAvg += (cur.getDouble(2) / count);
//
////                        Log.d("LOCDB", "Lat is: " + cur.getDouble(1));
////                        Log.d("LOCDB", "Long is: " + cur.getDouble(2));
//                    googleMap.addMarker(
//                            new MarkerOptions()
//                                    .position(temp)
//                                    .title(networkTypeMap[cur.getInt(5)])
//                                    .snippet(cur.getInt(11) + " dBm")
//                                    .icon(BitmapDescriptorFactory.
//                                            defaultMarker(colorMap[cur.getInt(12)])));
//
//
//                }
//                while(cur.moveToNext());
//            }
//            else{
//                googleMap.addMarker(new MarkerOptions().position(curLoc).title("Current Location").snippet("No data"));
//            }
//            cur.close();
//            sqLiteDatabase.close();

            // Del End of ++ spu
        } else {
            // Custom cluster method - currently not working correctly
//            List<Cluster> clusters = init(entries, 200);
//            calculate(clusters, entries);
//
//            for(Cluster cluster: clusters){
//                LatLng temp = new LatLng(cluster.centroid.getLatitude(), cluster.centroid.getLongitude());
//                latAvg += (cluster.centroid.getLatitude() / clusters.size());
//                longAvg += (cluster.centroid.getLongitude() / clusters.size());
//
//                googleMap.addMarker(
//                        new MarkerOptions()
//                                .position(temp)
//                                .title("Cluster " + cluster.id)
//                                .snippet("Number of entries: " + cluster.getEntries().size())
//                                );
//            }


            for (Entry e : entries) {
//                LatLng temp = new LatLng(e.coordinate.getLatitude(), e.coordinate.getLongitude());
                latAvg += (e.coordinate.getLatitude() / entries.size());
                longAvg += (e.coordinate.getLongitude() / entries.size());
                // For Cluster Manager approach
                clusterManager.addItem(e);

                // Uncomment for basic marker approach
//                googleMap.addMarker(
//                        new MarkerOptions()
//                                .position(temp)
//                                .title(networkTypeMap[e.networkCellType])
//                                .snippet(e.dbm + " dBm")
//                                .icon(BitmapDescriptorFactory.
//                                        defaultMarker(colorMap[e.signalLevel])));
            }

            //clusterManager.setAnimation(false); // ++ spu

            // Only needed for Cluster Manager
            googleMap.setOnCameraIdleListener(clusterManager);
            googleMap.setOnMarkerClickListener(clusterManager);
        }

        clusterManager.cluster(); //++ spu
        if (latAvg != 0 && longAvg != 0) curLoc = new LatLng(latAvg, longAvg);
        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(curLoc).zoom(15).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Ins begin of ++ spu
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
        // Dismiss Progress Bar Dialog Box
        if(progressBar != null && progressBar.isVisible()){
            progressBar.dismiss();
        }
        // Ins End of ++ spu
    }


    // Ins Begin of ++ spu
    public void refreshSignalMap(ArrayList<Entry> entries) {
        googleMap.clear();

        boolean allFilter = mapNetworkDialog.containsKey(_all_)
                && mapNetworkDialog.get(_all_);
//      Filter entries based on Network hash map
        if (entries != null && allFilter == false) {
            entries = filterNetworkEntries(entries);
        }

        // For showing a move to my location button
        googleMap.setMyLocationEnabled(true);
        // For dropping a marker at a point on the Map
        double lat = (ForegroundService.FusedApiLatitude != null) ?
                ForegroundService.FusedApiLatitude : 42.953431;
        double lon = (ForegroundService.FusedApiLongitude != null) ?
                ForegroundService.FusedApiLongitude : -78.818229;
        curLoc = new LatLng(lat, lon);
        double latAvg = 0;
        double longAvg = 0;

//        if(entries == null){ -- spu
        if (entries == null || entries.size() == 0) { //++ spu
            if (entries != null && entries.size() == 0)
                Toast.makeText(getActivity().getApplicationContext(), "No Data Available", Toast.LENGTH_SHORT).show();
            // Del Begin of ++ spu
//            String rawQuery = "SELECT * FROM mapRecords";
//            DBHandler dbHandler = new DBHandler(getActivity().getApplicationContext());
//            SQLiteDatabase sqLiteDatabase = dbHandler.getWritableDatabase();
//            Cursor cur = sqLiteDatabase.rawQuery(rawQuery, null);
//
//
//            double count = DatabaseUtils.queryNumEntries(sqLiteDatabase, "mapRecords");
//
//            if(cur.moveToFirst()){
//                do{
//                    LatLng temp = new LatLng(cur.getDouble(1), cur.getDouble(2));
//                    latAvg += (cur.getDouble(1) / count);
//                    longAvg += (cur.getDouble(2) / count);
//                    googleMap.addMarker(
//                            new MarkerOptions()
//                                    .position(temp)
//                                    .title(networkTypeMap[cur.getInt(5)])
//                                    .snippet(cur.getInt(11) + " dBm")
//                                    .icon(BitmapDescriptorFactory.
//                                            defaultMarker(colorMap[cur.getInt(12)])));
//
//
//                }
//                while(cur.moveToNext());
//            }
//            else{
//                googleMap.addMarker(new MarkerOptions().position(curLoc).title("Current Location").snippet("No data"));
//            }
//            cur.close();
//            sqLiteDatabase.close();
            // Del End of ++ spu
        } else {
            for (Entry e : entries) {
//                LatLng temp = new LatLng(e.coordinate.getLatitude(), e.coordinate.getLongitude());
                latAvg += (e.coordinate.getLatitude() / entries.size());
                longAvg += (e.coordinate.getLongitude() / entries.size());

            }
            // Adding functionality for HeatMaps
            signalHeatMap = new SignalHeatMap();
            signalHeatMap.buildHeatMap(entries, googleMap);

        }


        if (latAvg != 0 && longAvg != 0) curLoc = new LatLng(latAvg, longAvg);
        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(curLoc).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        // Ins begin of ++ spu
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
        // Dismiss Progress Bar Dialog Box
        if(progressBar != null && progressBar.isVisible()){
            progressBar.dismiss();
        }
        // Ins End of ++ spu
    }

    // Ins End of ++ spu


    private ArrayList<Cluster> init(List<Entry> entries, int numClusters) {
        Log.d(TAG, "Intitializing K-means");
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        double minLat, minLong;
        minLat = minLong = Double.MAX_VALUE;
        double maxLat, maxLong;
        maxLat = maxLong = Double.MAX_VALUE * -1;

        for (Entry e : entries) {
            minLat = Math.min(minLat, e.coordinate.getLatitude());
            minLong = Math.min(minLong, e.coordinate.getLongitude());
            maxLat = Math.max(maxLat, e.coordinate.getLatitude());
            maxLong = Math.max(maxLong, e.coordinate.getLongitude());
//            Log.d(TAG, "Max long: " + maxLong + " entry long: " + e.coordinate.getLongitude());
        }

        Log.d(TAG, "Min latitude: " + minLat + " max latitude: " + maxLat);
        Log.d(TAG, "Min longitude: " + minLong + " max longitude " + maxLong);
        for (int i = 0; i < numClusters; ++i) {
            Cluster cluster = new Cluster(i);
            Coordinate centroid = Coordinate.createRandomCoordinate(maxLat, minLat, minLong, maxLong);
            cluster.setCentroid(centroid);
            clusters.add(cluster);
        }

        Log.d(TAG, "K-means initialization complete");
        return clusters;
    }

    private void calculate(List<Cluster> clusters, List<Entry> entries) {
        Log.d(TAG, "K-means calculation beginning...");
        boolean finish = false;
        int iteration = 0;

        while (!finish) {
            clearClusters(clusters);

            List<Coordinate> lastCentroids = getCentroids(clusters);

            assignCluster(entries, clusters);

            calculateCentroids(clusters);

            iteration++;

            List<Coordinate> currentCentroids = getCentroids(clusters);

            double distance = 0;
            for (int i = 0; i < lastCentroids.size(); ++i) {
                distance += Coordinate.distance(lastCentroids.get(i), currentCentroids.get(i));
            }

            if (distance == 0) {
                finish = true;
            }
            if (iteration % 100 == 0) {
                Log.d(TAG, "Iteration: " + iteration);
            }
        }

        Log.d(TAG, "K-means calculating complete.");
    }

    private void clearClusters(List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            cluster.clear();
        }
    }

    private List<Coordinate> getCentroids(List<Cluster> clusters) {
        List<Coordinate> centroids = new ArrayList<Coordinate>();
        for (Cluster cluster : clusters) {
            Coordinate aux = cluster.getCentroid();
            Coordinate coordinate = new Coordinate(aux.getLatitude(), aux.getLongitude());
            centroids.add(coordinate);
        }
        return centroids;
    }

    private void assignCluster(List<Entry> entries, List<Cluster> clusters) {
        double max = Double.MAX_VALUE;
        double min = max;
        int cluster = 0;
        double distance = 0.0;

        for (Entry entry : entries) {
            min = max;
            for (int i = 0; i < clusters.size(); ++i) {
                Cluster c = clusters.get(i);
                distance = Coordinate.distance(entry.coordinate, c.getCentroid());
                if (distance < min) {
                    min = distance;
                    cluster = i;
                }
            }
            entry.clusterNumber = cluster;
            clusters.get(cluster).addEntry(entry);
        }
    }

    private void calculateCentroids(List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            double sumLat = 0;
            double sumLong = 0;
            List<Entry> entries = cluster.getEntries();
            int npoints = entries.size();

            for (Entry entry : entries) {
                sumLat += entry.coordinate.getLatitude();
                sumLong += entry.coordinate.getLongitude();
            }

            Coordinate centroid = cluster.getCentroid();
            if (npoints > 0) {
                double newLat = sumLat / npoints;
                double newLong = sumLong / npoints;
                centroid.setLatitude(newLat);
                centroid.setLongitude(newLong);
            }
        }
    }

    // Task that gets the earliest date for a user.
    // TODO:
    //   - Get maximum date as well.
    //   - Basic error handling
// Del Begin of ++ spu

//    class MinDateTask extends AsyncTask<String, Void, Long> {
//        private String type;
//        @Override
//        protected Long doInBackground(String... params) {
//            type = params[0];
//            if(mindate != null) return mindate;
//            String IMEI_HASH = "";
//            String responseStr = "";
//            Long ret = System.currentTimeMillis();
//            try {
//             /*HASH IMEI*/
//                IMEI_HASH = genHash(getIMEI());
//            }
//            catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            }
//            HttpResponse response = null;
//            try {
//                HttpClient client = new DefaultHttpClient();
//                HttpGet request = new HttpGet();
//                String customURL = "http://104.196.177.7/aggregator/mindate?imei_hash="
//                        + URLEncoder.encode(IMEI_HASH, "UTF-8");
////                Log.d(TAG, customURL);
//                request.setURI(new URI(customURL));
//                response = client.execute(request);
//
//                responseStr = EntityUtils.toString(response.getEntity());
//                Log.v(TAG, "RESPONSE" + responseStr);
//
//                /*PARSE JSON RESPONSE*/
//                JSONObject jsonObject = new JSONObject(responseStr);
//                String status = jsonObject.getString("status");
//                if(status.equals("SUCCESS")){
//                    String time = jsonObject.getString("timestamp");
//
//                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//                    long timestamp = Long.parseLong(time);
//                    String parsed = sdf.format(new Date(timestamp));
//                    Log.d(TAG, "Mindate: " + parsed);
//                    ret = timestamp;
//                }
//            } catch (URISyntaxException | IOException | JSONException e) {
//                e.printStackTrace();
//            }
//            return ret;
//        }
//
//        @Override
//        protected void onPostExecute(Long timestamp) {
//            switch (type){
//                case "day":
//                    Log.d(TAG, "Timestamp retrieved for day");
//                    DatePickerFragment fragment = DatePickerFragment.newInstance(timestamp); //new DatePickerFragment();
//                    fragment.setTargetFragment(MapFragment.this, 0);
//                    fragment.show(getActivity().getFragmentManager(), "datePicker");
//                    break;
//                case "week":
//                    Log.d(TAG, "Timestamp retrieved for week");
//                    WeekPickerFragment weekPickerFragment = WeekPickerFragment.newInstance(timestamp);
//                    weekPickerFragment.setTargetFragment(MapFragment.this, 1);
//                    weekPickerFragment.show(getActivity().getFragmentManager(), "weekPicker");
//                    break;
//                case "month":
//                    Log.d(TAG, "Timestamp retrieved for month");
//                    MonthPickerFragment monthPickerFragment = MonthPickerFragment.newInstance(timestamp);
//                    monthPickerFragment.setTargetFragment(MapFragment.this, 2);
//                    monthPickerFragment.show(getActivity().getFragmentManager(), "monthPicker");
//                    break;
//                case "start":
//                    Log.d(TAG, "Timestamp cached.");
//                    mindate = timestamp;
//                    break;
//                default:
//                    Log.d(TAG, "Other selected");
////                    return super.onOptionsItemSelected(item);
//            }
//        }
//    }

    // Del End of ++ spu


    // Del Begin of  ++ spu

//    class GetJSONTask extends AsyncTask<String, Void, Boolean> {
//        private ArrayList<Entry> entryList;
//
//        public GetJSONTask() {
//            entryList = new ArrayList<Entry>();
//        }
//
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            String timespan = strings[0];
//            String timestart = strings[1];
//            Boolean ret = false;
//
//            String IMEI_HASH = "";
//            String responseStr = "";
//            try {
//             /*HASH IMEI*/
//                IMEI_HASH = genHash(getIMEI());
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            }
//            HttpResponse response = null;
//            try {
//                HttpClient client = new DefaultHttpClient();
//                HttpGet request = new HttpGet();
////                String customURL = "http://104.196.177.7/aggregator/genjson?imei_hash="
////                        + URLEncoder.encode(IMEI_HASH, "UTF-8");
//                String customURL = "http://104.196.177.7/aggregator/genjson?";
//                List<NameValuePair> params = new LinkedList<NameValuePair>();
//                params.add(new BasicNameValuePair("imei_hash", IMEI_HASH));
//                params.add(new BasicNameValuePair("timespan", timespan));
//                params.add(new BasicNameValuePair("timestart", timestart));
//                String paramString = URLEncodedUtils.format(params, "utf-8");
//
//                customURL += paramString;
//
////                Log.d(TAG, customURL);
//                request.setURI(new URI(customURL));
//                response = client.execute(request);
//
//// Ins Begin of spu
//                HttpEntity entity = response.getEntity();
//                InputStream inputStream =  entity.getContent();
//                //create JsonParser object
//                ObjectMapper mapper = new ObjectMapper();
//                JsonParser jsonParser = mapper.getFactory().createParser(inputStream);
//
////                JsonToken token = jsonParser.nextToken();
////                if(token == null || !token.equals("status")){
////                    return false;
////                }
////                token = jsonParser.nextToken();
////                if(!token.equals("SUCCESS")){
////                    return false;
////                }
//
//                buildEntriesData(jsonParser, entryList);
//
//// Ins End of spu
//
//// Del Begin of spu
//
////                responseStr = EntityUtils.toString(response.getEntity());
////                Log.v(TAG, "JSON received.");
////
////
////                /*PARSE JSON RESPONSE*/
////                JSONObject jsonObject = new JSONObject(responseStr);
////                String status = jsonObject.getString("status");
////                if (status.equals("SUCCESS")) {
//////                  Ins Begin of ++ spu
////                    if (timespan.equals("week") || timespan.equals("month")) {
////                        fillEntriesData(jsonObject, entryList);
////                    } else {
////                        JSONObject data = jsonObject.getJSONObject("data");
////                        JSONArray entries = data.getJSONArray("entries");
////                        Log.d(TAG, entries.length() + " entries for selected timeframe");
////                        for (int i = 0; i < entries.length(); ++i) {
////                            JSONObject jsonentry = entries.getJSONObject(i);
////                            Entry entry = Entry.mapJSON(jsonentry);
////                            entryList.add(entry);
////                        }
////                    }
////
//////                  Ins End of ++ spu
////
////                    // Del Begin of ++ spu
//////                    JSONObject data = jsonObject.getJSONObject("data");
//////                    JSONArray entries = data.getJSONArray("entries");
//////                    Log.d(TAG, entries.length() + " entries for selected timeframe");
//////                    for(int i = 0; i < entries.length(); ++i){
//////                        JSONObject jsonentry = entries.getJSONObject(i);
////////                    Log.d(TAG, entry.toString());
//////                        Entry entry = Entry.mapJSON(jsonentry);
//////                        entryList.add(entry);
//////                    }
////                    // Del End of ++ spu
////                    // ret = true; -- spu
////                }
//
//// Del End of spu
//                ret = true;  // ++ spu
//
//            } catch (URISyntaxException | IOException e) {
//                e.printStackTrace();
//            }
//            return ret;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean b) {
//            if (b) {
//                // Ins Begin of ++ spu
//                entryListGlobal = entryList;
//                // added "all" network filter
//
//                if (mapView == 0)
//                    refreshMap(entryList, (mapNetworkDialog.containsKey(_all_)
//                            && mapNetworkDialog.get(_all_)));
//                else
//                    refreshSignalMap(entryList, (mapNetworkDialog.containsKey(_all_)
//                            && mapNetworkDialog.get(_all_)));
//                // Ins End of ++ spu
////                refreshMap(entryList); -- spu
//
//            } else {
//                Log.d(TAG, "No data for selected date");
//                // Add popup alert
//            }
//        }
//    }

    // Del End of  ++ spu

    private String genHash(String input) throws NoSuchAlgorithmException {
        String IMEI_Base64 = "";
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sha256Hash = sha256.digest(input.getBytes("UTF-8"));
            IMEI_Base64 = Base64.encodeToString(sha256Hash, Base64.DEFAULT);
            IMEI_Base64 = IMEI_Base64.replaceAll("\n", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return IMEI_Base64;
    }

    private String getIMEI() {
        TelephonyManager telephonyManager =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    // Ins Begin of ++ spu
    @Override
    public boolean onClusterClick(com.google.maps.android.clustering.Cluster<Entry> cluster) {
        // Show a toast with some info when the cluster is clicked.

        long avg_dbm = 0;
        int count = 0;
        Iterator<Entry> itr = cluster.getItems().iterator();

        while (itr.hasNext()) {
            avg_dbm += itr.next().dbm;
            count++;
        }

        avg_dbm = avg_dbm / count;
        Toast.makeText(getActivity().getApplicationContext(), avg_dbm + " average dbm", Toast.LENGTH_SHORT).show();
//
//        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
//        // inside of bounds, then animate to center of the bounds.
//
//        // Create the builder to collect all essential cluster items for the bounds.
//        LatLngBounds.Builder builder = LatLngBounds.builder();
//        for (ClusterItem item : cluster.getItems()) {
//            builder.include(item.getPosition());
//        }
//        // Get the LatLngBounds
//        final LatLngBounds bounds = builder.build();
//
//        // Animate camera to the bounds
//        try {
//            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return false; // Return false to show info window on cluster
//        return true;
    }

    @Override
    public void onClusterInfoWindowClick(com.google.maps.android.clustering.Cluster<Entry> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Entry item) {
        // Does nothing, but you could go into the user's profile page, for example.
        Toast.makeText(getActivity().getApplicationContext(), "dBm  " + item.dbm, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Entry item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

//    public void fillEntriesData(JSONObject jsonObject, ArrayList<Entry> entryList) {
//        try {
//            JSONArray data = jsonObject.getJSONArray("data");
//            for (int i = 0; i < data.length(); i++) {
//                JSONArray entries = data.getJSONArray(i);
//
//                for (int j = 0; j < entries.length(); j++) {
//                    JSONObject jsonentry = entries.getJSONObject(j);
//                    Entry entry = Entry.mapJSON(jsonentry);
//                    entryList.add(entry);
//                }
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

//    public void buildEntriesData(JsonParser jsonParser, ArrayList<Entry> entryList) {
//        try {
//            Entry entry = new Entry();
//            entry.coordinate = new Coordinate(0,0);
//            while(true){
//                JsonToken token = jsonParser.nextToken();
//                if(token == null){
//                    break;
//                }
//
//                String name = jsonParser.getCurrentName();
//                if(name != null){
//
//                    if(name.equals("network_type")){
//                        jsonParser.nextToken();
//                        entry.network_type = jsonParser.getIntValue();
//                        entryList.add(entry);
//                        entry = new Entry();
//                        entry.coordinate = new Coordinate(0,0);
//                    }
//                    else if(name.equals("timestamp")){
//                        jsonParser.nextToken();
//                        entry.timestamp= jsonParser.getLongValue();
//                    }
//                    else if(name.equals("fused_lat")){
//                        jsonParser.nextToken();
//                        entry.coordinate.setLatitude(jsonParser.getDoubleValue());
//                    }
//                    else if(name.equals("fused_long")){
//                        jsonParser.nextToken();
//                        entry.coordinate.setLongitude(jsonParser.getDoubleValue());
//                    }
//                    else if(name.equals("network_cell_type")){
//                        jsonParser.nextToken();
//                        entry.networkCellType = jsonParser.getIntValue();
//                    }
//
//                    else if(name.equals("signal_dbm")){
//                        jsonParser.nextToken();
//                        entry.dbm = jsonParser.getIntValue();
//                    }
//
//                    else if(name.equals("signal_level")){
//                        jsonParser.nextToken();
//                        entry.signalLevel = jsonParser.getIntValue();
//                    }
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
    // Ins End of ++ spu
}