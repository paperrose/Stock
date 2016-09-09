package com.artfonapps.clientrestore.messages.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by Altirez on 07.09.2016.
 */
public class ListPointAlertDialogFragment extends DialogFragment {
    public static ListPointAlertDialogFragment newInstance(String title) {
        ListPointAlertDialogFragment frag = new ListPointAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton("Да",
                        (dialog, whichButton) -> {
                            Log.e("dialogs", "YEAHHHH");
                        }
                )
                .setNegativeButton("Нет",
                        (dialog, whichButton) -> {
                            Log.e("dialogs", "NOOOOO");
                        }
                )
                .create();
    }
}
