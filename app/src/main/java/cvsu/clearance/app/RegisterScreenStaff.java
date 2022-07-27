package cvsu.clearance.app;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterScreenStaff extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText nameStaff,emailStaff,passwordStaff,passwordStaff2, StaffCode;
    Button registerButton;
    TextView alreadyRegistered;
    ProgressBar progressBar;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;
    CollectionReference collref;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    //fetch data of signing stations from firestore and put it in the array
    public String[] StaffStations;
    public String CurrentStation = null;
    public int[] firstcounter = new int[2];
    public int secondcounter = 0;
    private String ExistingStaffCode;
    // defining our own password pattern
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{8,}" +                // at least 8 characters
                    "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerstaff_screen);

        nameStaff       =   findViewById(R.id.nameStaff);
        emailStaff      =   findViewById(R.id.emailStaff);
        passwordStaff   =   findViewById(R.id.passwordStaff);
        passwordStaff2  =   findViewById(R.id.passwordStaff2);
        registerButton  =   findViewById(R.id.registerButton);
        alreadyRegistered =   findViewById(R.id.alreadyRegistered);
        progressBar     =   findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        StaffCode = findViewById(R.id.StaffCode);


        Spinner spin = (Spinner) findViewById(R.id.StaffStation);
        spin.setOnItemSelectedListener(this);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();
        collref = mStore.collection("SigningStation");

        // this method counts the number of fetched signing station from
        // firestore, the value will be used as the size of the array that will
        // contain the signing station names
        collref.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Note note = documentSnapshot.toObject(Note.class);

                            String StationNameCatch = note.getSigning_Station_Name();
                            if (StationNameCatch != null) {
                                firstcounter[0] = firstcounter[0] + 1;
                            }
                        }
                    }
                });

        //the signing station names will be passed in the array through the "note" object
        collref.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        StaffStations = new String [firstcounter[0]];

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Note note = documentSnapshot.toObject(Note.class);
                            String StationNameCatch = note.getSigning_Station_Name();
                            if (StationNameCatch != null) {
                                StaffStations[secondcounter] = StationNameCatch;
                                secondcounter++;
                            }
                        }
                        ArrayAdapter AA = new ArrayAdapter (RegisterScreenStaff.this, R.layout.dropdown_item_custom, StaffStations);
                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        //Setting the ArrayAdapter data on the Spinner
                        spin.setAdapter(AA);
                    }
                });



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

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }

    //authentication process
    private void performAuth() {

            String name = nameStaff.getText().toString();
            String email = emailStaff.getText().toString();
            String password = passwordStaff.getText().toString();
            String confirmPassword = passwordStaff2.getText().toString();
            String chosenStation = CurrentStation;
            String StaffCodeText = StaffCode.getText().toString().trim();
            boolean passValidate = isValidPassword(password);

        DocumentReference FetchCode = mStore.collection("Code").document("StaffCode");
        FetchCode.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                        ExistingStaffCode = document.getString("Code").trim();

                        //checking of input fields
                        if(!email.matches(emailPattern)){

                            emailStaff.setError("Please enter your CvSU email");
                            emailStaff.requestFocus();
                        }
                        else if (name.isEmpty()){
                            nameStaff.setError("Please enter your name");
                            nameStaff.requestFocus();
                        }
                        else if (password.isEmpty()){
                            passwordStaff.setError("Please enter your password");
                            passwordStaff.requestFocus();
                        }
                        else if (password.length()<8){
                            passwordStaff.setError("Password should be more than 8 characters");
                            passwordStaff.requestFocus();
                        }
                        else if (!passValidate){
                            passwordStaff.setError("Password must contain numbers and special characters (Ex. @#$%^&+=). Spaces are not allowed.");
                            passwordStaff.requestFocus();
                        }
                        else if (!password.equals(confirmPassword)){
                            passwordStaff2.setError("Passwords does not match");
                            passwordStaff2.requestFocus();
                        }
                        else if (StaffCodeText.isEmpty()){
                            StaffCode.setError("Input Staff Code");
                            StaffCode.requestFocus();
                        }
                        else if (!StaffCodeText.equals(ExistingStaffCode)){
                            StaffCode.setError("Incorrect Staff Code");
                            StaffCode.requestFocus();
                        }
                        else{
                            progressDialog.setMessage("Please wait while registration...");
                            progressDialog.setTitle("Registration");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            //user will be created
                            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()){
                                        //a verification email will be sent to the user's email
                                        progressDialog.dismiss();
                                        mUser   =   mAuth.getCurrentUser();
                                        mUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(RegisterScreenStaff.this, "A verification message has been sent to your email.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegisterScreenStaff.this, "An error has occurred in sending verification request.", Toast.LENGTH_SHORT).show();
                                                Log.d("onError","Failed sending verification message: " + e.getMessage());
                                            }
                                        });

                                        //user's display name will be created
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
                                        userInfo.put("Role","Staff");
                                        userInfo.put("Name",name);
                                        userInfo.put("Email",email);
                                        userInfo.put("Station",chosenStation);
                                        userInfo.put("Verified","No");
                                        userInfo.put("VerifyCount",0);



                                        // Storing the information of user
                                        mStore.collection("Staff").document(User.getUid()).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Success","DocumentSnapshot successfully written!");

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Error", "Error in DocumentSnapshot!");
                                            }
                                        });



                                        ProceedToNextActivity();
                                    }

                                    else{
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterScreenStaff.this, "Registration Failed. Your CvSU email might be already in use.", Toast.LENGTH_SHORT).show();
                                        emailStaff.setError("Email already in use.");
                                        emailStaff.requestFocus();
                                    }
                                }
                            });


                        }


                    } else {
                        Toast.makeText(RegisterScreenStaff.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });





    }

    public static boolean
    isValidPassword(String password)
    {

        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        Matcher matchpass = PASSWORD_PATTERN.matcher(password);

        // Return if the password
        // matched the ReGex
        return matchpass.matches();
    }

    private void ProceedToNextActivity() {


            Intent intent= new Intent(RegisterScreenStaff.this, LoginScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        CurrentStation = StaffStations[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

