package com.decad3nce.aegis.Fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.decad3nce.aegis.BackupDropboxAccountsActivity;
import com.decad3nce.aegis.BackupGoogleAccountsActivity;
import com.decad3nce.aegis.R;

public class ChooseBackupProgramDialogFragment extends DialogFragment{
    private ListView mButtonList;
    private String[] values;

    public interface ChooseBackupDialogListener {
        // Future use
    }


    public ChooseBackupProgramDialogFragment() {
        //Future use
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup_app_selector, container);
        mButtonList = (ListView) view.findViewById(R.id.backup_list);
        values = new String[] {getResourceString(R.string.aegis_choose_backup_application_1), getResourceString(R.string.aegis_choose_backup_application_2), getResourceString(R.string.advanced_dialog_cancel)};
        mButtonList.setAdapter(new backupImageAdapter(getActivity(), values));
        mButtonList.setOnItemClickListener(backupButtonListener);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
    
    private AdapterView.OnItemClickListener backupButtonListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            switch (position) {
            case 0:
                Intent firstIntent = new Intent(getActivity(), BackupGoogleAccountsActivity.class);
                startActivity(firstIntent);
                dismiss();
                break;
            case 1:
                Intent secondIntent = new Intent(getActivity(), BackupDropboxAccountsActivity.class);
                startActivity(secondIntent);
                dismiss();
                break;
            case 2:
                dismiss();
                break;
            }

        }
    };

    private String getResourceString(int id) {
        String resourceString = getActivity().getResources().getString(id);
        return resourceString;
    }

    private class backupImageAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;
        Typeface tf;
        private final Integer[] images = {R.drawable.ic_google_drive, R.drawable.ic_dropbox, android.R.drawable.ic_menu_close_clear_cancel};


        public backupImageAdapter(Context context, String[] values) {
            super(context, R.layout.fragment_backup_list_item_layout, values);
            this.context = context;
            this.values = values;
            this.tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.fragment_backup_list_item_layout, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.fragment_backup_text_view);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.fragment_backup_image_view);

            textView.setText(values[position]);
            textView.setTypeface(tf);
            imageView.setImageResource(images[position]);

            return rowView;
        }
    }
}
