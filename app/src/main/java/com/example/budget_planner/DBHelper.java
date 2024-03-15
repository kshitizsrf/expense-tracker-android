package com.example.budget_planner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Category (" +
                "category_id INTEGER PRIMARY KEY," +
                "category_name TEXT NOT NULL," +
                "category_icon TEXT," +
                "type TEXT NOT NULL)");
        insertDefaultCategories(db);

        db.execSQL("CREATE TABLE Transactions (" +
                "transaction_id INTEGER PRIMARY KEY," +
                "amount REAL NOT NULL," +
                "date DATE NOT NULL," +
                "time TIME NOT NULL," +
                "category_id INTEGER," +
                "note TEXT," +
                "FOREIGN KEY (category_id) REFERENCES Category(category_id))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Category");
        db.execSQL("DROP TABLE IF EXISTS Transactions");
        onCreate(db);
    }

    public long addCategory(Context context,String categoryName, String categoryIcon, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category_name", categoryName);
        values.put("category_icon", categoryIcon);
        values.put("type", type);
        long newRowId = db.insert("Category", null, values);
        if(newRowId== -1){
            Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"Added Successfully",Toast.LENGTH_SHORT).show();
        }
        db.close();
        return newRowId;
    }


    public int updateCategory(String oldCategoryName, String newCategoryName, String newCategoryIcon, String oldCategoryIcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category_name", newCategoryName);
        values.put("category_icon", newCategoryIcon);

        String whereClause = "category_name=?";
        String[] whereArgs = {oldCategoryName};

        try {
            // Perform the update operation
            int numRowsUpdated = db.update("Category", values, whereClause, whereArgs);

            // Close the database connection
            db.close();

            // Return the number of rows updated
            return numRowsUpdated;
        } catch (SQLException e) {
            // Log any exceptions
            Log.e("DBHelper", "Error updating category: " + e.getMessage());

            // Close the database connection
            db.close();

            // Return 0 indicating failure
            return 0;
        }
    }

    public int deleteCategory(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "category_name=?";
        String[] whereArgs = {categoryName};

        try {
            // Perform the delete operation
            int numRowsDeleted = db.delete("Category", whereClause, whereArgs);

            // Close the database connection
            db.close();

            // Return the number of rows deleted
            return numRowsDeleted;
        } catch (SQLException e) {
            // Log any exceptions
            Log.e("DBHelper", "Error deleting category: " + e.getMessage());

            // Close the database connection
            db.close();

            // Return 0 indicating failure
            return 0;
        }
    }


    public long addTransaction(double amount, String date, String time, String categoryName, String note, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("date", date);
        values.put("time", time);

        // Query the Category table to get the category_id for the provided categoryName
        String[] columns = {"category_id"};
        String selection = "category_name=?";
        String[] selectionArgs = {categoryName};
        Cursor cursor = db.query("Category", columns, selection, selectionArgs, null, null, null);
        int categoryId = -1; // Default value if category is not found
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("category_id");
            if (columnIndex != -1) {
                categoryId = cursor.getInt(columnIndex);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        values.put("category_id", categoryId);
        values.put("note", note);
        long newRowId = db.insert("Transactions", null, values); // Corrected table name
        db.close();

        if (newRowId != -1) {
            Toast.makeText(context, "Transaction Added Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to Add Transaction", Toast.LENGTH_SHORT).show();
        }

        return newRowId;
    }

    public int deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "transaction_id=?";
        String[] whereArgs = {String.valueOf(transactionId)};

        try {
            // Perform the delete operation
            int numRowsDeleted = db.delete("Transactions", whereClause, whereArgs);

            // Close the database connection
            db.close();

            // Return the number of rows deleted
            return numRowsDeleted;
        } catch (SQLException e) {
            // Log any exceptions
            Log.e("DBHelper", "Error deleting transaction: " + e.getMessage());

            // Close the database connection
            db.close();

            // Return 0 indicating failure
            return 0;
        }
    }



    Cursor realAllData(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }



    private void insertDefaultCategories(SQLiteDatabase db) {
        // Define an array of default category names and icons for expenses
        String[] defaultExpenseCategoryNames = {"Food", "Clothing", "Fruits","Shopping","Transportation","Home","Travel","Wine","Bills","Gift","Education","Vegetables","Sport","Health","Entertainment","Car","Insurance","Book","Pet"};
        String[] defaultExpenseCategoryIcons = {"icon_55", "icon_114", "icon_138","icon_5","icon_24","icon_80","icon_2","icon_137","icon_119","icon_70","icon_111","icon_139","icon_11","icon_90","icon_67","icon_33","icon_112","icon_87","icon_94"};

        // Insert default expense categories
        for (int i = 0; i < defaultExpenseCategoryNames.length; i++) {
            ContentValues values = new ContentValues();
            values.put("category_name", defaultExpenseCategoryNames[i]);
            values.put("category_icon", defaultExpenseCategoryIcons[i]);
            values.put("type", "Expense");
            db.insert("Category", null, values);
        }

        // Define an array of default category names and icons for income
        String[] defaultIncomeCategoryNames = {"Rental","Salary","Sale", "Awards","Investment","Other"};
        String[] defaultIncomeCategoryIcons = {"icon_25", "icon_132", "icon_124","icon_106","icon_37","icon_97"};

        // Insert default income categories
        for (int i = 0; i < defaultIncomeCategoryNames.length; i++) {
            ContentValues values = new ContentValues();
            values.put("category_name", defaultIncomeCategoryNames[i]);
            values.put("category_icon", defaultIncomeCategoryIcons[i]);
            values.put("type", "Income");
            db.insert("Category", null, values);
        }
    }

    public String getCategoryType(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"type"};
        String selection = "category_id=?";
        String[] selectionArgs = {String.valueOf(categoryId)};
        Cursor cursor = db.query("Category", columns, selection, selectionArgs, null, null, null);
        String type = "";
        if (cursor != null && cursor.moveToFirst()) {
            int column_type=cursor.getColumnIndex("type");
            type = cursor.getString(column_type);
        }
        if (cursor != null) {
            cursor.close();
        }
        return type;
    }


}
