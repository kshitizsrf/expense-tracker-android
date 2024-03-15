package com.example.budget_planner;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.opengl.GLDebugHelper;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class Category_Settings extends AppCompatActivity {
    private String mode;
    private String categoryToUpdate;
    private ImageView selectedImageView;
    private static final int ICON_SIZE_DP = 20;
    private static final int COLUMNS_IN_GRID = 5;
    private String selectedImageName; // Variable to hold the name of the selected image
    private String icontoupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_setting);

        // Get the GridLayout
        FloatingActionButton back=findViewById(R.id.Back);
        GridLayout gridLayout = findViewById(R.id.iconContainer);
        ImageView selectedIconImageView = findViewById(R.id.selected_icon);
        EditText cat_name=findViewById(R.id.category_name);

        mode = getIntent().getStringExtra("mode");
        categoryToUpdate = getIntent().getStringExtra("category_name");
        icontoupdate=getIntent().getStringExtra("category_icon");

        // Check if it's in edit mode
        if ("edit".equals(mode)) {
            // Populate EditText with category name for editing
            cat_name.setText(categoryToUpdate);
            int resourceId = getResources().getIdentifier(icontoupdate, "drawable", getPackageName());
            selectedIconImageView.setImageResource(resourceId);

            selectedImageName = icontoupdate;
        }

        FloatingActionButton done_cat=findViewById(R.id.done_cat);
        done_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category_name=cat_name.getText().toString().trim();
                if(category_name.isEmpty()){
                    Toast.makeText(Category_Settings.this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                }else{
                    Category_Fragment CF=new Category_Fragment();
                    DBHelper mydb=new DBHelper(getApplicationContext());
                    if ("edit".equals(mode)) {
                        // Update existing category
                        mydb.updateCategory(categoryToUpdate, category_name, selectedImageName.toString(),icontoupdate);
                        Toast.makeText(getApplicationContext(), "Category updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add new category
                        mydb.addCategory(getApplicationContext(), category_name, selectedImageName.toString(), CF.category);
                        Toast.makeText(getApplicationContext(), "Category added successfully", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Create an array list to store the drawable resource IDs
        ArrayList<Integer> iconDrawables = new ArrayList<>();

        // Fetch drawable resources dynamically and add them to the list
        int i = 1; // start index of icons
        while (true) {
            int resId = getResources().getIdentifier("icon_" + i, "drawable", getPackageName());
            if (resId == 0) {
                // If the resource ID is 0, it means the resource does not exist
                break;
            }
            iconDrawables.add(resId);
            i++;
        }

        // Calculate the number of rows needed
        int numRows = (int) Math.ceil((double) iconDrawables.size() / COLUMNS_IN_GRID);

        // Set the number of columns and rows in the GridLayout
        gridLayout.setColumnCount(COLUMNS_IN_GRID);
        gridLayout.setRowCount(numRows);

        // Now, you can use the iconDrawables list in your loop to create ImageViews
        for (int j = 0; j < iconDrawables.size(); j++) {
            int drawableId = iconDrawables.get(j);
            String iconName = "icon_" + (j + 1); // Generate icon name
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(drawableId);

            // Set the icon size smaller
            imageView.setLayoutParams(new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            ));
            imageView.getLayoutParams().width = dpToPx(ICON_SIZE_DP);
            imageView.getLayoutParams().height = dpToPx(ICON_SIZE_DP);

            // Set the circular background
            imageView.setBackgroundResource(R.drawable.circle_drawable);

            // Set scaleType to control how the icon is scaled
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            // Set layout parameters (optional)
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.setMargins(dpToPx(8), 0, dpToPx(8), 0); // Adjust margins as needed
            imageView.setLayoutParams(layoutParams);

            // Add ImageView to the GridLayout
            gridLayout.addView(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedImageView != null) {
                        selectedImageView.setSelected(false);
                        selectedImageView.setBackgroundResource(R.drawable.circle_drawable); // Set to default background
                        selectedImageView.setColorFilter(null);
                    }

                    // Set the current ImageView as selected
                    imageView.setSelected(true);
                    imageView.setBackgroundResource(R.drawable.circle_selected);
                    // Change the icon color to white
                    imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                    int randomColor = getRandomColor();
                    imageView.getBackground().setColorFilter(randomColor, PorterDuff.Mode.SRC_IN);
                    // Update the reference to the currently selected ImageView
                    selectedImageView = imageView;

                    // Store the name of the selected image
                    selectedImageName = iconName;

                    selectedIconImageView.setImageResource(drawableId);
                    selectedIconImageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                    ViewGroup.LayoutParams layoutParams = selectedIconImageView.getLayoutParams();
                    layoutParams.width = dpToPx(68); // Set the desired width in pixels
                    layoutParams.height = dpToPx(68); // Set the desired height in pixels
                    selectedIconImageView.setLayoutParams(layoutParams);
                    selectedIconImageView.getBackground().setColorFilter(randomColor, PorterDuff.Mode.SRC_IN);
                }
            });

            // Set icon_1 as default selected
            if (!"edit".equals(mode) && j == 0) {
                imageView.setSelected(true);
                imageView.setBackgroundResource(R.drawable.circle_selected);
                imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                int randomColor = getRandomColor();
                imageView.getBackground().setColorFilter(randomColor, PorterDuff.Mode.SRC_IN);
                selectedImageView = imageView;

                // Store the name of the initially selected image
                selectedImageName = iconName;
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getRandomColor() {
        Random random = new Random();
        return Color.argb(100, random.nextInt(101), random.nextInt(101), random.nextInt(101));
    }
}
