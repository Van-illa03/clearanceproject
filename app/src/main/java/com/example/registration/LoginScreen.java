package com.example.registration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;

public class LoginScreen extends AppCompatActivity {

    EditText emailStaff, passwordStaff;
    Button loginButton;
    TextView notAMemberYet;
    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);


        emailStaff      =   findViewById(R.id.emailStaff);
        passwordStaff   =   findViewById(R.id.passwordStaff);
        loginButton     =   findViewById(R.id.loginButton);
        notAMemberYet   =   findViewById(R.id.notAMemberYet);
        progressBar     =   findViewById(R.id.progressBar);
        fAuth           =   FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {


            String email = emailStaff.getText().toString().trim();
            String password = passwordStaff.getText().toString().trim();


            if(TextUtils.isEmpty(email)){
                emailStaff.setError("Email is required");
                emailStaff.requestFocus();
                return;
            }

            if(TextUtils.isEmpty(password)){
                passwordStaff.setError("Password is required");
                return;
            }

            if(password.length() < 6){
                passwordStaff.setError("Password must be greater than 6 characters");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {

                if (task.isSuccessful()){
                    Toast.makeText(LoginScreen.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                }
                else{
                    Toast.makeText(LoginScreen.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }

            });


        });


        loginButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),MainActivity.class)));

        notAMemberYet.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),RegisterScreen.class)));


    }
}