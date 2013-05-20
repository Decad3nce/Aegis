package com.decad3nce.aegis.Fragments;

import com.decad3nce.aegis.BackupDropboxAccountsActivity;
import com.decad3nce.aegis.BackupGoogleAccountsActivity;
import com.decad3nce.aegis.R;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChooseBackupProgramDialogFragment extends DialogFragment{
    private Button mFirstBackup;
    private Button mSecondBackup;
    private Button mCancelButton;
    
    public interface ChooseBackupDialogListener {
        // Future use
    }

    
    public ChooseBackupProgramDialogFragment() {
        // Future use
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup_app_selector, container);
        mFirstBackup = (Button) view.findViewById(R.id.first_backup_app);
        mSecondBackup = (Button) view.findViewById(R.id.second_backup_app);
        mCancelButton = (Button) view.findViewById(R.id.backup_cancel_button);
        
        mFirstBackup.setOnClickListener(backupButtonListener);
        mSecondBackup.setOnClickListener(backupButtonListener);
        mCancelButton.setOnClickListener(backupButtonListener);
        getDialog().setTitle(R.string.aegis_choose_backup_application);
        return view;
    }
    
    private OnClickListener backupButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.first_backup_app:
                Intent firstIntent = new Intent(getActivity(), BackupGoogleAccountsActivity.class);
                startActivity(firstIntent);
                dismiss();
                break;
            case R.id.second_backup_app:
                Intent secondIntent = new Intent(getActivity(), BackupDropboxAccountsActivity.class);
                startActivity(secondIntent);
                dismiss();
                break;
            case R.id.backup_cancel_button:  
                dismiss();
                break;
            }

        }
    };
}
