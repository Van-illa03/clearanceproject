package cvsu.clearance.app;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.provider.MediaStore;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterScreenStudent extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText nameStudent,emailStudent,passwordStudent,passwordStudent2, StudentNumber;
    Button registerButton;
    TextView alreadyRegistered;
    ProgressBar progressBar;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]+)?+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;


    private Uri mImageUri;
    private StorageTask mUploadTask;
    private StorageReference mStorageRef;
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
        mStorageRef = FirebaseStorage.getInstance().getReference("QRCodes");

        Spinner spin = (Spinner) findViewById(R.id.StudentCourse);
        spin.setOnItemSelectedListener(this);

        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        ArrayAdapter AA = new ArrayAdapter (this, R.layout.dropdown_item_custom, StdCourse);
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

        String name = nameStudent.getText().toString().trim();
        String email = emailStudent.getText().toString().trim();
        String password = passwordStudent.getText().toString().trim();
        String confirmPassword = passwordStudent2.getText().toString().trim();
        String chosenCourse = CurrentCourse;
        String StdNumStr = StudentNumber.getText().toString().trim();


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
                                                mUser   =   mAuth.getCurrentUser();
                                                mUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(RegisterScreenStudent.this, "A verification message has been sent to your email.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RegisterScreenStudent.this, "An error has occurred in sending verification request.", Toast.LENGTH_SHORT).show();
                                                        Log.d("onError","Failed sending verification message: " + e.getMessage());
                                                    }
                                                });

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
                                                userInfo.put("Role","Student");
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

                                                QRGeneration();
                                                uploadFile();

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


    private void QRGeneration() {


        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(userID, BarcodeFormat.QR_CODE, 500, 500);
            mImageUri = getImageUri(RegisterScreenStudent.this, bitmap);
        } catch(Exception e) {
            e.printStackTrace();
        }




    }

    public Uri getImageUri(Context inContext, Bitmap inImage){
        String filename = nameStudent.getText().toString().toUpperCase() + "_QR_CODE";

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG,100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, filename, null);
        return Uri.parse(path);
    }



    private void uploadFile() {
        if (mImageUri != null) {

            StorageReference fileReference = mStorageRef.child(mUser.getUid()
                    + "_QR_CODE.png");

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {




                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Map<String,Object> studentQR = new HashMap<>();
                                    FirebaseUser User = mAuth.getCurrentUser();
                                    Upload upload = new Upload(User.getUid(),
                                            uri.toString());


                                    studentQR.put("QRCode",upload);

                                    // (Reference from tutorial) ->mDatabaseRef.child(uploadId).setValue(upload);

                                    // Storing the information of user

                                    mStore.collection("QRCodes").document(User.getUid()).set(studentQR)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
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

                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterScreenStudent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "Upload was unsuccessful", Toast.LENGTH_SHORT).show();
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
