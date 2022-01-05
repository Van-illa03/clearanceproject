package cvsu.clearance.app;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class RegisterScreenStaff extends AppCompatActivity {
    EditText nameStaff,emailStaff,passwordStaff,passwordStaff2;
    Button registerButton;
    TextView alreadyRegistered;
    ProgressBar progressBar;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

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

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), AdminActivity.class));
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


            if(!email.matches(emailPattern)){

                emailStaff.setError("Please enter your CVSU email");
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
                                Toast.makeText(RegisterScreenStaff.this, "Registration Successful", Toast.LENGTH_SHORT).show();


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
                                userInfo.put("Name",nameStaff.getText().toString());
                                userInfo.put("Email",emailStaff.getText().toString());


                                // Giving the user the role of staff

                                userInfo.put("Role","Staff");


                                // Storing the information of user
                               mStore.collection("Users").document(User.getUid()).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("","DocumentSnapshot successfully written!");

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("", "Error in DocumentSnapshot!");
                                    }
                                });


                                ProceedToNextActivity();



                            }

                            else{
                                progressDialog.dismiss();
                                Toast.makeText(RegisterScreenStaff.this, "Registration Failed. Please try again later.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });


            }



    }

    private void ProceedToNextActivity() {


            Intent intent= new Intent(RegisterScreenStaff.this, StaffActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

    }
}