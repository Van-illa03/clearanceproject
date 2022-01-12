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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterScreenStudent extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText nameStudent,emailStudent,passwordStudent,passwordStudent2, StudentNumber;
    Button registerButton;
    TextView alreadyRegistered;
    ProgressBar progressBar;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    public String[] StdCourse = { "BS Agricultural and BioSystems Engineering","BS Architecture","BS Civil Engineering","BS Computer Engineering","BS Computer Science","BS Electrical Engineering","BS Electronics Engineering","BS Industrial Engineering","BS Industrial Technology - Automotive Tech","BS Industrial Technology - Electrical Tech","BS Industrial Technology - Electronics Tech","BS Information Technology","BS Office Administration" };
    public String CurrentCourse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerstudent_screen);

        nameStudent       =   findViewById(R.id.StudentName);
        emailStudent      =   findViewById(R.id.StudentEmail);
        StudentNumber     = findViewById(R.id.StudentNumber);
        passwordStudent   =   findViewById(R.id.StudentPass);
        passwordStudent2  =   findViewById(R.id.StudentPass2);
        registerButton  =   findViewById(R.id.registerButton);
        alreadyRegistered =   findViewById(R.id.alreadyRegistered);
        progressBar     =   findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);

        Spinner spin = (Spinner) findViewById(R.id.StudentCourse);
        spin.setOnItemSelectedListener(this);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        ArrayAdapter AA = new ArrayAdapter (this, android.R.layout.simple_spinner_item, StdCourse);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(AA);

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

        String name = nameStudent.getText().toString();
        String email = emailStudent.getText().toString();
        String password = passwordStudent.getText().toString();
        String confirmPassword = passwordStudent2.getText().toString();
        String chosenCourse = CurrentCourse;
        String StdNumStr = StudentNumber.getText().toString();





        if(!email.matches(emailPattern)){

            emailStudent.setError("Please enter your CvSU email.");
            emailStudent.requestFocus();
        }

        else if (name.isEmpty()){

            nameStudent.setError("Please enter your name.");
            nameStudent.requestFocus();

        }
        else if (StdNumStr.isEmpty()){

            StudentNumber.setError("Please enter your student number.");
            StudentNumber.requestFocus();

        }
        else if (StdNumStr.length() != 9){

            StudentNumber.setError("Your student number must be exactly nine numbers. Please check.");
            StudentNumber.requestFocus();

        }

        else if (password.isEmpty()){

            passwordStudent.setError("Please enter your password.");
            passwordStudent.requestFocus();

        }

        else if (password.length()<8){

            passwordStudent.setError("Password should be more than 8 characters.");
            passwordStudent.requestFocus();

        }

        else if (!password.equals(confirmPassword)){

            passwordStudent2.setError("Your password doesn't match.");
            passwordStudent2.requestFocus();
        }

        else{
            progressDialog.setMessage("Please wait while registration...");
            progressDialog.setTitle("Registration");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mStore.collection("Students")
                    .whereEqualTo("StdNo", StdNumStr)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            int duplicate = 0;
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Matched Result", document.getId() + " => " + document.getData());
                                    duplicate+=1;
                                }
                                if (duplicate >= 1) {
                                    progressDialog.dismiss();
                                    StudentNumber.setError("Student Number already taken/in use.");
                                    StudentNumber.requestFocus();
                                }
                                else {
                                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (task.isSuccessful()){

                                                progressDialog.dismiss();
                                                Toast.makeText(RegisterScreenStudent.this, "Registration Successful", Toast.LENGTH_SHORT).show();


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
                                                userInfo.put("Name",name);
                                                userInfo.put("Email",email);
                                                userInfo.put("StdNo",StdNumStr);
                                                userInfo.put("Course",chosenCourse);



                                                // Storing the information of user
                                                mStore.collection("Students").document(User.getUid()).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                Toast.makeText(RegisterScreenStudent.this, "Registration Failed. Your CvSU email might be already in use.", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                                }

                            } else {
                                Log.d("StdNo Filter Failed", "Error getting documents: ", task.getException());
                            }
                        }
                    });


        }
    }

    private void ProceedToNextActivity() {


        Intent intent= new Intent(RegisterScreenStudent.this, LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        CurrentCourse = StdCourse[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
