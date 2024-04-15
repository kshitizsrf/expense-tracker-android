package com.example.budget_planner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class Transaction_Fragment extends Fragment {

    private DBHelper mydb;
    TextView empty_txt;
    private ArrayList<Double> amount;
    private ArrayList<Integer> id;
    private ArrayList<String> time;
    private ArrayList<String> Category_id;
    private ArrayList<String> note;
    private ArrayList<String> date;
    private TransactionAdapter adapter;
    //private String query = "SELECT *, strftime('%Y-%m-%d', date) AS sortable_date FROM Transactions ORDER BY sortable_date DESC";
    private String query = "SELECT *, "
            + "substr(date, -4) || '-' || "
            + "CASE substr(date, 1, 3) "
            + "    WHEN 'Jan' THEN '01' "
            + "    WHEN 'Feb' THEN '02' "
            + "    WHEN 'Mar' THEN '03' "
            + "    WHEN 'Apr' THEN '04' "
            + "    WHEN 'May' THEN '05' "
            + "    WHEN 'Jun' THEN '06' "
            + "    WHEN 'Jul' THEN '07' "
            + "    WHEN 'Aug' THEN '08' "
            + "    WHEN 'Sep' THEN '09' "
            + "    WHEN 'Oct' THEN '10' "
            + "    WHEN 'Nov' THEN '11' "
            + "    WHEN 'Dec' THEN '12' "
            + "END || '-' || "
            + "substr(date, 5, 2) || ' ' || "
            + "substr(date, -8) AS sortable_date "
            + "FROM Transactions ORDER BY sortable_date DESC";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_, container, false);
        ListView listView = view.findViewById(R.id.listview);
        empty_txt=view.findViewById(R.id.empty);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.data_to_edit(position);
            }
        });

        mydb = new DBHelper(getActivity());
        id=new ArrayList<>();
        amount = new ArrayList<>();
        time = new ArrayList<>();
        Category_id = new ArrayList<>();
        note = new ArrayList<>();
        date = new ArrayList<>();
        adapter = new TransactionAdapter(getActivity(),id, amount, time, Category_id, note, date);
        listView.setAdapter(adapter);
        storeDataInArray();
        return view;
    }

    public void onResume() {
        super.onResume();
        // Fetch data from database
        storeDataInArray();
    }

    public void storeDataInArray() {
        id.clear();
        amount.clear();
        time.clear();
        Category_id.clear();
        note.clear();
        date.clear();
        Cursor cursor = mydb.readAllData(query);
        if (cursor.getCount() == 0) {
            empty_txt.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "No Transaction history found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                int column_id = cursor.getColumnIndex("transaction_id");
                int column_amount = cursor.getColumnIndex("amount");
                int column_time = cursor.getColumnIndex("time");
                int column_cat_id = cursor.getColumnIndex("category_id");
                int column_note = cursor.getColumnIndex("note");
                int column_date = cursor.getColumnIndex("date");
                id.add(cursor.getInt(column_id));
                amount.add(cursor.getDouble(column_amount));
                time.add(cursor.getString(column_time));
                Category_id.add(cursor.getString(column_cat_id));
                note.add(cursor.getString(column_note));
                date.add(cursor.getString(column_date));
            }

            // Notify adapter about the data change
            adapter.notifyDataSetChanged();
        }
        cursor.close();
    }
}
