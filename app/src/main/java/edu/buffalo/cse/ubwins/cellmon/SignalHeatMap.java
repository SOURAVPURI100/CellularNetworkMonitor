package edu.buffalo.cse.ubwins.cellmon;

import android.graphics.Color;
import android.test.InstrumentationTestRunner;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.*;

/**
 * Created by sourav on 6/26/17.
 */
// Ins Begin of ++ spu
public class SignalHeatMap {

    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.GREEN
//            Color.argb(0, 0, 255, 255),// transparent
//            Color.argb(255 / 3 * 2, 0, 255, 255),
//            Color.rgb(0, 191, 255),
//            Color.rgb(0, 0, 127),
//            Color.rgb(255, 0, 0)
    };
    private static final int[] RED_HEATMAP_GRADIENT_COLORS = {
            Color.RED
    };
    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.2f
//        , 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    public static final Gradient RED_HEATMAP_GRADIENT = new Gradient(RED_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider mDarkGreenProvider, mMedGreenProvider, mLightGreenProvider,
            mDarkRedProvider, mMedRedProvider, mLightRedProvider;
    private TileOverlay mDarkGreenOverlay, mMedGreenOverlay, mLightGreenOverlay,
            mDarkRedOverlay, mMedRedOverlay, mLightRedOverlay;

    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;
    private GoogleMap googleMap;
    private ArrayList<LatLng> mDataset;
    private Map<String, ArrayList<LatLng>> signalColorMap = new HashMap<>();
    private String[] signalColor = {"DarkGreen", "MedGreen", "LightGreen", "DarkRed", "MedRed",
                                    "LightRed"};

    public SignalHeatMap() {
        buildSignalMap();
    }

    public void buildHeatMap(ArrayList<Entry> entries, GoogleMap googleMap){

        // Fill signal ranges
            fillSignalRanges(entries);
        // Code to be deleted


//            ArrayList<LatLng> testData = getTestData();
//            mLightRedProvider = new HeatmapTileProvider.Builder().data(testData).build();
//            mLightRedProvider.setGradient(ALT_HEATMAP_GRADIENT);
//            mLightRedProvider.setOpacity(1);
//            mLightRedOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mLightRedProvider));


        // Code to be deleted
        // Build dataset for Longitude and Latitude of entries
        // Check if need to instantiate (avoid setData etc twice)
//            if (mProvider == null || mProvider != null) {

                // Dark Green
                if(signalColorMap.containsKey(signalColor[0]) && signalColorMap.get(signalColor[0]).size() > 0){
                    mDarkGreenProvider = new HeatmapTileProvider.Builder().data(signalColorMap.get(signalColor[0])).build();
                    mDarkGreenProvider.setGradient(ALT_HEATMAP_GRADIENT);
                    mDarkGreenProvider.setOpacity(1);
                    mDarkGreenOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mDarkGreenProvider));
                }

                // Render links
               // attribution.setMovementMethod(LinkMovementMethod.getInstance());
                // Medium Green
                if(signalColorMap.containsKey(signalColor[1]) && signalColorMap.get(signalColor[1]).size() > 0){
                    mMedGreenProvider = new HeatmapTileProvider.Builder().data(signalColorMap.get(signalColor[1])).build();
                    mMedGreenProvider.setGradient(ALT_HEATMAP_GRADIENT);
                    mMedGreenProvider.setOpacity(0.6);
                    mMedGreenOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mMedGreenProvider));
                }

                // Light Green
                if(signalColorMap.containsKey(signalColor[2]) && signalColorMap.get(signalColor[2]).size() > 0){
                    mLightGreenProvider = new HeatmapTileProvider.Builder().data(signalColorMap.get(signalColor[2])).build();
                    mLightGreenProvider.setGradient(ALT_HEATMAP_GRADIENT);
                    mLightGreenProvider.setOpacity(0.2);
                    mLightGreenOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mLightGreenProvider));
                }

                // Dark Red
                if(signalColorMap.containsKey(signalColor[3]) && signalColorMap.get(signalColor[3]).size() > 0){
                    mDarkRedProvider = new HeatmapTileProvider.Builder().data(signalColorMap.get(signalColor[3])).build();
                    mDarkRedProvider.setGradient(RED_HEATMAP_GRADIENT);
                    mDarkRedProvider.setOpacity(0.6);
                    mDarkRedOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mDarkRedProvider));
                }

                // Medium Red
                if(signalColorMap.containsKey(signalColor[4]) && signalColorMap.get(signalColor[4]).size() > 0){
                    mMedRedProvider = new HeatmapTileProvider.Builder().data(signalColorMap.get(signalColor[4])).build();
                    mMedRedProvider.setGradient(RED_HEATMAP_GRADIENT);
                    mMedRedProvider.setOpacity(0.3);
                    mMedRedOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mMedRedProvider));
                }

                // Light Red
                if(signalColorMap.containsKey(signalColor[5]) && signalColorMap.get(signalColor[5]).size() > 0){
                    mLightRedProvider = new HeatmapTileProvider.Builder().data(signalColorMap.get(signalColor[5])).build();
                    mLightRedProvider.setGradient(RED_HEATMAP_GRADIENT);
                    mLightRedProvider.setOpacity(0.1);
                    mLightRedOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mLightRedProvider));
                }



        //***
//            } else {
//
//                ArrayList<LatLng> temp = buildDataSet(entries);
//                mProvider.setData(temp);
//                mOverlay.clearTileCache();
//                mOverlay.setVisible(true);
//            }


            // Update attribution
//            attribution.setText(Html.fromHtml(String.format(getString(R.string.attrib_format),
//                    mLists.get(dataset).getUrl())));

    }

    public void formColorHeatMap(GoogleMap googleMap){

    }

    public ArrayList<LatLng> buildDataSet(ArrayList<Entry> entries){
        Set<LatLng> latLngSet = new HashSet<>();
        ArrayList<LatLng> latLngDataset = new ArrayList<>();
        for(Entry entry: entries){
            if(!latLngSet.contains(entry.getPosition()) && entry.dbm >= -100) {
                latLngDataset.add(entry.getPosition());
                latLngSet.add(entry.getPosition());
            }

        }
        return latLngDataset;

    }

    public void buildSignalMap(){
        for(String color: signalColor){
            ArrayList<LatLng> list = new ArrayList<>();
            signalColorMap.put(color,list);
        }

    }
      // Fill the color based ranges by using signal strength -> dBm
    public void fillSignalRanges(ArrayList<Entry> entries){

        for(Entry entry: entries){
            if(entry.dbm >= -80 && signalColorMap.containsKey(signalColor[0])){
                signalColorMap.get(signalColor[0]).add(entry.getPosition());
            }
            else if(entry.dbm >= -90 && signalColorMap.containsKey(signalColor[1])){
                signalColorMap.get(signalColor[1]).add(entry.getPosition());
            }
            else if(entry.dbm >= -100 && signalColorMap.containsKey(signalColor[2])){
                signalColorMap.get(signalColor[2]).add(entry.getPosition());
            }
            else if(entry.dbm >= -110 && signalColorMap.containsKey(signalColor[3])){
                signalColorMap.get(signalColor[3]).add(entry.getPosition());
            }
            else if(entry.dbm >= -120 && signalColorMap.containsKey(signalColor[4])){
                signalColorMap.get(signalColor[4]).add(entry.getPosition());
            }
            else
                signalColorMap.get(signalColor[5]).add(entry.getPosition());

        }


    }

    public ArrayList<LatLng> getTestData(){
        ArrayList<LatLng> testData = new ArrayList<>();

        double initLong = -78.7875974;

        for(int i =0; i<10000; i++){
            LatLng init = new LatLng(43.0026678, initLong);
            testData.add(init);
            initLong -= 0.001;
         }

        return  testData;
    }




//    /**
//     * Maps name of data set to data (list of LatLngs)
//     * Also maps to the URL of the data set for attribution
//     */
//    private HashMap<String, DataSet> mLists = new HashMap<String, DataSet>();
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.heatmaps_demo;
//    }
//
//    @Override
//    protected void startDemo() {
//        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-25, 143), 4));
//
//        // Set up the spinner/dropdown list
//        Spinner spinner = (Spinner) findViewById(R.id.spinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new SpinnerActivity());
//
//        try {
//            mLists.put(getString(R.string.police_stations), new DataSet(readItems(R.raw.police),
//                    getString(R.string.police_stations_url)));
//            mLists.put(getString(R.string.medicare), new DataSet(readItems(R.raw.medicare),
//                    getString(R.string.medicare_url)));
//        } catch (JSONException e) {
//            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
//        }
//
//        // Make the handler deal with the map
//        // Input: list of WeightedLatLngs, minimum and maximum zoom levels to calculate custom
//        // intensity from, and the map to draw the heatmap on
//        // radius, gradient and opacity not specified, so default are used
//    }

//    public void changeRadius(View view) {
//        if (mDefaultRadius) {
//            mProvider.setRadius(ALT_HEATMAP_RADIUS);
//        } else {
//            mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
//        }
//        mOverlay.clearTileCache();
//        mDefaultRadius = !mDefaultRadius;
//    }
//
//    public void changeGradient(View view) {
//        if (mDefaultGradient) {
//            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
//        } else {
//            mProvider.setGradient(HeatmapTileProvider.DEFAULT_GRADIENT);
//        }
//        mOverlay.clearTileCache();
//        mDefaultGradient = !mDefaultGradient;
//    }
//
//    public void changeOpacity(View view) {
//        if (mDefaultOpacity) {
//            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
//        } else {
//            mProvider.setOpacity(HeatmapTileProvider.DEFAULT_OPACITY);
//        }
//        mOverlay.clearTileCache();
//        mDefaultOpacity = !mDefaultOpacity;
//    }

//    // Dealing with spinner choices
//    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
//        public void onItemSelected(AdapterView<?> parent, View view,
//                                   int pos, long id) {
//            String dataset = parent.getItemAtPosition(pos).toString();
//
//            TextView attribution = ((TextView) findViewById(R.id.attribution));
//
//            // Check if need to instantiate (avoid setData etc twice)
//            if (mProvider == null) {
//                mProvider = new HeatmapTileProvider.Builder().data(
//                        mLists.get(getString(R.string.police_stations)).getData()).build();
//                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
//                // Render links
//                attribution.setMovementMethod(LinkMovementMethod.getInstance());
//            } else {
//                mProvider.setData(mLists.get(dataset).getData());
//                mOverlay.clearTileCache();
//            }
//            // Update attribution
//            attribution.setText(Html.fromHtml(String.format(getString(R.string.attrib_format),
//                    mLists.get(dataset).getUrl())));
//
//        }
//
//        public void onNothingSelected(AdapterView<?> parent) {
//            // Another interface callback
//        }
//    }

//    // Datasets from http://data.gov.au
//    private ArrayList<LatLng> readItems(int resource) throws JSONException {
//        ArrayList<LatLng> list = new ArrayList<LatLng>();
//        InputStream inputStream = getResources().openRawResource(resource);
//        String json = new Scanner(inputStream).useDelimiter("\\A").next();
//        JSONArray array = new JSONArray(json);
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject object = array.getJSONObject(i);
//            double lat = object.getDouble("lat");
//            double lng = object.getDouble("lng");
//            list.add(new LatLng(lat, lng));
//        }
//        return list;
//    }
//
//    /**
//     * Helper class - stores data sets and sources.
//     */
//    private class DataSet {
//        private ArrayList<LatLng> mDataset;
//        private String mUrl;
//
//        public DataSet(ArrayList<LatLng> dataSet, String url) {
//            this.mDataset = dataSet;
//            this.mUrl = url;
//        }
//
//        public ArrayList<LatLng> getData() {
//            return mDataset;
//        }
//
//        public String getUrl() {
//            return mUrl;
//        }
//    }
//
//}
}

// Ins End of ++ spu
