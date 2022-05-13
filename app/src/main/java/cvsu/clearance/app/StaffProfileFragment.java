package cvsu.clearance.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class StaffProfileFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button logoutButton;
    Activity currentActivity = this.getActivity();
    private StorageReference mStorageRef;
    private long mLastClickTime = 0;
    TextView StationName,StationLocation, StationNumber,RequirementDescription;
    CollectionReference reqcollection;
    int [] firstcounter = new int[1];
    int secondcounter = 0;
    String [] Requirements;
    String CurrentRequirement;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.staffprofilefragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        logoutButton = view.findViewById(R.id.logoutButton);
        mStore  =   FirebaseFirestore.getInstance();
        TextView User = (TextView) view.findViewById(R.id.WelcomeStaff);
        TextView DisplayEmail = view.findViewById(R.id.DisplayEmailStaff);
        TextView DisplayStation = view.findViewById(R.id.DisplayStationStaff);
        String[] languages = getResources().getStringArray(R.array.roles);
        StationName = view.findViewById(R.id.SPStationNameText);
        StationLocation = view.findViewById(R.id.SPStationLocationText);
        StationNumber = view.findViewById(R.id.SPStationNumberText);
        RequirementDescription = view.findViewById(R.id.SPReqDescriptionText);

        Spinner spin = (Spinner) view.findViewById(R.id.SPRequirementsSpinner);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(currentActivity  , "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }
        else {
            User.setText(""+mUser.getDisplayName());

        }

        DocumentReference docRef = mStore.collection("Staff").document(mUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());

                        String DocuEmail = (String) document.get("Email");
                        DisplayEmail.setText(DocuEmail);
                        String DocuStation = (String) document.get("Station");
                        DisplayStation.setText(DocuStation);

                        mStore.collection("SigningStation").document(DocuStation).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()){
                                                Double StatNum = new Double (document.getDouble("StationNumber"));
                                                int StatNumParsed = StatNum.intValue();
                                                String StatName = document.getString("Signing_Station_Name");

                                                StationName.setText(document.getString("Signing_Station_Name"));
                                                StationLocation.setText(document.getString("Location"));
                                                StationNumber.setText(Integer.toString(StatNumParsed));
                                                reqcollection = mStore.collection("SigningStation").document(StatName).collection("Requirements");

                                                // this method counts the number of fetched requirements from
                                                // firestore, the value will be used as the size of the array that will
                                                // contain the requirement names
                                                reqcollection.get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                                    CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);
                                                                    String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                                                                    if (RequirementsNameCatch != null) {
                                                                        firstcounter[0] = firstcounter[0] + 1;
                                                                    }
                                                                }
                                                            }
                                                        });

                                                //the signing station names will be passed in the array through the "catchStation Details" object
                                                reqcollection.get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                Requirements = new String [firstcounter[0]];

                                                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                                    CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);
                                                                    String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                                                                    if (RequirementsNameCatch != null) {
                                                                        Requirements[secondcounter] = RequirementsNameCatch;
                                                                        secondcounter++;
                                                                    }
                                                                }
                                                                ArrayAdapter AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, Requirements);
                                                                AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                                //Setting the ArrayAdapter data on the Spinner
                                                                spin.setAdapter(AA);
                                                            }
                                                        });


                                            }
                                        }
                                    }
                                });

                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CurrentRequirement = Requirements[position];

                reqcollection.document(CurrentRequirement).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        RequirementDescription.setText(document.getString("Description"));
                                    }
                                }
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), FrontScreen.class));


            }
        });
        return view;
    }
}
