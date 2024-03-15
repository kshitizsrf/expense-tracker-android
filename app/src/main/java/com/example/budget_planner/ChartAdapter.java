package com.example.budget_planner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChartAdapter extends ArrayAdapter<String> {
    private List<String> icon;
    private List<String> categories;
    private List<String> amounts;

    public ChartAdapter(Context context,List<String> icon, List<String> categories, List<String> amounts) {
        super(context, 0, categories);
        this.icon=icon;
        this.categories = categories;
        this.amounts = amounts;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data items for this position
        String icons = icon.get(position);
        String category = categories.get(position);
        String amount = amounts.get(position);

        // View lookup cache stored in tag
        ViewHolder viewHolder;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chart_listview_design, parent, false);

            // Find the views within custom layout
            viewHolder.nameTextView = convertView.findViewById(R.id.name);
            viewHolder.amountTextView = convertView.findViewById(R.id.amount);
            viewHolder.image_icon=convertView.findViewById(R.id.image);

            // Set the view holder as tag for view recycling
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template views using the data object
        viewHolder.nameTextView.setText(category);
        viewHolder.amountTextView.setText(amount);
        int resourceId = getContext().getResources().getIdentifier(icons, "drawable", getContext().getPackageName());
        viewHolder.image_icon.setImageResource(resourceId);

        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache stored in tag
    private static class ViewHolder {
        ImageView image_icon;
        TextView nameTextView;
        TextView amountTextView;
    }
}
