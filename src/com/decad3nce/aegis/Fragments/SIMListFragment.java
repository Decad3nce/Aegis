package com.decad3nce.aegis.Fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.ActionMode;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuInflater;
import android.widget.*;
import android.app.ListFragment;
import android.widget.AdapterView.OnItemLongClickListener;

import com.decad3nce.aegis.R;

public class SIMListFragment extends ListFragment implements OnItemLongClickListener {
    private ArrayList<String> identifiers;
    private Menu thisMenu;
    private int listItemRemove = -1;
    private int listItem;
    protected Object mActionMode;
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        identifiers = new ArrayList<String>();
        
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1,
                getIdentifiers());
        setHasOptionsMenu(true);
        setListAdapter(adapter);
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

    private void updateData() {
        if (adapter == null) {
            return;
        }

        if(setRemove()) {
            adapter.remove(identifiers.get(listItemRemove));
            listItemRemove = -1;
        }

        saveList();
    }

    private void saveList() {
        Set<String> set = new HashSet<String>();
        set.addAll(identifiers);
        SharedPreferences prefs = getActivity().getSharedPreferences("sim_list", 0);
        Editor editor = prefs.edit();
        editor.clear();
        editor.putStringSet("identifiers", set);
        editor.commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        thisMenu = menu;
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.sim_add_menu, thisMenu);
        super.onPrepareOptionsMenu(thisMenu);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapter, View view, int listPosition, long id) {
        if (mActionMode != null) {
            return false;
        }

        listItem = listPosition;
        mActionMode = getActivity().startActionMode(mActionModeCallback);
        view.setSelected(true);
        //view.setBackground(android.R.attr.activatedBackgroundIndicator);
        getListView().setItemChecked(listPosition, true);
        return true;
    }
    
    private List<String> getIdentifiers() {
        Set<String> set = new HashSet<String>();
        SharedPreferences prefs = getActivity().getSharedPreferences("sim_list", 0);
        set = prefs.getStringSet("identifiers", null);

        if(set != null) {
            identifiers.addAll(set);
        } else {
            identifiers.add("ADD AN IDENTIFIER");
        }
        return identifiers;
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
                    //TODO: Add dialogfragment to handle edit
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
        }
    };

}
