package cvsu.clearance.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.test.espresso.remote.EspressoRemoteMessage;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminReportFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button generateReport, searchReport, resetReport, syncReport;
    EditText StudentNumberInput;
    private long mLastClickTime = 0;
    RecyclerView AdminReportList;
    List<String> ReportID;
    ReportAdapterAdmin adminreportadapter;
    Context thiscontext;
    List<String> StudentDocuID, StudentName, StudentNumber, StudentCourse;
    int reportDocuCounterAdmin = 1, reportDocuCounterBackupAdmin = 1;
    List<String> checker = new ArrayList<>();
    List<String> checkExistence = new ArrayList<>();
    String completeID;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DBHelper DB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragview = inflater.inflate(R.layout.adminreportfragment, container, false);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        generateReport = fragview.findViewById(R.id.generateReportBtnAdmin);
        DB = new DBHelper(getActivity().getApplicationContext());
        ReportID = new ArrayList<>();
        thiscontext = container.getContext();
        AdminReportList = fragview.findViewById(R.id.ReportListAdmin);
        StudentDocuID = new ArrayList<>();
        StudentName = new ArrayList<>();
        StudentNumber = new ArrayList<>();
        StudentCourse = new ArrayList<>();
        searchReport = fragview.findViewById(R.id.searchReportAdminBtn);
        resetReport = fragview.findViewById(R.id.resetReportAdminBtn);
        StudentNumberInput = fragview.findViewById(R.id.searchReportAdmin);
        syncReport = fragview.findViewById(R.id.syncReportBtnAdmin);



        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginScreen.class));
        }

        viewReportData();
        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) fragview.findViewById(R.id.swipe_container_adminReport);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);



        syncReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Sync Data");
                alert.setMessage("Proceed syncing report data? \n\n(Note: It is advisable to sync the report data during and after the e-clearance signing period only. This is to avoid premature registration of reports. Do not sync if the signing stations and station requirements are not finalized.)");
                alert.setCancelable(false);
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SyncReportData();
                        adminreportadapter.notifyDataSetChanged();
                        viewReportData();

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });


        generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 5000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                
                Dexter.withContext(getActivity())
                        .withPermissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).withListener(new MultiplePermissionsListener() {
                            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if(report.areAllPermissionsGranted()){
                                    //Toast.makeText(getApplicationContext(), "Permission GRANTED", Toast.LENGTH_LONG).show();
                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                    alert.setTitle("Confirm Download?");
                                    alert.setCancelable(false);
                                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DB.deleteTableAdmin();
                                            mStore.collection("CompletedClearance").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    Boolean checkReportData=null;
                                                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                        if (documentSnapshot.exists()) {
                                                            String ID = documentSnapshot.getId();
                                                            String StudentNumber = documentSnapshot.get("StudentNumber").toString();
                                                            String Name = documentSnapshot.get("Name").toString();
                                                            String Course = documentSnapshot.get("Course").toString();
                                                            String Status = documentSnapshot.get("Status").toString();
                                                            String Timestamp = documentSnapshot.get("Timestamp").toString();

                                                            checkReportData = DB.insertReportDetailsAdmin(ID,StudentNumber, Name, Course, Status, Timestamp);
                                                            if(checkReportData){
                                                                Log.d("SUCCESS", "DATA SUCCESSFULLY INSERTED");
                                                                Log.d("REPORT-DATA", ID+"::"+StudentNumber+"::"+Name+"::"+Course+"::"+Status+"::"+Timestamp);
                                                            }
                                                            else{
                                                                Log.d("FAILED", "DATA FAILED TO INSERT");
                                                            }
                                                        }
                                                    }

                                                    if(checkReportData){
                                                        exportDB();
                                                    }
                                                    else{
                                                        Toast.makeText(getActivity().getApplicationContext(), "Error inserting report data.", Toast.LENGTH_SHORT).show();
                                                    }


                                                }
                                            });
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    alert.show();

                                }
                                else{
                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Permission DENIED</font>"));
                                    alert.setCancelable(false);
                                    alert.setMessage("Access to storage is required for system's certain functions to work.");
                                    alert.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    });
                                    alert.show();

                                }
                            }
                            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();


            }
        });

        searchReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                        mStore.collection("CompletedClearance").get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        ReportID.clear();
                                                        String StdNoInput = StudentNumberInput.getText().toString().trim();
                                                        for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                            if (document.getString("StudentNumber").equals(StdNoInput)){
                                                                ReportID.add(document.getId());
                                                                Log.d("Snapshots","Documents fetched");
                                                            }
                                                        }

                                                        if (ReportID.size() != 0){
                                                            adminreportadapter = new ReportAdapterAdmin(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            AdminReportList.setAdapter(adminreportadapter);
                                                            AdminReportList.setLayoutManager(gridLayoutManager);
                                                        }
                                                        else {
                                                            Toast.makeText(getActivity().getApplicationContext(), "No existing report data.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

            }
        });

        resetReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewReportData();
            }
        });

        return fragview;
    }

    public void viewReportData () {
        mStore.collection("CompletedClearance").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ReportID.clear();
                        for (QueryDocumentSnapshot document: queryDocumentSnapshots){

                            ReportID.add(document.getId());
                            Log.d("Snapshots","Documents fetched " + document.getId().toString());
                        }

                        //passing the array
                        adminreportadapter = new ReportAdapterAdmin(thiscontext,ReportID);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                        AdminReportList.setAdapter(adminreportadapter);
                        AdminReportList.setLayoutManager(gridLayoutManager);

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void SyncReportData () {
        ReportID.clear();
        reportDocuCounterAdmin = 1;
        StudentName.clear();
        StudentNumber.clear();
        StudentCourse.clear();
        checker.clear();
        reportDocuCounter();

        //Saving the student document IDs to the list
        mStore.collection("Students").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.get("Name").toString();
                            String studentNumber = documentSnapshot.get("StdNo").toString();
                            String course = documentSnapshot.get("Course").toString();
                            Log.d("Student Data", id+name+studentNumber+course);

                            checkerMethod(id);

                        }
                    }
                });

        Toast.makeText(getContext(),"Swipe down to refresh the reports.",Toast.LENGTH_SHORT).show();

    }
    public void reportDocuCounter(){
        mStore.collection("CompletedClearance").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    if(documentSnapshot.exists()){
                        reportDocuCounterAdmin++;
                        reportDocuCounterBackupAdmin = reportDocuCounterAdmin;
                    }
                    else {
                        reportDocuCounterAdmin = 1;
                        reportDocuCounterBackupAdmin = reportDocuCounterAdmin;
                    }
                }
            }
        });
    }

    private void savingMethod(String completeID){

        mStore.collection("Students").document(completeID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Date currentTime = Calendar.getInstance().getTime();
                String currentTimeString = currentTime.toString();


                //putting report data to HashMap
                Map<String,Object> insertReportDetailsAdmin = new HashMap<>();
                insertReportDetailsAdmin.put("StudentNumber", task.getResult().get("StdNo").toString());
                insertReportDetailsAdmin.put("Name", task.getResult().get("Name").toString());
                insertReportDetailsAdmin.put("Course", task.getResult().get("Course").toString());
                insertReportDetailsAdmin.put("Status", "Complete");
                insertReportDetailsAdmin.put("Timestamp", currentTimeString);

                mStore.collection("CompletedClearance").get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        String StudentNumber = task.getResult().get("StdNo").toString();
                                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                                            if (StudentNumber.equals(documentSnapshot.getString("StudentNumber"))){
                                                checkExistence.add("existing");
                                            }
                                        }

                                        if(checkExistence.size()!=0){
                                            checkExistence.clear();
                                        }
                                        else{
                                            mStore.collection("CompletedClearance").document(String.valueOf(reportDocuCounterAdmin)).set(insertReportDetailsAdmin)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Log.d("Report: ","Insertion of report data successful.");

                                                        }
                                                    });
                                            reportDocuCounterAdmin++;
                                        }

                                    }


                                });


            }
        });
    }

    private void checkerMethod(String id) {

        mStore.collection("Students").document(id).collection("Stations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            if (documentSnapshot.get("Status").equals("Not-Signed")){
                                checker.add("incomplete");
                                Log.d("INCOMPLETE: ", id);
                                break;
                            }
                        }

                        if(checker.size()!=0){
                            checker.clear();
                        }
                        else{
                            completeID=id;
                            savingMethod(completeID);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Report: ","Insertion of report data failed. Empty data");
                    }
                });
    }

    private void exportDB() {

        File exportDir = new File(getContext().getExternalFilesDir("REPORTS"),"CompletedClearance");
        String fileName = "ADMIN_RECORD_"+System.currentTimeMillis()+".csv";
        try
        {
            File file = new File(exportDir, fileName);
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }
            boolean created = file.createNewFile();
            if(created){
                Toast.makeText(getActivity().getApplicationContext(), "Download Started...",Toast.LENGTH_SHORT).show();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity().getApplicationContext(), App.CHANNEL_1_ID);
                builder.setContentTitle("A new file is downloaded");
                builder.setContentText(fileName);
                builder.setSmallIcon(R.drawable.download_icon);
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setCategory(NotificationCompat.CATEGORY_STATUS);
                builder.setAutoCancel(true);

                Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                PendingIntent pendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, intent, 0);
                builder.setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1,builder.build());
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = DB.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM ReportDetailsAdmin",null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext())
                {
                    //Columns to export
                    String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5)};
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
            }
            else{
                Toast.makeText(getActivity().getApplicationContext(), "An error has occured. Please try again later.", Toast.LENGTH_SHORT).show();
            }

        }
        catch(Exception sqlEx)
        {
            Log.e("StaffReportERROR", sqlEx.getMessage(), sqlEx);
        }
    }

    @Override
    public void onRefresh() {
        if (adminreportadapter != null){
            adminreportadapter.notifyDataSetChanged();
        }
        ReportID.clear();
        viewReportData();

        mSwipeRefreshLayout.setRefreshing(false);
    }
}