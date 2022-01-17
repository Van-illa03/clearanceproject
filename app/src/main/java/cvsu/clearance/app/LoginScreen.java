package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText jUserEmail, jUserPassword, AdminCodeInput, StaffCodeInput;
    Button loginButton;
    TextView notAMemberYet;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;
    DocumentSnapshot document;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    public String[] UserRoles = { "Student","Staff","Admin" };
    public String CurrentRole = null;
    public String StaffCode;
    public String AdminCode;
    private String ExistingCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);


        jUserEmail      =   findViewById(R.id.UserEmail);
        jUserPassword   =   findViewById(R.id.UserPassword);
        loginButton     =   findViewById(R.id.loginButton);
        notAMemberYet   =   findViewById(R.id.notAMemberYet);
        StaffCodeInput =    findViewById(R.id.StaffCode);
        AdminCodeInput =    findViewById(R.id.AdminCode);
        mAuth           =   FirebaseAuth.getInstance();
        mUser           =   mAuth.getCurrentUser();
        mStore          =   FirebaseFirestore.getInstance();
        StaffCode = StaffCodeInput.getText().toString();
        AdminCode = AdminCodeInput.getText().toString();

        Spinner spin = (Spinner) findViewById(R.id.RoleDropdown);
        spin.setOnItemSelectedListener(this);



        //Creating the ArrayAdapter instance having the bank name list

        ArrayAdapter AA = new ArrayAdapter (this, android.R.layout.simple_spinner_item, UserRoles);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(AA);


        DocumentReference FetchCode = mStore.collection("StaffCode").document("cvsu-ceit-sc");
        FetchCode.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                        ExistingCode = document.getString("Code");
                    } else {
                        Toast.makeText(LoginScreen.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();

            }
        });
        notAMemberYet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaffCode = StaffCodeInput.getText().toString();
                AdminCode = AdminCodeInput.getText().toString();

                DocumentReference FetchCode = mStore.collection("StaffCode").document("cvsu-ceit-sc");
                FetchCode.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            document = task.getResult();
                            if (document.exists()) {
                                Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                                ExistingCode = document.getString("Code");
                            } else {
                                Toast.makeText(LoginScreen.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("Error", "get failed with ", task.getException());
                        }
                    }
                });

                if (CurrentRole.equals("Student")) {
                    Intent intent = new Intent(getApplicationContext(), RegisterScreenStudent.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else if (CurrentRole.equals("Staff")) {
                    if (StaffCode.equals("")) {
                        StaffCodeInput.setError("Staff Code is Required.");
                        StaffCodeInput.requestFocus();
                    }
                    else if (StaffCode.equals(ExistingCode) ) {
                        Intent intent = new Intent(getApplicationContext(), RegisterScreenStaff.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else {
                        StaffCodeInput.setError("Incorrect Staff Code.");
                        StaffCodeInput.requestFocus();
                    }
                }
                else if (CurrentRole.equals("Admin")) {
                    if (AdminCode.equals("")){

                        AdminCodeInput.setError("Admin Code is Required.");
                        AdminCodeInput.requestFocus();
                    }
                    else {
                        Intent intent = new Intent(getApplicationContext(), RegisterScreenAdmin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }

            }
        });



    }

    private void performLogin() {

        String email = jUserEmail.getText().toString();
        String password = jUserPassword.getText().toString();
        String UserType = CurrentRole;
        StaffCode = StaffCodeInput.getText().toString();
        AdminCode = AdminCodeInput.getText().toString();

        DocumentReference FetchCode = mStore.collection("StaffCode").document("cvsu-ceit-sc");
        FetchCode.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                        ExistingCode = document.getString("Code");
                    } else {
                        Toast.makeText(LoginScreen.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });


            if (!email.matches(emailPattern)) {

                jUserEmail.setError("Enter a valid email");
                jUserEmail.requestFocus();
            } else if (password.isEmpty()) {

                jUserPassword.setError("Please enter your password");
                jUserPassword.requestFocus();

            } else if (password.length() < 8) {

                jUserPassword.setError("Password should be more than 8 characters");
                jUserPassword.requestFocus();

            } else {


                    if (UserType.equals("Student")){
                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mUser           =   mAuth.getCurrentUser();
                                if (task.isSuccessful()) {
                                    //testing if the user exists in different role types
                                    DocumentReference docRef = mStore.collection("Staff").document(mUser.getUid());
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                                                    Toast.makeText(LoginScreen.this, "You can't login as Student.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    DocumentReference docRef2 = mStore.collection("Admin").document(mUser.getUid());
                                                    docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot document = task.getResult();
                                                                if (document.exists()) {
                                                                    Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                                                                    Toast.makeText(LoginScreen.this, "You can't login as Student.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    if (mUser.isEmailVerified()){
                                                                        //if there's no match, then the the user is confirmed to be a student, and then will proceed to student profile screen
                                                                        Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();

                                                                        // Redirect to student activity screen
                                                                        studentActivity();
                                                                    }
                                                                    else {
                                                                        CheckVerification();
                                                                    }


                                                                }
                                                            } else {
                                                                Log.d("Error", "get failed with ", task.getException());
                                                            }
                                                        }
                                                    });
                                                }
                                            } else {
                                                Log.d("Error", "get failed with ", task.getException());
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginScreen.this, "Login Failed. Please try again later" + task.getException(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                    else if (UserType.equals("Staff")) {
                        if (StaffCode.equals("")) {
                            StaffCodeInput.setError("Staff Code is Required.");
                            StaffCodeInput.requestFocus();
                        }
                        else if (StaffCode.equals(ExistingCode)) {
                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        if (mUser.isEmailVerified()){
                                            Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();

                                            // Redirect to staff activity screen
                                            staffActivity();
                                        }
                                        else {
                                            CheckVerification();
                                        }

                                    } else {
                                        Toast.makeText(LoginScreen.this, "Login Failed. Please try again later" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                        else {
                            StaffCodeInput.setError("Incorrect Staff Code.");
                            StaffCodeInput.requestFocus();
                        }
                    }

                    else if (UserType.equals("Admin")) {
                        if (AdminCode.equals("")) {
                            AdminCodeInput.setError("Admin  Code is Required.");
                            AdminCodeInput.requestFocus();
                        }
                        else {
                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        if (mUser.isEmailVerified()) {
                                            Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();


                                            // Redirect to admin activity screen
                                            adminActivity();
                                        }
                                        else {
                                            CheckVerification();
                                        }

                                    } else {
                                        Toast.makeText(LoginScreen.this, "Login Failed. Please try again later" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    }
            }
        }

    private void staffActivity() {
        Intent intent= new Intent(LoginScreen.this, StaffProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void adminActivity() {
        Intent intent= new Intent(LoginScreen.this, AdminProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    private void studentActivity() {
        Intent intent= new Intent(LoginScreen.this, StudentProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    private void CheckVerification(){
        mUser = mAuth.getCurrentUser();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        mUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LoginScreen.this, "A verification message has been sent to your email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginScreen.this, "An error has occurred in sending verification request.", Toast.LENGTH_SHORT).show();
                                Log.d("onError","Failed sending verification message: " + e.getMessage());
                            }
                        });
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //no process to be made
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your email is not verified. Resend verification message?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        if (UserRoles[position] == "Staff"){
            StaffCodeInput.setVisibility(View.VISIBLE);
            AdminCodeInput.setVisibility(View.INVISIBLE);
            CurrentRole = "Staff";
        }
        else if (UserRoles[position] == "Admin") {
            StaffCodeInput.setVisibility(View.INVISIBLE);
            AdminCodeInput.setVisibility(View.VISIBLE);
            CurrentRole = "Admin";
        }
        else {
            StaffCodeInput.setVisibility(View.INVISIBLE);
            AdminCodeInput.setVisibility(View.INVISIBLE);
            CurrentRole = "Student";
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub

    }
}