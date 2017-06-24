package edu.buffalo.cse.ubwins.cellmon;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static edu.buffalo.cse.ubwins.cellmon.Scheduler.scheduler;

public class ForegroundService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{
    private static final String LOG_TAG = "ForegroundService";

    ScheduleIntentReceiver scheduleIntentReceiver;
//    Scheduler scheduler;
    private GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;
    public static Double FusedApiLatitude;
    public static Double FusedApiLongitude;
    public static long LastFusedLocation;

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    String URL_UPLOAD = "http://104.196.177.7:80/aggregator/upload/";
    String responsePhrase;
    String statusPhraseLogger;
    String recordsPhraseLogger;
    String IMEI_TO_POST;
    static PrintWriter printWriter = null;

    private SQLiteDatabase sqLiteDatabase;

    // Originally 5 hours and 12000 entries
//    static int hoursPerUpload = 5; // -- spu
    static int hoursPerUpload = 0; // ++ spu
    static int entriesToUpload = 12000;
    File exportDir;
    Alarm alarm = new Alarm();
    private Scheduler scheduler;

    PowerManager.WakeLock wakeLock;
    SharedPreferences preferences;
    static FileWriter fileWriter = null;
    static File file = null;
    ContentValues contentValues = new ContentValues();

    public final String TAG = "[CELMON-FRGRNDSRVC]";

    public static void initPrintWriter(File file)
    {
        try
        {
            printWriter = new PrintWriter(new FileWriter(file,true));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        buildGoogleApiClient();
        scheduleIntentReceiver = new ScheduleIntentReceiver();
        scheduler = new Scheduler();
        Log.v(LOG_TAG, "Creating Scheduler Instance");

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(receiver, filter);

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state))
        {
            Log.v(TAG, "MEDIA MOUNT ERROR!");
        }
        else {
            exportDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (!exportDir.exists())
            {
                exportDir.mkdirs();
                Log.v(TAG, "Directory made");
            }

            File file = new File(exportDir.getAbsolutePath() + "BatteryLevel.csv");

            if(!file.exists())
            {
                try
                {
                    Log.v(TAG, "CREATING FILE at " + exportDir.getAbsolutePath());
                    file.createNewFile();
                    initPrintWriter(file);
                    printWriter.write("TIMESTAMP, BATTERY_LEVEL");
                }
                catch(IOException ex)
                {
                    ex.printStackTrace();
                }
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        if(null == intent){
            return START_STICKY;
        }
        else if(null == intent.getAction()){
            return START_STICKY;
        }

        if (intent.getAction().equals("startforeground"))
        {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction("mainAction");
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("CellularNetworkMonitor is running")
                    .setSmallIcon(R.mipmap.m)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(101,
                    notification);

            /*ACQUIRING WAKELOCK*/
//            PowerManager mgr =
//                    (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
//            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
//            wakeLock.acquire();
//            Log.v(LOG_TAG, "Acquired WakeLock");

            mGoogleApiClient.connect();
            //finished connecting API Client
//            locationFinder = new LocationFinder(getApplicationContext());
            //calling getLocation() from Location provider
//            locationFinder.getLocation();

            /*CALL TO SCHEDULER METHOD*/
            alarm.setAlarm(this);

//            scheduler.beep(getApplicationContext());
            //Log.v(LOG_TAG, "SCHEDULER SET TO BEEP Every second");
            Toast.makeText(getApplicationContext(),
                    "Tracking set to ON!", Toast.LENGTH_SHORT).show();

        }
        else if (intent.getAction().equals("stopforeground"))
        {
            /*CANCEL SCHEDULER AND RELEASE WAKELOCK*/
//            if(wakeLock.isHeld()) {
//                wakeLock.release();
//            }
            //Log.v(LOG_TAG, "Releasing WakeLock");
            alarm.cancelAlarm(this);

//            Scheduler.stopScheduler();
            //Log.v(LOG_TAG, "Beeping Service Stoppped");

            /*to disconnect google api client*/
            if(mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.disconnect();
            }
            stopForeground(true);
            stopSelf();
            Toast.makeText(getApplicationContext(),
                    "Tracking set to OFF!", Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(10000);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        FusedApiLatitude = location.getLatitude();
        FusedApiLongitude = location.getLongitude();
        LastFusedLocation = System.currentTimeMillis();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(LOG_TAG,"Google Api client has been suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.i(LOG_TAG,"Google Api client connection has failed");
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        boolean isCharging = false;
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Long timeStamp;
            String action = intent.getAction();

            // ins Begin of ++ spu
            DBstore dbStore = new DBstore(context);

            timeStamp = System.currentTimeMillis();

            String msgType = "Upload_Data";
            String message = "Number of Records uploaded to Server are AAA test"+action;
            dbStore.insertLogData(timeStamp, msgType, message);

            // ins end of ++ spu

           // Toast.makeText(getApplicationContext(), action+"abc" , Toast.LENGTH_SHORT).show(); // ++ spu
            if(action.equals("android.intent.action.ACTION_POWER_CONNECTED")) // ++ spu
            {
                /*ACTION: CHARGING*/
                Log.e("FS","Charging");

                /*LOG BATTERY STATUS - WRITE TO CSV DIRECTLY*/
                try
                {
                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = context.registerReceiver(null, ifilter);

                    timeStamp = System.currentTimeMillis();
                    int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    float batteryPct = (batteryLevel / (float)scale)*100;

                    String batteryPctStr = String.valueOf(batteryPct);

                    Toast.makeText(getApplicationContext(), batteryPctStr , Toast.LENGTH_SHORT).show();

                    String record = timeStamp + "," + batteryPct;
                    Log.v(TAG, "attempting to write battery status to log file");
                    File file = new File(exportDir.getAbsolutePath() + "BatteryLevel.csv");
                    if(file.exists())
                    {
                        initPrintWriter(file);
                        printWriter.println(record);
                    }
                }
                catch(Exception exc)
                {
                    exc.printStackTrace();
                }
                finally
                {
                    if(printWriter != null) printWriter.close();
                }

                /*CHECK FOR WIFI*/
                isCharging = true;
                int count = 0;
                // Ins Begin of ++ spu
                msgType = "Upload_Data";
                message = "Message before charging  check"+action;
                dbStore.insertLogData(timeStamp, msgType, message);

                // Ins End of ++ spu
                /*UPLOAD 40 HOURS OF DATA IN ONE GO*/
                while (isCharging && count <= 15)
                {
                    //Log.v("FS","Charging: inside while loop");
                    int status = getConnectivityStatus(getApplicationContext());
                    //Toast.makeText(getApplicationContext(), "Wifi status "+status, Toast.LENGTH_LONG).show(); //++ spu

                    if(status == TYPE_WIFI)
                    {

                        // Ins Begin of ++ spu
                        msgType = "Upload_Data";
                        message = "Message On fetch clicked "+action;
                        dbStore.insertLogData(timeStamp, msgType, message);
                        // Ins End of ++ spu
                           String res = onFetchClicked();
                           if(res.equals("DB_EMPTY")||res.equals("Data not stale enough"))
                           {
                               break;
                           }
                        count += 1;
                    }
                    else if(status == TYPE_MOBILE || status == TYPE_NOT_CONNECTED)
                    {
                        /*WI-FI DISCONNECTED. STOP UPLOADING HERE*/
                        break;
                    }
                }
            }
            else if(action.equals("android.intent.action.ACTION_POWER_DISCONNECTED"))
            {
                /*ACTION: NOT CHARGING*/
                Log.e("FS","Not Charging");
                isCharging = false;

                /*WRITE TO CSV DIRECTLY*/
                try
                {
                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = context.registerReceiver(null, ifilter);

                    timeStamp = System.currentTimeMillis();

                    int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    float batteryPct = (batteryLevel / (float)scale)*100;
                    String batteryPctStr = String.valueOf(batteryPct);

                    Toast.makeText(getApplicationContext(), batteryPctStr , Toast.LENGTH_SHORT).show();

                    String record = timeStamp + "," + batteryPct;
                    Log.v(TAG, "attempting to write battery status to log file");
                    File file = new File(exportDir.getAbsolutePath() + "BatteryLevel.csv");
                    if(file.exists())
                    {
                        initPrintWriter(file);
                        printWriter.println(record);
                    }
                }
                catch(Exception exc)
                {
                    exc.printStackTrace();
                }
                finally
                {
                    if(printWriter != null) printWriter.close();
                }
            }
        }
    };

    public static int getConnectivityStatus(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork)
        {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public String onFetchClicked()
    {
        boolean isConnected = isConnected();
        String res = "";
        if(isConnected)
        {
            //Log.v(TAG, "isConnected = TRUE");
            try {

                // ins Begin of ++ spu
                DBstore dbStore = new DBstore(getApplicationContext());

                long timeStamp = System.currentTimeMillis();

                String msgType = "Upload_Data";
                String message = "Message before URL UPLOAD Async task";
                dbStore.insertLogData(timeStamp, msgType, message);
                LogAsyncTask myTask = new LogAsyncTask();
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                    message = "Message parallel execute SDK " + Build.VERSION.SDK_INT;
                    dbStore.insertLogData(timeStamp, msgType, message);
                    res = myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_UPLOAD).get();
                }
                else
                    res = myTask.execute(URL_UPLOAD).get();
                // ins end of ++ spu

//                res = new LogAsyncTask().execute(URL_UPLOAD).get();// -- spu

                // ins Begin of ++ spu
                message = "Message after URL UPLOAD Async task with status "+myTask.getStatus()+
                        " res value "+res;
                dbStore.insertLogData(timeStamp, msgType, message);
                // ins End of ++ spu

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Log.v(LOG_TAG, "isConnected = FALSE");
            Toast.makeText(getBaseContext(),
                    "Device has no Internet Connectivity! " +
                            "Please check your Network Connection and try again",
                    Toast.LENGTH_LONG).show();
        }
        return res;
    }

    private class LogAsyncTask extends AsyncTask<String, Void, String>
    {
        // Ins Begin of ++ spu
        @Override
        protected void onPreExecute() {
            DBstore dbStore = new DBstore(getApplicationContext());

            long timeStamp = System.currentTimeMillis();

            String msgType = "Upload_Data";
            String message = "Message on pre execute";
            dbStore.insertLogData(timeStamp, msgType, message);

        }

        // Ins End of ++ spu

        @Override
        protected String doInBackground(String... urls)
        {
            Log.v(LOG_TAG, "inside LogAsyncTask");

            // ins Begin of ++ spu
            DBstore dbStore = new DBstore(getApplicationContext());

            long timeStamp = System.currentTimeMillis();

            String msgType = "Upload_Data";
            String message = "Message before Log Post";
            dbStore.insertLogData(timeStamp, msgType, message);

            // ins end of ++ spu
            return LOG_POST(urls[0]);
        }
        @Override
        protected void onPostExecute(String result)
        {
            // ins Begin of ++ spu
            DBstore dbStore = new DBstore(getApplicationContext());

            long timeStamp = System.currentTimeMillis();

            String msgType = "Upload_Data";
            String message = "Message on post execute";
            dbStore.insertLogData(timeStamp, msgType, message);
            // ins end of ++ spu


            //Toast.makeText(getBaseContext(), "Attempt to POST made!", Toast.LENGTH_LONG).show();
        }
    }



    public String LOG_POST(String url)
    {
        String TEMP_TAG = "[CURSOR_DATA] : ";
        int statusCode;
        String result = "";

        Cursor cursor = fetchTopFromDB(entriesToUpload);
        int count = cursor.getCount();
        DataRecordOuterClass.DataRecord.Builder dataRecord =
                DataRecordOuterClass.DataRecord.newBuilder();
        DataRecordOuterClass.DataRecord recordToSend;
        boolean uploadflag = true;

        if(cursor.moveToFirst()) {
            long timeStamp = cursor.getLong(4);
            long currTimestamp = System.currentTimeMillis();
            long timeGap = currTimestamp - timeStamp;
            if(timeGap < hoursPerUpload*60*60*1000)
            {
                uploadflag = false;
                result = "Data not stale enough";
            }
        }

        if (cursor.moveToFirst() && uploadflag)
        {
            String IMEI = getIMEI();
            String networkOperatorCode = getNetworkOperatorCode();
            String networkOperatorName = getNetworkOperatorName();

            try{
                IMEI_TO_POST = genHash(IMEI);
            }
            catch(NoSuchAlgorithmException nsa)
            {
                nsa.printStackTrace();
            }
            dataRecord.setIMEIHASH(IMEI_TO_POST);
            dataRecord.setNETWORKOPERATORNAME(networkOperatorName);
            dataRecord.setNETWORKOPERATORCODE(networkOperatorCode);

            do {
                dataRecord.addENTRY(DataRecordOuterClass.DataEntry.newBuilder()
                        .setFUSEDLAT(cursor.getDouble(1))
                        .setFUSEDLONG(cursor.getDouble(2))
                        .setSTALE(cursor.getInt(3) > 0)
                        .setTIMESTAMP(cursor.getLong(4))
                        .setNETWORKCELLTYPEValue(cursor.getInt(5))
                        .setNETWORKTYPEValue(cursor.getInt(6))
                        .setNETWORKPARAM1(cursor.getInt(7))
                        .setNETWORKPARAM2(cursor.getInt(8))
                        .setNETWORKPARAM3(cursor.getInt(9))
                        .setNETWORKPARAM4(cursor.getInt(10))
                        .setSIGNALDBM(cursor.getInt(11))
                        .setSIGNALLEVEL(cursor.getInt(12))
                        .setSIGNALASULEVEL(cursor.getInt(13))
                        .setNETWORKSTATEValue(cursor.getInt(14))
                        .setNETWORKDATAACTIVITYValue(cursor.getInt(15))
                        .setVOICECALLSTATEValue(cursor.getInt(16)).build());

                recordToSend = dataRecord.build();
            } while (cursor.moveToNext());

            byte[] logToSend = recordToSend.toByteArray();
            int len = logToSend.length;
            Log.e("SIZE","Length of 5 entries is : "+len);

            // ins Begin of ++ spu
            DBstore dbStore = new DBstore(getApplicationContext());

            long timeStamp = System.currentTimeMillis();

            String msgType = "Upload_Data";
            String message = "Message before HTTP request";
            dbStore.insertLogData(timeStamp, msgType, message);

            // ins end of ++ spu

            try {

                    /*1. create HttpClient*/
                HttpClient httpclient = new DefaultHttpClient();

                    /*2. make POST request to the given URL*/
                HttpPost httpPost = new HttpPost(url);

                    /*3. Build ByteArrayEntity*/
                ByteArrayEntity byteArrayEntity = new ByteArrayEntity(logToSend);

                    /*4. Set httpPost Entity*/
                httpPost.setEntity(byteArrayEntity);

                    /*5. Execute POST request to the given URL*/
                HttpResponse httpResponse = httpclient.execute(httpPost);

                /*9. receive response as inputStream*/
                statusCode = httpResponse.getStatusLine().getStatusCode();

                /*CONVERT INPUT STREAM TO STRING*/
                responsePhrase = EntityUtils.toString(httpResponse.getEntity());
                Log.v(LOG_TAG, "RESPONSE" + responsePhrase);

                /*PARSE JSON RESPONSE*/
                JSONObject jsonObject = new JSONObject(responsePhrase);
                recordsPhraseLogger = jsonObject.getString("records");
                statusPhraseLogger = jsonObject.getString("status");

//                Log.e(LOG_TAG, "STATUS: " + statusPhraseLogger);
//                Log.e(LOG_TAG, "RECORDS INSERTED: " + recordsPhraseLogger);

                /*DELETE FROM DB IF NO OF RECORDS FETCHED == NO OF RECORDS INSERTED*/
                if(Integer.parseInt(recordsPhraseLogger)==count)
                {
                    Log.e(LOG_TAG, "Attempting to delete from DB");
                    String rawQuery =
                            "DELETE FROM cellRecords WHERE ID IN " +
                                    "(SELECT ID FROM cellRecords ORDER BY TIMESTAMP LIMIT " +
                                    count + ");";
                    DBHandler dbHandler = new DBHandler(getApplicationContext());
                    SQLiteDatabase sqLiteDatabase = dbHandler.getWritableDatabase();
                    sqLiteDatabase.beginTransaction();
                    sqLiteDatabase.execSQL(rawQuery);
                    sqLiteDatabase.setTransactionSuccessful();
                    sqLiteDatabase.endTransaction();
                    sqLiteDatabase.close();
                }
                // Begin of ins ++ spu
                dbStore = new DBstore(this);
                timeStamp = System.currentTimeMillis();

                msgType = "Upload_Data";
                message = "Number of Records uploaded to Server are "+Integer.parseInt(recordsPhraseLogger)+
                        " and status of uploading was "+statusCode;
                dbStore.insertLogData(timeStamp, msgType, message);
                // End of ins ++ spu
                if (statusCode != 404)
                {
                    result = Integer.toString(statusCode);
                    Log.v(LOG_TAG, "STATUS CODE: " + result);
                }
                else
                {
                    result = Integer.toString(statusCode);
                    Log.v(LOG_TAG, "STATUS CODE: " + result);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Log.e(TEMP_TAG, "DB IS BROKE AS HELL!");
            result = "DB_EMPTY";


            // ins Begin of ++ spu
            DBstore dbStore = new DBstore(getApplicationContext());

            long timeStamp = System.currentTimeMillis();

            String msgType = "Upload_Data";
            String message = "Message on Else of DB Empty or Broke";
            dbStore.insertLogData(timeStamp, msgType, message);

            // ins end of ++ spu
        }
        sqLiteDatabase.close();
        return result;
    }
    private String getIMEI() {
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    private String getNetworkOperatorCode() {
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperator();
    }

    private String getNetworkOperatorName() {
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperatorName();
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

    private Cursor fetchTopFromDB(int limit)
    {
        String rawQuery = "SELECT * FROM cellRecords ORDER BY TIMESTAMP LIMIT " + limit;
        DBHandler dbHandler = new DBHandler(getApplicationContext());
        sqLiteDatabase = dbHandler.getWritableDatabase();
        return sqLiteDatabase.rawQuery(rawQuery, null);
    }

    public boolean isConnected()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}