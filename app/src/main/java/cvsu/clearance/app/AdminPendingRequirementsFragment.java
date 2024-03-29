package cvsu.clearance.app;

import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPendingRequirementsFragment extends Fragment implements AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    FirebaseStorage mStorage;
    Activity currentActivity = this.getActivity();
    private static final int PICK_CSV_REQUEST = 1;
    private long mLastClickTime = 0;
    CollectionReference reqcollection;
    public String[] ArrayRequirements;
    private int [] firstcounter = new int[2];
    public int secondcounter = 0;
    public String CurrentRequirement;
    TextView ReqDesignatedStation, ReqFileName;
    EditText ReqName,ReqLoc, ReqDescription;
    ImageButton chooseFile, downloadFile, deleteFile;
    Button VerifyButton, DenyButton;
    Uri mFileUri;
    CheckBox checkbox;
    Context adminContext =  AdminMainActivity.getContextOfApplicationadmin();
    StorageReference storageReference;
    StorageTask mUploadTask;
    ProgressDialog progressDialog;
    StorageReference mStorageRef;
    SwipeRefreshLayout mSwipeRefreshLayout;

    String StaffName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adminpendingrequirementsfragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        ReqName = view.findViewById(R.id.RequirementsNameText);
        ReqDescription = view.findViewById(R.id.RequirementsDescText);
        ReqLoc = view.findViewById(R.id.RequirementsLocationText);
        ReqDesignatedStation = view.findViewById(R.id.RequirementsDesignationText);
        reqcollection = mStore.collection("PendingRequirements");
        VerifyButton =(Button) view.findViewById(R.id.VerifyReqButton);
        DenyButton =(Button) view.findViewById(R.id.DenyReqButton);
        storageReference = mStorage.getReference();
        ReqFileName = view.findViewById(R.id.ListText_Pending);
        chooseFile = view.findViewById(R.id.chooseFileBtn_Pending);
        deleteFile = view.findViewById(R.id.deleteFileBtn_Pending);
        downloadFile = view.findViewById(R.id.downloadFileBtn_Pending);
        checkbox = view.findViewById(R.id.checkBox_Pending);
        mStorageRef = FirebaseStorage.getInstance().getReference("Requirements");

        if (mAuth.getCurrentUser() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Error</font>"));
            alert.setMessage("Please log in first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            startActivity(new Intent(getContext(), LoginScreen.class));
        }

        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container_adminRequirements);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        Spinner spin = view.findViewById(R.id.PendingRequirementsSpinner);
        spin.setOnItemSelectedListener(this);

        // this method counts the number of fetched pending requirements from
        // firestore, the value will be used as the size of the array that will
        // contain the pending requirements
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
        //the signing station names will be passed in the array through the "ArraRequirements" object
        reqcollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayRequirements = new String [firstcounter[0]];

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                            String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                            if (RequirementsNameCatch != null) {
                                    ArrayRequirements[secondcounter] = documentSnapshot.getId();
                                    secondcounter++;
                            }
                        }
                        ArrayAdapter AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, ArrayRequirements);
                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        //Setting the ArrayAdapter data on the Spinner
                        spin.setAdapter(AA);
                    }
                });

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkbox.isChecked()){
                    Snackbar.make(view, "It's MANDATORY to follow the CSV Format to avoid errors. Check the format by downloading: ", 6000)
                            .setAction("DOWNLOAD", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DLFileSample();
                                }
                            }).setActionTextColor(Color.GREEN)
                            .show();
                    enableList();
                }
                else{
                    disabledList();
                }
            }
        });


        downloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                DLFile();

            }
        });

        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                Dexter.withContext(getActivity())
                        .withPermissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).withListener(new MultiplePermissionsListener() {
                            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if(report.areAllPermissionsGranted()){
                                    //Toast.makeText(getApplicationContext(), "Permission GRANTED", Toast.LENGTH_LONG).show();
                                    String noFile = "No file sent.";
                                    if(ReqFileName.getText().toString().equals(noFile) || mFileUri!=null){
                                        openFileChooser();
                                    }
                                    else{
                                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                        alert.setTitle("Overwrite the existing data?");
                                        alert.setCancelable(false);
                                        alert.setMessage("Staff have already sent data containing the incomplete list.")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        openFileChooser();
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
                                else{
                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Permission DENIED</font>"));
                                    alert.setCancelable(false);
                                    alert.setMessage("Access to storage is required for system's certain functions to work.");
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
                            }
                            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();


            }
        });

        deleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                String fileName = ReqFileName.getText().toString().trim();

                if (!fileName.equals("No file sent.")) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Confirm Delete?</font>"));
                    alert.setCancelable(false);
                    alert.setMessage("You are deleting the file sent by the staff. List of Incomplete will be unchecked.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String station = ReqDesignatedStation.getText().toString().trim();
                                    String name = ReqName.getText().toString().trim();
                                    Map<String,Object> updates = new HashMap<>();
                                    updates.put("IncompleteFileURI", FieldValue.delete());

                                    StorageReference fileRef = mStorageRef.child(fileName);

                                    fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("DELETION", "The file in Firestore Storage has been deleted.");
                                        }
                                    });

                                    mStore.collection("PendingRequirements").document(station+"_"+name).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            String noFile = "No file sent.";
                                            checkbox.setChecked(false);
                                            checkbox.setClickable(true);
                                            ReqFileName.setText(noFile);
                                            downloadFile.setClickable(false);
                                            chooseFile.setClickable(false);
                                            deleteFile.setClickable(false);
                                            downloadFile.getBackground().setAlpha(128);
                                            deleteFile.getBackground().setAlpha(128);
                                            chooseFile.getBackground().setAlpha(128);

                                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                            alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Delete Successful</font>"));
                                            alert.setMessage("File has been deleted.")
                                                    .setPositiveButton("Ok", null);
                                            alert.show();
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
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "List is currently empty.", Toast.LENGTH_LONG).show();
                }
            }
        });



        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }

                if (CurrentRequirement == null){
                    Toast.makeText(getContext(),"No chosen pending requirement",Toast.LENGTH_SHORT).show();
                }
                else{
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Confirm Verify?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(checkbox.isChecked()){
                                        // If null, admin didn't overwrite the list of data that staff sent
                                        if(mFileUri==null){
                                            saveRequirementsInStudentsAndStation_Checked();
                                        }
                                        // If not null, admin inserted another CSV which will be
                                        // the basis of inserting requirements to students.
                                        else{
                                            // Performs reading csv and saving all the details based on the
                                            // Student number from CSV. This also includes saving data to the
                                            // Station that sent the requirements
                                            updatedCSVandSavingData_CheckedAndNew();

                                        }

                                    }
                                    // List of Incomplete is unchecked
                                    else{
                                        saveVerifiedInStationAndStudent_Unchecked();

                                    }
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


        DenyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (CurrentRequirement == null){
                    Toast.makeText(getContext(),"No chosen pending requirement",Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Confirm Delete?</font>"))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String deletingFile = ReqFileName.getText().toString();
                                    if (!deletingFile.equals("No file sent.")) {
                                        mStorage.getReference().child("Requirements/" + deletingFile).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("", "File successfully deleted");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("", "File doesn't exist");
                                            }
                                        });

                                    }
                                    mStore.collection("PendingRequirements").document(CurrentRequirement).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(getActivity().getApplicationContext(), "The requirement has been deleted.", Toast.LENGTH_LONG).show();

                                            // Reload current fragment
                                            FragmentManager fm = getActivity().getSupportFragmentManager();
                                            FragmentTransaction ft = fm.beginTransaction();
                                            AdminPendingRequirementsFragment aprf = new AdminPendingRequirementsFragment();
                                            ft.replace(R.id.frag_container, aprf);
                                            ft.commit();
                                        }


                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Denying failed. Please try again later.", Toast.LENGTH_LONG).show();
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


        return view;
    }

    private void DLFile() {
        mStore.collection("PendingRequirements").document(CurrentRequirement).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        CatchRequirementsDetails catchRequirementsDetails = document.toObject(CatchRequirementsDetails.class);
                        Map<String,Object> RequirementsFileCatch = catchRequirementsDetails.getIncompleteFileUri();

                        String fileUrl = RequirementsFileCatch.get("fileUrl").toString();

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
                        String title = URLUtil.guessFileName(fileUrl, null, null);
                        request.setTitle(title);
                        request.setDescription("Downloading File please wait...");
                        String cookie = CookieManager.getInstance().getCookie(fileUrl);
                        request.addRequestHeader("cookie", cookie);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title);

                        DownloadManager downloadManager = (DownloadManager)getActivity().getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);

                        Toast.makeText(getActivity().getApplicationContext(), "File is now downloading...", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d("", "Document doesn't exists.");
                    }
                }
            }
        });

    }

    private void openFileChooser() {
        String [] mimeTypes = {"text/csv", "text/comma-separated-values"};


        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_CSV_REQUEST);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            mFileUri = data.getData();
            String station = ReqDesignatedStation.getText().toString().trim();
            String fileName = ReqName.getText().toString().trim();
            ReqFileName.setText(new StringBuilder().append(station).append("_").append(fileName).append(".csv").toString());

        }
        else{
            Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
        }

    }

    private List<ReadCSV> readCSV = new ArrayList<>();
    private ArrayList<String> localCSVData = new ArrayList<>();

    private void updatedCSVandSavingData_CheckedAndNew() {

        String requirements = ReqName.getText().toString().trim();
        String description = ReqDescription.getText().toString().trim();
        String location = ReqLoc.getText().toString().trim();
        String fileName = ReqFileName.getText().toString().trim();
        String Station = ReqDesignatedStation.getText().toString().trim();


        if (requirements.isEmpty()){
            ReqName.setError("Please enter the requirement's name.");
            ReqName.requestFocus();
        }
        else if(description.isEmpty()){
            ReqDescription.setError("Please provide a description.");
            ReqDescription.requestFocus();
        }
        else if(location.isEmpty()){
            ReqLoc.setError("Please enter the requirement's location");
            ReqLoc.requestFocus();
        }
        else if(fileName.isEmpty()){
            ReqFileName.setError("Please enter the file's name");
            ReqFileName.requestFocus();
        }


        else{
            showProgressDialog();

            Map<String,Object> requirementsInfo = new HashMap<>();
            requirementsInfo.put("RequirementsName", requirements);
            requirementsInfo.put("Description", description);
            requirementsInfo.put("Location", location);
            requirementsInfo.put("RequirementStatus", "Verified");
            requirementsInfo.put("SentBy", StaffName);


            mStore.collection("SigningStation").document(Station).collection("Requirements").document(requirements).set(requirementsInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("","DocumentSnapshot successfully written!");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("", "Error in DocumentSnapshot!");
                        }
                    });

        }



        if (mFileUri != null) {

            try {
                // Open the file through URI
                InputStream inputStream = getActivity().getApplicationContext().getContentResolver().openInputStream(mFileUri);
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String Line;

                while ((Line = r.readLine()) != null) {
                    ReadCSV csvData = new ReadCSV();
                    Log.d("CSV Activity", "Line: "+Line);
                    csvData.setStudentNumber(Line);
                    readCSV.add(csvData);
                    localCSVData.add(Line);
                    Log.d("CSV Activity", "Created: "+csvData);
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            readCSV.remove(0);
            localCSVData.remove(0);
            StorageReference fileReference = mStorage.getReference().child("Requirements").child(fileName);

            mUploadTask = fileReference.putFile(mFileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {


                                    Map<String,Object> requirementsFile = new HashMap<>();

                                    UploadRequirements upload = new UploadRequirements(Station+"_"+fileName+".csv",
                                            uri.toString(), readCSV);

                                    requirementsFile.put("IncompleteFileURI",upload);


                                    // Storing the information of user

                                    mStore.collection("SigningStation").document(Station).collection("Requirements").document(requirements).update(requirementsFile)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {


                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("", "Error in DocumentSnapshot!");
                                        }
                                    });

                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("", "Failed to upload the file");
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            /*progressBar.setVisibility(View.INVISIBLE);
                            progressBarLayout.setVisibility(View.INVISIBLE);*/
                        }
                    });
        } else {
            Log.d("","No file selected");
        }



        mStore.collection("Students").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<String> matched = new ArrayList<>();
                String Requirements = ReqName.getText().toString().trim();

                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    CatchStudentDetails catchStudentDetails = documentSnapshot.toObject(CatchStudentDetails.class);
                    String docuID = documentSnapshot.getId();
                    String studentNumberGet = catchStudentDetails.getStdNo();

                    for(int i=0; i<localCSVData.size(); i++){
                        if (studentNumberGet.equals(localCSVData.get(i))) {
                            matched.add("matched");
                            String Description = ReqDescription.getText().toString().trim();
                            String Location = ReqLoc.getText().toString().trim();
                            String Station = ReqDesignatedStation.getText().toString().trim();
                            Map<String,Object> requirementsInsert = new HashMap<>();

                            requirementsInsert.put("RequirementsName", Requirements);
                            requirementsInsert.put("Description", Description);
                            requirementsInsert.put("Location", Location);
                            requirementsInsert.put("Status", "Incomplete");

                            Map<String, Object> StationName = new HashMap<>();
                            StationName.put("Status","Not-Signed");

                            mStore.collection("Students").document(docuID).collection("Stations").document(Station).update(StationName)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    });

                            //changed file path
                            mStore.collection("Students").document(docuID).collection("Stations").document(Station).collection("Requirements").document(Requirements).set(requirementsInsert).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG,"Successfully Inserted Requirements in Student");
                                    progressDialog.dismiss();
                                    // Reload current fragment

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });


                        }
                    }

                    if(matched.size()!=0){
                        matched.clear();
                    }
                    else{
                        String Station = ReqDesignatedStation.getText().toString().trim();
                        mStore.collection("Students").document(docuID).collection("Stations").document(Station).collection("Requirements").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<String> incomplete = new ArrayList<>();
                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    String status = documentSnapshot.getString("Status");
                                    if(status.equals("Incomplete")){
                                        incomplete.add("Incomplete");
                                    }
                                }

                                if(incomplete.size()!=0){
                                    incomplete.clear();
                                }
                                else{
                                    Map<String, Object> StationName = new HashMap<>();
                                    StationName.put("Status","Signed");

                                    mStore.collection("Students").document(docuID).collection("Stations").document(Station).update(StationName)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                }
                                            });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


                    }

                }

                mStore.collection("PendingRequirements").document(Station+"_"+Requirements).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                alert.setMessage(ReqName.getText().toString().trim()+" has been verified");
                                alert.setCancelable(false);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        progressDialog.dismiss();
                                        FragmentManager fm = getActivity().getSupportFragmentManager();
                                        FragmentTransaction ft = fm.beginTransaction();
                                        AdminPendingRequirementsFragment aprf = new AdminPendingRequirementsFragment();
                                        ft.replace(R.id.frag_container, aprf);
                                        ft.commit();
                                    }
                                });
                                alert.show();

                            }
                        });


            }
        });




    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.show();

    }


    private void saveVerifiedInStationAndStudent_Unchecked() {

        String requirements = ReqName.getText().toString().trim();
        String description = ReqDescription.getText().toString().trim();
        String location = ReqLoc.getText().toString().trim();
        String staffStation = ReqDesignatedStation.getText().toString().trim();


        if (requirements.isEmpty()){
            ReqName.setError("Please enter the requirement's name.");
            ReqName.requestFocus();
        }
        else if(description.isEmpty()){
            ReqDescription.setError("Please provide a description.");
            ReqDescription.requestFocus();
        }
        else if(location.isEmpty()){
            ReqLoc.setError("Please enter the requirement's location");
            ReqLoc.requestFocus();
        }

        else{
            showProgressDialog();

            Map<String,Object> requirementsInfo = new HashMap<>();
            requirementsInfo.put("RequirementsName", requirements);
            requirementsInfo.put("Description", description);
            requirementsInfo.put("Location", location);
            requirementsInfo.put("RequirementStatus", "Verified");
            requirementsInfo.put("SentBy", StaffName);


            mStore.collection("SigningStation").document(staffStation).collection("Requirements").document(requirements).set(requirementsInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("", "Error in DocumentSnapshot!");
                        }
                    });

            mStore.collection("Students").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    String Requirements = ReqName.getText().toString().trim();
                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String docuID = documentSnapshot.getId();

                        String Description = ReqDescription.getText().toString().trim();
                        String Location = ReqLoc.getText().toString().trim();
                        String Station = ReqDesignatedStation.getText().toString().trim();
                        Map<String,Object> requirementsInsert = new HashMap<>();

                        requirementsInsert.put("RequirementsName", Requirements);
                        requirementsInsert.put("Description", Description);
                        requirementsInsert.put("Location", Location);
                        requirementsInsert.put("Status", "Incomplete");

                        Map<String, Object> StationName = new HashMap<>();
                        StationName.put("Status","Not-Signed");

                        mStore.collection("Students").document(docuID).collection("Stations").document(Station).update(StationName)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });




                        //changed file path
                        mStore.collection("Students").document(docuID).collection("Stations").document(Station).collection("Requirements").document(Requirements).set(requirementsInsert).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG,"Successfully Inserted Requirements in Student");
                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });

                    }

                    String Station = ReqDesignatedStation.getText().toString().trim();
                    mStore.collection("PendingRequirements").document(Station+"_"+Requirements).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                    alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                    alert.setMessage(ReqName.getText().toString().trim()+" has been verified");
                                    alert.setCancelable(false);
                                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            FragmentManager fm = getActivity().getSupportFragmentManager();
                                            FragmentTransaction ft = fm.beginTransaction();
                                            AdminPendingRequirementsFragment aprf = new AdminPendingRequirementsFragment();
                                            ft.replace(R.id.frag_container, aprf);
                                            ft.commit();
                                        }
                                    });
                                    alert.show();
                                }
                            });


                }
            });

        }




    }

    public void saveRequirementsInStudentsAndStation_Checked() {

        String requirements = ReqName.getText().toString().trim();
        String description = ReqDescription.getText().toString().trim();
        String location = ReqLoc.getText().toString().trim();
        String fileName = ReqFileName.getText().toString().trim();
        String staffStation = ReqDesignatedStation.getText().toString().trim();

        if (requirements.isEmpty()){
            ReqName.setError("Please enter the requirement's name.");
            ReqName.requestFocus();
        }
        else if(description.isEmpty()){
            ReqDescription.setError("Please provide a description.");
            ReqDescription.requestFocus();
        }
        else if(location.isEmpty()){
            ReqLoc.setError("Please enter the requirement's location");
            ReqLoc.requestFocus();
        }
        else if(fileName.isEmpty()){
            ReqFileName.setError("Please enter the file's name");
            ReqFileName.requestFocus();
        }
        else if (fileName.equals("No file sent.")){
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("No file is currently inserted or sent by staff.")
                    .setMessage("List of incomplete is checked but you either deleted the file sent by staff or an error has occurred. Please insert a new file and try again.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alert.show();
        }

        else{
            showProgressDialog();


            mStore.collection("PendingRequirements").document(CurrentRequirement).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        CatchRequirementsDetails catchRequirementsDetails = document.toObject(CatchRequirementsDetails.class);
                        Map<String, Object> RequirementsFileCatch = catchRequirementsDetails.getIncompleteFileUri();
                        List<Map<String, Object>> fileData = (List<Map<String, Object>>) RequirementsFileCatch.get("fileData");
                        ArrayList<String> studentNumber = new ArrayList<>();

                        for (Map<String, Object> group : fileData) {
                            String studentNum = (String) group.get("studentNumber");
                            studentNumber.add(studentNum);
                        }

                        String Requirements = ReqName.getText().toString().trim();
                        mStore.collection("Students").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<String> matched = new ArrayList<>();

                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    CatchStudentDetails catchStudentDetails = documentSnapshot.toObject(CatchStudentDetails.class);
                                    String docuID = documentSnapshot.getId();
                                    String studentNumberGet = catchStudentDetails.getStdNo();

                                    for(int i=0; i<studentNumber.size(); i++){
                                        if (studentNumberGet.equals(studentNumber.get(i))) {
                                            matched.add("matched");
                                            String Description = ReqDescription.getText().toString().trim();
                                            String Location = ReqLoc.getText().toString().trim();
                                            String Station = ReqDesignatedStation.getText().toString().trim();
                                            Map<String,Object> requirementsInsert = new HashMap<>();

                                            requirementsInsert.put("RequirementsName", Requirements);
                                            requirementsInsert.put("Description", Description);
                                            requirementsInsert.put("Location", Location);
                                            requirementsInsert.put("Status", "Incomplete");

                                            Map<String, Object> StationName = new HashMap<>();
                                            StationName.put("Status","Not-Signed");

                                            mStore.collection("Students").document(docuID).collection("Stations").document(Station).update(StationName)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                        }
                                                    });


                                            //changed file path
                                            mStore.collection("Students").document(docuID).collection("Stations").document(Station).collection("Requirements").document(Requirements).set(requirementsInsert).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d(TAG,"Successfully Inserted Requirements in Student");

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });

                                            break;
                                        }
                                    }

                                    if(matched.size()!=0){
                                        matched.clear();
                                    }
                                    else{
                                        String Station = ReqDesignatedStation.getText().toString().trim();
                                        mStore.collection("Students").document(docuID).collection("Stations").document(Station).collection("Requirements").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                List<String> incomplete = new ArrayList<>();
                                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    String status = documentSnapshot.getString("Status");
                                                    if(status.equals("Incomplete")){
                                                        incomplete.add("Incomplete");
                                                    }
                                                }

                                                if(incomplete.size()!=0){
                                                    incomplete.clear();
                                                }
                                                else{
                                                    Map<String, Object> StationName = new HashMap<>();
                                                    StationName.put("Status","Signed");

                                                    mStore.collection("Students").document(docuID).collection("Stations").document(Station).update(StationName)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {

                                                                }
                                                            });
                                                }


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

                                    }


                                }

                            }
                        });

                        Map<String,Object> requirementsInfo = new HashMap<>();
                        requirementsInfo.put("RequirementsName", requirements);
                        requirementsInfo.put("Description", description);
                        requirementsInfo.put("Location", location);
                        requirementsInfo.put("RequirementStatus", "Verified");
                        requirementsInfo.put("SentBy", StaffName);
                        requirementsInfo.put("fileUrl", RequirementsFileCatch.get("fileUrl"));
                        requirementsInfo.put("fileName", RequirementsFileCatch.get("name"));
                        requirementsInfo.put("fileData", RequirementsFileCatch.get("fileData"));




                        mStore.collection("SigningStation").document(staffStation).collection("Requirements").document(requirements).set(requirementsInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("","DocumentSnapshot successfully written!");
                                        progressDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("", "Error in DocumentSnapshot!");
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            }
                        });
                        String Station = ReqDesignatedStation.getText().toString().trim();
                        mStore.collection("PendingRequirements").document(Station+"_"+Requirements).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                        alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                        alert.setMessage(ReqName.getText().toString().trim()+" has been verified");
                                        alert.setCancelable(false);
                                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                                FragmentTransaction ft = fm.beginTransaction();
                                                AdminPendingRequirementsFragment aprf = new AdminPendingRequirementsFragment();
                                                ft.replace(R.id.frag_container, aprf);
                                                ft.commit();
                                            }
                                        });
                                        alert.show();
                                    }
                                });
                    }


                }
            });



        }




    }


    private void DLFileSample() {
        mStore.collection("SampleFormat").document("CSVData").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String fileUrl = document.get("fileUrl").toString();

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
                        String title = URLUtil.guessFileName(fileUrl, null, null);
                        request.setTitle(title);
                        request.setDescription("Downloading File please wait...");
                        String cookie = CookieManager.getInstance().getCookie(fileUrl);
                        request.addRequestHeader("cookie", cookie);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title);

                        DownloadManager downloadManager = (DownloadManager)getActivity().getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);

                        Toast.makeText(getActivity().getApplicationContext(), "File is now downloading...", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.d("", "Document doesn't exists.");
                    }
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        CurrentRequirement = ArrayRequirements[position];

        mStore.collection("PendingRequirements").document(CurrentRequirement).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        CatchRequirementsDetails catchRequirementsDetails = document.toObject(CatchRequirementsDetails.class);

                        String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                        String RequirementsLocationCatch = catchRequirementsDetails.getLocation();
                        String RequirementsDesignationCatch = catchRequirementsDetails.getSigningStation();
                        String RequirementsDescriptionCatch = catchRequirementsDetails.getDescription();
                        StaffName = catchRequirementsDetails.getSentBy();

                        if (document.getData().containsKey("IncompleteFileURI")) {
                            Map<String,Object> RequirementsFileCatch = catchRequirementsDetails.getIncompleteFileUri();
                            ReqFileName.setText(RequirementsFileCatch.get("name").toString());
                            downloadFile.setClickable(true);
                            downloadFile.getBackground().setAlpha(255);
                            deleteFile.getBackground().setAlpha(255);
                            chooseFile.getBackground().setAlpha(255);
                            checkbox.setChecked(true);
                            enableList();
                        }
                        else{
                            downloadFile.setClickable(false);
                            downloadFile.getBackground().setAlpha(128);
                            deleteFile.getBackground().setAlpha(128);
                            chooseFile.getBackground().setAlpha(128);
                            String Nofile = "No file sent.";
                            ReqFileName.setText(Nofile);
                            checkbox.setChecked(false);
                            disabledList();
                        }


                        ReqName.setText(RequirementsNameCatch);
                        ReqLoc.setText(RequirementsLocationCatch);
                        ReqDescription.setText(RequirementsDescriptionCatch);
                        ReqDesignatedStation.setText(RequirementsDesignationCatch);

                    }
                }

            }
        });
    }




    private void disabledList() {
        chooseFile.setClickable(false);
        deleteFile.setClickable(false);
        ReqFileName.setFocusable(false);
        ReqFileName.setFocusableInTouchMode(false);
        ReqFileName.setClickable(false);
        downloadFile.getBackground().setAlpha(128);
        deleteFile.getBackground().setAlpha(128);
        chooseFile.getBackground().setAlpha(128);
        /*ListText.setKeyListener(null);*/
    }

    private void enableList(){
        chooseFile.setClickable(true);
        deleteFile.setClickable(true);
        ReqFileName.setFocusable(true);
        ReqFileName.setFocusableInTouchMode(true);
        ReqFileName.setClickable(true);
        downloadFile.getBackground().setAlpha(255);
        deleteFile.getBackground().setAlpha(255);
        chooseFile.getBackground().setAlpha(255);
    }





    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onRefresh() {
        // Reload current fragment
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        StudentClearanceFragment srf = new StudentClearanceFragment();
        ft.replace(R.id.frag_container_student, srf);
        ft.commit();
    }





}
