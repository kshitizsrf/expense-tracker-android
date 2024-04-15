package com.example.budget_planner;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class Income_Sheet_Fragment extends Fragment {
    private DBHelper mydb;
    private ArrayList<String> category_name;
    private ArrayList<String> icon;
    private CustomAdapter adapter;
    private int incomeSelectedPosition = -1;
    private String query = "SELECT * FROM Category WHERE type = 'Income'";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_income__sheet_, container, false);
        ListView listView = view.findViewById(R.id.income_lv);

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        mydb = new DBHelper(getActivity());
        category_name = new ArrayList<>();
        icon = new ArrayList<>();

        // Create adapter and set to ListView
        adapter = new CustomAdapter(getActivity(), category_name, icon,true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                incomeSelectedPosition = position;
                adapter.setIncomeSelectedPosition(position);
                String selectedCategory = category_name.get(position);
                if(getActivity() instanceof page_add) {
                    ((page_add) getActivity()).setCategoryName(selectedCategory);
                }
                CustomAdapter.expenseSelectedPosition=-1;
            }
        });

        storeDataInArray();
        return view;
    }
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
            adapter.notifyDataSetChanged(); // Notify adapter about the data change
        }
    }
}