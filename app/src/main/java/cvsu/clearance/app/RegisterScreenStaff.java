package cvsu.clearance.app;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreenStaff extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText nameStaff,emailStaff,passwordStaff,passwordStaff2;
    Button registerButton;
    TextView alreadyRegistered;
    ProgressBar progressBar;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    public String[] StaffStations = { "CEIT Student Council","College Property Custodian","CEIT Reading Room","University Library","University Infirmary","Student Account Section","Central Student Government","Dean, Office of Student Affairs","Department Chairman","College Registrar","College Dean"};
    public String CurrentStation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerstaff_screen);

        nameStaff       =   findViewById(R.id.nameStaff);
        emailStaff      =   findViewById(R.id.emailStaff);
        passwordStaff   =   findViewById(R.id.passwordStaff);
        passwordStaff2  =   findViewById(R.id.passwordStaff2);
        registerButton  =   findViewById(R.id.registerButton);
        alreadyRegistered =   findViewById(R.id.alreadyRegistered);
        progressBar     =   findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);

        Spinner spin = (Spinner) findViewById(R.id.StaffStation);
        spin.setOnItemSelectedListener(this);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        ArrayAdapter AA = new ArrayAdapter (this, android.R.layout.simple_spinner_item, StaffStations);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(AA);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), AdminProfile.class));
            finish();

        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performAuth();

            }
        });




        alreadyRegistered.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),LoginScreen.class)));



    }

    private void performAuth() {

            String name = nameStaff.getText().toString();
            String email = emailStaff.getText().toString();
            String password = passwordStaff.getText().toString();
            String confirmPassword = passwordStaff2.getText().toString();
            String chosenStation = CurrentStation;


            if(!email.matches(emailPattern)){

                emailStaff.setError("Please enter your CvSU email");
                emailStaff.requestFocus();
            }

            else if (name.isEmpty()){

                nameStaff.setError("Please enter your name");
                nameStaff.requestFocus();

            }

            else if (password.isEmpty()){

                passwordStaff.setError("Please enter your password");
                passwordStaff.requestFocus();

            }

            else if (password.length()<8){

                passwordStaff.setError("Password should be more than 8 characters");
                passwordStaff.requestFocus();

            }

            else if (!password.equals(confirmPassword)){

                passwordStaff2.setError("Your password doesn't match");
                passwordStaff2.requestFocus();
            }

            else{
                    progressDialog.setMessage("Please wait while registration...");
                    progressDialog.setTitle("Registration");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();


                                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {

                                                if (task.isSuccessful()){

                                                    progressDialog.dismiss();
                                                    mUser   =   mAuth.getCurrentUser();
                                                    mUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(RegisterScreenStaff.this, "A verification message has been sent to your email.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(RegisterScreenStaff.this, "An error has occurred in sending verification request.", Toast.LENGTH_SHORT).show();
                                                            Log.d("onError","Failed sending verification message: " + e.getMessage());
                                                        }
                                                    });

                                                    FirebaseUser User = mAuth.getCurrentUser();
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(name).build();

                                                    User.updateProfile(profileUpdates)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Log.d("DisplayName", "User profile updated.");
                                                                    }
                                                                }
                                                            });

                                                    Map<String,Object> userInfo = new HashMap<>();
                                                    userInfo.put("Role","Staff");
                                                    userInfo.put("Name",name);
                                                    userInfo.put("Email",email);
                                                    userInfo.put("Station",chosenStation);



                                                    // Storing the information of user
                                                    mStore.collection("Staff").document(User.getUid()).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Success","DocumentSnapshot successfully written!");

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("Error", "Error in DocumentSnapshot!");
                                                        }
                                                    });



                                                    ProceedToNextActivity();
                            }

                            else{
                                progressDialog.dismiss();
                                Toast.makeText(RegisterScreenStaff.this, "Registration Failed. Your CvSU email might be already in use.", Toast.LENGTH_SHORT).show();
                                emailStaff.setError("Email already in use.");
                                emailStaff.requestFocus();
                            }
                        }
                    });


            }



    }

    private void ProceedToNextActivity() {


            Intent intent= new Intent(RegisterScreenStaff.this, LoginScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        CurrentStation = StaffStations[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}