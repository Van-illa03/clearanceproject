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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

        String name = nameAdmin.getText().toString();
        String email = emailAdmin.getText().toString();
        String password = passwordAdmin.getText().toString();
        String confirmPassword = passwordAdmin2.getText().toString();


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
                        Toast.makeText(RegisterScreenAdmin.this, "Registration Successful", Toast.LENGTH_SHORT).show();


                        FirebaseUser User = mAuth.getCurrentUser();

                        Map<String,Object> userInfo = new HashMap<>();
                        userInfo.put("Name",nameAdmin.getText().toString());
                        userInfo.put("Email",emailAdmin.getText().toString());


                        // Giving the user the role of admin

                        userInfo.put("Role","Admin");


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
                        Toast.makeText(RegisterScreenAdmin.this, "Registration Failed. Please try again later.", Toast.LENGTH_SHORT).show();

                    }
                }
            });


        }



    }

    private void ProceedToNextActivity() {


        Intent intent= new Intent(RegisterScreenAdmin.this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}

