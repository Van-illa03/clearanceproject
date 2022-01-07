package cvsu.clearance.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


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

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),LoginScreen.class));
            }
        });
    }
}