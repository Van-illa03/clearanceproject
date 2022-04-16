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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class StaffScanQRFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private FirebaseFirestore mStore;
    Button logoutButton;
    private Button scanBtn;
    private ProgressBar progressBar;
    private Uri mImageUri;
    Activity currentActivity = this.getActivity();
    TextView DisplayEmail, DisplayStation, StudentNameText, StudentCourseText;
    String scannedResults;
    Context applicationContext = StaffMainActivity.getContextOfApplicationstaff();
    private long mLastClickTime = 0;



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
        View view = inflater.inflate(R.layout.staffscanqrfragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        logoutButton = view.findViewById(R.id.logoutButton);
        scanBtn = view.findViewById(R.id.scanBtn);
        progressBar = view.findViewById(R.id.progressBar);
        mStore  =   FirebaseFirestore.getInstance();
        TextView User = (TextView) view.findViewById(R.id.WelcomeStaff);
        DisplayEmail = view.findViewById(R.id.DisplayEmailStaff);
        DisplayStation = view.findViewById(R.id.DisplayStationStaff);
        StudentNameText = view.findViewById(R.id.StudentNameText);
        StudentCourseText = view.findViewById(R.id.StudentCourseText);
        String[] languages = getResources().getStringArray(R.array.roles);




        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(applicationContext  , "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }
        else {
            User.setText(""+mUser.getDisplayName());

        }

        DocumentReference docRef = mStore.collection("Staff").document(mUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());

                        String DocuEmail = (String) document.get("Email");
                        DisplayEmail.setText(DocuEmail);
                        String DocuStation = (String) document.get("Station");
                        DisplayStation.setText(DocuStation);

                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
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



        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), FrontScreen.class));


            }
        });

        return view;
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
