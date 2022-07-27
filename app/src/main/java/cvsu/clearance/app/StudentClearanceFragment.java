package cvsu.clearance.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class StudentClearanceFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Activity currentActivity = this.getActivity();

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private ImageButton QRButton;
    private ImageView QRImage;
    StorageReference mStorageRef;
    RecyclerView StationList;
    List<String> StationNames;
    Adapter adapter;
    Context thiscontext;
    SwipeRefreshLayout mSwipeRefreshLayout;
    int subtract = 0;
    int reportDocuCounterAdmin = 1, reportDocuCounterBackupAdmin = 1;
    List<String> checker = new ArrayList<>();
    List<String> checkExistence = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.studentclearancefragment, container, false);
        thiscontext = container.getContext();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        QRButton = view.findViewById(R.id.ShowQRButton);
        mStorageRef = FirebaseStorage.getInstance().getReference("QRCodes");
        StationList = view.findViewById(R.id.StationList);

        StationNames = new ArrayList<>();

        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container_studentClearance);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(currentActivity, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }
        /*requirementsCheck();*/
        PassStations();
        reportDocuCounter();
        checkerMethod();
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

            }
        });







            QRButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DisplayQRDialog();
                }
            });




        return view;
    }


    public void DisplayQRDialog (){
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View DialogView = getLayoutInflater().inflate(R.layout.showqrfragment,null);
        QRImage = (ImageView) DialogView.findViewById(R.id.QRimage);


                            StorageReference fileReference = mStorageRef.child(mUser.getUid()
                                    + "_QR_CODE.png");

                            GlideApp.with(getContext())
                                    .load(fileReference)
                                    .into(QRImage);


        dialogBuilder.setView(DialogView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private ArrayList<String> Signatures = new ArrayList<>();
    private ArrayList<String> SignaturePassing = new ArrayList<>();


    public void PassStations(){
        mStore.collection("SigningStation").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int ctr = 0, ctr2 = 0;
                        String StationNameCatch;

                        //counting all the existing stations
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStationDetails catchStation = documentSnapshot.toObject(CatchStationDetails.class);
                            StationNameCatch = catchStation.getSigning_Station_Name();

                            //filtering the StationCount Document
                            if (StationNameCatch != null){
                                ctr++;
                            }
                        }


                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStationDetails catchStation = documentSnapshot.toObject(CatchStationDetails.class);
                            StationNameCatch = catchStation.getSigning_Station_Name();

                            //filtering the StationCount Document
                            if (StationNameCatch != null){
                                if (ctr2 < ctr){
                                    Signatures.add(StationNameCatch);
                                    Log.d("CTR count",""+ctr2 + "Station:" + StationNameCatch);
                                    ctr2++;
                                }
                            }
                        }

                        mStore.collection("StationCount").document("StationCount").get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()){

                                                for (int i = 0; i < 15; i++){

                                                    for (int j = 0; j < 15; j++){
                                                        Log.d("NOTICE",i +" "+ j + "Signatures: "+Signatures.get(j)+" Docu: " + document.getString("slot_"+(i+1)));
                                                        if (document.getString("slot_"+(i+1)).equals("") || document.getString("slot_"+(i+1)).equals("empty")){
                                                            subtract++;
                                                            break;
                                                        }
                                                        else if (Signatures.get(j).equals(document.getString("slot_"+(i+1)))) {
                                                            StationNames.add(Signatures.get(j));
                                                            SignaturePassing.add(i-subtract,Signatures.get(j));
                                                            break;
                                                        }
                                                    }
                                                }
                                                //passing the array
                                                adapter = new Adapter(thiscontext,StationNames,SignaturePassing);
                                                GridLayoutManager gridLayoutManager = new GridLayoutManager(thiscontext,2,GridLayoutManager.VERTICAL,false);
                                                StationList.setAdapter(adapter);
                                                StationList.setLayoutManager(gridLayoutManager);

                                                mSwipeRefreshLayout.setRefreshing(false);
                                            }
                                        }

                                    }
                                });
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
                TimeZone timeZone = TimeZone.getDefault();
                Calendar cal = Calendar.getInstance(timeZone);
                Date c = Calendar.getInstance(timeZone).getTime();

                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss", Locale.US);
                df.setTimeZone(timeZone);
                tf.setTimeZone(timeZone);

                String formattedDate = df.format(c);
                String formattedTime = tf.format(c);
                long rawTime = c.getTime();


                //putting report data to HashMap
                Map<String,Object> insertReportDetailsAdmin = new HashMap<>();
                insertReportDetailsAdmin.put("ID", reportDocuCounterAdmin);
                insertReportDetailsAdmin.put("StudentNumber", task.getResult().get("StdNo").toString());
                insertReportDetailsAdmin.put("Name", task.getResult().get("Name").toString());
                insertReportDetailsAdmin.put("Course", task.getResult().get("Course").toString());
                insertReportDetailsAdmin.put("Status", "Complete");
                insertReportDetailsAdmin.put("Date", formattedDate);
                insertReportDetailsAdmin.put("Time", formattedTime);
                insertReportDetailsAdmin.put("RawTime", rawTime);

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
                                }

                            }


                        });


            }
        });
    }

    private void checkerMethod() {

        mStore.collection("Students").document(mUser.getUid()).collection("Stations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                            if (documentSnapshot.get("Status").equals("Not-Signed")){
                                checker.add("incomplete");
                                Log.d("INCOMPLETE: ", mUser.getUid());
                                break;
                            }
                        }

                        if(checker.size()!=0){
                            checker.clear();
                        }
                        else{
                            savingMethod(mUser.getUid());
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Report: ","Insertion of report data failed. Empty data");
                    }
                });
    }


    @Override
    public void onRefresh() {
        // Reload current fragment
        reportDocuCounterAdmin = 1;
        reportDocuCounter();
        checkerMethod();


        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        StudentClearanceFragment srf = new StudentClearanceFragment();
        ft.replace(R.id.frag_container_student, srf);
        ft.commit();
    }
}



