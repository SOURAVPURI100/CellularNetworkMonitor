//package edu.buffalo.cse.ubwins.cellmon;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.BatteryManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import java.io.File;
//
//import static edu.buffalo.cse.ubwins.cellmon.DBstore.TAG;
//import static edu.buffalo.cse.ubwins.cellmon.ForegroundService.TYPE_MOBILE;
//import static edu.buffalo.cse.ubwins.cellmon.ForegroundService.TYPE_NOT_CONNECTED;
//import static edu.buffalo.cse.ubwins.cellmon.ForegroundService.TYPE_WIFI;
//import static edu.buffalo.cse.ubwins.cellmon.ForegroundService.initPrintWriter;
//import static edu.buffalo.cse.ubwins.cellmon.ForegroundService.printWriter;
//
///**
// * Created by sourav on 6/4/17.
// */
//
//
//// Ins Begin of ++ spu
//public class ChargingOnReceiver extends BroadcastReceiver
//    {
//        File exportDir;
//        boolean isCharging = false;
//        @Override
//        public void onReceive(Context context, Intent intent)
//        {
//            Long timeStamp;
//            String action = intent.getAction();
//            DBstore dbStore = new DBstore(context);
//
//            timeStamp = System.currentTimeMillis();
//
//            String msgType = "UploadData";
//            String message = "Number of Records uploaded to Server are ??? test";
//            dbStore.insertLogData(timeStamp, msgType, message);
//
//            Toast.makeText(context.getApplicationContext(), action+"abcdddddddddddddddddddd" , Toast.LENGTH_LONG).show(); // ++ spu
////            if(action.equals("android.intent.action.ACTION_POWER_CONNECTED")) // -- spu
//            if(action.equals("android.intent.action.ACTION_POWER_CONNECTED")
//                    || action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) // ++ spu
//            {
//                /*ACTION: CHARGING*/
//                Log.e("FS","Charging");
//
//                /*LOG BATTERY STATUS - WRITE TO CSV DIRECTLY*/
//                try
//                {
//                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//                    Intent batteryStatus = context.registerReceiver(null, ifilter);
//
//                    timeStamp = System.currentTimeMillis();
//                    int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//                    float batteryPct = (batteryLevel / (float)scale)*100;
//
//                    String batteryPctStr = String.valueOf(batteryPct);
//
//                    Toast.makeText(context.getApplicationContext(), batteryPctStr , Toast.LENGTH_SHORT).show();
//                    Toast.makeText(context.getApplicationContext(), batteryPctStr+"abc" , Toast.LENGTH_SHORT).show(); // ++ spu
//
//                    String record = timeStamp + "," + batteryPct;
//                    Log.v(TAG, "attempting to write battery status to log file");
//                    File file = new File(exportDir.getAbsolutePath() + "BatteryLevel.csv");
//                    if(file.exists())
//                    {
//                        initPrintWriter(file);
//                        printWriter.println(record);
//                    }
//                }
//                catch(Exception exc)
//                {
//                    exc.printStackTrace();
//                }
//                finally
//                {
//                    if(printWriter != null) printWriter.close();
//                }
//
//                /*CHECK FOR WIFI*/
//                isCharging = true;
//                int count = 0;
//
//                /*UPLOAD 40 HOURS OF DATA IN ONE GO*/
//                while (isCharging && count <= 15)
//                {
//                    //Log.v("FS","Charging: inside while loop");
//                    int status = getConnectivityStatus(context.getApplicationContext());
//                    Toast.makeText(context.getApplicationContext(), "Wifi status "+status, Toast.LENGTH_LONG).show(); //++ spu
//
//                    if(status == TYPE_WIFI)
//                    {
//                        String res = ""; //onFetchClicked();
//                        if(res.equals("DB_EMPTY")||res.equals("Data not stale enough"))
//                        {
//                            break;
//                        }
//                        count += 1;
//                    }
//                    else if(status == TYPE_MOBILE || status == TYPE_NOT_CONNECTED)
//                    {
//                        /*WI-FI DISCONNECTED. STOP UPLOADING HERE*/
//                        break;
//                    }
//                }
//            }
//            else if(action.equals("android.intent.action.ACTION_POWER_DISCONNECTED"))
//            {
//                /*ACTION: NOT CHARGING*/
//                Log.e("FS","Not Charging");
//                isCharging = false;
//
//                /*WRITE TO CSV DIRECTLY*/
//                try
//                {
//                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//                    Intent batteryStatus = context.registerReceiver(null, ifilter);
//
//                    timeStamp = System.currentTimeMillis();
//
//                    int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//                    float batteryPct = (batteryLevel / (float)scale)*100;
//                    String batteryPctStr = String.valueOf(batteryPct);
//
//                    Toast.makeText(context.getApplicationContext(), batteryPctStr , Toast.LENGTH_SHORT).show();
//
//                    String record = timeStamp + "," + batteryPct;
//                    Log.v(TAG, "attempting to write battery status to log file");
//                    File file = new File(exportDir.getAbsolutePath() + "BatteryLevel.csv");
//                    if(file.exists())
//                    {
//                        initPrintWriter(file);
//                        printWriter.println(record);
//                    }
//                }
//                catch(Exception exc)
//                {
//                    exc.printStackTrace();
//                }
//                finally
//                {
//                    if(printWriter != null) printWriter.close();
//                }
//            }
//        }
//
//        public static int getConnectivityStatus(Context context)
//        {
//            ConnectivityManager cm =
//                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            if (null != activeNetwork)
//            {
//                if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
//                    return TYPE_WIFI;
//
//                if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
//                    return TYPE_MOBILE;
//            }
//            return TYPE_NOT_CONNECTED;
//        }
//
//
//
//
//}
//
//// Ins End of ++ spu
