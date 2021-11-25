package cvsu.clearance.app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;



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
    Button scanBtn, genBtn;
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
        qrCodeResult = findViewById(R.id.qrCodeResult);


        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap("testing", BarcodeFormat.QR_CODE, 500, 500);
                    ImageView qrCodeResult = (ImageView) findViewById(R.id.qrCodeResult);
                    qrCodeResult.setImageBitmap(bitmap);
                } catch(Exception e) {
                    e.printStackTrace();
                }


            }
        });



    }

//    private void QRGeneration() {
//
//        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//
//    }



}

