package cvsu.clearance.app;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class StaffRequirementsFragment extends Fragment {

    private long mLastClickTime = 0;
    private static final int PICK_CSV_REQUEST = 1;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button logoutButton, sendToAdminBtn;
    EditText RequirementsText, DescriptionText, LocationText, ListText;
    CheckBox checkBox;
    ImageButton chooseFileBtn_csv,deleteFileBtn_csv;
    Activity currentActivity = this.getActivity();
    String StaffName, StaffStation;
    Uri mFileUri;
    StorageReference mStorageRef;
    StorageTask mUploadTask;
    RelativeLayout progressBarLayout;
    ProgressBar progressBar;
    CollectionReference requirementsRef;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.staff_requirements_fragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        logoutButton = view.findViewById(R.id.logoutButton);
        sendToAdminBtn = view.findViewById(R.id.SendToAdminBtn);
        mStore  =   FirebaseFirestore.getInstance();
        RequirementsText = view.findViewById(R.id.RequirementsText);
        DescriptionText = view.findViewById(R.id.DescriptionText);
        LocationText = view.findViewById(R.id.LocationText);
        ListText = view.findViewById(R.id.ListText);
        checkBox = view.findViewById(R.id.checkBox);
        chooseFileBtn_csv = view.findViewById(R.id.chooseFileBtn_csv);
        deleteFileBtn_csv = view.findViewById(R.id.deleteFileBtn_csv);
        mStorageRef = FirebaseStorage.getInstance().getReference("PendingRequirements");
        progressBarLayout = view.findViewById(R.id.progressBar_RequirementsLayout);
        progressBar = view.findViewById(R.id.progressBar_Requirements);
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(currentActivity  , "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }

        mStore.collection("Staff").document(mUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    StaffName = (String) document.get("Name");
                    StaffStation = (String) document.get("Station");
                }
            }
        });



        chooseFileBtn_csv.setOnClickListener(new View.OnClickListener() {
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

        deleteFileBtn_csv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                mFileUri = null;
                ListText.setText(null);
            }
        });



        // Disables and enables requirement section based on the checkbox
        disabledList();
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(checkBox.isChecked()){

                    chooseFileBtn_csv.setClickable(true);
                    deleteFileBtn_csv.setClickable(true);
                    ListText.setFocusable(true);
                    ListText.setFocusableInTouchMode(true);
                    ListText.setClickable(true);

                }

                else{
                    disabledList();
                }

            }

        });




        sendToAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if(checkBox.isChecked()){
                    performCheckedBox();
                    uploadFile();
                    checkBox.setChecked(false);
                }
                else{

                    performUncheckedBox();
                }

            }
        });

        return view;
    }

    private void disabledList() {
        chooseFileBtn_csv.setClickable(false);
        deleteFileBtn_csv.setClickable(false);
        ListText.setFocusable(false);
        ListText.setFocusableInTouchMode(false);
        ListText.setClickable(false);
        /*ListText.setKeyListener(null);*/
    }

    private void performCheckedBox() {


        String requirements = RequirementsText.getText().toString().trim();
        String description = DescriptionText.getText().toString().trim();
        String location = LocationText.getText().toString().trim();
        String fileName = ListText.getText().toString().trim();

        if (requirements.isEmpty()){
            RequirementsText.setError("Please enter the requirement's name.");
            RequirementsText.requestFocus();
        }
        else if(description.isEmpty()){
            DescriptionText.setError("Please provide a description.");
            DescriptionText.requestFocus();
        }
        else if(location.isEmpty()){
            LocationText.setError("Please enter the requirement's location");
            LocationText.requestFocus();
        }
        else if(fileName.isEmpty()){
            ListText.setError("Please enter the file's name");
            ListText.requestFocus();
        }

        else{
            progressBar.setVisibility(View.VISIBLE);
            progressBarLayout.setVisibility(View.VISIBLE);

            Map<String,Object> requirementsInfo = new HashMap<>();
            requirementsInfo.put("RequirementsName", requirements);
            requirementsInfo.put("Description", description);
            requirementsInfo.put("Location", location);
            requirementsInfo.put("RequirementStatus", "Pending");
            requirementsInfo.put("Sent by", StaffName);
            requirementsInfo.put("SigningStation", StaffStation);

            mStore.collection("PendingRequirements").document(StaffStation+"_Requirements").collection("All of Requirements").document(requirements).set(requirementsInfo)
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
    }

    private void performUncheckedBox() {

            String requirements = RequirementsText.getText().toString().trim();
            String description = DescriptionText.getText().toString().trim();
            String location = LocationText.getText().toString().trim();


            if (requirements.isEmpty()){
                RequirementsText.setError("Please enter the requirement's name.");
                RequirementsText.requestFocus();
            }
            else if(description.isEmpty()){
                DescriptionText.setError("Please provide a description.");
                DescriptionText.requestFocus();
            }
            else if(location.isEmpty()){
                LocationText.setError("Please enter the requirement's location");
                LocationText.requestFocus();
            }

            else{
                progressBar.setVisibility(View.VISIBLE);
                progressBarLayout.setVisibility(View.VISIBLE);
                Map<String,Object> requirementsInfo = new HashMap<>();
                requirementsInfo.put("RequirementsName", requirements);
                requirementsInfo.put("Description", description);
                requirementsInfo.put("Location", location);
                requirementsInfo.put("RequirementStatus", "Pending");
                requirementsInfo.put("Sent by", StaffName);
                requirementsInfo.put("SigningStation", StaffStation);

                mStore.collection("PendingRequirements").document(StaffStation+"_Requirements").collection("All of Requirements").document(requirements).set(requirementsInfo)
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
                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        progressBarLayout.setVisibility(View.INVISIBLE);
                    }
                });

            }



    }

    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("text/csv");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_CSV_REQUEST);


    }







    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            mFileUri = data.getData();
        }

    }

    private void uploadFile() {
        if (mFileUri != null) {

            String requirements = RequirementsText.getText().toString().trim();
            String fileName = ListText.getText().toString().trim();
            StorageReference fileReference = mStorageRef.child(StaffStation+"_"+fileName+".csv");

            mUploadTask = fileReference.putFile(mFileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {


                                    Map<String,Object> requirementsFile = new HashMap<>();

                                    UploadRequirements upload = new UploadRequirements(StaffStation+"_"+fileName+".csv",
                                            uri.toString());

                                    requirementsFile.put("IncompleteFileURI",upload);


                                    // Storing the information of user

                                    mStore.collection("PendingRequirements").document(StaffStation+"_Requirements").collection("All of Requirements").document(requirements).update(requirementsFile)
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
                            Log.d("", "Failed to upload the file");
                        }
                    }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            progressBarLayout.setVisibility(View.INVISIBLE);
                        }
                    })
                    /*.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    })*/;
        } else {
            Log.d("","No file selected");
        }


    }



}