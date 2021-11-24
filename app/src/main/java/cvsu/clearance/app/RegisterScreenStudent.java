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

public class RegisterScreenStudent extends AppCompatActivity {
    EditText nameStudent,emailStudent,passwordStudent,passwordStudent2;
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
        setContentView(R.layout.activity_registerstudent_screen);

        nameStudent       =   findViewById(R.id.StudentName);
        emailStudent      =   findViewById(R.id.StudentEmail);
        passwordStudent   =   findViewById(R.id.StudentPass);
        passwordStudent2  =   findViewById(R.id.StudentPass2);
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

        String name = nameStudent.getText().toString();
        String email = emailStudent.getText().toString();
        String password = passwordStudent.getText().toString();
        String confirmPassword = passwordStudent2.getText().toString();


        if(!email.matches(emailPattern)){

            emailStudent.setError("Please enter your CVSU email");
            emailStudent.requestFocus();
        }

        else if (name.isEmpty()){

            nameStudent.setError("Please enter your name");
            nameStudent.requestFocus();

        }

        else if (password.isEmpty()){

            passwordStudent.setError("Please enter your password");
            passwordStudent.requestFocus();

        }

        else if (password.length()<8){

            passwordStudent.setError("Password should be more than 8 characters");
            passwordStudent.requestFocus();

        }

        else if (!password.equals(confirmPassword)){

            passwordStudent2.setError("Your password doesn't match");
            passwordStudent2.requestFocus();
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
                        Toast.makeText(RegisterScreenStudent.this, "Registration Successful", Toast.LENGTH_SHORT).show();


                        FirebaseUser User = mAuth.getCurrentUser();

                        Map<String,Object> userInfo = new HashMap<>();
                        userInfo.put("Name",nameStudent.getText().toString());
                        userInfo.put("Email",emailStudent.getText().toString());


                        // Giving the user the role of staff

                        userInfo.put("Role","Student");


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
                        Toast.makeText(RegisterScreenStudent.this, "Registration Failed. Please try again later.", Toast.LENGTH_SHORT).show();

                    }
                }
            });


        }



    }

    private void ProceedToNextActivity() {


        Intent intent= new Intent(RegisterScreenStudent.this, StaffActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}
