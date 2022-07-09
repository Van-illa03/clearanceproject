package cvsu.clearance.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminResetClearanceFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Activity currentActivity = this.getActivity();
    private long mLastClickTime = 0;
    Button ResetClearanceButton;
    ProgressDialog progressDialog;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adminresetclearancefragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        ResetClearanceButton = view.findViewById(R.id.ResetClearanceBtn);
        progressBar     =   view.findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(getContext());



        if (mAuth.getCurrentUser() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("Please log in first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            startActivity(new Intent(getContext(), LoginScreen.class));
        }


        ResetClearanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Warning").setMessage("This will reset the e-clearance form data. Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Resetting e-clearance form data...");
                                progressDialog.setTitle("Reset E-clearance Data");
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();

                                mStore.collection("Students").get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                //loop for each student document
                                                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                    String studentDocu = document.getId();

                                                    //passing the student document ID
                                                    deleteRequirementCollectionOnStudent(studentDocu);
                                                }

                                                progressDialog.dismiss();
                                                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                                alert.setTitle("Success");
                                                alert.setMessage("Reset Complete");
                                                alert.setPositiveButton("OK", null);
                                                alert.show();
                                            }
                                        });

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                            }
                        });
                alert.show();
            }
        });

        return view;
    }

    public void deleteRequirementCollectionOnStudent (String studentDocument){

        //getting the Station Collection in the particular student document
        mStore.collection("Students").document(studentDocument).collection("Stations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //loop for each signing station name
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            //Getting the signing station name
                            String StationName = documentSnapshot.getString("Signing_Station_Name");

                            CollectionReference RequirementReference = mStore.collection("Students").document(studentDocument).collection("Stations").document(StationName).collection("Requirements");

                            //Getting the Requirement collection in the particular signing station name
                            RequirementReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    //loop for every document in the requirements collection
                                    for (QueryDocumentSnapshot documentSnapshots: queryDocumentSnapshots){
                                        String requirementDocumentID = documentSnapshots.getId();
                                        DocumentReference docuRef = mStore.collection("Students").document(studentDocument).collection("Stations").document(StationName).collection("Requirements").document(requirementDocumentID);

                                        deleteRequirementDocument(docuRef);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    public void deleteRequirementDocument (DocumentReference ReqDocuRef){
        ReqDocuRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("Requirement on Student:", "Deleted");
            }
        });
    }
}
