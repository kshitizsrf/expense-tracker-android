package com.example.budget_planner;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.ArrayList;

public class TransactionAdapter extends ArrayAdapter<Double> {
    private Context context;
    private ArrayList<Integer> id;
    private ArrayList<Double> amount;
    private ArrayList<String> time;
    private ArrayList<String> category_id;
    private ArrayList<String> note;
    private ArrayList<String> date;

    public TransactionAdapter(Context context,ArrayList<Integer> id, ArrayList<Double> amount, ArrayList<String> time, ArrayList<String> category_id, ArrayList<String> note, ArrayList<String> date) {
        super(context, R.layout.transaction_listview_design, amount);
        this.context = context;
        this.id=id;
        this.amount = amount;
        this.time = time;
        this.category_id = category_id;
        this.note = note;
        this.date = date;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.transaction_listview_design, parent, false);
            holder = new ViewHolder();
            holder.dateTextView = row.findViewById(R.id.date);
            holder.amountTextView = row.findViewById(R.id.amount);
            holder.timeTextView = row.findViewById(R.id.time);
            holder.categoryTextView = row.findViewById(R.id.name);
            holder.noteTextView = row.findViewById(R.id.note_data);
            holder.icon = row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        String amount_data = String.valueOf(amount.get(position));

        holder.timeTextView.setText(time.get(position));
        holder.noteTextView.setText(note.get(position));

        String[] categoryInfo = getCategoryInfo(category_id.get(position));
        if (categoryInfo != null) {
            holder.categoryTextView.setText(categoryInfo[0]); // Category name
            String type = categoryInfo[2]; // Category type
            if (type != null) {
                if (type.equals("Expense")) {
                    holder.amountTextView.setText("-" + amount_data);
                    holder.amountTextView.setTextColor(Color.RED);
                    holder.icon.setImageResource(R.drawable.arrow_circle_down_svgrepo_com);
                } else if (type.equals("Income")) {
                    holder.amountTextView.setText("+" + amount_data);
                    holder.amountTextView.setTextColor(Color.GREEN);
                    holder.icon.setImageResource(R.drawable.arrow_circle_up_svgrepo_com);
                }
            }
        }

        // Show or hide date TextView based on position
        if (position == 0 || !date.get(position).equals(date.get(position - 1))) {
            holder.dateTextView.setVisibility(View.VISIBLE);
            holder.dateTextView.setText(date.get(position));
        } else {
            holder.dateTextView.setVisibility(View.GONE);
        }



        return row;
    }

    private String[] getCategoryInfo(String categoryId) {
        DBHelper dbHelper = new DBHelper(context);
        String[] categoryInfo = new String[3]; // 0: category name, 1: category type
        String query = "SELECT category_name,category_icon, type FROM Category WHERE category_id = '" + categoryId + "'";
        Cursor cursor = dbHelper.readAllData(query);
        if (cursor.moveToFirst()) {
            int column_cat_name = cursor.getColumnIndex("category_name");
            int column_cat_icon = cursor.getColumnIndex("category_icon");
            int column_type = cursor.getColumnIndex("type");
            categoryInfo[0] = cursor.getString(column_cat_name);
            categoryInfo[1] = cursor.getString(column_cat_icon);
            categoryInfo[2] = cursor.getString(column_type);
        }
        cursor.close();
        dbHelper.close();
        return categoryInfo;
    }
    public void data_to_edit(int position) {
        Intent intent = new Intent(context, Detail_Transaction.class);
        intent.putExtra("id", id.get(position));
        intent.putExtra("amount", amount.get(position));
        String categoryName = getCategoryInfo(category_id.get(position))[0]; // Get category name
        intent.putExtra("category_name", categoryName);
        String categoryIcon = getCategoryInfo(category_id.get(position))[1];
        intent.putExtra("category_icon", categoryIcon);
        String type = getCategoryInfo(category_id.get(position))[2];
        intent.putExtra("type", type);
        intent.putExtra("date", date.get(position));
        intent.putExtra("time", time.get(position));
        intent.putExtra("note", note.get(position)); // Pass note
        // Add more data if needed
        context.startActivity(intent);
    }


    static class ViewHolder {

        TextView dateTextView;
        TextView amountTextView;
        TextView timeTextView;
        TextView categoryTextView;
        TextView noteTextView;
        ImageView icon;
    }
}
