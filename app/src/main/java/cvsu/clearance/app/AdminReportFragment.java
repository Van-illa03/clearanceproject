package cvsu.clearance.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminReportFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button generateReport, SyncData, searchReport, resetReport;
    EditText StudentNumberInput;
    private long mLastClickTime = 0;
    String StaffStation;
    RecyclerView AdminReportList;
    List<String> ReportID;
    ReportAdapterAdmin adminreportadapter;
    Context thiscontext;
    List<String> StudentDocuID, StudentName, StudentNumber, StudentCourse;
    int index=0, reportDocuCounterAdmin = 1, reportDocuCounterBackupAdmin = 1;
    List<String> checker = new ArrayList<>();
    List<String> checkExistence = new ArrayList<>();
    String completeID;

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
        SyncData = fragview.findViewById(R.id.SyncBtnAdmin);
        StudentDocuID = new ArrayList<>();
        StudentName = new ArrayList<>();
        StudentNumber = new ArrayList<>();
        StudentCourse = new ArrayList<>();
        searchReport = fragview.findViewById(R.id.searchReportAdminBtn);
        resetReport = fragview.findViewById(R.id.resetReportAdminBtn);
        StudentNumberInput = fragview.findViewById(R.id.searchReportAdmin);



        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginScreen.class));

        }
        ReportID.clear();
        displayReportData();


        SyncData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 5000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
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
            }


        });


        generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 5000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

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
                            Toast.makeText(getActivity().getApplicationContext(), "Data inserted", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "NULL value", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
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
                                                        String StdNoInput = StudentNumberInput.getText().toString();
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
                displayReportData();
            }
        });


        return fragview;
    }




    private void displayReportData () {
        ReportID.clear();
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
                            }
                        });


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


}