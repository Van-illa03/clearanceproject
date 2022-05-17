package cvsu.clearance.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

public class StudentProfileFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button logoutButton;
    Activity currentActivity = this.getActivity();
    TextView StationLocation, ReqDescription;
    Spinner spin, reqspin;
    private String CurrentStation, CurrentRequirement;
    private int [] thirdcounter = new int[1], firstcounter = new int[1];
    private int secondcounter, fourthcounter;
    private String [] Stations, Requirements;
    CollectionReference stationcollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragview = inflater.inflate(R.layout.studentprofilefragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();
        logoutButton = fragview.findViewById(R.id.logoutButton);
        TextView User = (TextView) fragview.findViewById(R.id.WelcomeStudent);
        TextView DisplayEmail = fragview.findViewById(R.id.DisplayEmail);
        TextView DisplayStdNo = fragview.findViewById(R.id.DisplayStdNo);
        TextView DisplayCourse = fragview.findViewById(R.id.DisplayCourse);
        StationLocation = fragview.findViewById(R.id.STPStationLocationText);
        ReqDescription = fragview.findViewById(R.id.STPReqDescriptionText);

        String[] languages = getResources().getStringArray(R.array.roles);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(currentActivity, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }
        else {
            User.setText(""+mUser.getDisplayName());

        }

        DocumentReference docRef = mStore.collection("Students").document(mUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());

                        String DocuEmail = (String) document.get("Email");
                        DisplayEmail.setText(DocuEmail);
                        String DocuStdNo = (String) document.get("StdNo");
                        DisplayStdNo.setText(DocuStdNo);
                        String DocuCourse = (String) document.get("Course");
                        DisplayCourse.setText(DocuCourse);
                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });

        spin = (Spinner) fragview.findViewById(R.id.STPStationNameSpinner);

        //listener whenever there is an item change in listener
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CurrentStation = Stations[position];

                //gets the signing station that was shown in the spinner and displays necessary details
                mStore.collection("SigningStation").document(CurrentStation).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){

                                        String StationNameCatch = document.getString("Signing_Station_Name");
                                        String StationLocationCatch = document.getString("Location");

                                        if (StationNameCatch != null) {
                                            StationLocation.setText(StationLocationCatch);

                                        }
                                    }
                                }

                                //the purpose of this collection call is to count the requirements inside this station
                                mStore.collection("Students").document(mUser.getUid()).collection("Stations").document(CurrentStation).collection("Requirements").get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                thirdcounter[0] = 0;
                                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                                                    String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                                                    if (RequirementsNameCatch != null) {
                                                        thirdcounter[0] = thirdcounter[0] + 1;
                                                        Log.d("Third Counter", " "+thirdcounter[0]);
                                                    }
                                                }
                                                if (thirdcounter[0] != 0){
                                                    //if the counter is not equal to 0, it means there are existing requirements for the station
                                                    RequirementsSpinner(CurrentStation, thirdcounter[0],getContext(), fragview);
                                                }
                                                else {
                                                    //if the counter is zero, it means that the current signing station has no requirements.
                                                    //hence, we pass "None" to be inputted in the spinner, and thirdcounter[0] + 1
                                                    //in which its value is only 1. This will be used as a parameter in the array that will store the None string.
                                                    RequirementsSpinner("None", thirdcounter[0]+1,getContext(), fragview);
                                                }

                                            }
                                        });

                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        stationcollection =  mStore.collection("Students").document(mUser.getUid()).collection("Stations");


        // this method counts the number of fetched signing station from
        // firestore, the value will be used as the size of the array that will
        // contain the signing station names
        stationcollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);

                            String StationNameCatch = catchStationDetails.getSigning_Station_Name();
                            if (StationNameCatch != null) {
                                firstcounter[0] = firstcounter[0] + 1;
                            }
                        }
                    }
                });

        //the signing station names will be passed in the array through the "catchStation Details" object
        stationcollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Stations = new String [firstcounter[0]];

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);
                            String StationNameCatch = catchStationDetails.getSigning_Station_Name();
                            if (StationNameCatch != null) {
                                Stations[secondcounter] = StationNameCatch;
                                secondcounter++;
                            }
                        }
                        ArrayAdapter AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, Stations);
                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        //Setting the ArrayAdapter data on the Spinner
                        spin.setAdapter(AA);
                    }
                });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), FrontScreen.class));


            }
        });
        return fragview;
    }

    public void RequirementsSpinner (String StationName, int RowCount, Context ctx , View view) {
        if (StationName.equals("None")){
            reqspin = (Spinner) view.findViewById(R.id.RequirementsSpinner);
            Requirements = new String [RowCount];
            Requirements[0] = StationName;

            ArrayAdapter RAA = new ArrayAdapter (ctx, android.R.layout.simple_spinner_item, Requirements);
            RAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Setting the ArrayAdapter data on the Spinner
            reqspin.setAdapter(RAA);
        } else {
            reqspin = (Spinner) view.findViewById(R.id.STPRequirementsSpinner);
            Requirements = new String [RowCount];
            fourthcounter = 0;

            mStore.collection("Students").document(mUser.getUid()).collection("Stations").document(CurrentStation).collection("Requirements").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            Log.d("REQUIREMENTS SPINNER"," CALLED");

                            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);
                                String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();

                                Requirements[fourthcounter] = RequirementsNameCatch;
                                Log.d("NOTICE","" + Requirements[fourthcounter]);
                                fourthcounter++;

                            }

                            for (int i = 0; i < RowCount; i++){
                                Log.d("ROW COUNT"," " + RowCount);
                                Log.d("REQUIREMENTS"," " + i + Requirements[i]);
                            }

                            ArrayAdapter RAA = new ArrayAdapter (ctx, android.R.layout.simple_spinner_item, Requirements);
                            RAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //Setting the ArrayAdapter data on the Spinner
                            reqspin.setAdapter(RAA);

                            reqspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (Requirements[position] != "None"){
                                        CurrentRequirement = Requirements[position];

                                        mStore.collection("SigningStation").document(CurrentStation).collection("Requirements").document(CurrentRequirement).get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                                                        String RequirementDescCatch = catchRequirementsDetails.getDescription();

                                                        ReqDescription.setText(RequirementDescCatch);
                                                    }
                                                });
                                    }

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    });
        }

    }


}
