package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfile extends AppCompatActivity {


FirebaseAuth mAuth;
FirebaseUser mUser;
FirebaseFirestore mStore;
Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        logoutButton = findViewById(R.id.logoutButton);
        TextView User = (TextView) findViewById(R.id.WelcomeAdmin);
        TextView DisplayEmail = findViewById(R.id.DisplayEmail);
        TextView verifyButton = findViewById(R.id.gotoVerifyStaff);


        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(AdminProfile.this, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
            finish();

        } else {
            User.setText(mUser.getDisplayName());

        }

        DocumentReference docRef = mStore.collection("Users").document(mUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());

                        String DocuEmail = (String) document.get("Email");
                        DisplayEmail.setText(DocuEmail);
                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
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


        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminProfile.this, ActivityVerifyStaff.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

}