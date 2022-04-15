package cvsu.clearance.app;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

public class AdminViewStationFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Context applicationContext = AdminMainActivity.getContextOfApplicationadmin();
    EditText stationRequirements,stationLocation;
    TextView signatureName;
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
    public String CurrentStation = null;
    public int[] firstcounter = new int[2];
    public int secondcounter = 0;
    String StationNameCatch;
    String StationRequirementCatch;
    String StationLocationCatch;
    String StationIsRequiredCatch;

    boolean signaturedoc,signaturefile;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adminviewstationfragment,container,false);



        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        deleteButton = (Button) view.findViewById(R.id.deleteButtonView);
        stationRequirements = view.findViewById(R.id.viewstationRequirements);
        stationLocation = view.findViewById(R.id.viewstationLocation);
        signatureName = view.findViewById(R.id.viewsignatureName);
        fileButton = view.findViewById(R.id.fileButtonView);
        updateButton = view.findViewById(R.id.updateButtonView);
        requiredSignSwitch = (Switch) view.findViewById(R.id.viewrequiredSignSwitch);
        progressBar = view.findViewById(R.id.progressBar3);
        mStorageRef = FirebaseStorage.getInstance().getReference("signatures");
        mStore  =   FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("Please log in first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            startActivity(new Intent(getContext(), LoginScreen.class));
        } else {
        }

        Spinner spin = (Spinner) view.findViewById(R.id.StaffStation);
        spin.setOnItemSelectedListener(this);
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
                performValidation();
                uploadFile();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStation();
            }
        });

        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


        return view;
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


        String sRequirements = stationRequirements.getText().toString().trim();
        String sLocation = stationLocation.getText().toString().trim();


        Map<String,Object> signingStationInfo = new HashMap<>();


        signingStationInfo.put("isRequired",isRequired);
        signingStationInfo.put("Location",sLocation);
        if(sRequirements.isEmpty()){
            signingStationInfo.put("Requirements", "");
        }
        else{
            signingStationInfo.put("Requirements", sRequirements);
        }




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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        CurrentStation = Stations[position];
        stationcollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //in this code block gets the information of the station displayed on the spinner (dropdown)
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);

                            StationNameCatch = catchStationDetails.getSigning_Station_Name();
                            StationRequirementCatch = catchStationDetails.getRequirements();
                            StationLocationCatch = catchStationDetails.getLocation();
                            StationIsRequiredCatch = catchStationDetails.getIsRequired();

                            if (StationNameCatch != null) {
                                if (CurrentStation.equals(StationNameCatch))
                                {
                                    stationRequirements.setText(StationRequirementCatch);
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
                    }
                });

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
