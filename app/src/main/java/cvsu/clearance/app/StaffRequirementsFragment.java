package cvsu.clearance.app;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StaffRequirementsFragment extends Fragment {

    private long mLastClickTime = 0;
    private static final int PICK_CSV_REQUEST = 1;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button logoutButton, sendToAdminBtn;
    EditText RequirementsText, DescriptionText, LocationText;
    CheckBox checkBox;
    ImageButton chooseFileBtn_csv,deleteFileBtn_csv;
    Activity currentActivity = this.getActivity();
    String StaffName, StaffStation;
    Uri mFileUri;
    StorageReference mStorageRef;
    StorageTask mUploadTask;
    RelativeLayout progressBarLayout;
    ProgressBar progressBar;
    TextView requirementsLabel;

    private static final Pattern slash =
            Pattern.compile("/");

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
        checkBox = view.findViewById(R.id.checkBox);
        chooseFileBtn_csv = view.findViewById(R.id.chooseFileBtn_csv);
        deleteFileBtn_csv = view.findViewById(R.id.deleteFileBtn_csv);
        mStorageRef = FirebaseStorage.getInstance().getReference("Requirements");
        progressBarLayout = view.findViewById(R.id.progressBar_RequirementsLayout);
        progressBar = view.findViewById(R.id.progressBar_Requirements);
        requirementsLabel = view.findViewById(R.id.RequirementsLabel);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(currentActivity  , "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }





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
            }
        });



        // Disables and enables requirement section based on the checkbox
        disabledList();
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(checkBox.isChecked()){

                    enableList();

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


                mStore.collection("Staff").document(mUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            StaffName = (String) document.get("Name");
                            StaffStation = (String) document.get("Station");

                            if(checkBox.isChecked()){
                                performCheckedBox(StaffStation);
                                uploadFile();
                                checkBox.setChecked(false);
                            }
                            else{

                                performUncheckedBox(StaffStation);
                            }
                        }
                    }
                });

            }
        });

        return view;
    }

    public static boolean
    isValidRequirementName(String requirementName)
    {

        Matcher matchslash = slash.matcher(requirementName);

        return matchslash.find();
    }

    private void disabledList() {
        chooseFileBtn_csv.setClickable(false);
        deleteFileBtn_csv.setClickable(false);
    }

    private void enableList(){
        chooseFileBtn_csv.setClickable(true);
        deleteFileBtn_csv.setClickable(true);
    }

    private void performCheckedBox(String StationStaff) {


        String requirements = RequirementsText.getText().toString().trim();
        String description = DescriptionText.getText().toString().trim();
        String location = LocationText.getText().toString().trim();
        boolean ReqNameValidate = isValidRequirementName(requirements);

        if (requirements.isEmpty()){
            RequirementsText.setError("Please enter the requirement's name.");
            RequirementsText.requestFocus();
        }
        else if (ReqNameValidate) {
            RequirementsText.setError("Requirement's name must not contain a slash.");
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
        else if (mFileUri==null){
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("No file is currently inserted")
                    .setMessage("List of incomplete is checked but no file is detected. Please insert a new file and try again.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alert.show();

        }

        else{
            progressBar.setVisibility(View.VISIBLE);
            progressBarLayout.setVisibility(View.VISIBLE);
            Map<String,Object> requirementsInfo = new HashMap<>();
            requirementsInfo.put("RequirementsName", requirements);
            requirementsInfo.put("Description", description);
            requirementsInfo.put("Location", location);
            requirementsInfo.put("RequirementStatus", "Pending");
            requirementsInfo.put("SentBy", StaffName);
            requirementsInfo.put("SigningStation", StaffStation);

            mStore.collection("PendingRequirements").document(StationStaff+"_"+requirements).set(requirementsInfo)
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

    private void performUncheckedBox(String StationStaff) {

            String requirements = RequirementsText.getText().toString().trim();
            String description = DescriptionText.getText().toString().trim();
            String location = LocationText.getText().toString().trim();
            boolean ReqNameValidate = isValidRequirementName(requirements);


            if (requirements.isEmpty()){
                RequirementsText.setError("Please enter the requirement's name.");
                RequirementsText.requestFocus();
            }
            else if (ReqNameValidate) {
                RequirementsText.setError("Requirement's name must not contain a slash.");
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
                requirementsInfo.put("SentBy", StaffName);
                requirementsInfo.put("SigningStation", StaffStation);

                mStore.collection("PendingRequirements").document(StationStaff+"_"+requirements).set(requirementsInfo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("","DocumentSnapshot successfully written!");

                                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                alert.setMessage("Requirement has been forwarded to administrator for verification.");
                                alert.setPositiveButton("OK", null);
                                alert.show();

                                // Reload current fragment
                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                StaffRequirementsFragment srf = new StaffRequirementsFragment();
                                ft.replace(R.id.frag_container_staff, srf);
                                ft.commit();
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
        }
        else{
            Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
        }


    }
    private List<ReadCSV> readCSV = new ArrayList<>();
    private void uploadFile() {

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
                    Log.d("CSV Activity", "Created: "+csvData);
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            readCSV.remove(0);
            String requirements = RequirementsText.getText().toString().trim();
            StorageReference fileReference = mStorageRef.child(StaffStation+"_"+requirements+".csv");

            mUploadTask = fileReference.putFile(mFileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {


                                    Map<String,Object> requirementsFile = new HashMap<>();

                                    UploadRequirements upload = new UploadRequirements(StaffStation+"_"+requirements+".csv",
                                            uri.toString(), readCSV);

                                    requirementsFile.put("IncompleteFileURI",upload);


                                    // Storing the information of user

                                    mStore.collection("PendingRequirements").document(StaffStation+"_"+requirements).update(requirementsFile)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("","DocumentSnapshot successfully written!");
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                                    alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                                    alert.setMessage("Requirement and CSV file has been forwarded to administrator for verification.");
                                                    alert.setPositiveButton("OK", null);
                                                    alert.show();

                                                    // Reload current fragment
                                                    FragmentManager fm = getActivity().getSupportFragmentManager();
                                                    FragmentTransaction ft = fm.beginTransaction();
                                                    StaffRequirementsFragment srf = new StaffRequirementsFragment();
                                                    ft.replace(R.id.frag_container_staff, srf);
                                                    ft.commit();

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