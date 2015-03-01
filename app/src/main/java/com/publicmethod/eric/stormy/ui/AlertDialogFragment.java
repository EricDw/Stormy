package com.publicmethod.eric.stormy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Owner on 2/19/2015.
 */
public class AlertDialogFragment extends DialogFragment {
    public static final String TITLE_KEY = "title";
    public static final String MESSAGE_KEY = "message";
    public static final String BUTTON_KEY = "positiveButton";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Context context = getActivity();
        
        String title = getArguments().getString(TITLE_KEY);
        String message = getArguments().getString(MESSAGE_KEY);
        String positiveButton = getArguments().getString(BUTTON_KEY);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, null);
                AlertDialog alertDialog = builder.create();
        return alertDialog;
    }
}
