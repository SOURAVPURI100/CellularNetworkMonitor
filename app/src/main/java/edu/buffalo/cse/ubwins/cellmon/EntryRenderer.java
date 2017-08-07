package edu.buffalo.cse.ubwins.cellmon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.*;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sourav on 6/7/17.
 */

// Ins Begin of ++ spu
class NetworkData{
    int avg_dBm;
    int count;

    public NetworkData(int avg_dBm){
        this.avg_dBm = avg_dBm;
        this.count = 1;
    }
}


public class EntryRenderer extends DefaultClusterRenderer<Entry> {
      private IconGenerator mIconGenerator = null;
      private IconGenerator mClusterIconGenerator = null;
      private float[] visibilty = {0.25f ,0.5f, 0.75f, 1.0f};
      private Map<Integer, String>  mapNetwork = new HashMap<>();
      private Context mContext = null;

    public EntryRenderer(Context context, GoogleMap googleMap, ClusterManager<Entry> clusterManager) {
        super(context, googleMap, clusterManager);
        mClusterIconGenerator = new IconGenerator(context);
        mIconGenerator = new IconGenerator(context);
        buildNetworkMap();
        mContext = context;
    }

//    @Override
    protected void onBeforeClusterItemRendered(Entry entry, MarkerOptions markerOptions) {

        String technology = "";
        super.onBeforeClusterItemRendered(entry, markerOptions);
        markerOptions.alpha(setVisibilty(entry.dbm));
        if(mapNetwork.containsKey(entry.network_type)) {
            technology = mapNetwork.get(entry.network_type);
            if(technology.equals("4G")){
                buildCustomMarker(markerOptions, R.drawable._4ghigh);
            }
            else if(technology.equals("3G")){
                buildCustomMarker(markerOptions, R.drawable._3ghigh);
            }
            else if(technology.equals("2.5G")){
                buildCustomMarker(markerOptions, R.drawable._2_5ghigh);
            }
            else if(technology.equals("2G")){
                buildCustomMarker(markerOptions, R.drawable._2ghigh);
            }
            else{
                buildCustomMarker(markerOptions, R.drawable._unknown);
            }

        }
        markerOptions.title("Technology");
        markerOptions.snippet(technology);

    }

    @Override
    protected void onBeforeClusterRendered(com.google.maps.android.clustering.Cluster<Entry> cluster, MarkerOptions markerOptions) {

        Map<String, NetworkData> mapNetTemp = new HashMap<>();
        Set<String> networks = new HashSet<>();
        int maxNetCount = 0;
        String maxNetType ="";
        long avg_dbm = 0;
//        int count = 0;
        String clusterText ="";
        Iterator<Entry> itr = cluster.getItems().iterator();
        NetworkData networkData = null;
        while(itr.hasNext()){
            Entry entry = itr.next();
            int netType= entry.network_type;
            String net = "";
            if(mapNetwork.containsKey(netType)){
                net = mapNetwork.get(netType); // 2G, 3G etc.
                if(mapNetTemp.containsKey(net)){
                    networkData = mapNetTemp.get(net);
                    networkData.avg_dBm += entry.dbm;
                    networkData.count++;
                }
                else{
                    networkData = new NetworkData(entry.dbm);
                    mapNetTemp.put(net,networkData);
                }

            }

            if(maxNetCount < networkData.count){
                maxNetCount = networkData.count;
                maxNetType = net;
            }
            // Add network into set for e.g 2G, 3G, 3.5G etc
            if(mapNetwork.containsKey(netType))
                networks.add(mapNetwork.get(netType));

        }

        if(mapNetTemp.containsKey(maxNetType)){
            networkData = mapNetTemp.get(maxNetType);
            avg_dbm = (networkData.avg_dBm)/networkData.count;

        }

        markerOptions.title("Technologies Percentage");
        markerOptions.snippet(fetchClusterTechnologies(mapNetTemp, cluster.getSize()));
        markerOptions.alpha(setVisibilty(avg_dbm));
        markerOptions.visible(true);

        // generate color for cluster based on Network Type


//        mIconGenerator.setColor(generateColor(indexMaxNetwork));
//        mIconGenerator.setColor(generateColor(maxNetType));
//        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(avg_dbm));
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        // Make a text for all networks
        Iterator<String> netItr = networks.iterator();
        while(netItr.hasNext()){
            clusterText += netItr.next()+" ";
        }

        if(maxNetType.equals("4G")){
            buildCustomMarker(markerOptions, R.drawable._4ghigh);
        }
        else if(maxNetType.equals("3G")){
            buildCustomMarker(markerOptions, R.drawable._3ghigh);
        }
        else if(maxNetType.equals("2.5G")){
            buildCustomMarker(markerOptions, R.drawable._2_5ghigh);
        }
        else if(maxNetType.equals("2G")){
            buildCustomMarker(markerOptions, R.drawable._2ghigh);
        }
        else{
            buildCustomMarker(markerOptions, R.drawable._unknown);

        }

    }

    @Override
    protected boolean shouldRenderAsCluster(com.google.maps.android.clustering.Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 6;
    }

    public float setVisibilty(long dbmValue){

        if(dbmValue <= -115)
            return visibilty[0];
        else if(dbmValue <= -100)
            return visibilty[1];
        else if(dbmValue <= -85)
            return visibilty[2];
        else
            return visibilty[3];

    }
    // Generate color based on Network type
    public int generateColor(String networkType){

        if(networkType.equals("4G")) //LTE
            return Color.GREEN;
        else if (networkType.equals("3.5G"))
            return Color.BLUE;
        else if (networkType.equals("3G"))
            return Color.RED;
        else if (networkType.equals("2.5G"))
            return Color.YELLOW;
        else if (networkType.equals("2G"))
            return Color.rgb(Color.RED, Color.GREEN, Color.BLUE);
        else
            return Color.GRAY;

    }

    // Build Network Map
    public void buildNetworkMap(){
          CommonMethods.networkMap(mapNetwork);
    }
    // Build custom marker for cluster item and individual marker
    public void buildCustomMarker(MarkerOptions marker, int resource){
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
                resource);

        Bitmap newIcon = Bitmap.createScaledBitmap(
                icon, 135, 120, false);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(newIcon);
        marker.icon(descriptor);

    }

    public String fetchClusterTechnologies(Map<String, NetworkData> mapNet, int totalItems){
        String snippet = "";
        int count = 0;
        int currCount = 0;

        // cluster percentage for 4G
        if(mapNet.containsKey("4G")) {
            count = (mapNet.get("4G").count) * 100 / totalItems;
        }
        snippet += "4G "+count+"%, ";

        currCount += count;
        count = 0;
        // cluster percentage for 3G
        if(mapNet.containsKey("3G")) {

            if (mapNet.containsKey("2.5G") || mapNet.containsKey("2G")) {
                count = (mapNet.get("3G").count * 100) / totalItems;
            } else {
                count = 100 - currCount;
            }
        }
        snippet += "3G "+count+"%, ";

        currCount += count;
        count = 0;
        // cluster percentage for 2.5G
        if(mapNet.containsKey("2.5G")){

            if(mapNet.containsKey("2G")){
                count = (mapNet.get("2.5G").count * 100)/totalItems;
            }
            else {
                count = 100 - currCount;
            }
        }
        snippet += "2.5G "+count+"%, ";
        currCount += count;
        count = 0;
        // cluster percentage for 2G
        if(mapNet.containsKey("2G")) {
            count = 100 - currCount;
        }
        snippet += "2G "+count+"%";

        return snippet;
    }
}

// Ins End of ++ spu
