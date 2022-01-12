package cvsu.clearance.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StaffActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        logoutButton = findViewById(R.id.logoutButton);



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