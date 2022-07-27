package cvsu.clearance.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class StaffScanQRFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private FirebaseFirestore mStore;
    private Button scanBtn, signBtn, updateBtn;
    private ProgressBar progressBar;
    private Uri mImageUri;
    Activity currentActivity = this.getActivity();
    TextView StudentNameText, StudentCourseText;
    String scannedResults, scannedResultsBackup;
    private long mLastClickTime = 0;
    private String CurrentRequirement;
    CollectionReference ReqCollection;
    private String StaffStation;
    private TextView PendingReqDesc, StudNo, StudentStatus;
    ArrayAdapter AA;
    Spinner spin;
    int reportDocuCounter = 1, reportDocuCounterBackup = 1;
    int docuExist = 0, CurrentRequirementPosition=0;
    private ArrayList<String> RequirementsAlternative = new ArrayList<>();





    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {

                if (result.getContents() == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    reportDocuCounter = reportDocuCounterBackup;
                    scannedResults = scannedResultsBackup;
                } else {

                    scannedResults = result.getContents();
                    scannedResultsBackup = scannedResults;
                    signBtn.setClickable(true);
                    signBtn.getBackground().setAlpha(255);
                        Log.d("SCANNED: ", scannedResults);
                        docuExist=0;
                            mStore = FirebaseFirestore.getInstance();
                            mStore.collection("Students").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                         String studID = documentSnapshot.getId();
                                        if(studID.equals(scannedResults)){
                                            docuExist++;
                                            reportDocuCounter = 1;
                                            setText(scannedResults);
                                            reportDocuCounter();
                                            break;
                                        }
                                    }

                                    if(docuExist!=1){
                                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                        alert.setTitle(Html.fromHtml("<font color='#E84A5F'>User doesn't exists.</font>"))
                                                .setMessage("Please check the QR Code and try again later.")
                                                .setPositiveButton("OK", null);
                                        alert.show();
                                    }
                                }
                            });



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
        StudentNameText = fragview.findViewById(R.id.StudentNameText);
        StudentCourseText = fragview.findViewById(R.id.StudentCourseText);
        StudentStatus = fragview.findViewById(R.id.StudentStatus);
        String[] languages = getResources().getStringArray(R.array.roles);
        StudNo = fragview.findViewById(R.id.DisplayStdUID);
        PendingReqDesc = fragview.findViewById(R.id.PendingReqDescriptionText);
        signBtn = fragview.findViewById(R.id.SignButton);
        updateBtn = fragview.findViewById(R.id.UpdateButton);
        spin = (Spinner) fragview.findViewById(R.id.PendingRequirementsSpinner);





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

        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                reportDocuCounter = 1;
                reportDocuCounter();

                String StudentStatusText = StudentStatus.getText().toString().trim();

                if(scannedResults==null){
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Invalid Request</font>"))
                            .setMessage("Please scan a qr code first before proceeding.")
                            .setPositiveButton("OK", null);
                    alert.show();
                }
                else if(StudentStatusText.equals("Signed")){
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Invalid Request</font>"))
                            .setMessage("Student is already signed. Please check again.")
                            .setPositiveButton("OK", null);
                    alert.show();
                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Confirm signing user " + StudentNameText.getText().toString() +"?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Map<String,Object> updateReqInfo = new HashMap<>();
                                    updateReqInfo.put("Status", "Signed");
                                    mStore.collection("Students").document(scannedResults).collection("Stations").document(StaffStation).update(updateReqInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {


                                            String studNo = StudNo.getText().toString();
                                            String studName = StudentNameText.getText().toString();
                                            String studCourse = StudentCourseText.getText().toString();

                                            TimeZone timeZone = TimeZone.getDefault();
                                            Calendar cal = Calendar.getInstance(timeZone);
                                            Date c = cal.getTime();

                                            SimpleDateFormat formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                                            SimpleDateFormat formattedTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
                                            long RawTime = c.getTime();
                                            formattedDate.setTimeZone(timeZone);
                                            formattedTime.setTimeZone(timeZone);


                                            Map<String,Object> insertReportDetails = new HashMap<>();
                                            insertReportDetails.put("ID", reportDocuCounter);
                                            insertReportDetails.put("StudentNumber", studNo);
                                            insertReportDetails.put("Name", studName);
                                            insertReportDetails.put("Course", studCourse);
                                            insertReportDetails.put("RequirementName", CurrentRequirement);
                                            insertReportDetails.put("Status", "Complete");
                                            insertReportDetails.put("Type", "Sign");
                                            insertReportDetails.put("Date", formattedDate.format(c));
                                            insertReportDetails.put("Time", formattedTime.format(c));
                                            insertReportDetails.put("RawTime", RawTime);



                                            mStore.collection("SigningStation").document(StaffStation).collection("Report").document(String.valueOf(reportDocuCounter)).set(insertReportDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    StudentStatus.setText(Html.fromHtml("<font color='#20BF55'>Signed</font>"));
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                                    alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                                    alert.setMessage(StudentNameText.getText().toString()+"'s clearance form is signed");
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
                                                    Toast.makeText(getActivity().getApplicationContext(), "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }

                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity().getApplicationContext(), "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
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

            }
        });



        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                reportDocuCounter = 1;
                reportDocuCounter();
                if(scannedResults==null){
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Invalid Request</font>"))
                            .setMessage("Please scan a qr code first before proceeding.")
                            .setPositiveButton("OK", null);
                    alert.show();
                }
                else{
                    if(CurrentRequirement.equals("None")){
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Invalid Request</font>"))
                                .setMessage("There is no current pending requirement selected")
                                .setPositiveButton("OK", null);
                        alert.show();
                    }
                    else{
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Confirm update " + CurrentRequirement +"?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Map<String,Object> updateReqInfo = new HashMap<>();
                                        updateReqInfo.put("Status", "Complete");
                                        mStore.collection("Students").document(scannedResults).collection("Stations").document(StaffStation).collection("Requirements").document(CurrentRequirement).update(updateReqInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                String studNo = StudNo.getText().toString();
                                                String studName = StudentNameText.getText().toString();
                                                String studCourse = StudentCourseText.getText().toString();

                                                TimeZone timeZone = TimeZone.getDefault();
                                                Calendar cal = Calendar.getInstance(timeZone);
                                                Date c = cal.getTime();

                                                SimpleDateFormat formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                                                SimpleDateFormat formattedTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
                                                long RawTime = c.getTime();
                                                formattedDate.setTimeZone(timeZone);
                                                formattedTime.setTimeZone(timeZone);


                                                Map<String,Object> insertReportDetails = new HashMap<>();
                                                insertReportDetails.put("ID", reportDocuCounter);
                                                insertReportDetails.put("StudentNumber", studNo);
                                                insertReportDetails.put("Name", studName);
                                                insertReportDetails.put("Course", studCourse);
                                                insertReportDetails.put("RequirementName", CurrentRequirement);
                                                insertReportDetails.put("Status", "Complete");
                                                insertReportDetails.put("Type", "Update");
                                                insertReportDetails.put("Date", formattedDate.format(c));
                                                insertReportDetails.put("Time", formattedTime.format(c));
                                                insertReportDetails.put("RawTime", RawTime);

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
                                                        RequirementsAlternative.remove(CurrentRequirementPosition);
                                                        if(RequirementsAlternative.size()==0){
                                                            RequirementsAlternative.add("None");
                                                            PendingReqDesc.setText("-");
                                                            signBtn.setClickable(true);
                                                            signBtn.getBackground().setAlpha(255);

                                                        }
                                                        AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, RequirementsAlternative);
                                                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                        //Setting the ArrayAdapter data on the Spinner
                                                        spin.setAdapter(AA);

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity().getApplicationContext(), "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }

                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getActivity().getApplicationContext(), "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
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

                }


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
                        RequirementsAlternative.clear();

                        ReqCollection = mStore.collection("Students").document(scannedResults).collection("Stations").document(StaffStation).collection("Requirements");


                        ReqCollection.get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                if(documentSnapshot.exists()){
                                                    CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);
                                                    String StationNameCatch = catchRequirementsDetails.getRequirementsName();
                                                    if (StationNameCatch != null) {
                                                        String status = documentSnapshot.get("Status").toString();
                                                        if(status.equals("Incomplete")){
                                                            RequirementsAlternative.add(StationNameCatch);
                                                        }
                                                    }
                                                }
                                            }
                                            if(RequirementsAlternative.size()==0){
                                                RequirementsAlternative.add("None");
                                            }

                                            AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, RequirementsAlternative);
                                            AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            //Setting the ArrayAdapter data on the Spinner
                                            spin.setAdapter(AA);

                                    }
                                });


                    }else {
                        Toast.makeText(getActivity().getApplicationContext(), "NULL value", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        CurrentRequirement = RequirementsAlternative.get(position);
                        CurrentRequirementPosition = position;
                        if(CurrentRequirement.equals("None")){
                            signBtn.setClickable(true);
                            signBtn.getBackground().setAlpha(255);
                        }
                        else{
                            signBtn.setClickable(false);
                            signBtn.getBackground().setAlpha(128);
                        }


                        mStore.collection("Students").document(scannedResults).collection("Stations").document(StaffStation).collection("Requirements").document(CurrentRequirement).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){
                                            CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

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



        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withContext(getActivity().getApplicationContext())
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                scannedResults = null;
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
                            @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Permission DENIED</font>"));
                                alert.setCancelable(false);
                                alert.setMessage("Access to camera is required for system's certain functions to work.");
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
                            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();

                            }
                        }).check();






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

                        String DocuStudentName = document.get("Name").toString();
                        StudentNameText.setText(DocuStudentName);
                        String DocuStudentCourse = document.get("Course").toString();
                        StudentCourseText.setText(DocuStudentCourse);
                        String DocuStudentNumber = document.get("StdNo").toString();
                        StudNo.setText(DocuStudentNumber);

                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }

            }
        });
        mStore.collection("Students").document(UID).collection("Stations").document(StaffStation).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String status = documentSnapshot.get("Status").toString();
                    if(status.equals("Signed")){
                        StudentStatus.setText(Html.fromHtml("<font color='#20BF55'>"+status+"</font>"));
                        signBtn.setClickable(false);
                        signBtn.getBackground().setAlpha(128);
                    }
                    else{
                        StudentStatus.setText(Html.fromHtml("<font color='#E84A5F'>"+status+"</font>"));
                    }

                }
                else{
                    String status = "Failed to Retrieve data";
                    StudentStatus.setText(status);
                }
            }
        });

    }

    public void reportDocuCounter(){
        mStore.collection("SigningStation").document(StaffStation).collection("Report").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    if(documentSnapshot.exists()){
                        reportDocuCounter++;
                        reportDocuCounterBackup = reportDocuCounter;
                    }
                }
            }
        });
    }



}