package edu.buffalo.cse.ubwins.cellmon;

// Ins Begin of ++spu
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by sourav on 6/17/17.
 */
//https://guides.codepath.com/android/Using-DialogFragment
public class NetworkSelect extends DialogFragment implements View.OnClickListener,
        CheckBox.OnCheckedChangeListener{

    CheckBox checkall, check2G, check2_5G, check3G, check3_5G, check4G ;
    Button butOK= null;
    String title ="";

    public NetworkSelect()
    {
        // Empty Constructor required
    }


    public static NetworkSelect newInstance(String title) {
        NetworkSelect netSelect = new NetworkSelect();
        Bundle args = new Bundle();
        args.putString("title", title);
        netSelect.setArguments(args);
        return netSelect;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pick_network, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        checkall = (CheckBox) view.findViewById(R.id.all);
        checkall.setChecked(true);
        checkall.setOnCheckedChangeListener(this);
        check2G = (CheckBox) view.findViewById(R.id._2G);
        check2G.setChecked(true);
        check2G.setOnCheckedChangeListener(this);
        check2_5G = (CheckBox) view.findViewById(R.id._2_5G);
        check2_5G.setChecked(true);
        check2_5G.setOnCheckedChangeListener(this);
        check3G = (CheckBox) view.findViewById(R.id._3G);
        check3G.setChecked(true);
        check3G.setOnCheckedChangeListener(this);
        check3_5G = (CheckBox) view.findViewById(R.id._3_5G);
        check3_5G.setChecked(true);
        check3_5G.setOnCheckedChangeListener(this);
        check4G =(CheckBox) view.findViewById(R.id._4G);
        check4G.setChecked(true);
        check4G.setOnCheckedChangeListener(this);

        butOK =(Button) view.findViewById(R.id.ok);
        butOK.setOnClickListener(this);

        // Fetch arguments from bundle and set title
        title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);

        onCheckedChanged(checkall, true);
        // Hide soft keyboard automatically and request focus to field
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:

                NetworkDialogListener listener = (NetworkDialogListener) getTargetFragment();
                listener.onFinishNetworkDialog(checkall.isChecked(), check2G.isChecked(),
                    check2_5G.isChecked(), check3G.isChecked(), check3_5G.isChecked(),
                    check4G.isChecked());
                dismiss();
                break;
//
//            case R.id._2G:
//                check2G.setChecked(!check2G.isChecked());
//            case R.id._2_5G:
//                check2_5G.setChecked(!check2_5G.isChecked());
//            case R.id._3G:
//                check3G.setChecked(!check3G.isChecked());
//            case R.id._3_5G:
//                check3_5G.setChecked(!check3_5G.isChecked());
//            case R.id._4G:
//                check4G.setChecked(!check4G.isChecked());
           default:
               break;
                 // Do Nothing
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView,
                               boolean isChecked) {

        if(buttonView == checkall) {
            checkall.setChecked(isChecked);
            if(isChecked == true){
                check2G.setEnabled(false);
                check2_5G.setEnabled(false);
                check3G.setEnabled(false);
                check3_5G.setEnabled(false);
                check4G.setEnabled(false);
            }
            else{
                check2G.setEnabled(true);
                check2_5G.setEnabled(true);
                check3G.setEnabled(true);
                check3_5G.setEnabled(true);
                check4G.setEnabled(true);
            }

        }
        else if(buttonView == check2G)
            check2G.setChecked(isChecked);
        else if(buttonView == check2_5G)
            check2_5G.setChecked(isChecked);
        else if(buttonView == check3G)
            check3G.setChecked(isChecked);
        else if(buttonView == check3_5G)
            check3_5G.setChecked(isChecked);
        else if(buttonView == check4G)
            check4G.setChecked(isChecked);

    }



}

// Ins End of ++spu
