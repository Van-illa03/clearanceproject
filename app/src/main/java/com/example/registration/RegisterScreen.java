package com.example.registration;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;

public class RegisterScreen extends AppCompatActivity {
    EditText nameStaff,usernameStaff,emailStaff,passwordStaff,passwordStaff2;
    Button registerButton;
    TextView alreadyRegistered;
    FirebaseAuth fAuth;
    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        nameStaff       =   findViewById(R.id.nameStaff);
        usernameStaff   =   findViewById(R.id.usernameStaff);
        emailStaff      =   findViewById(R.id.emailStaff);
        passwordStaff   =   findViewById(R.id.passwordStaff);
        passwordStaff2  =   findViewById(R.id.passwordStaff2);
        registerButton  =   findViewById(R.id.registerButton);
        alreadyRegistered =   findViewById(R.id.alreadyRegistered);
        fAuth   =   FirebaseAuth.getInstance();
        progressBar     =   findViewById(R.id.progressBar);


        if (fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        }

        registerButton.setOnClickListener(v -> {

            String email = emailStaff.getText().toString().trim();
            String password = passwordStaff.getText().toString().trim();


            if(TextUtils.isEmpty(email)){
                emailStaff.setError("Email is required");
                emailStaff.requestFocus();
                return;
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                emailStaff.setError("Enter a valid email address");
                emailStaff.requestFocus();
                return;
            }

            if(TextUtils.isEmpty(password)){
                passwordStaff.setError("Enter the password");
                passwordStaff.requestFocus();
                return;
            }

            if(password.length() < 6){
                passwordStaff.setError("Password must be greater than 6 characters");
                passwordStaff.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {

                if (task.isSuccessful()){
                    Toast.makeText(RegisterScreen.this, "You have successfully registered!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(RegisterScreen.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.VISIBLE);
                }
            });



        });

        alreadyRegistered.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),LoginScreen.class)));



    }
}