package com.example.budget_planner;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class Income_Category_Fragment extends Fragment {

    private DBHelper mydb;
    private ArrayList<String> category_name;
    private ArrayList<String> icon;
    private CustomAdapter adapter;
    private String query = "SELECT * FROM Category WHERE type = 'Income'";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income__category_, container, false);
        ListView listView = view.findViewById(R.id.income_lv);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (getParentFragment() instanceof Category_Fragment) {
                    // Notify the parent fragment about the scroll event
                    ((Category_Fragment) getParentFragment()).onListScroll(firstVisibleItem > 0);
                }
            }
        });
        mydb = new DBHelper(getActivity());
        category_name = new ArrayList<>();
        icon = new ArrayList<>();

        // Create adapter and set to ListView
        adapter = new CustomAdapter(getActivity(), category_name, icon,false);
        listView.setAdapter(adapter);

        storeDataInArray();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch data from database
        storeDataInArray();
    }

    private void storeDataInArray() {
        category_name.clear();
        icon.clear();
        Cursor cursor = mydb.readAllData(query);
        if (cursor.getCount() == 0) {
            Toast.makeText(getActivity(), "No income categories found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                int column_cat_name = cursor.getColumnIndex("category_name");
                int column_icon = cursor.getColumnIndex("category_icon");
                category_name.add(cursor.getString(column_cat_name));
                icon.add(cursor.getString(column_icon));
            }
            adapter.notifyDataSetChanged();
        }
    }
}
