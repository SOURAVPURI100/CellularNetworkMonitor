package edu.buffalo.cse.ubwins.cellmon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

public class EntryRenderer extends DefaultClusterRenderer<Entry> {
      private IconGenerator mIconGenerator = null;
      private IconGenerator mClusterIconGenerator = null;
      private float[] visibilty = {0.2f ,0.4f, 0.6f, 0.8f, 1.0f};
      private Map<Integer, String> mapNetwork = new HashMap<>();
//    private final ImageView mImageView;
//    private final ImageView mClusterImageView;
//    private final int mDimension;

    public EntryRenderer(Context context, GoogleMap googleMap, ClusterManager<Entry> clusterManager) {
        super(context, googleMap, clusterManager);
        mClusterIconGenerator = new IconGenerator(context);
        mIconGenerator = new IconGenerator(context);
        buildNetworkMap();
//
//        View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
//        mClusterIconGenerator.setContentView(multiProfile);
//        mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
//
//        mImageView = new ImageView(getApplicationContext());
//        mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
//        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
//        int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
//        mImageView.setPadding(padding, padding, padding, padding);
//        mIconGenerator.setContentView(mImageView);
    }

//    @Override
    protected void onBeforeClusterItemRendered(Entry entry, MarkerOptions markerOptions) {
//        // Draw a single person.
//        // Set the info window to show their name.
//        mImageView.setImageResource(person.profilePhoto);
//        Bitmap icon = mIconGenerator.makeIcon();
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);

        markerOptions.title("Average dBm "+entry.dbm);
        markerOptions.snippet("dBm "+entry.dbm);
    }

    @Override
    protected void onBeforeClusterRendered(com.google.maps.android.clustering.Cluster<Entry> cluster, MarkerOptions markerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
//        List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
//        int width = mDimension;
//        int height = mDimension;
//
//        for (Person p : cluster.getItems()) {
//            // Draw 4 at most.
//            if (profilePhotos.size() == 4) break;
//            Drawable drawable = getResources().getDrawable(p.profilePhoto);
//            drawable.setBounds(0, 0, width, height);
//            profilePhotos.add(drawable);
//        }
//        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
//        multiDrawable.setBounds(0, 0, width, height);
//
//        mClusterImageView.setImageDrawable(multiDrawable);
//        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        Set<String> networks = new HashSet<>();
        int[] networkTypes = new int[19];
        int indexMaxNetwork = -1;
        int maxNetCount = 0;
        long avg_dbm = 0;
        int count = 0;
        String clusterText ="";
        Iterator<Entry> itr = cluster.getItems().iterator();

        while(itr.hasNext()){

            Entry entry = itr.next();
            avg_dbm += entry.dbm;
            count++;

            int netType= entry.network_type;
            networkTypes[netType] += 1;

            if(networkTypes[netType] > maxNetCount){
                maxNetCount = networkTypes[netType];
                indexMaxNetwork = netType;
            }
            if(mapNetwork.containsKey(netType))
                networks.add(mapNetwork.get(netType));
            // Add network into set for e.g 2G, 3G, 3.5G etc
        }

        avg_dbm = avg_dbm / count;
        markerOptions.title("Average dBm "+avg_dbm);
        markerOptions.snippet("dBm "+avg_dbm);
//        if(cluster.getSize() > 50)
//            markerOptions.alpha(1);
//        else
//            markerOptions.alpha(0.4f);

        markerOptions.alpha(setVisibilty(avg_dbm));

        // generate color for cluster based on Network Type
        mIconGenerator.setColor(generateColor(indexMaxNetwork));


//        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(avg_dbm));
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        // Make a text for all networks
        Iterator<String> netItr = networks.iterator();
        while(netItr.hasNext()){
            clusterText += netItr.next()+" ";
        }

        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(clusterText));

        markerOptions.icon(descriptor);

    }

    @Override
    protected boolean shouldRenderAsCluster(com.google.maps.android.clustering.Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }

    public float setVisibilty(long dbmValue){

        dbmValue = Math.abs(dbmValue);

        if(dbmValue < 80)
            return visibilty[0];
        else if(dbmValue >= 80 && dbmValue < 90)
            return visibilty[1];
        else if(dbmValue >= 90 && dbmValue < 100)
            return visibilty[2];
        else if(dbmValue >= 100 && dbmValue < 110)
            return visibilty[3];
        else
            return visibilty[4];

    }
    // Generate color based on Network type
    public int generateColor(int networkType){

//       return Color.rgb(Color.RED, Color.GREEN, Color.BLUE);

        if(networkType == 13) // LTE
            return Color.GREEN;
        else if(networkType == 4 || networkType == 5 || networkType == 6
                || networkType == 15) // 3.5G
            return Color.BLUE;
        else if(networkType == 3 || networkType == 8 || networkType == 9
                || networkType == 10 || networkType == 14 || networkType == 17
                || networkType == 18) // 3G
            return Color.RED;
        else if(networkType == 1 || networkType == 2) // 2.5G
            return Color.YELLOW;
        else if(networkType == 7 || networkType == 11 || networkType == 12
            || networkType == 16) // 2G
            return Color.rgb(Color.RED, Color.GREEN, Color.BLUE);
        return Color.GRAY; // Unkwonn
    }

    // Build Network Map
    public void buildNetworkMap(){

        mapNetwork.put(1, "2.5G");
        mapNetwork.put(2, "2.5G");
        mapNetwork.put(3, "3G");
        mapNetwork.put(4, "3.5G");
        mapNetwork.put(5, "3.5G");
        mapNetwork.put(6, "3.5G");
        mapNetwork.put(7, "2G");
        mapNetwork.put(8, "3G");
        mapNetwork.put(9, "3G");
        mapNetwork.put(10, "3G");
        mapNetwork.put(11, "2G");
        mapNetwork.put(12, "2G");
        mapNetwork.put(13, "4G");
        mapNetwork.put(14, "3G");
        mapNetwork.put(15, "3.5G");
        mapNetwork.put(16, "2G");
        mapNetwork.put(17, "3G");
        mapNetwork.put(18, "3G");

    }
}

// Ins End of ++ spu
