package edu.buffalo.cse.ubwins.cellmon;

/**
 * Created by sourav on 6/17/17.
 */

  //Defines the listener interface with a method passing back data result.
    public interface NetworkDialogListener {
        void onFinishNetworkDialog(boolean all, boolean _2G, boolean _2_5G, boolean _3G,
                                   boolean _3_G, boolean _4G);
    }

