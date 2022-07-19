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


public class AdminReportFragment2 extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button generateReport, searchReport, reportFilter, reportFilter_Apply, reportFilter_Cancel,nextBtn;
    ImageButton reportFilter_DateBtn, reportFilter_ResetBtn,reportFilter_DateBtn2, reportFilter_ResetBtn2;
    TextView reportFilter_DateText, reportFilter_DateText2;
    EditText StudentNumberInput;
    private long mLastClickTime = 0;
    RecyclerView AdminReportList;
    List<String> ReportID, ReportIDTemp;
    ReportAdapterAdmin adminreportadapter;
    Context thiscontext;
    List<String> StudentDocuID, StudentName, StudentNumber, StudentCourse;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DBHelper DB;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialogg;
    public String[] Courses = { "None","BS Agricultural and BioSystems Engineering","BS Architecture","BS Civil Engineering","BS Computer Engineering","BS Computer Science","BS Electrical Engineering","BS Electronics Engineering","BS Industrial Engineering","BS Industrial Technology - Automotive Tech","BS Industrial Technology - Electrical Tech","BS Industrial Technology - Electronics Tech","BS Information Technology","BS Office Administration" };
    String ChosenDate = "None", ChosenDate2 = "None", ChosenCourse = "None";
    String docuID = null;
    int limit = 5;
    Spinner CourseSpinner;
    long startDate, endDate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragview = inflater.inflate(R.layout.adminreportfragment2, container, false);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        generateReport = fragview.findViewById(R.id.generateReportBtnAdmin2);
        DB = new DBHelper(getActivity().getApplicationContext());
        ReportID = new ArrayList<>();
        ReportIDTemp = new ArrayList<>();
        thiscontext = container.getContext();
        AdminReportList = fragview.findViewById(R.id.ReportListAdmin2);
        StudentDocuID = new ArrayList<>();
        StudentName = new ArrayList<>();
        StudentNumber = new ArrayList<>();
        StudentCourse = new ArrayList<>();
        searchReport = fragview.findViewById(R.id.searchReportAdminBtn2);
        StudentNumberInput = fragview.findViewById(R.id.searchReportAdmin2);

        reportFilter = fragview.findViewById(R.id.ReportFilter2);
        nextBtn = fragview.findViewById(R.id.nextBtnAdmin2);

        dialogBuilder = new AlertDialog.Builder(getContext());

        View DialogView = getLayoutInflater().inflate(R.layout.adminreportfilterinterface2,container,false);
        CourseSpinner = DialogView.findViewById(R.id.reportFilter_CourseSpinner);
        CourseSpinner.setOnItemSelectedListener(this);

        ArrayAdapter AA = new ArrayAdapter (getContext(), R.layout.dropdown_item_custom, Courses);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        CourseSpinner.setAdapter(AA);

        reportFilter_DateText = DialogView.findViewById(R.id.reportFilter_DateText);
        reportFilter_DateBtn = DialogView.findViewById(R.id.reportFilter_DateBtn);
        reportFilter_ResetBtn = DialogView.findViewById(R.id.reportFilter_ResetBtn);

        reportFilter_DateText2 = DialogView.findViewById(R.id.reportFilter_DateText2);
        reportFilter_DateBtn2 = DialogView.findViewById(R.id.reportFilter_DateBtn2);
        reportFilter_ResetBtn2 = DialogView.findViewById(R.id.reportFilter_ResetBtn2);
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

        viewReportData("None",0, 0);
        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) fragview.findViewById(R.id.swipe_container_adminReport);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);



        nextBtn.setOnClickListener(AdminReportFragment2.this);

        reportFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogg.show();

                dialogg.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                reportFilter_DateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startDate = 0;
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("Date changed", "Detected");
                                Calendar c = Calendar.getInstance();
                                c.set(year,month,dayOfMonth);
                                Date d = c.getTime();
                                month = month + 1;

                                if (dayOfMonth < 10 && month < 10) {
                                    ChosenDate = "0"+dayOfMonth+"-0"+month+"-"+year;
                                    startDate = d.getTime();
                                    reportFilter_DateText.setText(ChosenDate);
                                }
                                else if (dayOfMonth < 10 && month > 9){
                                    ChosenDate = "0"+dayOfMonth+"-"+month+"-"+year;
                                    startDate = d.getTime();
                                    reportFilter_DateText.setText(ChosenDate);
                                }
                                else if (dayOfMonth > 9 && month < 10){
                                    ChosenDate = dayOfMonth+"-0"+month+"-"+year;
                                    startDate = d.getTime();
                                    reportFilter_DateText.setText(ChosenDate);
                                }
                                else if (dayOfMonth > 9 && month > 9){
                                    ChosenDate = dayOfMonth+"-"+month+"-"+year;
                                    startDate = d.getTime();
                                    reportFilter_DateText.setText(ChosenDate);
                                }
                            }
                        }, year, month, day);
                        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        datePickerDialog.show();
                    }
                });

                reportFilter_DateBtn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        endDate = 0;
                        DatePickerDialog datePickerDialog2 = new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("Date changed", "Detected");

                                Calendar c = Calendar.getInstance();
                                c.set(year,month,dayOfMonth);
                                Date d = c.getTime();
                                month = month + 1;

                                if (dayOfMonth < 10 && month < 10) {
                                    ChosenDate2 = "0"+dayOfMonth+"-0"+month+"-"+year;
                                    endDate = d.getTime();
                                    reportFilter_DateText2.setText(ChosenDate2);
                                }
                                else if (dayOfMonth < 10 && month > 9){
                                    ChosenDate2 = "0"+dayOfMonth+"-"+month+"-"+year;
                                    endDate = d.getTime();
                                    reportFilter_DateText2.setText(ChosenDate2);
                                }
                                else if (dayOfMonth > 9 && month < 10){
                                    ChosenDate2 = dayOfMonth+"-0"+month+"-"+year;
                                    endDate = d.getTime();
                                    reportFilter_DateText2.setText(ChosenDate2);
                                }
                                else if (dayOfMonth > 9 && month > 9){
                                    ChosenDate2 = dayOfMonth+"-"+month+"-"+year;
                                    endDate = d.getTime();
                                    reportFilter_DateText2.setText(ChosenDate2);
                                }
                            }
                        }, year, month, day);
                        datePickerDialog2.getDatePicker().setMaxDate(System.currentTimeMillis());
                        datePickerDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        datePickerDialog2.show();
                    }
                });

                reportFilter_ResetBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChosenDate = "None";
                        startDate = 0;
                        reportFilter_DateText.setText("-");
                    }
                });
                reportFilter_ResetBtn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChosenDate2 = "None";
                        endDate = 0;
                        reportFilter_DateText2.setText("-");
                    }
                });

                reportFilter_Apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogg.dismiss();

                        if (startDate != 0 && endDate != 0){


                            ChosenDate = "None";
                            viewReportData(ChosenCourse, startDate, endDate);
                            Toast.makeText(getActivity().getApplicationContext(), "Filter Applied", Toast.LENGTH_SHORT).show();

                        }
                        else {
                            viewReportData(ChosenCourse, 0,0);
                            Toast.makeText(getActivity().getApplicationContext(), "Filter Applied", Toast.LENGTH_SHORT).show();

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
                mStore.collection("CompletedClearance2").orderBy("ID").get()
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



        return fragview;
    }

    public void viewReportData (String Course, long dateStart, long dateEnd) {
        Log.d("date values", "date start "+ dateStart +" / date end "+ dateEnd);

        SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat ed = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String StartDateStr = sd.format(dateStart);
        String EndDateStr = ed.format(dateEnd);

        if (StartDateStr.equals(EndDateStr) && (dateStart != 0 && dateEnd != 0)){
            if (Course.equals("None")){
                mStore.collection("CompletedClearance2").whereEqualTo("Date",EndDateStr).limit(limit).get()
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
            else {
                mStore.collection("CompletedClearance2").whereEqualTo("Date",EndDateStr).whereEqualTo("Course", Course).limit(limit).get()
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
        else {
            if (Course.equals("None") && (dateStart == 0 && dateEnd == 0)){
                mStore.collection("CompletedClearance2").limit(limit).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                Log.d("Inner If2 Condition 1", "true");
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
            else if (Course != "None" && (dateStart != 0 && dateEnd != 0)){
                mStore.collection("CompletedClearance2")
                        .whereGreaterThanOrEqualTo("RawTime",dateStart)
                        .whereLessThanOrEqualTo("RawTime",dateEnd)
                        .orderBy("RawTime").limit(limit)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                ReportID.clear();
                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                    if (document.getString("Course").equals(Course)){
                                        ReportID.add(document.getId());
                                        Log.d("Snapshots","Documents fetched " + document.getId().toString());
                                        docuID = document.getId();
                                    }
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
                else if (Course.equals("None") && (dateStart != 0 && dateEnd != 0)){
                mStore.collection("CompletedClearance2").whereGreaterThanOrEqualTo("RawTime",dateStart)
                        .whereLessThanOrEqualTo("RawTime",dateEnd).orderBy("RawTime").limit(limit).get()
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
                else if (Course != "None" && (dateStart == 0 && dateEnd == 0)){
                mStore.collection("CompletedClearance2").whereEqualTo("Course",Course).limit(limit).get()
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
                if(!ChosenDate.equals("None") && ChosenCourse.equals("None")){
                    curCSV = db.rawQuery("SELECT * FROM ReportDetailsAdmin WHERE Date='"+ChosenDate+"'",null);
                }
                else if(!ChosenCourse.equals("None") && ChosenDate.equals("None")){
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
        reportFilter_DateText2.setText("-");
        startDate = 0;
        endDate = 0;
        viewReportData("None",0,0);

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

//.orderBy("RawTime").startAt(startDate).endAt(endDate)
    @Override
    public void onClick(View v) {
        SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat ed = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String StartDateStr = sd.format(startDate);
        String EndDateStr = ed.format(endDate);

        if (docuID != null){
            if (StartDateStr.equals(EndDateStr) && (startDate != 0 && endDate != 0)){
                if (ChosenCourse.equals("None")){
                    mStore.collection("CompletedClearance2").whereEqualTo("Date",EndDateStr).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
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
                else {
                    mStore.collection("CompletedClearance2").whereEqualTo("Date",EndDateStr).whereEqualTo("Course",ChosenCourse).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
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
            if (ChosenCourse.equals("None") && (startDate == 0 && endDate == 0)){
                ReportIDTemp.clear();
                mStore.collection("CompletedClearance2").orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
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
            else if (ChosenCourse != "None" && (startDate != 0 && endDate != 0)){
                ReportIDTemp.clear();
                mStore.collection("CompletedClearance2").orderBy(FieldPath.documentId()).startAfter(docuID).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                int size = 0;
                                if (queryDocumentSnapshots.size() != 0){
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        long RawTime = documentSnapshot.getLong("RawTime");
                                        String Course = documentSnapshot.getString("Course");

                                        if ((RawTime >= startDate && RawTime <= endDate) && Course.equals(ChosenCourse)) {
                                            ReportIDTemp.add(documentSnapshot.getId());
                                            ReportID.add((documentSnapshot.getId()));
                                            docuID = documentSnapshot.getId();
                                        }

                                        size++;

                                        if (ReportID.size() == 5){
                                            //passing the array
                                            adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                            AdminReportList.setAdapter(adminreportadapter);
                                            AdminReportList.setLayoutManager(gridLayoutManager);
                                            mSwipeRefreshLayout.setRefreshing(false);
                                        }
                                        else if (size == queryDocumentSnapshots.size()){
                                            Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                            //passing the array
                                            adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                            AdminReportList.setAdapter(adminreportadapter);
                                            AdminReportList.setLayoutManager(gridLayoutManager);
                                            mSwipeRefreshLayout.setRefreshing(false);
                                        }

                                    }
                                }
                                else {
                                    Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
            }
            else if (ChosenCourse.equals("None") && (startDate != 0 && endDate != 0)) {
                    ReportIDTemp.clear();
                    mStore.collection("CompletedClearance2").orderBy(FieldPath.documentId()).startAfter(docuID).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    int size = 0;
                                    if (queryDocumentSnapshots.size() != 0){
                                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            long RawTime = documentSnapshot.getLong("RawTime");

                                            if (RawTime >= startDate && RawTime <= endDate) {
                                                ReportIDTemp.add(documentSnapshot.getId());
                                                ReportID.add((documentSnapshot.getId()));
                                                docuID = documentSnapshot.getId();
                                            }

                                            size++;

                                            if (ReportID.size() == 5){
                                                //passing the array
                                                adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                AdminReportList.setAdapter(adminreportadapter);
                                                AdminReportList.setLayoutManager(gridLayoutManager);
                                                mSwipeRefreshLayout.setRefreshing(false);
                                            }
                                            else if (size == queryDocumentSnapshots.size()){
                                                Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                                //passing the array
                                                adminreportadapter = new ReportAdapterAdmin(thiscontext, ReportID);
                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                AdminReportList.setAdapter(adminreportadapter);
                                                AdminReportList.setLayoutManager(gridLayoutManager);
                                                mSwipeRefreshLayout.setRefreshing(false);
                                            }

                                        }
                                    }
                                    else {
                                        Toast.makeText(thiscontext, "end of results.", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            });

            }
            else if (ChosenCourse != "None" && (startDate == 0 && endDate == 0)){
                ReportIDTemp.clear();
                mStore.collection("CompletedClearance2").whereEqualTo("Course", ChosenCourse).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
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