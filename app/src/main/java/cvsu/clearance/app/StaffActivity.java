package cvsu.clearance.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
        TextView User = (TextView) findViewById(R.id.WelcomeStaff);

        String[] languages = getResources().getStringArray(R.array.roles);


        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(StaffActivity.this, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
            finish();

        }
        else {
            User.setText("Welcome, " + mUser.getDisplayName());

        }



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