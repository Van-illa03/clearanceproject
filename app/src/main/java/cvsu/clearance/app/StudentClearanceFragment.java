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

import com.bumptech.glide.Glide;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.List;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class StudentClearanceFragment extends Fragment{
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
    SwipeRefreshLayout refreshLayout;

    int subtract = 0;

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
        refreshLayout = view.findViewById(R.id.refreshLayout);
        StationNames = new ArrayList<>();



        String[] languages = getResources().getStringArray(R.array.roles);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(currentActivity, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }

       PassStations();



            QRButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DisplayQRDialog();
                }
            });

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Reload current fragment
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft =  fm.beginTransaction();
                    StudentClearanceFragment scf = new StudentClearanceFragment();
                    ft.replace(R.id.frag_container_student, scf);
                    ft.commit();

                    if (refreshLayout.isRefreshing()){
                        refreshLayout.setRefreshing(false);
                    }
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
                                            }
                                        }

                                    }
                                });
                    }
                });
    }
}



