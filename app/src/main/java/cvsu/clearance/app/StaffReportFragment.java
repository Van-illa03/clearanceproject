package cvsu.clearance.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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


public class StaffReportFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button generateReport, viewReport;
    private long mLastClickTime = 0;
    String StaffStation;

    DBHelper DB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragview = inflater.inflate(R.layout.fragment_staffreportfragment, container, false);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        generateReport = fragview.findViewById(R.id.generateReportBtn);
        viewReport = fragview.findViewById(R.id.viewDataBtn);
        DBHelper DB = new DBHelper(getActivity().getApplicationContext());


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
                                String StudentNumber = documentSnapshot.get("StudentNumber").toString();
                                String Name = documentSnapshot.get("Name").toString();
                                String Course = documentSnapshot.get("Course").toString();
                                String RequirementName = documentSnapshot.get("RequirementName").toString();
                                String Status = documentSnapshot.get("Status").toString();
                                String Type = documentSnapshot.get("Type").toString();
                                String Timestamp = documentSnapshot.get("Timestamp").toString();

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
                });
            }
        });

        viewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = DB.getData();

                if(cursor.getCount()==0){
                    Toast.makeText(getActivity().getApplicationContext(), "No data is saved", Toast.LENGTH_SHORT).show();
                }

                StringBuffer buffer = new StringBuffer();
                while(cursor.moveToNext()){
                    buffer.append("ID: "+cursor.getString(0)+"\n");
                    buffer.append("StudentNumber: "+cursor.getString(1)+"\n");
                    buffer.append("Name: "+cursor.getString(2)+"\n");
                    buffer.append("Course: "+cursor.getString(3)+"\n");
                    buffer.append("RequirementName: "+cursor.getString(4)+"\n");
                    buffer.append("Status: "+cursor.getString(5)+"\n");
                    buffer.append("Type: "+cursor.getString(6)+"\n");
                    buffer.append("Timestamp: "+cursor.getString(7)+"\n\n");
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setCancelable(true);
                alert.setTitle("Data");
                alert.setMessage(buffer.toString());
                alert.show();

            }
        });





        return fragview;
    }


}