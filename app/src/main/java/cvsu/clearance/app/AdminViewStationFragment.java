package cvsu.clearance.app;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.HashMap;
import java.util.Map;

public class AdminViewStationFragment extends Fragment {
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Context applicationContext = AdminMainActivity.getContextOfApplicationadmin();
    EditText stationLocation;
    TextView signatureName,RequirementDescription;
    Button fileButton, updateButton, deleteButton;
    ProgressBar progressBar;
    Switch requiredSignSwitch;
    Uri mImageUri;
    StorageReference mStorageRef;
    String isRequired;
    StorageTask mUploadTask;
    CollectionReference stationcollection;
    //fetch data of signing stations from firestore and put it in the array
    public String[] Stations;
    public String[] Requirements;
    public String CurrentStation = null;
    public String CurrentRequirement = null;
    public int[] firstcounter = new int[1];
    public int secondcounter = 0;
    public int[] thirdcounter = new int[1];
    public int fourthcounter = 0;
    String StationNameCatch;
    String StationLocationCatch;
    String StationIsRequiredCatch;
    private long mLastClickTime = 0;
    int totalslotcount = 15;
    Spinner reqspin;



    boolean signaturedoc,signaturefile;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragview = inflater.inflate(R.layout.adminviewstationfragment,container,false);



        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        deleteButton = (Button) fragview.findViewById(R.id.deleteButtonView);
        stationLocation = fragview.findViewById(R.id.viewstationLocation);
        signatureName = fragview.findViewById(R.id.viewsignatureName);
        fileButton = fragview.findViewById(R.id.fileButtonView);
        updateButton = fragview.findViewById(R.id.updateButtonView);
        requiredSignSwitch = (Switch) fragview.findViewById(R.id.viewrequiredSignSwitch);
        progressBar = fragview.findViewById(R.id.progressBar3);
        mStorageRef = FirebaseStorage.getInstance().getReference("signatures");
        mStore  =   FirebaseFirestore.getInstance();
        RequirementDescription = fragview.findViewById(R.id.ReqDescriptionText);

        if (mAuth.getCurrentUser() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("Please log in first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            startActivity(new Intent(getContext(), LoginScreen.class));
        } else {
        }

        Spinner spin = (Spinner) fragview.findViewById(R.id.StaffStation);

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

                                        StationNameCatch = document.getString("Signing_Station_Name");
                                        String StationIsRequiredCatch = document.getString("isRequired");
                                        StationLocationCatch = document.getString("Location");

                                        if (StationNameCatch != null) {
                                            stationLocation.setText(StationLocationCatch);

                                            if (StationIsRequiredCatch.equals("")){
                                                requiredSignSwitch.setChecked(false);
                                                isRequired="";
                                            }
                                            else if (StationIsRequiredCatch.equals("Required"))  {
                                                requiredSignSwitch.setChecked(true);
                                                isRequired="Required";
                                            }

                                            // Get reference to the file
                                            StorageReference fileRef = mStorageRef.child(StationNameCatch+".jpg");

                                            fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                                @Override
                                                public void onSuccess(StorageMetadata storageMetadata) {
                                                    String filename = storageMetadata.getName();
                                                    signatureName.setText(filename);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Uh-oh, an error occurred!
                                                }
                                            });


                                        }
                                    }
                                }

                                //the purpose of this collection call is to count the requirements inside this station
                                mStore.collection("SigningStation").document(CurrentStation).collection("Requirements").get()
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







        stationcollection = mStore.collection("SigningStation");

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







        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                performValidation();
                uploadFile();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                deleteStation();
                StationCounter();
            }
        });

        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                openFileChooser();
            }
        });

        requiredSignSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    requiredSignSwitch.setOnCheckedChangeListener (this);
                    requiredSignSwitch.setChecked(true);
                    isRequired = "Required";
                }
                else{
                    requiredSignSwitch.setOnCheckedChangeListener (this);
                    requiredSignSwitch.setChecked(false);
                    isRequired = "";
                }
            }
        });


        return fragview;
    }

    private void performValidation() {

        String sLocation = stationLocation.getText().toString().trim();


        if(sLocation.isEmpty()){
            stationLocation.setError("Please enter the signing station's location.");
            stationLocation.requestFocus();

        }

        else{

            performSavingInfo();

        }
    }

    private void deleteStation(){
        // Get reference to the file
        StorageReference fileRef = mStorageRef.child(CurrentStation.trim()+".jpg");
        DocumentReference delStation = mStore.collection("SigningStation").document(CurrentStation);
        DocumentReference delSignature = mStore.collection("Signatures").document(CurrentStation);
        CollectionReference AllStations = mStore.collection("SigningStation");
        HashMap<String,Object> obj = new HashMap<>();

        mStore.collection("StationCount").document("StationCount").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();

                        for(int i = 1; i <=totalslotcount; i++){
                            if (CurrentStation.equals(document.getString("slot_"+i))){
                                obj.put("slot_"+i,"empty");
                                break;
                            }
                        }
                        mStore.collection("StationCount").document("StationCount").update(obj)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("NOTICE","update success");
                                    }
                                });
                    }
                });


        //deleting the signing station document
        delStation.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                //deleting the signature document
                delSignature.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                //deleting the signature file
                                fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                        alert.setTitle("Success");
                                        alert.setMessage("Signing Station Deleted.");
                                        alert.setPositiveButton("OK", null);
                                        alert.show();

                                        // Reload current fragment
                                        FragmentManager fm = getActivity().getSupportFragmentManager();
                                        FragmentTransaction ft = fm.beginTransaction();
                                        AdminViewStationFragment avsf = new AdminViewStationFragment();
                                        ft.replace(R.id.frag_container, avsf);
                                        ft.commit();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                        alert.setTitle("Success");
                                        alert.setMessage("Signing Station Deleted (No existing signature file).");
                                        alert.setPositiveButton("OK", null);
                                        alert.show();

                                        // Reload current fragment
                                        FragmentManager fm = getActivity().getSupportFragmentManager();
                                        FragmentTransaction ft = fm.beginTransaction();
                                        AdminViewStationFragment avsf = new AdminViewStationFragment();
                                        ft.replace(R.id.frag_container, avsf);
                                        ft.commit();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Error");
                        alert.setMessage("Cannot find the signature details document. Deletion Failed.");
                        alert.setPositiveButton("OK", null);
                        alert.show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Error");
                alert.setMessage("Cannot find the signing station details document. Deletion Failed.");
                alert.setPositiveButton("OK", null);
                alert.show();
            }
        });
    }


    private void performSavingInfo(){


        String sLocation = stationLocation.getText().toString().trim();


        Map<String,Object> signingStationInfo = new HashMap<>();


        signingStationInfo.put("isRequired",isRequired);
        signingStationInfo.put("Location",sLocation);



        mStore.collection("SigningStation").document(CurrentStation).update(signingStationInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("","DocumentSnapshot successfully written!");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("", "Error in DocumentSnapshot!");
            }
        });

    }



    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mImageUri = data.getData();

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = applicationContext.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile() {
        if (mImageUri != null) {
            // Temporary named in time. To be changed as Signing Station name in the future
            StorageReference fileReference = mStorageRef.child(CurrentStation.trim()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            }, 500);

                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                            alert.setTitle("Success");
                            alert.setMessage("Signing station successfully updated.");
                            alert.setPositiveButton("OK", null);
                            alert.show();
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Map<String,Object> signingStationSignature = new HashMap<>();
                                    FirebaseUser User = mAuth.getCurrentUser();
                                    String station = CurrentStation;
                                    // Temporarily named "Signing Station" which is to be changed after updating the Firestore
                                    Upload upload = new Upload(CurrentStation.trim(),
                                            uri.toString());
                                    // The value of uploadId is to be changed to




                                    signingStationSignature.put("SignatureURI",upload);

                                    // (Reference from tutorial) ->mDatabaseRef.child(uploadId).setValue(upload);

                                    // Storing the information of user

                                    mStore.collection("Signatures").document(station).set(signingStationSignature)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("","DocumentSnapshot successfully written!");

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
                            Toast.makeText(applicationContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            // Reload current fragment
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            FragmentTransaction ft = fm.beginTransaction();
                            AdminViewStationFragment avsf = new AdminViewStationFragment();
                            ft.replace(R.id.frag_container, avsf);
                            ft.commit();
                        }
                    });
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Success");
            alert.setMessage("Signing station successfully updated.");
            alert.setPositiveButton("OK", null);
            alert.show();

            // Reload current fragment
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            AdminViewStationFragment avsf = new AdminViewStationFragment();
            ft.replace(R.id.frag_container, avsf);
            ft.commit();
        }


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
            reqspin = (Spinner) view.findViewById(R.id.RequirementsSpinner);
            Requirements = new String [RowCount];
            fourthcounter = 0;

            mStore.collection("SigningStation").document(StationName).collection("Requirements").get()
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

                                                        RequirementDescription.setText("-"+RequirementDescCatch);
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


    private void StationCounter (){
        stationcollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        String StationNameCatch;
                        Double StationNumberCatch;
                        int loopcounter1 = 0;

                        Map<String,Object> StationNumbers = new HashMap<>();

                        //counting total stations
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);

                            StationNameCatch = catchStationDetails.getSigning_Station_Name();
                            if (StationNameCatch != null ){
                                loopcounter1 += 1;
                            }
                        }

                        //assigning station names to slots in a Hashmap based on station numbers
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);

                            StationNameCatch = catchStationDetails.getSigning_Station_Name();
                            if (StationNameCatch != null){
                                StationNumberCatch = catchStationDetails.getStationNumber();
                                Double StatNum = new Double (StationNumberCatch);
                                int StationNumber = StatNum.intValue();

                                for (int i = 1; i <= totalslotcount; i++){
                                    if ((StationNumber == i) || (StationNumber == 0)) {
                                        StationNumbers.put("StationCount",  loopcounter1); //Total stations
                                        StationNumbers.put("slot_"+i, StationNameCatch); //putting station names in slots
                                    }
                                    else {
                                    }
                                }

                            }
                        }


                        //putting the hash map containing signing station names in StationCount Document
                        mStore.collection("StationCount").document("StationCount").update(StationNumbers)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //No arg
                                    }
                                });

                    }
                });
    }

}
