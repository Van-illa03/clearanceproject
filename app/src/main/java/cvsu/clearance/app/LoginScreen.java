package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginScreen extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText jUserEmail, jUserPassword, AdminCodeInput, StaffCodeInput;
    Button loginButton;
    TextView notAMemberYet;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    public String[] UserRoles = { "Student","Staff","Admin" };
    public String CurrentRole = null;
    public String StaffCode;
    public String AdminCode;

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
                    else {
                        Intent intent = new Intent(getApplicationContext(), RegisterScreenStaff.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
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

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();

                            // Method to check the access level of user that logged in
                            checkAccessLevel();
                        } else {
                            Toast.makeText(LoginScreen.this, "Login Failed. Please try again later" + task.getException(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }


    private void checkAccessLevel() {

        mUser = mAuth.getCurrentUser();
        String docuID = mUser.getUid();



        // Specification of Data and Collection in the Firebase FireStore
        DocumentReference documentReference = mStore.collection("Users").document(docuID);

        // Fetching the data in the specified collection stated above

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("", "onSuccess: " + documentSnapshot.getData());

                // Checking the role of the user that logged in

                if (documentSnapshot.getString("Role").equals("Staff")){

                    // The user that logged in is Staff
                    staffActivity();
                }
                else if (documentSnapshot.getString("Role").equals("Admin")){
                    adminActivity();
                }
                else {
                    studentActivity();
                }


            }
        });


    }


    private void staffActivity() {
        Intent intent= new Intent(LoginScreen.this, StaffActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void adminActivity() {
        Intent intent= new Intent(LoginScreen.this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    private void studentActivity() {
        Intent intent= new Intent(LoginScreen.this, StudentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

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
        //Toast.makeText(getApplicationContext(), UserRoles[position], Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub

    }
}