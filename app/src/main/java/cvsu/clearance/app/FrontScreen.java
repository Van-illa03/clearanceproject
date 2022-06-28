package cvsu.clearance.app;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;


public class FrontScreen extends AppCompatActivity {

    Button buttonStart;
    TextView Appname;
    ImageView Logo;
    Animation logoanim, titleanim, buttonanim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_screen);

        buttonStart =   findViewById(R.id.buttonStart);
        Appname = findViewById(R.id.AppName);
        Logo = findViewById(R.id.AppLogo);
        logoanim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        titleanim = AnimationUtils.loadAnimation(this, R.anim.title_anim);
        buttonanim = AnimationUtils.loadAnimation(this, R.anim.button_anim);

        buttonStart.setAnimation(buttonanim);
        Appname.setAnimation(titleanim);
        Logo.setAnimation(logoanim);

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.areAllPermissionsGranted()){
                            //Toast.makeText(getApplicationContext(), "Permission GRANTED", Toast.LENGTH_LONG).show();
                        }
                        else{
                            AlertDialog.Builder alert = new AlertDialog.Builder(FrontScreen.this);
                            alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Permission DENIED</font>"));
                            alert.setMessage("Access to storage is required for system's certain functions to work.");
                            alert.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            });
                            alert.show();

                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),LoginScreen.class));
            }
        });
    }
}