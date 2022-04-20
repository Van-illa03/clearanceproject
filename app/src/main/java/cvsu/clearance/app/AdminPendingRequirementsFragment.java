package cvsu.clearance.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

public class AdminPendingRequirementsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Activity currentActivity = this.getActivity();
    private long mLastClickTime = 0;
    CollectionReference reqcollection;
    public String[] ArrayRequirements;
    private int [] firstcounter = new int[2];
    public int secondcounter = 0;
    public String CurrentRequirement;
    TextView ReqName,ReqLoc, ReqDesignatedStation, ReqDescription;
    Button VerifyButton, DenyButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adminpendingrequirementsfragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();
        ReqName = view.findViewById(R.id.RequirementsNameText);
        ReqDescription = view.findViewById(R.id.RequirementsDescText);
        ReqLoc = view.findViewById(R.id.RequirementsLocationText);
        ReqDesignatedStation = view.findViewById(R.id.RequirementsDesignationText);
        TextView verifyButton = view.findViewById(R.id.gotoVerifyStaff);
        reqcollection = mStore.collection("PendingRequirements");
        VerifyButton =(Button) view.findViewById(R.id.VerifyReqButton);
        DenyButton =(Button) view.findViewById(R.id.DenyReqButton);


        if (mAuth.getCurrentUser() == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("Please log in first.");
            alert.setPositiveButton("OK", null);
            alert.show();
            startActivity(new Intent(getContext(), LoginScreen.class));
        }


        Spinner spin = view.findViewById(R.id.PendingRequirementsSpinner);
        spin.setOnItemSelectedListener(this);

        // this method counts the number of fetched pending requirements from
        // firestore, the value will be used as the size of the array that will
        // contain the pending requirements
        reqcollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                            String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                            if (RequirementsNameCatch != null) {
                                    firstcounter[0] = firstcounter[0] + 1;
                            }
                        }
                    }
                });
        //the signing station names will be passed in the array through the "ArraRequirements" object
        reqcollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayRequirements = new String [firstcounter[0]];

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                            String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                            if (RequirementsNameCatch != null) {
                                    ArrayRequirements[secondcounter] = RequirementsNameCatch;
                                    secondcounter++;
                            }
                        }
                        ArrayAdapter AA = new ArrayAdapter (getContext(), android.R.layout.simple_spinner_item, ArrayRequirements);
                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        //Setting the ArrayAdapter data on the Spinner
                        spin.setAdapter(AA);
                    }
                });


        DenyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStore.collection("PendingRequirements").document(CurrentRequirement).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Success");
                        alert.setMessage("The specific requirement has been deleted.");
                        alert.setPositiveButton("OK", null);
                        alert.show();

                        // Reload current fragment
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        AdminPendingRequirementsFragment aprf = new AdminPendingRequirementsFragment();
                        ft.replace(R.id.frag_container, aprf);
                        ft.commit();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Error");
                        alert.setMessage("An error occurred in deleting the requirement document.");
                        alert.setPositiveButton("OK", null);
                        alert.show();
                    }
                });
            }
        });


        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        CurrentRequirement = ArrayRequirements[position];
        mStore.collection("PendingRequirements").document(CurrentRequirement).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        CatchRequirementsDetails catchRequirementsDetails = document.toObject(CatchRequirementsDetails.class);

                        String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                        String RequirementsLocationCatch = catchRequirementsDetails.getLocation();
                        String RequirementsDesignationCatch = catchRequirementsDetails.getSigningStation();
                        String RequirementsDescriptionCatch = catchRequirementsDetails.getDescription();

                        ReqName.setText(RequirementsNameCatch);
                        ReqLoc.setText(RequirementsLocationCatch);
                        ReqDescription.setText(RequirementsDescriptionCatch);
                        ReqDesignatedStation.setText(RequirementsDesignationCatch);

                    }
                }

            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
