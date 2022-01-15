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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ActivityVerifyStaff extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    private Button GenerateCodeButton;
    String StaffCode;
    TextView DisplayCode;
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verifystaff);
        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();
        GenerateCodeButton = findViewById(R.id.GenerateButton);
        DisplayCode = (TextView) findViewById(R.id.StaffCodeDisplay);
        StaffCode = "";


        String[] languages = getResources().getStringArray(R.array.roles);


        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(ActivityVerifyStaff.this, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
            finish();

        }
        else {

        }

        DocumentReference FetchCode = mStore.collection("StaffCode").document("cvsu-ceit-sc");
        FetchCode.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                        String ExistingCode = document.getString("Code");
                            DisplayCode.setText(ExistingCode);
                    } else {
                        Toast.makeText(ActivityVerifyStaff.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });


    GenerateCodeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DocumentReference FetchData = mStore.collection("StaffCode").document("cvsu-ceit-sc");
            FetchData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                            String ExistingCode = document.getString("Code");
                            if (ExistingCode != ""){
                                Toast.makeText(ActivityVerifyStaff.this, "There is an existing staff code. Cannot generate another one.", Toast.LENGTH_SHORT).show();
                                DisplayCode.setText(ExistingCode);
                            }
                            else {
                                StaffCode = getRandomString(7);
                                DisplayCode.setText(StaffCode);

                                Map<String,Object> newStaffCode = new HashMap<>();
                                newStaffCode.put("Code",StaffCode);


                                // Storing the staff code
                                mStore.collection("StaffCode").document("cvsu-ceit-sc").set(newStaffCode).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Success","Staff code saved successfully.");
                                        Toast.makeText(ActivityVerifyStaff.this, "Staff code saved successfully.", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Error", "Encountered an error. Staff code not saved.");
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(ActivityVerifyStaff.this, "Document does not exist.", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Log.d("Error", "get failed with ", task.getException());
                    }
                }
            });
        }
    });

    }

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder CreatedCode = new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            CreatedCode.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return CreatedCode.toString();
    }
    }

