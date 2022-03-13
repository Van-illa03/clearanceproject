package cvsu.clearance.app;

import  androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ActivityVerifyStaff extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    private Button GenerateCodeButton;
    String StaffCode;
    TextView DisplayCode;
    TextView StaffName;
    TextView StaffEmail;
    TextView StaffDesignation;
    TextView StaffVerify;
    Button ShowCode;
    Button Verify;
    Button Deny;
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    CollectionReference collref;
    public String[] ArrayStaff, ArrayStaff2;
    public int[] firstcounter = new int[2];
    public int secondcounter = 0;
    public String CurrentStaff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verifystaff);
        mAuth   =   FirebaseAuth.getInstance();
        mUser   =   mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();
        GenerateCodeButton = findViewById(R.id.GenerateButton);
        DisplayCode = (TextView) findViewById(R.id.StaffCodeDisplay);
        StaffCode = "";
        ShowCode = (Button) findViewById(R.id.GenerateButton);
        collref = mStore.collection("Staff");
        StaffName = (TextView) findViewById(R.id.StaffNameText);
        StaffEmail = (TextView) findViewById(R.id.StaffEmailText);
        StaffDesignation = (TextView) findViewById(R.id.StaffStationText);
        StaffVerify = (TextView) findViewById(R.id.StaffVerifyText);
        Verify = (Button) findViewById(R.id.VerifyButton);
        Deny = (Button) findViewById(R.id.DenyButton);

        Spinner spin = findViewById(R.id.RegisteredStaffSpinner);
        spin.setOnItemSelectedListener(this);


        String[] languages = getResources().getStringArray(R.array.roles);


        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(ActivityVerifyStaff.this, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), LoginScreen.class));
            finish();

        }
        else {

        }
        //fetching code from the database to confirm it's existence
        //and to change the text label of generate button
        DocumentReference FetchData = mStore.collection("StaffCode").document("cvsu-ceit-sc");
        FetchData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                        String ExistingCode = document.getString("Code");
                        if (ExistingCode != ""){
                            DisplayCode.setText(ExistingCode);
                            ShowCode.setText("Create New Code");
                        }
                        else {
                            ShowCode.setText("Generate Code");
                        }
                    } else {
                        Toast.makeText(ActivityVerifyStaff.this, "Document does not exist.", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });


        DocumentReference FetchCode = mStore.collection("StaffCode").document("cvsu-ceit-sc");
        FetchCode.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                        String ExistingCode = document.getString("Code");
                            DisplayCode.setText(ExistingCode);
                    } else {
                        Toast.makeText(ActivityVerifyStaff.this, "Document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });

        // this method counts the number of fetched signing staff from
        // firestore, the value will be used as the size of the array that will
        // contain the staff names
        collref.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStaffDetails catchStaffDetails = documentSnapshot.toObject(CatchStaffDetails.class);

                            String StaffNameCatch = catchStaffDetails.getName();
                            if (StaffNameCatch != null) {
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
                        ArrayStaff = new String [firstcounter[0]];

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStaffDetails catchStaffDetails = documentSnapshot.toObject(CatchStaffDetails.class);

                            String StaffNameCatch = catchStaffDetails.getName();
                            if (StaffNameCatch != null) {
                                ArrayStaff[secondcounter] = StaffNameCatch;
                                secondcounter++;
                            }
                        }
                        ArrayAdapter AA = new ArrayAdapter (ActivityVerifyStaff.this, android.R.layout.simple_spinner_item, ArrayStaff);
                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        //Setting the ArrayAdapter data on the Spinner
                        spin.setAdapter(AA);
                    }
                });


        GenerateCodeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //getting the staff code from firestore
            DocumentReference FetchData = mStore.collection("StaffCode").document("cvsu-ceit-sc");
            FetchData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Saving the code to ExistingCode variable if it exists
                            Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());
                            String ExistingCode = document.getString("Code");

                            //testing the content of the fetched staff code
                            if (ExistingCode != ""){
                                DisplayCode.setText(ExistingCode);
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    //if a code is existing, a dialog box will appear asking the user if the user
                                    //wants to generate new staff code
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            //if the user chose "yes"
                                            case DialogInterface.BUTTON_POSITIVE:
                                                StaffCode = getRandomString(7);
                                                DisplayCode.setText(StaffCode);

                                                Map<String,Object> newStaffCode = new HashMap<>();
                                                newStaffCode.put("Code",StaffCode);


                                                // Storing the staff code
                                                mStore.collection("StaffCode").document("cvsu-ceit-sc").set(newStaffCode).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("Success","Staff code saved successfully.");
                                                        Toast.makeText(ActivityVerifyStaff.this, "Staff code saved successfully.", Toast.LENGTH_SHORT).show();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("Error", "Encountered an error. Staff code not saved.");
                                                        Toast.makeText(ActivityVerifyStaff.this, "Staff code not saved.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                break;
                                            //if the user chose "no"
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //no process to be made
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityVerifyStaff.this);
                                builder.setMessage("There is an existing staff code. Generate new one?").setPositiveButton("Yes", dialogClickListener)
                                        .setNegativeButton("No", dialogClickListener).show();
                            }
                            else {
                                StaffCode = getRandomString(7);
                                DisplayCode.setText(StaffCode);
                                ShowCode.setText("Create New Code");

                                Map<String,Object> newStaffCode = new HashMap<>();
                                newStaffCode.put("Code",StaffCode);


                                // Storing the staff code
                                mStore.collection("StaffCode").document("cvsu-ceit-sc").set(newStaffCode).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Success","Staff code saved successfully.");
                                        Toast.makeText(ActivityVerifyStaff.this, "Staff code saved successfully.", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Error", "Encountered an error. Staff code not saved.");
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(ActivityVerifyStaff.this, "Document does not exist.", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Log.d("Error", "get failed with ", task.getException());
                    }
                }
            });
        }
    });

        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    //if a code is existing, a dialog box will appear asking the user if the user
                    //wants to generate new staff code
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            //if the user chose "yes"
                            case DialogInterface.BUTTON_POSITIVE:
                                collref.get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                //in this code block gets the information of the staff displayed on the spinner (dropdown)
                                                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    CatchStaffDetails catchStaffDetails = documentSnapshot.toObject(CatchStaffDetails.class);

                                                    String StaffNameCatch = catchStaffDetails.getName();
                                                    String StaffVerifyCatch = catchStaffDetails.getVerified();
                                                    String StaffUID = documentSnapshot.getId();


                                                    if (StaffNameCatch != null) {
                                                        if (CurrentStaff.equals(StaffNameCatch)) {
                                                            if (StaffVerifyCatch.equals("No"))      {

                                                                Map<String,Object> YesVerify = new HashMap<>();
                                                                YesVerify.put("Verified","Yes");

                                                                DocumentReference StaffDoc = mStore.collection("Staff").document(StaffUID);

                                                                // Storing the verification status "Yes"
                                                                StaffDoc.update(YesVerify).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d("Success","Verification Success");
                                                                        Toast.makeText(ActivityVerifyStaff.this, "Verification Success.", Toast.LENGTH_SHORT).show();

                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w("Error", "Encountered an error.");
                                                                        Toast.makeText(ActivityVerifyStaff.this, "Verification Failed.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                                String StaffEmailCatch = catchStaffDetails.getEmail();
                                                                String StaffStationCatch = catchStaffDetails.getStation();
                                                                String StaffVerifyCatch2 = catchStaffDetails.getVerified();

                                                                StaffName.setText(StaffNameCatch);
                                                                StaffEmail.setText(StaffEmailCatch);
                                                                StaffDesignation.setText(StaffStationCatch);
                                                                StaffVerify.setText(StaffVerifyCatch2);

                                                            }
                                                            else if (StaffVerifyCatch.equals("Yes")) {
                                                                Toast.makeText(ActivityVerifyStaff.this, "This staff is already verified.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                break;

                            //if the user chose "no"
                            case DialogInterface.BUTTON_NEGATIVE:
                                //no process to be made
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityVerifyStaff.this);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }

        });

        Deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder CreatedCode = new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            CreatedCode.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return CreatedCode.toString();
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        CurrentStaff = ArrayStaff[position];
        Toast.makeText(ActivityVerifyStaff.this, "Current Staff: " + CurrentStaff, Toast.LENGTH_SHORT).show();

        collref.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //in this code block gets the information of the staff displayed on the spinner (dropdown)
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchStaffDetails catchStaffDetails = documentSnapshot.toObject(CatchStaffDetails.class);

                            String StaffNameCatch = catchStaffDetails.getName();
                            String StaffEmailCatch = catchStaffDetails.getEmail();
                            String StaffStationCatch = catchStaffDetails.getStation();
                            String StaffVerifyCatch = catchStaffDetails.getVerified();

                            if (StaffNameCatch != null) {
                                if (CurrentStaff.equals(StaffNameCatch))
                                {
                                    StaffName.setText(StaffNameCatch);
                                    StaffEmail.setText(StaffEmailCatch);
                                    StaffDesignation.setText(StaffStationCatch);
                                    StaffVerify.setText(StaffVerifyCatch);
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

