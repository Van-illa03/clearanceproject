package com.example.registration;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterScreen extends AppCompatActivity {
    EditText nameStaff,usernameStaff,emailStaff,passwordStaff,passwordStaff2;
    Button registerButton;
    TextView alreadyRegisteredButton;
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
        alreadyRegisteredButton =   findViewById(R.id.alreadyRegistered);
        fAuth   =   FirebaseAuth.getInstance();
        progressBar     =   findViewById(R.id.progressBar);


        if (fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        }

        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String email = emailStaff.getText().toString().trim();
                String password = passwordStaff.getText().toString().trim();


                if(TextUtils.isEmpty(email)){
                    emailStaff.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    passwordStaff.setError("Password is required");
                    return;
                }

                if(password.length() < 6){
                    passwordStaff.setError("Password must be greater than 6 characters");

                }

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(RegisterScreen.this, "You have successfully registered!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        }
                        else{
                            Toast.makeText(RegisterScreen.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            });



    }
}