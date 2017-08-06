package edu.buffalo.cse.ubwins.cellmon;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sourav on 7/29/17.
 */

public class CommonMethods {

    static Long mindate;
    private static final String _all_ = "all";
    private static final String _2G_ = "_2G";
    private static final String _2_5G_ = "_2_5G";
    private static final String _3G_ = "_3G";
    private static final String _4G_ = "_4G";

    private static String genHash(String input) throws NoSuchAlgorithmException
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


    // Build Network Map
    public static void networkMap(Map<Integer, String> mapNetwork){
        mapNetwork.put(0, "Unknown");
        mapNetwork.put(1, "2.5G");
        mapNetwork.put(2, "2.5G");
        mapNetwork.put(3, "3G");
        mapNetwork.put(4, "3G");
        mapNetwork.put(5, "3G");
        mapNetwork.put(6, "3G");
        mapNetwork.put(7, "2G");
        mapNetwork.put(8, "3G");
        mapNetwork.put(9, "3G");
        mapNetwork.put(10, "3G");
        mapNetwork.put(11, "2G");
        mapNetwork.put(12, "2G");
        mapNetwork.put(13, "4G");
        mapNetwork.put(14, "3G");
        mapNetwork.put(15, "3G");
        mapNetwork.put(16, "2G");
        mapNetwork.put(17, "3G");
        mapNetwork.put(18, "3G");

    }

    public static void buildMapNetworkDialog(Map<String, Boolean> mapNetworkDialog){
        // Update global network hashmap to all set initially
        mapNetworkDialog.put(_all_, true);
        mapNetworkDialog.put(_2G_, true);
        mapNetworkDialog.put(_2_5G_, true);
        mapNetworkDialog.put(_3G_, true);
        mapNetworkDialog.put(_4G_, true);

    }

}
