package edu.buffalo.cse.ubwins.cellmon;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.todddavies.components.progressbar.ProgressWheel;

/**
 * Created by sourav on 8/6/17.
 */

// Ins Begin of ++ spu
public class ProgressBar extends DialogFragment{
    ProgressWheel pw = null;
    public static ProgressBar newInstance(String title) {
        ProgressBar progressBar = new ProgressBar();
        Bundle args = new Bundle();
        args.putString("title", title);
        progressBar.setArguments(args);
        return progressBar;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.progress_wheel, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pw = (ProgressWheel) view.findViewById(R.id.pw_spinner);
        pw.setVisibility(View.VISIBLE);
        pw.startSpinning();
        pw.setSpinSpeed(4);
        pw.setText("Loading...");

    }

}

// Ins End of ++spu

