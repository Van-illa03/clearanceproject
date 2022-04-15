package cvsu.clearance.app;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginScreen2 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText jUserEmail, jUserPassword, AdminCodeInput, StaffCodeInput;
    Button loginButton;
    Button proceedSignup;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;
    DocumentSnapshot document;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    private String[] UserRoles = { "Student","Staff","Admin" };
    private String CurrentRole = null;
    private String StaffCode;
    private String AdminCode;
    private String ExistingCode;
    private String VerifyStatus;
    private double VerifyAttempt;
    private int[] confirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen2);


        jUserEmail      =   findViewById(R.id.UserEmail);
        jUserPassword   =   findViewById(R.id.UserPassword);
        loginButton     =   findViewById(R.id.loginButton);
        proceedSignup   =   findViewById(R.id.signupProceedButton);
        StaffCodeInput =    findViewById(R.id.StaffCode);
        AdminCodeInput =    findViewById(R.id.AdminCode);
        mAuth           =   FirebaseAuth.getInstance();
        mUser           =   mAuth.getCurrentUser();
        mStore          =   FirebaseFirestore.getInstance();
        StaffCode = StaffCodeInput.getText().toString();
        AdminCode = AdminCodeInput.getText().toString();

        Spinner spin = (Spinner) findViewById(R.id.RoleDropdown);
        spin.setOnItemSelectedListener(this);



        //Creating the ArrayAdapter instance

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
                        Toast.makeText(LoginScreen2.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
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
        proceedSignup.setOnClickListener(new View.OnClickListener() {
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
                                Log.d("Retrieve data", "Staff code retrieved");
                                ExistingCode = document.getString("Code");
                            } else {
                                Toast.makeText(LoginScreen2.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
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
        //getting the verification information of the staff
        String UserType = CurrentRole;

        DocumentReference UserDoc = mStore.collection("Staff").document(mUser.getUid());

        //9pm discord, revise login and sign up process.


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
                                    if (mUser.isEmailVerified()){
                                        //if there's no match, then the the user is confirmed to be a student, and then will proceed to student profile screen
                                        Toast.makeText(LoginScreen2.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                                        // Redirect to student activity screen
                                        studentActivity();
                                    }
                                    else {
                                        CheckVerification();
                                    }

                                } else {
                                    Toast.makeText(LoginScreen2.this, "Login Failed. Please try again." + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else if (UserType.equals("Staff")) {
                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    mUser           =   mAuth.getCurrentUser();
                                    if (task.isSuccessful()) {

                                        if (mUser.isEmailVerified()){
                                            //getting the verification information of the staff
                                            DocumentReference StaffDoc = mStore.collection("Staff").document(mUser.getUid());
                                            StaffDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        document = task.getResult();
                                                        if (document.exists()) {
                                                            Log.d("Retrieve data", "Staff Information Retrieved");
                                                            VerifyStatus = document.getString("Verified");
                                                            VerifyAttempt = document.getDouble("VerifyCount");

                                                            if(VerifyStatus.equals("Denied")){ // if verification is denied
                                                                if (VerifyAttempt <= 2 ){ // if verification request attempt is less than 3
                                                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            switch (which){
                                                                                //if the user chose "Resend Request"
                                                                                case DialogInterface.BUTTON_POSITIVE:
                                                                                    StaffDoc.update("Verified","No").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            Log.d("Success","Verification Deny Success");
                                                                                            Toast.makeText(LoginScreen2.this, "Verification request sent.", Toast.LENGTH_SHORT).show();
                                                                                            FirebaseAuth.getInstance().signOut();
                                                                                            startActivity(new Intent(getApplicationContext(), LoginScreen2.class));
                                                                                            finish();
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            Log.w("Error", "Encountered an error.");
                                                                                            Toast.makeText(LoginScreen2.this, "Verification request failed.", Toast.LENGTH_SHORT).show();
                                                                                            FirebaseAuth.getInstance().signOut();
                                                                                            startActivity(new Intent(getApplicationContext(), LoginScreen2.class));
                                                                                            finish();
                                                                                        }
                                                                                    });
                                                                                    break;
                                                                                    //if the user chose "Delete Account"
                                                                                case DialogInterface.BUTTON_NEGATIVE:
                                                                                    String currentEmail = mUser.getEmail();
                                                                                    String password = jUserPassword.getText().toString();
                                                                                    AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);
                                                                                    mUser.reauthenticate(credential)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {

                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        mUser.delete()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful()) {
                                                                                                                            Toast.makeText(LoginScreen2.this, "Account has been deleted.", Toast.LENGTH_LONG).show();
                                                                                                                            StaffDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onSuccess(Void unused) {
                                                                                                                                    startActivity(new Intent(getApplicationContext(), LoginScreen2.class));
                                                                                                                                    finish();
                                                                                                                                }
                                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                @Override
                                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                                    Toast.makeText(LoginScreen2.this, "Staff detail document deletion failed.", Toast.LENGTH_LONG).show();
                                                                                                                                }
                                                                                                                            });
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    } else {
                                                                                                        Toast.makeText(LoginScreen2.this, "Your password is incorrect.", Toast.LENGTH_LONG).show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                    break;
                                                                            }
                                                                        }
                                                                     };
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreen2.this);
                                                                    builder.setMessage("Your verification request has been denied by the administrator.\nVerification attempts remaining: " + (3 - VerifyAttempt)).setPositiveButton("Resend Request", dialogClickListener)
                                                                            .setNegativeButton("Delete Account", dialogClickListener).show();
                                                                }
                                                                else { // if staff exceeds three verification request attempts
                                                                    String currentEmail = mUser.getEmail();
                                                                    String password = jUserPassword.getText().toString();
                                                                    AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);

                                                                    mUser.reauthenticate(credential)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {

                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        mUser.delete()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            AlertDialog.Builder alert = new AlertDialog.Builder(LoginScreen2.this);
                                                                                                            alert.setTitle("Account Deleted.");
                                                                                                            alert.setMessage("You exceeded the maximum verification request attempts allowed. Your account is deleted from the system.");
                                                                                                            alert.setPositiveButton("OK", null);
                                                                                                            alert.show();
                                                                                                            StaffDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(Void unused) {
                                                                                                                    startActivity(new Intent(getApplicationContext(), LoginScreen2.class));
                                                                                                                    finish();
                                                                                                                }
                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                @Override
                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                    Toast.makeText(LoginScreen2.this, "Staff detail document deletion failed.", Toast.LENGTH_LONG).show();
                                                                                                                }
                                                                                                            });
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    } else {
                                                                                        Toast.makeText(LoginScreen2.this, "Your password is incorrect.", Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                            else if (VerifyStatus.equals("No")){ //if verification is No
                                                                Toast.makeText(LoginScreen2.this, "Your account is not yet verified. Contact the administrator.", Toast.LENGTH_SHORT).show();
                                                                FirebaseAuth.getInstance().signOut();
                                                            }
                                                            else if (VerifyStatus.equals("Yes")) { //if verification is Yes
                                                                Toast.makeText(LoginScreen2.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                                                                // Redirect to staff activity screen
                                                                staffActivity();
                                                            }
                                                        } else {
                                                            Toast.makeText(LoginScreen2.this, "Staff does not exist.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Log.d("Error", "get failed with ", task.getException());
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            CheckVerification();
                                        }
                                    } else {
                                        Toast.makeText(LoginScreen2.this, "Login Failed. Please try again." + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
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
                                    mUser           =   mAuth.getCurrentUser();
                                    if (task.isSuccessful()) {

                                            if (mUser.isEmailVerified()){
                                                //if there's no match, then the the user is confirmed to be a student, and then will proceed to student profile screen
                                                Toast.makeText(LoginScreen2.this, "Login is Successful", Toast.LENGTH_SHORT).show();

                                                // Redirect to student activity screen
                                                adminActivity();
                                            }
                                            else {
                                                CheckVerification();
                                            }
                                    } else {
                                        Toast.makeText(LoginScreen2.this, "Login Failed. Please try again." + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }
            }

    private void staffActivity() {
        Intent intent= new Intent(LoginScreen2.this, StaffMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void adminActivity() {
        Intent intent= new Intent(LoginScreen2.this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    private void studentActivity() {
        Intent intent= new Intent(LoginScreen2.this, StudentMainActivity.class);
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
                                Toast.makeText(LoginScreen2.this, "A verification message has been sent to your email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginScreen2.this, "An error has occurred in sending verification request.", Toast.LENGTH_SHORT).show();
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
        builder.setMessage("Your email is not verified. Resend verification message to your email?").setPositiveButton("Yes", dialogClickListener)
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