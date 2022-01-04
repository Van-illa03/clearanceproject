package cvsu.clearance.app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class StaffActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Button scanBtn, genBtn, profileBtn;
    ImageView qrCodeResult;

    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {

                if (result.getContents() == null) {
                    Toast.makeText(StaffActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(StaffActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            });


    




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        scanBtn = findViewById(R.id.scanBtn);
        genBtn = findViewById(R.id.genBtn);
        profileBtn = findViewById(R.id.profileBtn);
        qrCodeResult = findViewById(R.id.qrCodeResult);


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


        genBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Method for QR generation of users
                QRGeneration();



            }
        });


        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),StaffProfile.class));

            }
        });



    }

    private void QRGeneration() {

        // Temporarily coded here for testing purposes.
//        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid(); <-- will be used in registration instead

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap("testing", BarcodeFormat.QR_CODE, 500, 500);
            qrCodeResult.setImageBitmap(bitmap);
        } catch(Exception e) {
            e.printStackTrace();
        }


    }



}

