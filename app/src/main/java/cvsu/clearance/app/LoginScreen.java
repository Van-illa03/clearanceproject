package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.storage.StorageReference;

public class LoginScreen extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText jUserEmail, jUserPassword;
    Button loginButton;
    TextView notAMemberYet, ForgotPassword;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    DocumentSnapshot document;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;

    private String[] UserRoles = { "Student","Staff","Admin" };
    private String CurrentRole = null;
    private String StaffCode;

    private String VerifyStatus;
    private double VerifyAttempt;
    private long mLastClickTime = 0;
    private Dialog dialogBuilder;
    private EditText ForgotPass_Email;
    private Button ForgotPass_Proceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);


        jUserEmail      =   findViewById(R.id.UserEmail);
        jUserPassword   =   findViewById(R.id.UserPassword);
        loginButton     =   findViewById(R.id.loginButton);
        notAMemberYet   =   findViewById(R.id.notAMemberYet);
        mAuth           =   FirebaseAuth.getInstance();
        mUser           =   mAuth.getCurrentUser();
        mStore          =   FirebaseFirestore.getInstance();
        progressBar     =   findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        ForgotPassword = findViewById(R.id.ForgotPassword);

        Spinner spin = (Spinner) findViewById(R.id.RoleDropdown);
        spin.setOnItemSelectedListener(this);


        //Creating the ArrayAdapter instance
        ArrayAdapter AA = new ArrayAdapter (this, R.layout.dropdown_item_custom, UserRoles);
        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(AA);



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This method prevents user from clicking the button too much.
                // It only last for 1.5 seconds.
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                performLogin();

            }
        });
        notAMemberYet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (CurrentRole.equals("Student")) {
                    Intent intent = new Intent(getApplicationContext(), RegisterScreenStudent.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else if (CurrentRole.equals("Staff")) {
                        Intent intent = new Intent(getApplicationContext(), RegisterScreenStaff.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                }
            }
        });

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder = new Dialog(LoginScreen.this);
                dialogBuilder.setContentView(R.layout.forgotpasswordinterface);
                dialogBuilder.setTitle("Reset Password");
                ForgotPass_Email = (EditText) dialogBuilder.findViewById(R.id.forgotpass_emailtext);
                ForgotPass_Proceed = (Button) dialogBuilder.findViewById(R.id.forgotpass_proceedbtn);
                dialogBuilder.show();

                dialogBuilder.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                ForgotPass_Proceed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ForgotPass_EmailString = ForgotPass_Email.getText().toString().trim();
                        if (ForgotPass_EmailString.isEmpty()){
                            ForgotPass_Email.setError("Please enter your email");
                            ForgotPass_Email.requestFocus();
                        }
                        else if (!ForgotPass_EmailString.matches(emailPattern)) {
                            ForgotPass_Email.setError("Enter a valid email");
                            ForgotPass_Email.requestFocus();
                        }
                        else {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(ForgotPass_EmailString).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialogBuilder.dismiss();
                                    Toast.makeText(LoginScreen.this,"A password reset link has been sent to your email. Check your inbox or spam messages.",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });



            }
        });



    }

    private void performLogin() {

        String email = jUserEmail.getText().toString().trim();
        String password = jUserPassword.getText().toString().trim();
        String UserType = CurrentRole;

            if (!email.matches(emailPattern)) {

                jUserEmail.setError("Enter a valid email");
                jUserEmail.requestFocus();
            } else if (password.isEmpty()) {

                jUserPassword.setError("Please enter your password");
                jUserPassword.requestFocus();

            } else {
                progressDialog.setMessage("Logging in...");
                progressDialog.setTitle("Authentication");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                    if (UserType.equals("Student")){
                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mUser           =   mAuth.getCurrentUser();
                                if (task.isSuccessful()) {

                                    String currentUser = mUser.getUid();
                                    mStore.collection("Admin").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                String docuId = document.getId();
                                                if(docuId.equals(currentUser)){
                                                    progressDialog.dismiss();
                                                    FirebaseAuth.getInstance().signOut();
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(LoginScreen.this);
                                                    alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Login Error.</font>"));
                                                    alert.setMessage("Your account does not match any records in Student database. Please check carefully.");
                                                    alert.setPositiveButton("OK", null);
                                                    alert.show();
                                                    break;
                                                }
                                                else {
                                                    if (mUser.isEmailVerified()){
                                                        DocumentReference StudentDoc = mStore.collection("Students").document(mUser.getUid());
                                                        StudentDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                                                                        // Redirect to student activity screen
                                                                        studentActivity();
                                                                    }
                                                                    else {
                                                                        progressDialog.dismiss();
                                                                        FirebaseAuth.getInstance().signOut();
                                                                        AlertDialog.Builder alert = new AlertDialog.Builder(LoginScreen.this);
                                                                        alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Login Error.</font>"));
                                                                        alert.setMessage("Your account does not match any records in Student database. Please check carefully.");
                                                                        alert.setPositiveButton("OK", null);
                                                                        alert.show();
                                                                    }
                                                                }
                                                            }
                                                        });

                                                    }
                                                    else {
                                                        progressDialog.dismiss();
                                                        CheckVerification();
                                                    }
                                                }
                                            }
                                        }
                                    });

                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(LoginScreen.this, "Login Failed. Please check your login credentials.", Toast.LENGTH_SHORT).show();
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

                                        String currentUser = mUser.getUid();
                                        mStore.collection("Admin").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                    String docuId = document.getId();
                                                    if(docuId.equals(currentUser)){
                                                        progressDialog.dismiss();
                                                        FirebaseAuth.getInstance().signOut();
                                                        AlertDialog.Builder alert = new AlertDialog.Builder(LoginScreen.this);
                                                        alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Login Error.</font>"));
                                                        alert.setMessage("Your account does not match any records in Staff database. Please check carefully.");
                                                        alert.setPositiveButton("OK", null);
                                                        alert.show();
                                                        break;
                                                    }
                                                    else {
                                                        if (mUser.isEmailVerified()){
                                                            //getting the verification information of the staff
                                                            DocumentReference StaffDoc = mStore.collection("Staff").document(mUser.getUid());
                                                            StaffDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot document = task.getResult();
                                                                        if (document.exists()) {
                                                                            Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                                                                            VerifyStatus = document.getString("Verified");
                                                                            VerifyAttempt = document.getDouble("VerifyCount");


                                                                            if(VerifyStatus.equals("Denied")){ // if verification is denied
                                                                                if (VerifyAttempt < 3 ){ // if verification request attempt is less than 3
                                                                                    progressDialog.dismiss();

                                                                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                                            switch (which){
                                                                                                //if the user chose "Resend Request"
                                                                                                case DialogInterface.BUTTON_POSITIVE:
                                                                                                    StaffDoc.update("Verified","No").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(Void aVoid) {
                                                                                                            Toast.makeText(LoginScreen.this, "Verification request sent.", Toast.LENGTH_SHORT).show();
                                                                                                            FirebaseAuth.getInstance().signOut();
                                                                                                            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
                                                                                                            finish();
                                                                                                        }
                                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                            Log.w("Error", "Encountered an error.");
                                                                                                            Toast.makeText(LoginScreen.this, "Verification request failed.", Toast.LENGTH_SHORT).show();
                                                                                                            FirebaseAuth.getInstance().signOut();
                                                                                                            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
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
                                                                                                                                            Toast.makeText(LoginScreen.this, "Account has been deleted.", Toast.LENGTH_LONG).show();
                                                                                                                                            StaffDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                                @Override
                                                                                                                                                public void onSuccess(Void unused) {
                                                                                                                                                    startActivity(new Intent(getApplicationContext(), LoginScreen.class));
                                                                                                                                                    finish();
                                                                                                                                                }
                                                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                                @Override
                                                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                                                    Toast.makeText(LoginScreen.this, "Staff detail document deletion failed.", Toast.LENGTH_LONG).show();
                                                                                                                                                }
                                                                                                                                            });
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                });
                                                                                                                    } else {
                                                                                                                        Toast.makeText(LoginScreen.this, "Your password is incorrect.", Toast.LENGTH_LONG).show();
                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                    break;
                                                                                            }
                                                                                        }
                                                                                    };

                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreen.this);
                                                                                    builder.setMessage("Your verification request has been denied by the administrator.\nVerification attempts remaining: " + (3 - VerifyAttempt)).setPositiveButton("Resend Request", dialogClickListener)
                                                                                            .setNegativeButton("Delete Account", dialogClickListener).setCancelable(false).show();
                                                                                }
                                                                                else { // if staff exceeds three verification request attempts
                                                                                    progressDialog.dismiss();
                                                                                    String currentEmail = mUser.getEmail();
                                                                                    String password = jUserPassword.getText().toString();
                                                                                    AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);

                                                                                    mUser.reauthenticate(credential)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {

                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        StaffDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void unused) {
                                                                                                                AlertDialog.Builder alert = new AlertDialog.Builder(LoginScreen.this);
                                                                                                                alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Account Deleted</font>"));
                                                                                                                alert.setMessage("You exceeded the maximum verification request attempts allowed. Your account is deleted from the system.");
                                                                                                                alert.setPositiveButton("OK", null);
                                                                                                                alert.show();
                                                                                                            }
                                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                                            @Override
                                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                                Toast.makeText(LoginScreen.this, "Staff detail document deletion failed.", Toast.LENGTH_LONG).show();
                                                                                                            }
                                                                                                        });

                                                                                                        mUser.delete()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful()) {
                                                                                                                            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
                                                                                                                            finish();
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    } else {
                                                                                                        Toast.makeText(LoginScreen.this, "Your password is incorrect.", Toast.LENGTH_LONG).show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                            else if (VerifyStatus.equals("No")){ //if verification is No
                                                                                progressDialog.dismiss();
                                                                                Toast.makeText(LoginScreen.this, "Your account is not yet verified. Contact the administrator.", Toast.LENGTH_SHORT).show();
                                                                                FirebaseAuth.getInstance().signOut();

                                                                            }
                                                                            else if (VerifyStatus.equals("Yes")) { //if verification is Yes
                                                                                progressDialog.dismiss();
                                                                                Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                                                                                // Redirect to staff activity screen
                                                                                staffActivity();
                                                                            }
                                                                        } else {
                                                                            progressDialog.dismiss();
                                                                            FirebaseAuth.getInstance().signOut();
                                                                            AlertDialog.Builder alert = new AlertDialog.Builder(LoginScreen.this);
                                                                            alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Login Error.</font>"));
                                                                            alert.setMessage("Your account does not match any records in staff database. Please check carefully.");
                                                                            alert.setPositiveButton("OK", null);
                                                                            alert.show();
                                                                        }
                                                                    } else {
                                                                        progressDialog.dismiss();
                                                                        Log.d("Error", "get failed with ", task.getException());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        else {
                                                            progressDialog.dismiss();
                                                            CheckVerification();
                                                        }
                                                    }
                                                }
                                            }
                                        });

                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginScreen.this, "Login Failed. Please check your login credentials.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    }

                    else if (UserType.equals("Admin")) {
                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    mUser           =   mAuth.getCurrentUser();
                                    if (task.isSuccessful()) {
                                            DocumentReference AdminDoc = mStore.collection("Admin").document(mUser.getUid());
                                            AdminDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        document = task.getResult();
                                                        if (document.exists()) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                                                            // Redirect to student activity screen
                                                            adminActivity();
                                                        }
                                                        else {
                                                            progressDialog.dismiss();
                                                            FirebaseAuth.getInstance().signOut();
                                                            AlertDialog.Builder alert = new AlertDialog.Builder(LoginScreen.this);
                                                            alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Login Error.</font>"));
                                                            alert.setMessage("Your account does not match any records in Administrator database. Please check carefully.");
                                                            alert.setPositiveButton("OK", null);
                                                            alert.show();
                                                        }
                                                    }
                                                }
                                            });

                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginScreen.this, "Login Failed. Please check your login credentials.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    }
            }
        }

    private void staffActivity() {
        Intent intent= new Intent(LoginScreen.this, StaffMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void adminActivity() {
        Intent intent= new Intent(LoginScreen.this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    private void studentActivity() {
        Intent intent= new Intent(LoginScreen.this, StudentMainActivity.class);
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
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(LoginScreen.this, "A verification message has been sent to your email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(LoginScreen.this, "An error has occurred in sending verification request.", Toast.LENGTH_SHORT).show();
                                Log.d("onError","Failed sending verification message: " + e.getMessage());
                            }
                        });
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        FirebaseAuth.getInstance().signOut();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your email is not verified. Resend verification message to your email?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).setCancelable(false).show();
    }


    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        if (UserRoles[position] == "Staff"){
            notAMemberYet.setVisibility(View.VISIBLE);
            CurrentRole = "Staff";
        }
        else if (UserRoles[position] == "Admin") {
            notAMemberYet.setVisibility(View.INVISIBLE);
            CurrentRole = "Admin";
        }
        else {
            notAMemberYet.setVisibility(View.VISIBLE);
            CurrentRole = "Student";
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
// TODO Auto-generated method stub

    }
}