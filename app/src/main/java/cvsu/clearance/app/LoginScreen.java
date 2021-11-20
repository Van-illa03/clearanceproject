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


import com.example.registration.R;
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

    EditText UserEmail, UserPassword;
    Button loginButton;
    TextView notAMemberYet;
    String emailPattern = "[a-z.]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    public String[] UserRoles = { "Student","Staff","Admin" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);


        UserEmail      =   findViewById(R.id.emailStaff);
        UserPassword   =   findViewById(R.id.passwordStaff);
        loginButton     =   findViewById(R.id.loginButton);
        notAMemberYet   =   findViewById(R.id.notAMemberYet);
        mAuth           =   FirebaseAuth.getInstance();
        mUser           =   mAuth.getCurrentUser();
        mStore          =   FirebaseFirestore.getInstance();

        Spinner spin = (Spinner) findViewById(R.id.RoleDropdown);
        String SpinnerData = spin.getSelectedItem().toString();
        spin.setOnItemSelectedListener(this);


        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter AA;
        AA = new ArrayAdapter (this, android.R.layout.simple_spinner_item, UserRoles);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(AA);



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performLogin();

            }
        });


        notAMemberYet.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),RegisterScreen.class)));


    }

    private void performLogin() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(!email.matches(emailPattern)){

            UserEmail.setError("Enter a valid email");
            UserEmail.requestFocus();
        }

        else if (password.isEmpty()){

            UserPassword.setError("Please enter your password");
            UserPassword.requestFocus();

        }

        else if (password.length()<8){

            UserPassword.setError("Password should be more than 8 characters");
            UserPassword.requestFocus();

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
                        Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();

                        // Method to check the access level of user that logged in
                        checkAccessLevel(mUser.getUid());
                    }

                    else{
                        progressDialog.dismiss();
                        Toast.makeText(LoginScreen.this, "Login Failed. Please try again later"+task.getException(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    private void checkAccessLevel(String uid) {

        // Specification of Data and Collection in the Firebase FireStore
        DocumentReference documentReference = mStore.collection("Users").document(uid);

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

                else{

                    adminActivity();

                }


            }
        });


    }


    private void staffActivity() {
        Intent intent= new Intent(LoginScreen.this, StaffScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void adminActivity() {
        Intent intent= new Intent(LoginScreen.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(), UserRoles[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub

    }
}