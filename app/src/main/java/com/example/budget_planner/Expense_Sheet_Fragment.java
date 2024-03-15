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


public class Expense_Sheet_Fragment extends Fragment {
    private DBHelper mydb;
    private ArrayList<String> category_name;
    private ArrayList<String> icon;
    public static int selectedPosition = 0;
    private CustomAdapter adapter;
    


    private String query = "SELECT * FROM Category WHERE type = 'Expense'";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_expense__sheet_, container, false);
        ListView listView = view.findViewById(R.id.exp_lv);
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
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                selectedPosition=position;
                adapter.setExpenseSelectedPosition(position);

                String selectedCategory = category_name.get(position);
                if(getActivity() instanceof page_add) {
                    ((page_add) getActivity()).setCategoryName(selectedCategory);
                }
                CustomAdapter.incomeSelectedPosition=-1;
            }
        });

        storeDataInArray();
        if (category_name.size() > 0 && getActivity() instanceof page_add) {
            ((page_add) getActivity()).setCategoryName(category_name.get(selectedPosition));
        }

        return view;
    }
    public void onResume() {
        super.onResume();
        // Fetch data from database
        storeDataInArray();

    }

    public void storeDataInArray() {
        category_name.clear();
        icon.clear();
        Cursor cursor = mydb.realAllData(query);
        if (cursor.getCount() == 0) {
            Toast.makeText(getActivity(), "No expense categories found", Toast.LENGTH_SHORT).show();
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