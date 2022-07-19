package cvsu.clearance.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminResetClearanceFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Activity currentActivity = this.getActivity();
    private long mLastClickTime = 0;
    Button ResetClearanceButton;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    StorageReference mStorageRef;
    AlertDialog dialogg;
    AlertDialog.Builder dialogBuilder;
    private EditText ClearanceReset_Password;
    private Button ClearanceReset_Proceed;
    AlertDialog.Builder alert;



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
        mStorageRef = FirebaseStorage.getInstance().getReference("Requirements");





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

                alert = new AlertDialog.Builder(getContext());
                alert.setTitle(Html.fromHtml("<font color='#E84A5F'>Warning</font>")).setMessage("This will reset the e-clearance form data. Are you sure?")
                        .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialogBuilder = new AlertDialog.Builder(getContext());
                                final View DialogView = getLayoutInflater().inflate(R.layout.clearancereset_passwordinterface,null);
                                ClearanceReset_Password = (EditText) DialogView.findViewById(R.id.clearancereset_passwordtext);
                                ClearanceReset_Proceed = (Button) DialogView.findViewById(R.id.clearancereset_proceedbtn);
                                dialogBuilder.setTitle("Reset Clearance Form");
                                dialogBuilder.setView(DialogView);
                                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        Toast.makeText(getActivity().getApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                                    }
                                });
                                dialogg = dialogBuilder.create();
                                dialogg.show();

                                dialogg.getWindow().setSoftInputMode(
                                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                                ClearanceReset_Proceed.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String Password = ClearanceReset_Password.getText().toString().trim();

                                        if (Password.isEmpty()){
                                            ClearanceReset_Password.setError("Please enter your password");
                                            ClearanceReset_Password.requestFocus();
                                        }
                                        else {
                                          mStore.collection("Admin").document(mUser.getUid()).get()
                                                  .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                      @Override
                                                      public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                          String Email = documentSnapshot.getString("Email");
                                                          AuthCredential credential = EmailAuthProvider.getCredential(Email, Password);

                                                          mUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                              @Override
                                                              public void onSuccess(Void unused) {

                                                                dialogg.dismiss();
                                                                Log.d("test" , "success");

                                                                  progressDialog.setMessage("Resetting e-clearance form data. This might take a while...");
                                                                  progressDialog.setTitle("Reset E-clearance Data");
                                                                  progressDialog.setCanceledOnTouchOutside(false);
                                                                  progressDialog.show();

                                                                  //getting the students
                                                                  mStore.collection("Students").get()
                                                                          .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                              @Override
                                                                              public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                                  //loop for each student document
                                                                                  for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                                      String studentDocu = document.getId();

                                                                                      //passing the student document ID
                                                                                      StudentCollectionProcesses(studentDocu);
                                                                                  }
                                                                              }
                                                                          });

                                                                  //getting the signing stations
                                                                  mStore.collection("SigningStation").get()
                                                                          .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                              @Override
                                                                              public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                  for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                                                                                      //getting the signing station names (Document ID)
                                                                                      String StationName = documentSnapshot.getId();

                                                                                      //getting the Requirements Collection inside the Signing station document
                                                                                      mStore.collection("SigningStation").document(StationName).collection("Requirements").get()
                                                                                              .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                                  @Override
                                                                                                  public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                                                      for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                                                                                                          String RequirementDocu = document.getId();

                                                                                                          // Get reference to the file
                                                                                                          StorageReference fileRef = mStorageRef.child(StationName+"_"+RequirementDocu.trim()+".csv");

                                                                                                          //create method for deleting requirements csv
                                                                                                          deleteRequirementsCSVFile(fileRef);


                                                                                                          DocumentReference RequirementRef = mStore.collection("SigningStation").document(StationName).collection("Requirements").document(RequirementDocu);

                                                                                                          deleteRequirementOnStation(RequirementRef);


                                                                                                      }
                                                                                                  }
                                                                                              });

                                                                                      //getting the reports collection
                                                                                      mStore.collection("SigningStation").document(StationName).collection("Report").get()
                                                                                              .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                                  @Override
                                                                                                  public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                                      for(QueryDocumentSnapshot ReportDocu: queryDocumentSnapshots){
                                                                                                          String ReportDocuID = ReportDocu.getId();

                                                                                                          DocumentReference ReportDocuRef =  mStore.collection("SigningStation").document(StationName).collection("Report").document(ReportDocuID);

                                                                                                          //passing the document reference of each report file
                                                                                                          deleteStationReport(ReportDocuRef);
                                                                                                      }
                                                                                                  }
                                                                                              });
                                                                                  }
                                                                              }
                                                                          });

                                                                  mStore.collection("CompletedClearance").orderBy("ID").startAt().endAt().get()
                                                                          .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                              @Override
                                                                              public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                                  for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                                                                                      String ReportID = documentSnapshot.getId();

                                                                                      DocumentReference ReportRef = mStore.collection("CompletedClearance").document(ReportID);
                                                                                      deleteAdminReport(ReportRef);
                                                                                  }
                                                                              }
                                                                          });


                                                                  progressDialog.dismiss();
                                                                  AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                                                                  alert.setTitle(Html.fromHtml("<font color='#20BF55'>Successful</font>"));
                                                                  alert.setMessage("Reset Complete");
                                                                  alert.setPositiveButton("OK", null);
                                                                  alert.show();
                                                              }
                                                          }).addOnFailureListener(new OnFailureListener() {
                                                              @Override
                                                              public void onFailure(@NonNull Exception e) {
                                                                  ClearanceReset_Password.setError("Incorrect Password");
                                                                  ClearanceReset_Password.requestFocus();
                                                              }
                                                          });
                                                      }
                                                  });



                                        }

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

    public void StudentCollectionProcesses (String studentDocument){

        //getting the Station Collection in the particular student document
        mStore.collection("Students").document(studentDocument).collection("Stations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //loop for each signing station name
                        for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){

                            //Getting the signing station name
                            String StationName = documentSnapshot.getString("Signing_Station_Name");

                            //makes a document reference of station document in student that
                            // will be used to alter the signing status of stations from Signed to Not-Signed
                            DocumentReference StationDocRef = mStore.collection("Students").document(studentDocument).collection("Stations").document(StationName);

                            changeStationStatus(StationDocRef,StationName);

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

    public void deleteRequirementOnStation (DocumentReference ReqDocuRef){
        ReqDocuRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("Requirement on Station:", "Deleted");
            }
        });
    }

    public void changeStationStatus (DocumentReference StationDocuRef, String StationName){

        mStore.collection("SigningStation").document(StationName).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String StationIsRequired = documentSnapshot.getString("isRequired");

                        Map<String, Object> StationInfo = new HashMap<>();

                        if (StationIsRequired.equals("Required")){
                            StationInfo.put("Signing_Station_Name",StationName);
                            StationInfo.put("Status","Not-Signed");

                            StationDocuRef.update(StationInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                }
                            });
                        }
                        else if (StationIsRequired.equals("")){
                            StationInfo.put("Signing_Station_Name",StationName);
                            StationInfo.put("Status","Signed");

                            StationDocuRef.update(StationInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                }
                            });
                        }
                    }
                });
    }

    public void deleteAdminReport (DocumentReference ReportDocuRef){
        ReportDocuRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });
    }

    public void deleteStationReport(DocumentReference ReportDocuRef){
        ReportDocuRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });
    }

    public void deleteRequirementsCSVFile (StorageReference ReqFileRef){
        ReqFileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
    }
}
