package cvsu.clearance.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context) {
        super(context, "ReportData.db",null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create TABLE ReportDetails (ID TEXT primary key, StudentNumber TEXT, Name TEXT, Course TEXT, RequirementName TEXT, Status TEXT, Type TEXT, Timestamp TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop TABLE if EXISTS ReportDetails");
    }

    public Boolean insertReportDetails(String ID,String StudentNumber, String Name, String Course, String RequirementName, String Status, String Type, String Timestamp){

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID", ID);
        contentValues.put("StudentNumber", StudentNumber);
        contentValues.put("Name", Name);
        contentValues.put("Course", Course);
        contentValues.put("RequirementName", RequirementName);
        contentValues.put("Status", Status);
        contentValues.put("Type", Type);
        contentValues.put("Timestamp", Timestamp);

        long result = DB.insert("ReportDetails", null, contentValues);

        if(result ==-1){
            return false;
        }
        else{
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT * FROM ReportDetails", null);
        return cursor;

    }

}
