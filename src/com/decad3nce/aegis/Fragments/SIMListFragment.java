package com.decad3nce.aegis.Fragments;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;
import com.decad3nce.aegis.FontAdapter;
import com.decad3nce.aegis.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SIMListFragment extends Fragment implements OnItemLongClickListener {
    public static String PREFERENCES_SIM_LIST_INITIALIZED = "sim_list_initialized";
    private ArrayList<String> identifiers;
    private Menu thisMenu;
    private int listItemRemove = -1;
    private Long listItemAdd = null;
    private int listItem;
    protected Object mActionMode;
    private String backupNumber;
    private ListView mSIMList;
    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        View mainView = inflater.inflate(R.layout.list_fragment_layout, container, false);
;
        backupNumber = preferences
                .getString(AdvancedSettingsFragment.PREFERENCES_BACKUP_PHONE_NUMBER, getActivity().getResources()
                .getString(R.string.config_default_advanced_backup_phone_number));

        if(backupNumber == null || backupNumber.equals("0000000000")) {
            //Send to settings fragment, toast.
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, new AdvancedSettingsFragment()).commit();
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.sim_fragment_request_backup_number), Toast.LENGTH_LONG).show();
        }
        identifiers = getIdentifiers();
        setHasOptionsMenu(true);
        mSIMList = (ListView) mainView.findViewById(R.id.sim_list);
        mSIMList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                //future use
                mSIMList.setItemChecked(pos, false);
            }

        });
        adapter = new FontAdapter(getActivity(), R.layout.list_fragment_item, identifiers);
        mSIMList.setAdapter(adapter);
        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    private boolean setRemove() {
        if(listItemRemove != -1) {
            return true;
        }
        return false;
    }

    private boolean setAdd() {
        if(listItemAdd != null) {
            return true;
        }
        return false;
    }

    private void updateData() {
        if (adapter == null) {
            return;
        }

        if(setRemove()) {
            adapter.remove(identifiers.get(listItemRemove));
            listItemRemove = -1; //background workaround reset
        }

        if(setAdd()) {
            adapter.add(Long.toString(listItemAdd));
            listItemAdd = null;
        }

        saveList();
    }

    private void saveList() {
        Set<String> set = new HashSet<String>();
        set.addAll(identifiers);
        SharedPreferences prefs = getActivity().getSharedPreferences("imsi_list", 0);
        Editor editor = prefs.edit();
        editor.clear();
        editor.putStringSet("identifiers", set);
        editor.commit();
    }

    private Set<String> getList() {
        Set<String> set = new HashSet<String>();
        SharedPreferences prefs = getActivity().getSharedPreferences("imsi_list", 0);
        set = prefs.getStringSet("identifiers", null);
        return set;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSIMList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mSIMList.setOnItemLongClickListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        thisMenu = menu;
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.sim_add_menu, thisMenu);
        super.onPrepareOptionsMenu(thisMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sim_menu_add:
                showAddDialog();
        }
        return true;
    }

    private void showAddDialog() {
        final TelephonyManager mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getActivity().getResources().getString(R.string.sim_fragment_add_dialog));
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        alertDialog.setView(input);
        String addButton = getActivity().getResources().getString(R.string.sim_fragment_add_dialog_add);
        String readButton = getActivity().getResources().getString(R.string.sim_fragment_add_dialog_read);

        alertDialog.setPositiveButton(addButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().toString() != null) {
                    String inputText = input.getText().toString();
                    if(!inputText.isEmpty() || !inputText.trim().equals("")) {
                        listItemAdd = Long.parseLong(inputText);
                        updateData();
                        mSIMList.setItemChecked(listItem, false);
                        return;
                    }
                }
                Toast.makeText(getActivity(), getResources().getString(R.string.sim_fragment_edit_dialog_null), Toast.LENGTH_LONG).show();
                mSIMList.setItemChecked(listItem, false);
            }
        });

        alertDialog.setNegativeButton(readButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //meh
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                input.setText(mTelephonyManager.getSubscriberId());            }
        });
    }

    private void showEditDialog(final int listItem) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getActivity().getResources().getString(R.string.sim_fragment_edit_dialog));
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        alertDialog.setView(input);
        String addButton = getActivity().getResources().getString(R.string.sim_fragment_edit_dialog_save);
        String readButton = getActivity().getResources().getString(R.string.sim_fragment_edit_dialog_cancel);

        input.setText(identifiers.get(listItem));

        alertDialog.setPositiveButton(addButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(input.getText().toString() != null) {
                    String inputText = input.getText().toString();
                    if(!inputText.isEmpty() || !inputText.trim().equals("")) {
                        listItemRemove = listItem;
                        listItemAdd = Long.parseLong(inputText);
                        updateData();
                        mSIMList.setItemChecked(listItem, false);
                        return;
                    }
                }
                mSIMList.setItemChecked(listItem, false);
                Toast.makeText(getActivity(), getResources().getString(R.string.sim_fragment_edit_dialog_null), Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.setNegativeButton(readButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapter, View view, int listPosition, long id) {
        if (mActionMode != null) {
            return false;
        }

        listItem = listPosition;
        mActionMode = getActivity().startActionMode(mActionModeCallback);
        //view.setSelected(true);
        mSIMList.setItemChecked(listPosition, true);
        return true;
    }
    
    private ArrayList<String> getIdentifiers() {
        if(getList() != null) {
            return new ArrayList<String>(getList());
        } else {
            return new ArrayList<String>();
        }
    }
    
    public boolean add(String imei) {
        return identifiers.add(imei);
    }

    public boolean isEmpty() {
        return identifiers.isEmpty();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.sim_contextual_menu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sim_menu_edit:
                    showEditDialog(listItem);
                    mode.finish();
                    return true;
                case R.id.sim_menu_remove:
                    listItemRemove = listItem;
                    updateData();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mSIMList.setItemChecked(listItem, false);
        }
    };

}
