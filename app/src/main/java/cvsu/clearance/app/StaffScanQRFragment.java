package cvsu.clearance.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StaffScanQRFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private FirebaseFirestore mStore;
    private Button scanBtn, signBtn, updateBtn;
    private ProgressBar progressBar;
    private Uri mImageUri;
    Activity currentActivity = this.getActivity();
    TextView StudentNameText, StudentCourseText;
    String scannedResults, copyScannedResults;
    private long mLastClickTime = 0;
    private String CurrentRequirement;
    CollectionReference ReqCollection;
    private String StaffStation;
    private int [] firstcounter = new int[1];
    private int secondcounter;
    private String [] Requirements;
    private TextView PendingReqDesc;
    private TextView StudNo;
    ArrayAdapter AA;
    int reportDocuCounter = 1;






    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {

                if (result.getContents() == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
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
        signBtn = fragview.findViewById(R.id.SignButton);
        updateBtn = fragview.findViewById(R.id.UpdateButton);




        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getContext(), LoginScreen.class));

        }



        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Confirm update " + CurrentRequirement +"?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String,Object> updateReqInfo = new HashMap<>();
                                updateReqInfo.put("Status", "Complete");
                                mStore.collection("Students").document(scannedResults).collection("Stations").document(StaffStation).collection("Requirements").document(CurrentRequirement).update(updateReqInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity().getApplicationContext(), "GG error", Toast.LENGTH_LONG).show();
                                    }
                                });


                                mStore.collection("SigningStation").document(StaffStation).collection("Report").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            if(documentSnapshot.exists()){
                                                reportDocuCounter++;
                                            }
                                        }
                                    }
                                });

                                String studNo = StudNo.getText().toString();
                                String studName = StudentNameText.getText().toString();
                                String studCourse = StudentCourseText.getText().toString();
                                Date currentTime = Calendar.getInstance().getTime();
                                String currentTimeString = currentTime.toString();

                                Map<String,Object> insertReportDetails = new HashMap<>();
                                insertReportDetails.put("StudentNumber", studNo);
                                insertReportDetails.put("Name", studName);
                                insertReportDetails.put("Course", studCourse);
                                insertReportDetails.put("Year&Section", "1-3");
                                insertReportDetails.put("RequirementName", CurrentRequirement);
                                insertReportDetails.put("Status", "Complete");
                                insertReportDetails.put("Type", "Update");
                                insertReportDetails.put("Timestamp", currentTimeString);

                                mStore.collection("SigningStation").document(StaffStation).collection("Report").document(String.valueOf(reportDocuCounter)).set(insertReportDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                        alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                        alert.setMessage(CurrentRequirement+" has been updated");
                                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        alert.show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity().getApplicationContext(), "GG error", Toast.LENGTH_LONG).show();
                                    }
                                });




                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                            }
                        });
                alert.show();

            }
        });

            Spinner spin = (Spinner) fragview.findViewById(R.id.PendingRequirementsSpinner);
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    CurrentRequirement = Requirements[position];


                                            mStore.collection("Students").document(scannedResults).collection("Stations").document(StaffStation).collection("Requirements").document(CurrentRequirement).get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            if(documentSnapshot.exists()){
                                                            CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                                                                assert catchRequirementsDetails != null;
                                                                if(catchRequirementsDetails.getDescription() != null){
                                                                    PendingReqDesc.setText(catchRequirementsDetails.getDescription());
                                                                }


                                                            }
                                                        }
                                                    });



                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            StudNo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {


                    if(scannedResults != null)  {

                        mStore.collection("Staff").document(mUser.getUid()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()){
                                                StaffStation = document.getString("Station");

                                                ReqCollection = mStore.collection("Students").document(scannedResults).collection("Stations").document(StaffStation).collection("Requirements");

                                                // this method counts the number of fetched signing station from
                                                // firestore, the value will be used as the size of the array that will
                                                // contain the signing station names
                                                ReqCollection.get()
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
                                                ReqCollection.get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                if (firstcounter[0] == 0){
                                                                    Requirements = new String[1];
                                                                    Requirements[0] = "None";

                                                                    AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, Requirements);
                                                                    AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                                    //Setting the ArrayAdapter data on the Spinner
                                                                    spin.setAdapter(AA);

                                                                }
                                                                else {
                                                                    Requirements = new String [firstcounter[0]];

                                                                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                                        CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);
                                                                        String StationNameCatch = catchRequirementsDetails.getRequirementsName();
                                                                        if (StationNameCatch != null) {
                                                                            Requirements[secondcounter] = StationNameCatch;
                                                                            secondcounter++;
                                                                        }
                                                                    }
                                                                    AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, Requirements);
                                                                    AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                                    //Setting the ArrayAdapter data on the Spinner
                                                                    spin.setAdapter(AA);
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    }
                                });



                    }else {
                        Toast.makeText(getActivity().getApplicationContext(), "NULL value", Toast.LENGTH_SHORT).show();
                    }
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
                        String DocuStudentNumber = (String) document.get("StdNo");
                        StudNo.setText(DocuStudentNumber);

                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }

            }
        });

    }


}