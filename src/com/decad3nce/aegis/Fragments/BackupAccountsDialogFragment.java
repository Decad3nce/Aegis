package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class BackupAccountsDialogFragment extends DialogFragment{
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    NoticeDialogListener mListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.aegis_backup_service_options_menu)
                .setPositiveButton(R.string.aegis_backup_service_choose_account,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            mListener.onDialogPositiveClick(BackupAccountsDialogFragment.this);
                            }
                        })
                .setNegativeButton(R.string.aegis_backup_service_logout_from_account,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            mListener.onDialogNegativeClick(BackupAccountsDialogFragment.this);
                            }
                        });
        return builder.create();
    }
}
