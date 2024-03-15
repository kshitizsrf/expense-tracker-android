package com.example.budget_planner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> category_name;
    private ArrayList<String> icon;
    public static int expenseSelectedPosition = 0;
    public static int incomeSelectedPosition = -1;
    private boolean buttonvisible;

    public CustomAdapter(Context context, ArrayList<String> category_name, ArrayList<String> icon,boolean buttonvisible) {
        super(context, R.layout.category_listview_design, R.id.name, category_name);
        this.context = context;
        this.category_name = category_name;
        this.icon = icon;
        this.buttonvisible=buttonvisible;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.category_listview_design, parent, false);
        TextView myCategory = row.findViewById(R.id.name);
        ImageView myIcon = row.findViewById(R.id.image);
        ImageView tickMark=row.findViewById(R.id.tick_mark);
        FloatingActionButton edit_fab=row.findViewById(R.id.edit);
        FloatingActionButton delete_fab=row.findViewById(R.id.delete_cat);
        if (buttonvisible) {
            edit_fab.setVisibility(View.GONE);
            delete_fab.setVisibility(View.GONE);
            if (expenseSelectedPosition == position || incomeSelectedPosition == position ) {
                tickMark.setVisibility(View.VISIBLE);
            } else {
                tickMark.setVisibility(View.GONE);
            }
        }
        delete_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Category!")
                        .setMessage("Are you sure you want to delete this category and all related transactions? This action cannot be undone.")
                        .setCancelable(true).setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBHelper dbHelper = new DBHelper(getContext());
                                int rowsDeleted = dbHelper.deleteCategory(category_name.get(position));
                                if (rowsDeleted > 0) {
                                    Toast.makeText(getContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show();
                                    category_name.remove(position);
                                    icon.remove(position);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            }
        });
        edit_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Category_Settings.class);
                intent.putExtra("mode", "edit"); // Set mode to "edit"
                intent.putExtra("category_name", category_name.get(position));
                intent.putExtra("category_icon", icon.get(position));
                context.startActivity(intent);
            }
        });

        myCategory.setText(category_name.get(position));
        // Load icon if available
        if (!icon.get(position).isEmpty()) {
            int resourceId = context.getResources().getIdentifier(icon.get(position), "drawable", context.getPackageName());
            myIcon.setImageResource(resourceId);
        } else {
            myIcon.setVisibility(View.GONE); // Hide the ImageView if no icon provided
        }

        return row;
    }


    public void setExpenseSelectedPosition(int exp_position) {
        expenseSelectedPosition = exp_position;
        notifyDataSetChanged(); // Notify adapter about the data change
    }

    public void setIncomeSelectedPosition(int in_position) {
        incomeSelectedPosition = in_position;
        notifyDataSetChanged(); // Notify adapter about the data change
    }


}
