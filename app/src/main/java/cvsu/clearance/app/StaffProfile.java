package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class StaffProfile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private FirebaseFirestore mStore;
    Button logoutButton;
    private Button scanBtn;
    private ProgressBar progressBar;
    private Uri mImageUri;



    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {

                if (result.getContents() == null) {
                    Toast.makeText(StaffProfile.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(StaffProfile.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_profile);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        logoutButton = findViewById(R.id.logoutButton);
        scanBtn = findViewById(R.id.scanBtn);
        progressBar = findViewById(R.id.progressBar);
        mStore  =   FirebaseFirestore.getInstance();
        TextView User = (TextView) findViewById(R.id.WelcomeStaff);
        TextView DisplayEmail = findViewById(R.id.DisplayEmail);
        TextView DisplayStation = findViewById(R.id.DisplayStation);
        String[] languages = getResources().getStringArray(R.array.roles);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(StaffProfile.this, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
            finish();

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

                scanBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


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
            }
        });






        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), FrontScreen.class));
                finish();


            }
        });
    }
}