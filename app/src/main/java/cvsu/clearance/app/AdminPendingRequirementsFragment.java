package cvsu.clearance.app;

import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPendingRequirementsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    FirebaseStorage mStorage;
    Activity currentActivity = this.getActivity();
    private long mLastClickTime = 0;
    CollectionReference reqcollection;
    public String[] ArrayRequirements;
    private int [] firstcounter = new int[2];
    public int secondcounter = 0;
    public String CurrentRequirement;
    TextView ReqName,ReqLoc, ReqDesignatedStation, ReqDescription, ReqFileName;
    ImageButton chooseFile, downloadFile, deleteFile;
    Button VerifyButton, DenyButton;
    Uri mFileUri;
    CheckBox checkbox;
    CollectionReference requirementsCol;
    Context adminContext =  AdminMainActivity.getContextOfApplicationadmin();
    StorageReference storageReference;

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
        requirementsCol = mStore.collection("PendingRequirements");
        storageReference = mStorage.getReference();
        ReqFileName = view.findViewById(R.id.ListText_Pending);
        chooseFile = view.findViewById(R.id.chooseFileBtn_Pending);
        deleteFile = view.findViewById(R.id.deleteFileBtn_Pending);
        downloadFile = view.findViewById(R.id.downloadFileBtn_Pending);
        checkbox = view.findViewById(R.id.checkBox_Pending);

        if (mAuth.getCurrentUser() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("Please log in first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            startActivity(new Intent(getContext(), LoginScreen.class));
        }


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
                                    ArrayRequirements[secondcounter] = RequirementsNameCatch;
                                    secondcounter++;
                            }
                        }
                        ArrayAdapter AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, ArrayRequirements);
                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        //Setting the ArrayAdapter data on the Spinner
                        spin.setAdapter(AA);
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
            }
        });

        deleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

            }
        });



        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }

                if(checkbox.isChecked()){

                }
                else{

                }
                readAndUpdate();

            }
        });


        DenyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deletingFile = ReqFileName.getText().toString();
                if(!deletingFile.equals("No file sent.")) {
                    mStorage.getReference().child("PendingRequirements/"+ deletingFile).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Success");
                        alert.setMessage("The specific requirement has been deleted.");
                        alert.setPositiveButton("OK", null);
                        alert.show();

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
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Error");
                        alert.setMessage("An error occurred in deleting the requirement document.");
                        alert.setPositiveButton("OK", null);
                        alert.show();
                    }
                });
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

                        Toast.makeText(getActivity().getApplicationContext(), "Nice walang error", Toast.LENGTH_SHORT).show();
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
                        if (document.getData().containsKey("IncompleteFileURI")) {
                            Map<String,Object> RequirementsFileCatch = catchRequirementsDetails.getIncompleteFileUri();
                            ReqFileName.setText(RequirementsFileCatch.get("name").toString());
                            downloadFile.setClickable(true);
                            downloadFile.getBackground().setAlpha(255);
                            checkbox.setChecked(true);
                        }
                        else{
                            downloadFile.setClickable(false);
                            downloadFile.getBackground().setAlpha(128);
                            String Nofile = "No file sent.";
                            ReqFileName.setText(Nofile);
                            checkbox.setChecked(false);
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

    private List<ReadCSV> readCSVinFirestore = new ArrayList<>();
    private List<ReadCSV> readCSVinLocal = new ArrayList<>();
    public void readAndUpdate() {
               /* try {
                    // Open the file through URI
                    InputStream inputStream = getActivity().getApplicationContext().getContentResolver().openInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    String Line;

                    while ((Line = r.readLine()) != null) {
                        Log.d("CSV Activity", "Line: "+Line);
                        ReadCSV csvData = new ReadCSV();
                        csvData.setStudentNumber(r.readLine());
                        readCSV.add(csvData);
                        Log.d("CSV Activity", "Created: "+csvData);
                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

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

                    mStore.collection("Students").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                CatchStudentDetails catchStudentDetails = documentSnapshot.toObject(CatchStudentDetails.class);
                                String docuID = documentSnapshot.getId();
                                String studentNumberGet = catchStudentDetails.getStdNo();

                                for(int i=0; i<studentNumber.size(); i++){
                                    if (studentNumberGet.equals(studentNumber.get(i))) {
                                        String Requirements = ReqName.getText().toString().trim();
                                        String Description = ReqDescription.getText().toString().trim();
                                        String Location = ReqLoc.getText().toString().trim();
                                        String Station = ReqDesignatedStation.getText().toString().trim();
                                        Map<String,Object> requirementsInsert = new HashMap<>();

                                        InsertRequirements insertRequirements = new InsertRequirements(Requirements, Description, Location, "Incomplete");

                                        requirementsInsert.put(Requirements, insertRequirements);


                                        mStore.collection("Students").document(docuID).collection("Requirements").document(Station).set(requirementsInsert).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                            Log.d(TAG,"Successfully Inserted Requirements in Student");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });


                                    }
                                }


                            }

                        }
                    });




                }


            }
        });






    }




    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
