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
    Button generateReport, viewReport, SyncData;
    private long mLastClickTime = 0;
    String StaffStation;
    RecyclerView ReportList;
    List<String> ReportID;
    ReportAdapter staffreportadapter;
    Context thiscontext;
    List<String> StudentDocuID, StudentName, StudentNumber, StudentCourse;
    boolean CompleteChecker;
    int index=0, reportDocuCounterAdmin = 1, reportDocuCounterBackupAdmin = 1;
    List<String> checker = new ArrayList<>();
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
        viewReport = fragview.findViewById(R.id.viewDataBtnAdmin);
        DB = new DBHelper(getActivity().getApplicationContext());
        ReportID = new ArrayList<>();
        thiscontext = container.getContext();
        ReportList = fragview.findViewById(R.id.ReportListAdmin);
        SyncData = fragview.findViewById(R.id.SyncBtnAdmin);
        StudentDocuID = new ArrayList<>();
        StudentName = new ArrayList<>();
        StudentNumber = new ArrayList<>();
        StudentCourse = new ArrayList<>();



        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginScreen.class));

        }

        mStore.collection("Staff").document(mUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){
                                StaffStation = document.getString("Station");
                            }
                        }
                    }
                });


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

               /* mStore.collection("SigningStation").document(StaffStation).collection("Report").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "NULL value", Toast.LENGTH_SHORT).show();
                        }


                    }
                });*/
            }
        });

        viewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /*mStore.collection("SigningStation").document(StaffStation).collection("Report").get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                ReportID.clear();
                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){

                                    ReportID.add(document.getId().toString());
                                    Log.d("Snapshots","Documents fetched");
                                }
                            }
                        });*/

                //passing the array
                staffreportadapter = new ReportAdapter(thiscontext,ReportID);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,1,GridLayoutManager.VERTICAL,false);
                ReportList.setAdapter(staffreportadapter);
                ReportList.setLayoutManager(gridLayoutManager);
            }
        });

        return fragview;
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

                mStore.collection("CompletedClearance").document(String.valueOf(reportDocuCounterAdmin)).set(insertReportDetailsAdmin)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Report: ","Insertion of report data successful.");

                            }
                        });
                reportDocuCounterAdmin++;



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