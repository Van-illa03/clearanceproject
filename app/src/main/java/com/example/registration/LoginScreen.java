package com.example.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginScreen extends AppCompatActivity {

    EditText emailStaff, passwordStaff;
    Button loginButton;
    TextView notAMemberYet;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String emailPattern = "[a-z.]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);


        emailStaff      =   findViewById(R.id.emailStaff);
        passwordStaff   =   findViewById(R.id.passwordStaff);
        loginButton     =   findViewById(R.id.loginButton);
        notAMemberYet   =   findViewById(R.id.notAMemberYet);
        mAuth           =   FirebaseAuth.getInstance();
        mUser           =   mAuth.getCurrentUser();


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performLogin();

            }
        });


        notAMemberYet.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),RegisterScreen.class)));


    }

    private void performLogin() {

        String email = emailStaff.getText().toString();
        String password = passwordStaff.getText().toString();

        if(!email.matches(emailPattern)){

            emailStaff.setError("Enter a valid email");
            emailStaff.requestFocus();
        }

        else if (password.isEmpty()){

            passwordStaff.setError("Please enter your password");
            passwordStaff.requestFocus();

        }

        else if (password.length()<8){

            passwordStaff.setError("Password should be more than 8 characters");
            passwordStaff.requestFocus();

        }

        else {
            progressDialog.setMessage("Please wait...");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();



            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        progressDialog.dismiss();
                        ProceedToNextActivity();
                        Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                    }

                    else{
                        progressDialog.dismiss();
                        Toast.makeText(LoginScreen.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }


    private void ProceedToNextActivity() {
        Intent intent= new Intent(LoginScreen.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}