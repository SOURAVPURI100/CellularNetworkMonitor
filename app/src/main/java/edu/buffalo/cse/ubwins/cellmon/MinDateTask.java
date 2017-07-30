package edu.buffalo.cse.ubwins.cellmon;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static edu.buffalo.cse.ubwins.cellmon.PhoneCallStateRecorder.TAG;

/**
 * Created by sourav on 7/29/17.
 */
// Ins Begin of ++ spu

class MinDateTask extends AsyncTask<String, Void, Long> {
    private String type;
    private Activity activity;
    private Fragment fragment = null;

    public MinDateTask(Activity activity, Fragment fragment){
        this.activity = activity;
        this.fragment = fragment;
    }
    @Override
    protected Long doInBackground(String... params) {
        type = params[0];
        if(CommonMethods.mindate != null) return CommonMethods.mindate;
        String IMEI_HASH = "";
        String responseStr = "";
        Long ret = System.currentTimeMillis();
        try {
             /*HASH IMEI*/
            IMEI_HASH = genHash(getIMEI());
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        HttpResponse response = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            String customURL = "http://104.196.177.7/aggregator/mindate?imei_hash="
                    + URLEncoder.encode(IMEI_HASH, "UTF-8");
//                Log.d(TAG, customURL);
            request.setURI(new URI(customURL));
            response = client.execute(request);

            responseStr = EntityUtils.toString(response.getEntity());
            Log.v(TAG, "RESPONSE" + responseStr);

                /*PARSE JSON RESPONSE*/
            JSONObject jsonObject = new JSONObject(responseStr);
            String status = jsonObject.getString("status");
            if(status.equals("SUCCESS")){
                String time = jsonObject.getString("timestamp");

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                long timestamp = Long.parseLong(time);
                String parsed = sdf.format(new Date(timestamp));
                Log.d(TAG, "Mindate: " + parsed);
                ret = timestamp;
            }
        } catch (URISyntaxException | IOException | JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Long timestamp) {
        switch (type){
            case "day":
                Log.d(TAG, "Timestamp retrieved for day");
                DatePickerFragment fragment = DatePickerFragment.newInstance(timestamp); //new DatePickerFragment();
                fragment.setTargetFragment(this.fragment, 0);
                fragment.show(activity.getFragmentManager(), "datePicker");
                break;
            case "week":
                Log.d(TAG, "Timestamp retrieved for week");
                WeekPickerFragment weekPickerFragment = WeekPickerFragment.newInstance(timestamp);
                weekPickerFragment.setTargetFragment(this.fragment, 1);
                weekPickerFragment.show(activity.getFragmentManager(), "weekPicker");
                break;
            case "month":
                Log.d(TAG, "Timestamp retrieved for month");
                MonthPickerFragment monthPickerFragment = MonthPickerFragment.newInstance(timestamp);
                monthPickerFragment.setTargetFragment(this.fragment, 2);
                monthPickerFragment.show(activity.getFragmentManager(), "monthPicker");
                break;
            case "start":
                Log.d(TAG, "Timestamp cached.");
                CommonMethods.mindate = timestamp;
                break;
            default:
                Log.d(TAG, "Other selected");
//                    return super.onOptionsItemSelected(item);
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

// Ins End of ++ spu