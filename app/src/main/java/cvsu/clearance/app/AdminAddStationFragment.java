package cvsu.clearance.app;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import java.util.HashMap;
import java.util.Map;

public class AdminAddStationFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button logoutButton;
    Context applicationContext = AdminMainActivity.getContextOfApplicationadmin();
    EditText stationName,stationLocation;
    Button fileButton, addButton;
    ProgressBar progressBar;
    Switch requiredSignSwitch;
    Uri mImageUri;
    StorageReference mStorageRef;
    String isRequired;
    StorageTask mUploadTask;
    private long mLastClickTime = 0;
    CollectionReference stationcollref;
    TextView signatureLabel;
    int [] firstcounter = new int [1];

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adminaddstationfragment,container,false);



        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        logoutButton = (Button) view.findViewById(R.id.logoutButton);
        stationName = view.findViewById(R.id.stationName);
        stationLocation = view.findViewById(R.id.stationLocation);
        signatureLabel = view.findViewById(R.id.signatureName);
        fileButton = view.findViewById(R.id.fileButton);
        addButton = view.findViewById(R.id.addButton);
        requiredSignSwitch = (Switch) view.findViewById(R.id.requiredSignSwitch);
        progressBar = view.findViewById(R.id.progressBar3);
        mStorageRef = FirebaseStorage.getInstance().getReference("signatures");
        mStore  =   FirebaseFirestore.getInstance();
        stationcollref = mStore.collection("SigningStation");

        if (mAuth.getCurrentUser() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("Please log in first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            startActivity(new Intent(getContext(), LoginScreen.class));
        } else {
        }

        //Counts the number of stations and putting it in the StationCount Document
        StationCounter();

        addButton.setOnClickListener(new View.OnClickListener() {
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
        requiredSignSwitch.setOnCheckedChangeListener (null);
        requiredSignSwitch.setChecked(false);
        isRequired="";
        requiredSignSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    isRequired = "Required";
                }
                else{
                    isRequired = "";
                }


            }
        });

        return view;
    }

    private void performValidation() {

        String sName = stationName.getText().toString().trim();
        String sLocation = stationLocation.getText().toString().trim();



        if(sName.isEmpty()){
            stationName.setError("Please enter signing station name.");
            stationName.requestFocus();
        }

        else if(sLocation.isEmpty()){
            stationLocation.setError("Please enter the signing station's location.");
            stationLocation.requestFocus();

        }

        else{
            performSavingInfo();
        }
    }

    private void performSavingInfo(){

        String sName = stationName.getText().toString().trim();
        String sLocation = stationLocation.getText().toString().trim();


        mStore.collection("SigningStation").document("StationCount").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                          String sName = stationName.getText().toString().trim();
                          String sLocation = stationLocation.getText().toString().trim();
                          DocumentSnapshot document = task.getResult();
                          Double StationCountDbl = new Double(document.getDouble("StationCount"));
                          int StationNumber = StationCountDbl.intValue() + 1;

                            Map<String,Object> signingStationInfo = new HashMap<>();
                            signingStationInfo.put("isRequired",isRequired);
                            signingStationInfo.put("Location",sLocation);
                            signingStationInfo.put("Signing_Station_Name", sName);
                            signingStationInfo.put("StationNumber", StationNumber);

                            mStore.collection("SigningStation").document(sName).set(signingStationInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.w("", "Document saved.");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("", "Error in DocumentSnapshot!");
                                }
                            });
                        }
                    }
                });

        mStore.collection("SigningStation").document(sName).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            StationCounter();
                        }
                    }
                });


    }



    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mImageUri = data.getData();

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile() {
        if (mImageUri != null) {
            // Temporary named in time. To be changed as Signing Station name in the future
            StorageReference fileReference = mStorageRef.child(stationName.getText().toString().trim()
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
                            alert.setMessage("Signing station successfully added.");
                            alert.setPositiveButton("OK", null);
                            alert.show();
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Map<String,Object> signingStationSignature = new HashMap<>();
                                    FirebaseUser User = mAuth.getCurrentUser();
                                    String station = stationName.getText().toString().trim();
                                    // Temporarily named "Signing Station" which is to be changed after updating the Firestore
                                    Upload upload = new Upload(stationName.getText().toString().trim(),
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
                            AdminAddStationFragment aasf = new AdminAddStationFragment();
                            ft.replace(R.id.frag_container, aasf);
                            ft.commit();
                        }
                    });
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("No File Selected");
            alert.setPositiveButton("OK", null);
            alert.show();
        }


    }

    private void StationCounter (){
        stationcollref.get()
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
                            if (StationNameCatch != null){
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

                                for (int i = 1; i <= loopcounter1; i++){
                                    if (StationNumber == i) {
                                        StationNumbers.put("StationCount",  loopcounter1);
                                        StationNumbers.put("slot_"+i, StationNameCatch);
                                    }
                                    else {
                                        Log.d("Empty Station Number", "Station Number is empty");
                                    }
                                }

                            }
                        }

                        //to be used in initializing the StationCount document
                        Map<String,Object> TotalStations = new HashMap<>();
                        TotalStations.put("StationCount", firstcounter[0]);
                        for (int i = 1; i <= 15; i++){
                            TotalStations.put("slot_"+i, "");
                        }

                        //initially setting the slots to null string
                        mStore.collection("SigningStation").document("StationCount").set(TotalStations)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //No arg
                                    }
                                });

                        //putting the hash map containing signing station names in StationCount Documment
                        mStore.collection("SigningStation").document("StationCount").update(StationNumbers)
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
