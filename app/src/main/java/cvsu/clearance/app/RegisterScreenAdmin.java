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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterScreenAdmin extends AppCompatActivity {
    EditText nameAdmin,emailAdmin,passwordAdmin,passwordAdmin2;
    Button registerButton;
    TextView alreadyRegistered;
    ProgressBar progressBar;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    // defining our own password pattern
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{8,}" +                // at least 8 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeradmin_screen);

        nameAdmin       =   findViewById(R.id.AdminName);
        emailAdmin      =   findViewById(R.id.AdminEmail);
        passwordAdmin   =   findViewById(R.id.AdminPass);
        passwordAdmin2  =   findViewById(R.id.AdminPass2);
        registerButton  =   findViewById(R.id.registerButton);
        alreadyRegistered =   findViewById(R.id.alreadyRegistered);
        progressBar     =   findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (mAuth.getCurrentUser() != null){
            mAuth.signOut();

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

        String name = nameAdmin.getText().toString();
        String email = emailAdmin.getText().toString();
        String password = passwordAdmin.getText().toString();
        String confirmPassword = passwordAdmin2.getText().toString();
        boolean passValidate = isValidPassword(password);

        if(!email.matches(emailPattern)){

            emailAdmin.setError("Please enter your CVSU email");
            emailAdmin.requestFocus();
        }

        else if (name.isEmpty()){

            nameAdmin.setError("Please enter your name");
            nameAdmin.requestFocus();

        }

        else if (password.isEmpty()){

            passwordAdmin.setError("Please enter your password");
            passwordAdmin.requestFocus();

        }

        else if (password.length()<8){

            passwordAdmin.setError("Password should be more than 8 characters");
            passwordAdmin.requestFocus();

        }
        else if (!passValidate){
            passwordAdmin.setError("Password must contain numbers and special characters (Ex. @#$%^&+=). Spaces are not allowed.");
            passwordAdmin.requestFocus();
        }
        else if (!password.equals(confirmPassword)){

            passwordAdmin2.setError("Your password doesn't match");
            passwordAdmin2.requestFocus();
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
                                Toast.makeText(RegisterScreenAdmin.this, "A verification message has been sent to your email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterScreenAdmin.this, "An error has occurred in sending verification request.", Toast.LENGTH_SHORT).show();
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
                        userInfo.put("Role","Admin");
                        userInfo.put("Name",nameAdmin.getText().toString());
                        userInfo.put("Email",emailAdmin.getText().toString());


                        // Storing the information of user
                        mStore.collection("Admin").document(User.getUid()).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                        Toast.makeText(RegisterScreenAdmin.this, "Registration Failed. Your CvSU email might be already in use.", Toast.LENGTH_SHORT).show();

                    }
                }
            });


        }



    }

    public static boolean
    isValidPassword(String password)
    {

        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        Matcher matchpass = PASSWORD_PATTERN.matcher(password);

        // Return if the password
        // matched the ReGex
        return matchpass.matches();
    }

    private void ProceedToNextActivity() {


        Intent intent= new Intent(RegisterScreenAdmin.this,LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}

