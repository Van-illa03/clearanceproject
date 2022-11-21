package cvsu.clearance.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.Notification;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
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

import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class StaffReportFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button generateReport, searchReport, reportFilter, reportFilter_Apply, reportFilter_Cancel, nextBtn;
    ImageButton reportFilter_DateBtn, reportFilter_ResetBtn , reportFilter_DateBtn2;
    TextView reportFilter_DateText , reportFilter_DateText2;
    EditText StudentNumberInput;
    private long mLastClickTime = 0;
    String StaffStation;
    RecyclerView ReportList;
    List<String> ReportID, ReportIDTemp;
    ReportAdapterStaff staffreportadapter;
    Context thiscontext;
    SwipeRefreshLayout mSwipeRefreshLayout;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialogg;
    public String[] Courses = { "None","BS Agricultural and BioSystems Engineering","BS Architecture","BS Civil Engineering","BS Computer Engineering","BS Computer Science","BS Electrical Engineering","BS Electronics Engineering","BS Industrial Engineering","BS Industrial Technology - Automotive Tech","BS Industrial Technology - Electrical Tech","BS Industrial Technology - Electronics Tech","BS Information Technology","BS Office Administration" };
    String ChosenDate = "None", ChosenDate2 = "None", ChosenCourse = "None";
    String docuID = null;
    int limit = 5;
    Spinner CourseSpinner;
    long startDate, endDate;




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
        ReportIDTemp = new ArrayList<>();
        thiscontext = container.getContext();
        ReportList = fragview.findViewById(R.id.ReportList);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendar.getTime());
        searchReport = fragview.findViewById(R.id.searchReportStaffBtn);
        StudentNumberInput = fragview.findViewById(R.id.searchReportStaff);
        reportFilter = fragview.findViewById(R.id.ReportFilterStaff);
        nextBtn = fragview.findViewById(R.id.nextBtnStaff);

        dialogBuilder = new AlertDialog.Builder(getContext());


        View DialogView = getLayoutInflater().inflate(R.layout.staffreportfilterinterface,container,false);
        CourseSpinner = DialogView.findViewById(R.id.SreportFilter_CourseSpinner);
        CourseSpinner.setOnItemSelectedListener(this);

        ArrayAdapter AA = new ArrayAdapter (getContext(), R.layout.dropdown_item_custom, Courses);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        CourseSpinner.setAdapter(AA);

        reportFilter_DateText = DialogView.findViewById(R.id.SreportFilter_DateText);
        reportFilter_DateBtn = DialogView.findViewById(R.id.SreportFilter_DateBtn);
        reportFilter_ResetBtn = DialogView.findViewById(R.id.SreportFilter_ResetBtn);

        reportFilter_DateText2 = DialogView.findViewById(R.id.SreportFilter_DateText2);
        reportFilter_DateBtn2 = DialogView.findViewById(R.id.SreportFilter_DateBtn2);
        reportFilter_Apply = (Button) DialogView.findViewById(R.id.SreportFilter_applybtn);
        reportFilter_Cancel = (Button) DialogView.findViewById(R.id.SreportFilter_cancelbtn);

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
        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) fragview.findViewById(R.id.swipe_container_staffReport);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

                displayReportData(ChosenCourse, 0 , 0);
            }
        });

        nextBtn.setOnClickListener(StaffReportFragment.this);

        reportFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                dialogg.show();

                dialogg.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                reportFilter_DateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        startDate = 0;
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("Date changed", "Detected");
                                TimeZone timeZone = TimeZone.getDefault();
                                Calendar c = Calendar.getInstance(timeZone);
                                c.set(year, month, dayOfMonth);
                                c.set(Calendar.HOUR_OF_DAY,0);
                                c.set(Calendar.MINUTE,0);
                                c.set(Calendar.SECOND,0);
                                c.set(Calendar.MILLISECOND, 0);
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
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        if(startDate == 0) {
                            reportFilter_DateText.setError("Required");
                            Toast.makeText(getActivity().getApplicationContext(), "Set a start date first before setting end date.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            reportFilter_DateText2.setError(null);
                            endDate = 0;
                            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    Log.d("Date changed", "Detected");
                                    TimeZone timeZone = TimeZone.getDefault();
                                    Calendar c = Calendar.getInstance(timeZone);
                                    c.set(year, month, dayOfMonth);
                                    c.set(Calendar.HOUR_OF_DAY,23);
                                    c.set(Calendar.MINUTE,59);
                                    c.set(Calendar.SECOND,58);
                                    c.set(Calendar.MILLISECOND, 0);
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
                            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            datePickerDialog.show();
                        }
                    }
                });

                reportFilter_ResetBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ChosenDate = "None";
                        startDate = 0;
                        reportFilter_DateText.setText("-");
                        ChosenDate2 = "None";
                        endDate = 0;
                        reportFilter_DateText2.setText("-");
                    }
                });

                reportFilter_Apply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        //pababa here(2)
                        if((startDate != 0 && endDate != 0) && endDate>startDate){
                            reportFilter_DateText.setError(null);
                            reportFilter_DateText2.setError(null);
                            displayReportData(ChosenCourse, startDate, endDate);
                            dialogg.dismiss();
                        }
                        else if((startDate != 0 && endDate != 0) && endDate<startDate){
                            dialogg.dismiss();
                            reportFilter_DateText.setError(null);
                            reportFilter_DateText2.setError(null);
                            ChosenDate = "None";
                            startDate = 0;
                            reportFilter_DateText.setText("-");
                            ChosenDate2 = "None";
                            endDate = 0;
                            reportFilter_DateText2.setText("-");
                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                            alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Filter ERROR</font>"));
                            alert.setMessage("The end date must be ahead of the start date. Please check carefully.");
                            alert.setPositiveButton("OK", null);
                            alert.show();

                        }
                        else if(startDate!=0 && endDate==0){
                            reportFilter_DateText2.setError("End date is required");
                            reportFilter_DateText2.requestFocus();
                            Toast.makeText(getActivity().getApplicationContext(), "End date is required", Toast.LENGTH_LONG).show();
                        }
                        else if(startDate==0 && endDate!=0){
                            reportFilter_DateText.setError("Start date is required");
                            reportFilter_DateText.requestFocus();
                            Toast.makeText(getActivity().getApplicationContext(), "Start date is required", Toast.LENGTH_LONG).show();
                        }
                        else {
                            reportFilter_DateText.setError(null);
                            reportFilter_DateText2.setError(null);

                            displayReportData(ChosenCourse, 0,0);
                            dialogg.dismiss();
                        }
                        //pataas here(2)

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
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                Log.d("Report ID", ""+ReportID.size());
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
                                                DB.deleteTable();
                                                mStore.collection("SigningStation").document(StaffStation).collection("Report").orderBy("ID").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                                                                String Date = documentSnapshot.get("Date").toString(); // to be displayed
                                                                String Time = documentSnapshot.get("Time").toString();
                                                                checkReportData = DB.insertReportDetails(ID,StudentNumber, Name, Course,RequirementName, Status, Type, Date, Time);
                                                                if(checkReportData){
                                                                    Log.d("SUCCESS", "DATA SUCCESSFULLY INSERTED");
                                                                    Log.d("REPORT-DATA", ID+"::"+StudentNumber+"::"+Name+"::"+Course+"::"+RequirementName+"::"+Status+"::"+Type+"::"+Date+"::"+Time);
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
                                                            Toast.makeText(getActivity().getApplicationContext(), "NULL value", Toast.LENGTH_SHORT).show();
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
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");

                                        mStore.collection("SigningStation").document(StaffStation).collection("Report").orderBy("ID").get()
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
                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);
                                                        }
                                                        else {
                                                            Toast.makeText(getActivity().getApplicationContext(), "No existing report data.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }

                            }
                        });
            }
        });

        return fragview;
    }

    private void displayReportData (String Course, long dateStart, long dateEnd) {
        Log.d("date values", "date start "+ dateStart +" / date end "+ dateEnd);

        SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat ed = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        String StartDateStr = sd.format(dateStart);
        String EndDateStr = ed.format(dateEnd);

        if (StartDateStr.equals(EndDateStr) && (dateStart != 0 && dateEnd != 0)){
            if (Course.equals("None")){

                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");

                                        mStore.collection("SigningStation").document(StaffStation).collection("Report").whereEqualTo("Date",EndDateStr).orderBy(FieldPath.documentId()).limit(limit).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if (queryDocumentSnapshots.size() != 0){
                                                            ReportID.clear();
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                ReportID.add(document.getId());
                                                                Log.d("Snapshots","Documents fetched");
                                                                docuID = document.getId();
                                                            }

                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);

                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }
                                                        else {
                                                            ReportID.clear();
                                                            Toast.makeText(thiscontext, "No data fetched.", Toast.LENGTH_LONG).show();
                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);
                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }

                                                    }
                                                });
                                    }
                                }

                            }
                        });
            }
            else {

                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");

                                        mStore.collection("SigningStation").document(StaffStation).collection("Report").whereEqualTo("Date",EndDateStr).whereEqualTo("Course", Course).orderBy(FieldPath.documentId()).limit(limit).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if (queryDocumentSnapshots.size() != 0){
                                                            ReportID.clear();
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                ReportID.add(document.getId());
                                                                Log.d("Snapshots","Documents fetched");
                                                                docuID = document.getId();
                                                            }

                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);

                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }
                                                        else {
                                                            ReportID.clear();
                                                            Toast.makeText(thiscontext, "No data fetched.", Toast.LENGTH_LONG).show();
                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);
                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }

                                                    }
                                                });
                                    }
                                }

                            }
                        });
            }
        }
        else { //if startDate and endDate are not same dates (not a single date)
            if (Course.equals("None") && (dateStart == 0 && dateEnd == 0)){

                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");

                                        mStore.collection("SigningStation").document(StaffStation).collection("Report").orderBy(FieldPath.documentId()).limit(limit).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if (queryDocumentSnapshots.size() != 0){
                                                            ReportID.clear();
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                ReportID.add(document.getId());
                                                                Log.d("Snapshots","Documents fetched");
                                                                docuID = document.getId();
                                                            }

                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);

                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }
                                                        else {
                                                            ReportID.clear();
                                                            Toast.makeText(thiscontext, "No data fetched.", Toast.LENGTH_LONG).show();
                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);
                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }

                                                    }
                                                });
                                    }
                                }

                            }
                        });
            }
            else if (Course != "None" && (dateStart != 0 && dateEnd != 0)){
                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");

                                        mStore.collection("SigningStation").document(StaffStation).collection("Report")
                                                .orderBy(FieldPath.documentId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        int size = 0;
                                                        if (queryDocumentSnapshots.size() != 0){
                                                            ReportID.clear();
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                long RawTime = document.getLong("RawTime");
                                                                if (document.getString("Course").equals(Course) && (RawTime >= startDate && RawTime <= endDate)){
                                                                    ReportID.add(document.getId());
                                                                    Log.d("Snapshots","Documents fetched");
                                                                    docuID = document.getId();
                                                                }
                                                                size++;

                                                                if (ReportID.size() == limit){
                                                                    staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                                    ReportList.setAdapter(staffreportadapter);
                                                                    ReportList.setLayoutManager(gridLayoutManager);

                                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                                    break;
                                                                }
                                                                else if (size == queryDocumentSnapshots.size()){
                                                                    Toast.makeText(getActivity().getApplicationContext(), "End of results", Toast.LENGTH_SHORT).show();
                                                                    staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                                    ReportList.setAdapter(staffreportadapter);
                                                                    ReportList.setLayoutManager(gridLayoutManager);

                                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        else {
                                                            ReportID.clear();
                                                            Toast.makeText(thiscontext, "No data fetched.", Toast.LENGTH_LONG).show();
                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);
                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }
                                                    }
                                                });
                                    }
                                }

                            }
                        });
            }
            else if (Course.equals("None") && (dateStart != 0 && dateEnd != 0)){

                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");

                                        mStore.collection("SigningStation").document(StaffStation).collection("Report")
                                                .orderBy(FieldPath.documentId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        int size = 0;
                                                        if (queryDocumentSnapshots.size() != 0){
                                                            ReportID.clear();
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                long RawTime = document.getLong("RawTime");
                                                                if (RawTime >= startDate && RawTime <= endDate){
                                                                    ReportID.add(document.getId());
                                                                    Log.d("Snapshots","Documents fetched");
                                                                    docuID = document.getId();
                                                                }
                                                                size++;

                                                                if (ReportID.size() == limit){
                                                                    staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                                    ReportList.setAdapter(staffreportadapter);
                                                                    ReportList.setLayoutManager(gridLayoutManager);

                                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                                    break;
                                                                }
                                                                else if (size == queryDocumentSnapshots.size()){
                                                                    Toast.makeText(getActivity().getApplicationContext(), "End of results.", Toast.LENGTH_SHORT).show();
                                                                    staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                                    ReportList.setAdapter(staffreportadapter);
                                                                    ReportList.setLayoutManager(gridLayoutManager);

                                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        else {
                                                            ReportID.clear();
                                                            Toast.makeText(thiscontext, "No data fetched.", Toast.LENGTH_LONG).show();
                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);
                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }

                                                    }
                                                });
                                    }
                                }

                            }
                        });
            }
            else if (Course != "None" && (dateStart == 0 && dateEnd == 0)){

                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");

                                        mStore.collection("SigningStation").document(StaffStation).collection("Report").whereEqualTo("Course",Course).orderBy(FieldPath.documentId()).limit(limit).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if (queryDocumentSnapshots.size() != 0){
                                                            ReportID.clear();
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                ReportID.add(document.getId());
                                                                Log.d("Snapshots","Documents fetched");
                                                                docuID = document.getId();
                                                            }

                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);

                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }
                                                        else {
                                                            ReportID.clear();
                                                            Toast.makeText(thiscontext, "No data fetched.", Toast.LENGTH_LONG).show();
                                                            staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                            GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                            ReportList.setAdapter(staffreportadapter);
                                                            ReportList.setLayoutManager(gridLayoutManager);
                                                            mSwipeRefreshLayout.setRefreshing(false);
                                                        }
                                                    }
                                                });
                                    }
                                }

                            }
                        });
            }
        }




    }

    List<String> rangeOfDate = new ArrayList<>();
    private void exportDB() {
        File exportDir = new File(getContext().getExternalFilesDir("REPORTS"),StaffStation+"_SigningRecords");
        String fileName = StaffStation+"_RECORD_"+System.currentTimeMillis()+".csv";
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
                builder.setContentText(fileName+" downloaded");
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
                // to be changed
                //Add where clause based on the selection of filter
                if((!ChosenDate.equals("None") && !ChosenDate2.equals("None")) && ChosenCourse.equals("None")){
                    Cursor curCSV = db.rawQuery("SELECT * FROM ReportDetails",null);
                    csvWrite.writeNext(curCSV.getColumnNames());
                    saveDatesList();
                    while(curCSV.moveToNext())
                    {
                        for(int i=0; i<rangeOfDate.size();i++){
                            if(rangeOfDate.get(i).equals(curCSV.getString(7))){
                                //Columns to export
                                String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8)};
                                csvWrite.writeNext(arrStr);
                            }
                        }

                    }
                    csvWrite.close();
                    curCSV.close();

                }
                else if(!ChosenCourse.equals("None") && (ChosenDate.equals("None") && (ChosenDate2.equals("None")))){
                    Cursor curCSV = db.rawQuery("SELECT * FROM ReportDetails WHERE Course='"+ChosenCourse+"'",null);
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while(curCSV.moveToNext())
                    {
                        //Columns to export
                        String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8)};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    curCSV.close();
                }
                else if((!ChosenDate.equals("None") && (!ChosenDate2.equals("None"))) && !ChosenCourse.equals("None")){
                    Cursor curCSV = db.rawQuery("SELECT * FROM ReportDetails WHERE Course='"+ChosenCourse+"'",null);
                    csvWrite.writeNext(curCSV.getColumnNames());
                    saveDatesList();
                    while(curCSV.moveToNext())
                    {
                        for(int i=0; i<rangeOfDate.size();i++){
                            if(rangeOfDate.get(i).equals(curCSV.getString(7))){
                                //Columns to export
                                String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8)};
                                csvWrite.writeNext(arrStr);
                            }
                        }

                    }
                    csvWrite.close();
                    curCSV.close();
                }
                else if(ChosenCourse.equals("None") && (ChosenDate.equals("None") && (ChosenDate2.equals("None")))){
                    Cursor curCSV = db.rawQuery("SELECT * FROM ReportDetails",null);
                    csvWrite.writeNext(curCSV.getColumnNames());
                    while(curCSV.moveToNext())
                    {
                        //Columns to export
                        String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7), curCSV.getString(8)};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    curCSV.close();
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "An error has occured. Please try again later.1", Toast.LENGTH_SHORT).show();
                }



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

    private void saveDatesList(){

        if(!ChosenDate.equals(ChosenDate2)){
            rangeOfDate.clear();
            Date dateStart, dateBetween, dateEnd;
            TimeZone timeZone = TimeZone.getDefault();
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            // start date
            Calendar start = Calendar.getInstance(timeZone);
            start.setTimeInMillis(startDate);
            dateStart = start.getTime();
            // end date
            Calendar end = Calendar.getInstance(timeZone);
            end.setTimeInMillis(endDate);
            dateEnd = start.getTime();

            // add first date
            rangeOfDate.add(df.format(dateStart));

            while (start.before(end)) {
                start.add(Calendar.DAY_OF_MONTH, 1); // add one day
                dateBetween = start.getTime();
                Log.d("GETTING DATES", "between start and end:" + df.format(dateBetween));// show all the day between end and start
                rangeOfDate.add(df.format(dateBetween));
            }

            // add last date
            rangeOfDate.add(df.format(dateEnd));
        }
        else{
            rangeOfDate.clear();
            rangeOfDate.add(ChosenDate);
        }

    }

    @Override
    public void onRefresh() {
        staffreportadapter.notifyDataSetChanged();
        ReportID.clear();
        ChosenCourse = "None";
        ChosenDate = "None";
        ChosenDate2 = "None";
        startDate = 0;
        endDate = 0;
        CourseSpinner.setSelection(0);
        reportFilter_DateText.setText("-");
        reportFilter_DateText2.setText("-");
        reportFilter_DateText.setError(null);
        reportFilter_DateText2.setError(null);
        displayReportData("None",0, 0);

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ChosenCourse = Courses[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if (docuID != null){

            if (ChosenDate.equals(ChosenDate2) && (startDate != 0 && endDate != 0)){
                    if (ChosenCourse.equals("None")) {

                        mStore.collection("Staff").document(mUser.getUid()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()){
                                                StaffStation = document.getString("Station");

                                                mStore.collection("SigningStation").document(StaffStation).collection("Report").whereEqualTo("Date",ChosenDate2).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                if (queryDocumentSnapshots.size() != 0){
                                                                    ReportID.clear();
                                                                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                        ReportID.add(document.getId());
                                                                        Log.d("Snapshots","Documents fetched");
                                                                        docuID = document.getId();
                                                                    }

                                                                    staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                                    ReportList.setAdapter(staffreportadapter);
                                                                    ReportList.setLayoutManager(gridLayoutManager);

                                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                                }
                                                                else {
                                                                    Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });
                                            }
                                        }

                                    }
                                });
                    }
                    else {

                        mStore.collection("Staff").document(mUser.getUid()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()){
                                                StaffStation = document.getString("Station");

                                                mStore.collection("SigningStation").document(StaffStation).collection("Report").whereEqualTo("Date",ChosenDate2).whereEqualTo("Course", ChosenCourse).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                if (queryDocumentSnapshots.size() != 0){
                                                                    ReportID.clear();
                                                                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                        ReportID.add(document.getId());
                                                                        Log.d("Snapshots","Documents fetched");
                                                                        docuID = document.getId();
                                                                    }

                                                                    staffreportadapter = new ReportAdapterStaff(thiscontext,ReportID);
                                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                                                                    ReportList.setAdapter(staffreportadapter);
                                                                    ReportList.setLayoutManager(gridLayoutManager);

                                                                    mSwipeRefreshLayout.setRefreshing(false);
                                                                }
                                                                else {
                                                                    Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });
                                            }
                                        }

                                    }
                                });
                    }
                }
            else { // if Start date and End date is not the same date
                if (ChosenCourse.equals("None") && (startDate == 0 && endDate == 0)){
                    ReportIDTemp.clear();
                    mStore.collection("Staff").document(mUser.getUid()).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()){
                                            StaffStation = document.getString("Station");

                                            mStore.collection("SigningStation").document(StaffStation).collection("Report").orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                ReportIDTemp.add(document.getId());
                                                                docuID = document.getId();
                                                                Log.d("Snapshots","Documents fetched");
                                                            }

                                                            if (ReportIDTemp.size() == 0){
                                                                Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                ReportID.clear();
                                                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                    ReportID.add(document.getId());
                                                                    docuID = document.getId();
                                                                }

                                                                staffreportadapter = new ReportAdapterStaff(thiscontext, ReportID);
                                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                                ReportList.setAdapter(staffreportadapter);
                                                                ReportList.setLayoutManager(gridLayoutManager);
                                                                mSwipeRefreshLayout.setRefreshing(false);
                                                            }
                                                        }
                                                    });
                                        }
                                    }

                                }
                            });
                }
                else if (ChosenCourse != "None" && (startDate != 0 && endDate != 0)){

                    mStore.collection("Staff").document(mUser.getUid()).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()){
                                            StaffStation = document.getString("Station");

                                            mStore.collection("SigningStation").document(StaffStation).collection("Report").whereEqualTo("Course",ChosenCourse).whereEqualTo("Date", ChosenDate).orderBy(FieldPath.documentId()).startAfter(docuID).get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            int size = 0;
                                                            if (queryDocumentSnapshots.size() != 0) {
                                                                ReportIDTemp = ReportID;
                                                                ReportID.clear();
                                                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                    long RawTime = document.getLong("RawTime");
                                                                    String Course = document.getString("Course");

                                                                    if ((RawTime >= startDate && RawTime <= endDate) && Course.equals(ChosenCourse)) {
                                                                        ReportID.add((document.getId()));
                                                                        docuID = document.getId();
                                                                    }

                                                                    size++;

                                                                    if (ReportID.size() == limit){
                                                                        //passing the array
                                                                        staffreportadapter = new ReportAdapterStaff(thiscontext, ReportID);
                                                                        GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                                        ReportList.setAdapter(staffreportadapter);
                                                                        ReportList.setLayoutManager(gridLayoutManager);
                                                                        mSwipeRefreshLayout.setRefreshing(false);
                                                                        break;
                                                                    }
                                                                    else if (size == queryDocumentSnapshots.size() && ReportID.size() != 0){
                                                                        //passing the array
                                                                        staffreportadapter = new ReportAdapterStaff(thiscontext, ReportID);
                                                                        GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                                        ReportList.setAdapter(staffreportadapter);
                                                                        ReportList.setLayoutManager(gridLayoutManager);
                                                                        mSwipeRefreshLayout.setRefreshing(false);
                                                                        break;
                                                                    }
                                                                    else if (size == queryDocumentSnapshots.size() && ReportID.size() == 0){
                                                                        ReportID = ReportIDTemp;
                                                                        Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }
                                                            else {
                                                                Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });
                                        }
                                    }

                                }
                            });
                }
                else if (ChosenCourse.equals("None") && (startDate != 0 && endDate != 0)) {

                    mStore.collection("Staff").document(mUser.getUid()).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()){
                                            StaffStation = document.getString("Station");

                                            mStore.collection("SigningStation").document(StaffStation).collection("Report").orderBy(FieldPath.documentId()).startAfter(docuID).get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            int size = 0;
                                                            if (queryDocumentSnapshots.size() != 0) {
                                                                ReportIDTemp = ReportID;
                                                                ReportID.clear();
                                                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                    long RawTime = document.getLong("RawTime");

                                                                    if ((RawTime >= startDate && RawTime <= endDate)) {
                                                                        ReportIDTemp.add(document.getId());
                                                                        ReportID.add((document.getId()));
                                                                        docuID = document.getId();
                                                                    }

                                                                    size++;

                                                                    if (ReportID.size() == limit){
                                                                        //passing the array
                                                                        staffreportadapter = new ReportAdapterStaff(thiscontext, ReportID);
                                                                        GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                                        ReportList.setAdapter(staffreportadapter);
                                                                        ReportList.setLayoutManager(gridLayoutManager);
                                                                        mSwipeRefreshLayout.setRefreshing(false);
                                                                        break;
                                                                    }
                                                                    else if (size == queryDocumentSnapshots.size() && ReportID.size() != 0){
                                                                        //passing the array
                                                                        staffreportadapter = new ReportAdapterStaff(thiscontext, ReportID);
                                                                        GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                                        ReportList.setAdapter(staffreportadapter);
                                                                        ReportList.setLayoutManager(gridLayoutManager);
                                                                        mSwipeRefreshLayout.setRefreshing(false);
                                                                        break;
                                                                    }
                                                                    else if (size == queryDocumentSnapshots.size() && ReportID.size() == 0){
                                                                        ReportID = ReportIDTemp;
                                                                        Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }
                                                            else {
                                                                Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }

                                }
                            });
                }
                else if (ChosenCourse != "None" && (startDate == 0 && endDate == 0)){
                    ReportIDTemp.clear();
                    mStore.collection("Staff").document(mUser.getUid()).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()){
                                            StaffStation = document.getString("Station");

                                            mStore.collection("SigningStation").document(StaffStation).collection("Report").whereEqualTo("Course",ChosenCourse).orderBy(FieldPath.documentId()).startAfter(docuID).limit(limit).get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                ReportIDTemp.add(document.getId());
                                                                docuID = document.getId();
                                                                Log.d("Snapshots","Documents fetched");
                                                            }

                                                            if (ReportIDTemp.size() == 0){
                                                                Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                ReportID.clear();
                                                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                    ReportID.add(document.getId());
                                                                    docuID = document.getId();
                                                                }

                                                                staffreportadapter = new ReportAdapterStaff(thiscontext, ReportID);
                                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext, 1, GridLayoutManager.VERTICAL, false);
                                                                ReportList.setAdapter(staffreportadapter);
                                                                ReportList.setLayoutManager(gridLayoutManager);
                                                                mSwipeRefreshLayout.setRefreshing(false);
                                                            }
                                                        }
                                                    });
                                        }
                                    }

                                }
                            });
                }
            }


        }
        else {
            Toast.makeText(thiscontext, "End of results.", Toast.LENGTH_SHORT).show();
        }
    }
}