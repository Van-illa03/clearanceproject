package cvsu.clearance.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class StaffScanQRFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private FirebaseFirestore mStore;
    private Button scanBtn;
    private ProgressBar progressBar;
    private Uri mImageUri;
    Activity currentActivity = this.getActivity();
    TextView StudentNameText, StudentCourseText;
    String scannedResults;
    private long mLastClickTime = 0;
    private String CurrentRequirement;
    CollectionReference ReqCollection;
    private String StaffStation;
    private int [] firstcounter;
    private int secondcounter;
    private int [] thirdcounter;
    private String [] Requirements;
    private TextView StudNo, PendingReqDesc;




    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {

                if (result.getContents() == null) {
                    //no arg
                } else {
                    scannedResults = result.getContents();
                    setText(scannedResults);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragview = inflater.inflate(R.layout.staffscanqrfragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        scanBtn = fragview.findViewById(R.id.scanBtn);
        progressBar = fragview.findViewById(R.id.progressBar);
        mStore  =   FirebaseFirestore.getInstance();
        TextView User = (TextView) fragview.findViewById(R.id.WelcomeStaff);
        StudentNameText = fragview.findViewById(R.id.StudentNameText);
        StudentCourseText = fragview.findViewById(R.id.StudentCourseText);
        String[] languages = getResources().getStringArray(R.array.roles);
        StudNo = fragview.findViewById(R.id.DisplayStdUID);
        PendingReqDesc = fragview.findViewById(R.id.PendingReqDescriptionText);





        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginScreen.class));

        }

        Spinner spin = (Spinner) fragview.findViewById(R.id.PendingRequirementsSpinner);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CurrentRequirement = Requirements[position];

                mStore.collection("Students").document(scannedResults).collection(StaffStation).document(CurrentRequirement).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                                PendingReqDesc.setText(catchRequirementsDetails.getDescription());

                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                // Journeyapps library that utilizes the ZXing library
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                options.setPrompt("Scan a QR Code");
                options.setCameraId(0);  // Use a specific camera of the device
                options.setBeepEnabled(false);
                options.setOrientationLocked(false);
                barcodeLauncher.launch(new ScanOptions());

                // this method counts the number of fetched signing station from
                // firestore, the value will be used as the size of the array that will
                // contain the signing station names
                ReqCollection.get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                                    String StationNameCatch = catchRequirementsDetails.getSigningStation();
                                    if (StationNameCatch != null) {
                                        firstcounter[0] = firstcounter[0] + 1;
                                    }
                                }
                            }
                        });

                //the signing station names will be passed in the array through the "catchStation Details" object
                ReqCollection.get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (firstcounter[0] == 0){
                                    Requirements = new String[1];
                                    Requirements[0] = "None";
                                }
                                else {
                                    Requirements = new String [firstcounter[0]];

                                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);
                                        String StationNameCatch = catchStationDetails.getSigning_Station_Name();
                                        if (StationNameCatch != null) {
                                            Requirements[secondcounter] = StationNameCatch;
                                            secondcounter++;
                                        }
                                    }
                                    ArrayAdapter AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, Requirements);
                                    AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    //Setting the ArrayAdapter data on the Spinner
                                    spin.setAdapter(AA);
                                }
                            }
                        });
            }
        });


        return fragview;
    }

    public void setText(String UID){
        DocumentReference scannedDocument = mStore.collection("Students").document(UID);
        scannedDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isComplete()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());

                        String DocuStudentName = (String) document.get("Name");
                        StudentNameText.setText(DocuStudentName);
                        String DocuStudentCourse = (String) document.get("Course");
                        StudentCourseText.setText(DocuStudentCourse);
                        StudNo.setText(scannedResults);

                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }

                mStore.collection("Staff").document(mUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()){
                                        StaffStation = document.getString("Station");
                                        ReqCollection = mStore.collection("Students").document(UID).collection(StaffStation);
                                    }
                                }
                            }
                        });
            }
        });

    }


}
