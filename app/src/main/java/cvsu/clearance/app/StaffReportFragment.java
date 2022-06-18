package cvsu.clearance.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class StaffReportFragment extends Fragment {

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button generateReport;
    private long mLastClickTime = 0;
    String StaffStation;
    RecyclerView ReportList;
    List<String> ReportID;
    ReportAdapterStaff staffreportadapter;
    Context thiscontext;




    DBHelper DB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragview = inflater.inflate(R.layout.staffreportfragment, container, false);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        generateReport = fragview.findViewById(R.id.generateReportBtn);
        DB = new DBHelper(getActivity().getApplicationContext());
        ReportID = new ArrayList<>();
        thiscontext = container.getContext();
        ReportList = fragview.findViewById(R.id.ReportList);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendar.getTime());


        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginScreen.class));

        }

        displayReportData();


        generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 5000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                mStore.collection("SigningStation").document(StaffStation).collection("Report").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Boolean checkReportData=null;
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                String ID = documentSnapshot.getId();
                                String StudentNumber = documentSnapshot.get("StudentNumber").toString(); // to be displayed
                                String Name = documentSnapshot.get("Name").toString();  // to be displayed
                                String Course = documentSnapshot.get("Course").toString();
                                String RequirementName = documentSnapshot.get("RequirementName").toString(); // to be displayed
                                String Status = documentSnapshot.get("Status").toString(); // to be displayed
                                String Type = documentSnapshot.get("Type").toString();
                                String Timestamp = documentSnapshot.get("Timestamp").toString(); // to be displayed

                                checkReportData = DB.insertReportDetails(ID,StudentNumber, Name, Course,RequirementName, Status, Type, Timestamp);
                                if(checkReportData){
                                    Log.d("SUCCESS", "DATA SUCCESSFULLY INSERTED");
                                    Log.d("REPORT-DATA", ID+"::"+StudentNumber+"::"+Name+"::"+Course+"::"+RequirementName+"::"+Status+"::"+Type+"::"+Timestamp);
                                }
                                else{
                                    Log.d("FAILED", "DATA FAILED TO INSERT");
                                }
                            }
                        }

                        if(checkReportData){
                            Toast.makeText(getActivity().getApplicationContext(), "Data inserted", Toast.LENGTH_SHORT).show();
                            exportDB();
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "NULL value", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        });


        return fragview;
    }

    private void displayReportData () {
        mStore.collection("Staff").document(mUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                StaffStation = document.getString("Station");

                                mStore.collection("SigningStation").document(StaffStation).collection("Report").get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                    ReportID.add(document.getId());
                                                    Log.d("Snapshots","Documents fetched");
                                                }

                                                staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                ReportList.setAdapter(staffreportadapter);
                                                ReportList.setLayoutManager(gridLayoutManager);
                                            }
                                        });
                            }
                        }

                    }
                });

    }


    private void exportDB() {


        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        try
        {
            File file = new File(exportDir, "test.csv");
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = DB.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM ReportDetails",null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                //Columns to export
                String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
        }
        catch(Exception sqlEx)
        {
            Log.e("StaffReportERROR", sqlEx.getMessage(), sqlEx);
        }
    }


}