package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginScreen extends AppCompatActivity {

    EditText emailLogin, passwordLogin;
    Button loginButton;
    TextView notAMemberYet;
    String emailPattern = "([a-zA-Z]+(\\.?[a-zA-Z]?+)+)@cvsu\\.edu\\.ph";
    ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);


        emailLogin      =   findViewById(R.id.emailLogin);
        passwordLogin   =   findViewById(R.id.passwordLogin);
        loginButton     =   findViewById(R.id.loginButton);
        notAMemberYet   =   findViewById(R.id.notAMemberYet);
        mAuth           =   FirebaseAuth.getInstance();
        mUser           =   mAuth.getCurrentUser();
        mStore          =   FirebaseFirestore.getInstance();


        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), AdminActivity.class));
            finish();

        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performLogin();

            }
        });

        notAMemberYet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), RegisterScreen.class));

            }
        });



    }

    private void performLogin() {

        String email = emailLogin.getText().toString();
        String password = passwordLogin.getText().toString();

        if(email.isEmpty() || !email.matches(emailPattern)){

            emailLogin.setError("Enter your email");
            emailLogin.requestFocus();
        }

        else if (password.isEmpty()){

            passwordLogin.setError("Please enter your password");
            passwordLogin.requestFocus();

        }

        else if (password.length()<8){

            passwordLogin.setError("Password should be more than 8 characters");
            passwordLogin.requestFocus();

        }

        else {
            progressDialog.setMessage("Please wait...");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();



            mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginScreen.this, "Login is Successful", Toast.LENGTH_SHORT).show();
                        // Method to check the access level of user that logged in
                        checkAccessLevel(Objects.requireNonNull(authResult.getUser()).getUid());




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginScreen.this, "Login Failed. Check your credentials", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
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

                String Role = documentSnapshot.getString("Role");

                // Checking the role of the user that logged in

                if (documentSnapshot.getString(Role) == "Staff"){

                    // The user that logged in is Staff
                    staffActivity();

                }

                else if(documentSnapshot.getString(Role) == "Admin"){

                    adminActivity();

                }

                else if (documentSnapshot.getString(Role) == null){

                    Toast.makeText(LoginScreen.this,"Error ", Toast.LENGTH_SHORT).show();
                    startActivity(getIntent());

                }

                else{

                    Toast.makeText(LoginScreen.this,"Error ", Toast.LENGTH_SHORT).show();
                    startActivity(getIntent());

                }


            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(LoginScreen.this, "Login Failed. Please check your credentials", Toast.LENGTH_SHORT).show();
                startActivity(getIntent());
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


}