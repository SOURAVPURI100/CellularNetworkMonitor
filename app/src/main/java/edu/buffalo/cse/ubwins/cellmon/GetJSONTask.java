package edu.buffalo.cse.ubwins.cellmon;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static edu.buffalo.cse.ubwins.cellmon.CellularDataRecorder.TAG;

/** Ins Begin of ++spu
 * Created by sourav on 8/4/17.
 */

class GetJSONTask extends AsyncTask<String, Void, Boolean> {
    private ArrayList<Entry> entryList;
    private Fragment fragment;
    private MapFragment mapFragment;
    private UIStatistics UIFragment;
    private Activity activity;
    private int mapView = 0; // For technology // 1 for signal

    public GetJSONTask(Activity activity, Fragment fragment) {
        entryList = new ArrayList<Entry>();

        this.fragment = fragment;
        if(fragment instanceof MapFragment){
            this.mapFragment = (MapFragment) fragment;
        }
        else if(fragment instanceof  UIStatistics){
            this.UIFragment = (UIStatistics) fragment;
        }

        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String timespan = strings[0];
        String timestart = strings[1];
        mapView = Integer.parseInt(strings[2]);
        Boolean ret = false;

        String IMEI_HASH = "";
        String responseStr = "";
        try {
             /*HASH IMEI*/
            IMEI_HASH = genHash(getIMEI());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        HttpResponse response = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
//                String customURL = "http://104.196.177.7/aggregator/genjson?imei_hash="
//                        + URLEncoder.encode(IMEI_HASH, "UTF-8");
            String customURL = "http://104.196.177.7/aggregator/genjson?";
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair("imei_hash", IMEI_HASH));
            params.add(new BasicNameValuePair("timespan", timespan));
            params.add(new BasicNameValuePair("timestart", timestart));
            String paramString = URLEncodedUtils.format(params, "utf-8");

            customURL += paramString;

//                Log.d(TAG, customURL);
            request.setURI(new URI(customURL));
            response = client.execute(request);

// Ins Begin of spu
            HttpEntity entity = response.getEntity();
            InputStream inputStream =  entity.getContent();
            //create JsonParser object
            ObjectMapper mapper = new ObjectMapper();
            JsonParser jsonParser = mapper.getFactory().createParser(inputStream);
            buildEntriesData(jsonParser, entryList);

// Ins End of spu

// Del Begin of spu

//                responseStr = EntityUtils.toString(response.getEntity());
//                Log.v(TAG, "JSON received.");
//
//
//                /*PARSE JSON RESPONSE*/
//                JSONObject jsonObject = new JSONObject(responseStr);
//                String status = jsonObject.getString("status");
//                if (status.equals("SUCCESS")) {
////                  Ins Begin of ++ spu
//                    if (timespan.equals("week") || timespan.equals("month")) {
//                        fillEntriesData(jsonObject, entryList);
//                    } else {
//                        JSONObject data = jsonObject.getJSONObject("data");
//                        JSONArray entries = data.getJSONArray("entries");
//                        Log.d(TAG, entries.length() + " entries for selected timeframe");
//                        for (int i = 0; i < entries.length(); ++i) {
//                            JSONObject jsonentry = entries.getJSONObject(i);
//                            Entry entry = Entry.mapJSON(jsonentry);
//                            entryList.add(entry);
//                        }
//                    }
//
////                  Ins End of ++ spu
//
//                    // Del Begin of ++ spu
////                    JSONObject data = jsonObject.getJSONObject("data");
////                    JSONArray entries = data.getJSONArray("entries");
////                    Log.d(TAG, entries.length() + " entries for selected timeframe");
////                    for(int i = 0; i < entries.length(); ++i){
////                        JSONObject jsonentry = entries.getJSONObject(i);
//////                    Log.d(TAG, entry.toString());
////                        Entry entry = Entry.mapJSON(jsonentry);
////                        entryList.add(entry);
////                    }
//                    // Del End of ++ spu
//                    // ret = true; -- spu
//                }

// Del End of spu
            ret = true;  // ++ spu

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Boolean b) {
        if (b) {
            // Ins Begin of ++ spu

            // added "all" network filter
            if(fragment != null && fragment.getActivity() != null){

                if(fragment instanceof MapFragment){
                    mapFragment.entryListGlobal = entryList;
                    if (mapView == 0)
                        mapFragment.refreshMap(entryList);
                    else
                        mapFragment.refreshSignalMap(entryList);
                    // Ins End of ++ spu
//                refreshMap(entryList); -- spu
                }
                else if(fragment instanceof UIStatistics){
                    UIFragment.entryListGlobal = entryList;
                    UIFragment.buildStatisticsView(entryList);
                }
            }

        } else {
            Log.d(TAG, "No data for selected date");
            // Add popup alert
        }
    }


    public void buildEntriesData(JsonParser jsonParser, ArrayList<Entry> entryList) {
        try {
            Entry entry = new Entry();
            entry.coordinate = new Coordinate(0,0);
            while(true){
                JsonToken token = jsonParser.nextToken();
                if(token == null){
                    break;
                }

                String name = jsonParser.getCurrentName();
                if(name != null){

                    if(name.equals("network_type")){
                        jsonParser.nextToken();
                        entry.network_type = jsonParser.getIntValue();
                        entryList.add(entry);
                        entry = new Entry();
                        entry.coordinate = new Coordinate(0,0);
                    }
                    else if(name.equals("timestamp")){
                        jsonParser.nextToken();
                        entry.timestamp= jsonParser.getLongValue();
                    }
                    else if(name.equals("fused_lat")){
                        jsonParser.nextToken();
                        entry.coordinate.setLatitude(jsonParser.getDoubleValue());
                    }
                    else if(name.equals("fused_long")){
                        jsonParser.nextToken();
                        entry.coordinate.setLongitude(jsonParser.getDoubleValue());
                    }
                    else if(name.equals("network_cell_type")){
                        jsonParser.nextToken();
                        entry.networkCellType = jsonParser.getIntValue();
                    }

                    else if(name.equals("signal_dbm")){
                        jsonParser.nextToken();
                        entry.dbm = jsonParser.getIntValue();
                    }

                    else if(name.equals("signal_level")){
                        jsonParser.nextToken();
                        entry.signalLevel = jsonParser.getIntValue();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String genHash(String input) throws NoSuchAlgorithmException
    {
        String IMEI_Base64="";
        try
        {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sha256Hash = sha256.digest(input.getBytes("UTF-8"));
            IMEI_Base64 = Base64.encodeToString(sha256Hash, Base64.DEFAULT);
            IMEI_Base64=IMEI_Base64.replaceAll("\n", "");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return IMEI_Base64;
    }

    private String getIMEI() {
        TelephonyManager telephonyManager =
                (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}


//Ins End of ++spu