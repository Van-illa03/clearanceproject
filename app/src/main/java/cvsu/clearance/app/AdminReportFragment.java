package cvsu.clearance.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AdminReportFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener, View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button generateReport, searchReport, resetReport, syncReport, reportFilter, reportFilter_Apply, reportFilter_Cancel,nextBtn;
    ImageButton reportFilter_DateBtn, reportFilter_ResetBtn;
    TextView reportFilter_DateText;
    EditText StudentNumberInput;
    private long mLastClickTime = 0;
    RecyclerView AdminReportList;
    List<String> ReportID, ReportIDTemp;
    ReportAdapterAdmin adminreportadapter;
    Context thiscontext;
    List<String> StudentDocuID, StudentName, StudentNumber, StudentCourse;
    int reportDocuCounterAdmin = 1, reportDocuCounterBackupAdmin = 1;
    List<String> checker = new ArrayList<>();
    List<String> checkExistence = new ArrayList<>();
    String completeID;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DBHelper DB;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialogg;
    public String[] Courses = { "None","BS Agricultural and BioSystems Engineering","BS Architecture","BS Civil Engineering","BS Computer Engineering","BS Computer Science","BS Electrical Engineering","BS Electronics Engineering","BS Industrial Engineering","BS Industrial Technology - Automotive Tech","BS Industrial Technology - Electrical Tech","BS Industrial Technology - Electronics Tech","BS Information Technology","BS Office Administration" };
    String ChosenDate = "None", ChosenCourse = "None";
    String docuID = null;
    int limit = 5;
    Spinner CourseSpinner;


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
        ReportIDTemp = new ArrayList<>();
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
        reportFilter = fragview.findViewById(R.id.ReportFilter);
        nextBtn = fragview.findViewById(R.id.nextBtnAdmin);

        dialogBuilder = new AlertDialog.Builder(getContext());

        View DialogView = getLayoutInflater().inflate(R.layout.adminreportfilterinterface,container,false);
        CourseSpinner = DialogView.findViewById(R.id.reportFilter_CourseSpinner);
        CourseSpinner.setOnItemSelectedListener(this);

        ArrayAdapter AA = new ArrayAdapter (getContext(), R.layout.dropdown_item_custom, Courses);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        CourseSpinner.setAdapter(AA);

        reportFilter_DateText = DialogView.findViewById(R.id.reportFilter_DateText);
        reportFilter_DateBtn = DialogView.findViewById(R.id.reportFilter_DateBtn);
        reportFilter_ResetBtn = DialogView.findViewById(R.id.reportFilter_ResetBtn);
        reportFilter_Apply = (Button) DialogView.findViewById(R.id.reportFilter_applybtn);
        reportFilter_Cancel = (Button) DialogView.findViewById(R.id.reportFilter_cancelbtn);



        dialogBuilder.setTitle("Apply Filters");
        dialogBuilder.setView(DialogView);
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
            }
        });
        dialogg = dialogBuilder.create();

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);



        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginScreen.class));
        }

        viewReportData("None","None");
        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) fragview.findViewById(R.id.swipe_container_adminReport);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);



        nextBtn.setOnClickListener(AdminReportFragment.this);

        reportFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogg.show();

                dialogg.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                reportFilter_DateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),AlertDialog.THEME_HOLO_LIGHT,AdminReportFragment.this::onDateSet,year,month,day);
                        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        datePickerDialog.show();
                    }
                });

                reportFilter_ResetBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChosenDate = "None";
                        reportFilter_DateText.setText("-");
                    }
                });

                reportFilter_Apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogg.dismiss();
                        Toast.makeText(getActivity().getApplicationContext(), "Filter Applied", Toast.LENGTH_SHORT).show();

                        if (ChosenDate.isEmpty()){
                            ChosenDate = "None";
                            viewReportData(ChosenCourse,ChosenDate);
                        }
                        else {
                            viewReportData(ChosenCourse,ChosenDate);
                        }

                    }
                });

                reportFilter_Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogg.dismiss();
                        Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


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
                        viewReportData("None", "None");

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
                                            mStore.collection("CompletedClearance").orderBy("ID").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                                                            String Date = documentSnapshot.get("Date").toString();
                                                            String Time = documentSnapshot.get("Time").toString();
                                                            checkReportData = DB.insertReportDetailsAdmin(ID,StudentNumber, Name, Course, Status, Date, Time);
                                                            if(checkReportData){
                                                                Log.d("SUCCESS", "DATA SUCCESSFULLY INSERTED");
                                                                Log.d("REPORT-DATA", ID+"::"+StudentNumber+"::"+Name+"::"+Course+"::"+Status+"::"+Date+"::"+Time);
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
                                        mStore.collection("CompletedClearance").orderBy("ID").get()
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
                StudentNumberInput.setText("");
                viewReportData("None", "None");
            }
        });

        return fragview;
    }

    public void viewReportData (String Course, String Datee) {
        if (Course.equals("None") && Datee.equals("None")){
            mStore.collection("CompletedClearance").limit(limit).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ReportID.clear();
                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                ReportID.add(document.getId());
                                Log.d("Snapshots","Documents fetched " + document.getId().toString());
                                docuID = document.getId();
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
        else if (Course != "None" && Datee != "None"){
            mStore.collection("CompletedClearance").whereEqualTo("Course",Course).whereEqualTo("Date",Datee).limit(limit).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ReportID.clear();
                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                ReportID.add(document.getId());
                                Log.d("Snapshots","Documents fetched " + document.getId().toString());
                                docuID = document.getId();
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
        else if (Course.equals("None") && Datee != "None"){
            mStore.collection("CompletedClearance").whereEqualTo("Date",Datee).limit(limit).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ReportID.clear();
                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                ReportID.add(document.getId());
                                Log.d("Snapshots","Documents fetched " + document.getId().toString());
                                docuID = document.getId();
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
        else if (Course != "None" && Datee.equals("None")){
            mStore.collection("CompletedClearance").whereEqualTo("Course",Course).limit(limit).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ReportID.clear();
                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                ReportID.add(document.getId());
                                Log.d("Snapshots","Documents fetched " + document.getId().toString());
                                docuID = document.getId();
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
                Date c = Calendar.getInstance().getTime();

                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
                String formattedDate = df.format(c);
                String formattedTime = tf.format(c);
                //putting report data to HashMap
                Map<String,Object> insertReportDetailsAdmin = new HashMap<>();
                insertReportDetailsAdmin.put("StudentNumber", task.getResult().get("StdNo").toString());
                insertReportDetailsAdmin.put("Name", task.getResult().get("Name").toString());
                insertReportDetailsAdmin.put("Course", task.getResult().get("Course").toString());
                insertReportDetailsAdmin.put("Status", "Complete");
                insertReportDetailsAdmin.put("Date", formattedDate);
                insertReportDetailsAdmin.put("Time", formattedTime);

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
                Cursor curCSV;
                //Add where clause based on the selection of filter
                if(!ChosenDate.equals("None")){
                    curCSV = db.rawQuery("SELECT * FROM ReportDetailsAdmin WHERE Date='"+ChosenDate+"'",null);
                }
                else if(!ChosenCourse.equals("None")){
                    curCSV = db.rawQuery("SELECT * FROM ReportDetailsAdmin WHERE Course='"+ChosenCourse+"'",null);
                }
                else if(!ChosenDate.equals("None") && !ChosenCourse.equals("None")){
                    curCSV = db.rawQuery("SELECT * FROM ReportDetailsAdmin WHERE Date='"+ChosenDate+"' AND Course='"+ChosenCourse+"'",null);
                }
                else{
                    curCSV = db.rawQuery("SELECT * FROM ReportDetailsAdmin",null);
                }

                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext())
                {
                    //Columns to export
                    String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6)};
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
        ChosenCourse = "None";
        ChosenDate = "None";
        CourseSpinner.setSelection(0);
        reportFilter_DateText.setText("-");
        viewReportData("None","None");

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ChosenCourse = Courses[position];
        //Toast.makeText(getContext(),""+Courses[position],Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month = month + 1;

        if (dayOfMonth < 10 && month < 10) {
            ChosenDate = "0"+dayOfMonth+"-0"+month+"-"+year;
            reportFilter_DateText.setText(ChosenDate);
        }
        else if (dayOfMonth < 10 && month > 9){
            ChosenDate = "0"+dayOfMonth+"-"+month+"-"+year;
            reportFilter_DateText.setText(ChosenDate);
        }
        else if (dayOfMonth > 9 && month < 10){
            ChosenDate = dayOfMonth+"-0"+month+"-"+year;
            reportFilter_DateText.setText(ChosenDate);
        }
        else if (dayOfMonth > 9 && month > 9){
            ChosenDate = dayOfMonth+"-"+month+"-"+year;
            reportFilter_DateText.setText(ChosenDate);
        }


    }

    @Override
    public void onClick(View v) {
        if (docuID != null){
            if (ChosenCourse.equals("None") && ChosenDate.equals("None")){
                ReportIDTemp.clear();
                mStore.collection("CompletedClearance").orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                                    ReportIDTemp.add(documentSnapshot.getId());
                                    docuID = documentSnapshot.getId();
                                }

                                if (ReportIDTemp.size() == 0){
                                    Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    ReportID.clear();
                                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                        ReportID.add(document.getId());
                                        docuID = document.getId();
                                    }

                                    //passing the array
                                    adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                    AdminReportList.setAdapter(adminreportadapter);
                                    AdminReportList.setLayoutManager(gridLayoutManager);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
            }
            else if (ChosenCourse != "None" && ChosenDate != "None"){
                ReportIDTemp.clear();
                mStore.collection("CompletedClearance").whereEqualTo("Course", ChosenCourse).whereEqualTo("Date",ChosenDate).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                                    ReportIDTemp.add(documentSnapshot.getId());
                                    docuID = documentSnapshot.getId();
                                }

                                if (ReportIDTemp.size() == 0){
                                    Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    ReportID.clear();
                                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                        ReportID.add(document.getId());
                                        docuID = document.getId();
                                    }

                                    //passing the array
                                    adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                    AdminReportList.setAdapter(adminreportadapter);
                                    AdminReportList.setLayoutManager(gridLayoutManager);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
            }
            else if (ChosenCourse.equals("None") && ChosenDate != "None") {
                ReportIDTemp.clear();
                mStore.collection("CompletedClearance").whereEqualTo("Date",ChosenDate).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                                    ReportIDTemp.add(documentSnapshot.getId());
                                    docuID = documentSnapshot.getId();
                                }

                                if (ReportIDTemp.size() == 0){
                                    Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    ReportID.clear();
                                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                        ReportID.add(document.getId());
                                        docuID = document.getId();
                                    }

                                    //passing the array
                                    adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                    AdminReportList.setAdapter(adminreportadapter);
                                    AdminReportList.setLayoutManager(gridLayoutManager);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
            }
            else if (ChosenCourse != "None" && ChosenDate.equals("None")){
                ReportIDTemp.clear();
                mStore.collection("CompletedClearance").whereEqualTo("Course", ChosenCourse).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                                    ReportIDTemp.add(documentSnapshot.getId());
                                    docuID = documentSnapshot.getId();
                                }

                                if (ReportIDTemp.size() == 0){
                                    Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    ReportID.clear();
                                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                        ReportID.add(document.getId());
                                        docuID = document.getId();
                                    }

                                    //passing the array
                                    adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                    AdminReportList.setAdapter(adminreportadapter);
                                    AdminReportList.setLayoutManager(gridLayoutManager);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }


                            }
                        });
            }

        }
        else {
            Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
        }
    }
}