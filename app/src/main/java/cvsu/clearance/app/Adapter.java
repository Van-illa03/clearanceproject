package cvsu.clearance.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    List<String> StationNames;
    ArrayList<String> Signatures;
    LayoutInflater layoutInflater;
    Context context;
    StorageReference mStorageRef;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private ImageView QRImage;
    ImageView SignatureImg;
    FirebaseFirestore mStore;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    TextView SlotViewStationName, SlotViewStationLocation, SlotViewRequirementDescription;
    Spinner RequirementsSpinner;
    String [] Requirements;
    String CurrentRequirement;
    int [] firstcounter = new int[1];
    int secondcounter = 0;


    public Adapter (Context ctx, List<String> stationNames, ArrayList<String> signatures){
        this.StationNames = stationNames;
        this.Signatures = signatures;
        this.context = ctx;
        this.layoutInflater = LayoutInflater.from(ctx);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View clearanceview = layoutInflater.inflate(R.layout.clearance_slot_layout,parent,false);
        return new ViewHolder(clearanceview);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("signatures");
        LayoutInflater inflater;
        inflater = LayoutInflater.from(context);




        mStore.collection("Students").document(mUser.getUid()).collection("Stations").document(StationNames.get(position)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                boolean isNotSigned = false;

                                if (document.getString("Status").equals("Not-Signed")) {
                                    isNotSigned = true;
                                }

                                if (isNotSigned) {
                                    holder.StationName.setText(StationNames.get(position));
                                    holder.StationName.setTextColor(context.getResources().getColor(R.color.redclearance));
                                }
                                else if (!isNotSigned){
                                    holder.StationName.setText(StationNames.get(position));
                                    holder.StationName.setTextColor(context.getResources().getColor(R.color.successclearanceslot));
                                    StorageReference fileReference = mStorageRef.child(Signatures.get(position)
                                            + ".jpg");

                                    GlideApp.with(context)
                                            .load(fileReference)
                                            .into((holder.imgSignatures));
                                }
                            }
                            else {
                                Log.d("Document ", "not exists");
                            }
                        }else {
                            Log.d("Task ", "not successful");
                        }
                    }
                });

        
            holder.SlotLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    holder.getBindingAdapterPosition();
                    dialogBuilder = new AlertDialog.Builder(context);
                    final View DialogView = inflater.inflate(R.layout.showstationdetails,null);
                    firstcounter[0] = 0;
                    secondcounter = 0;


                    SlotViewStationName = (TextView) DialogView.findViewById(R.id.SignSlotStationNameText);
                    SlotViewStationLocation = (TextView) DialogView.findViewById((R.id.SignSlotStationLocationText));
                    SlotViewRequirementDescription = (TextView) DialogView.findViewById(R.id.SignSlotRequirementDescriptionText);
                    RequirementsSpinner = (Spinner) DialogView.findViewById(R.id.SignSlotRequirementsSpinner);


                    mStore.collection("SigningStation").document(StationNames.get(position)).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);
                                    SlotViewStationName.setText(catchStationDetails.getSigning_Station_Name());
                                    SlotViewStationLocation.setText(catchStationDetails.getLocation());

                                }
                            });

                    //listener whenever there is an item change in spinner
                    RequirementsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            CurrentRequirement = Requirements[pos];

                            //gets the requirement name of the specific signing station
                            mStore.collection("SigningStation").document(StationNames.get(position)).collection("Requirements").document(CurrentRequirement).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()){
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()){

                                                    String RequirementsDescriptionCatch = document.getString("Description");
                                                    SlotViewRequirementDescription.setText(RequirementsDescriptionCatch);

                                                }
                                            }

                                        }
                                    });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    //getting requirement count of the signing station
                    mStore.collection("SigningStation").document(StationNames.get(position)).collection("Requirements").get()
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


                    //inserting the requirements name into the Requirements array, then passing it in the array adapter
                    mStore.collection("SigningStation").document(StationNames.get(position)).collection("Requirements").get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    Requirements = new String [firstcounter[0]];

                                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        CatchRequirementsDetails catchRequirementsDetails = documentSnapshot.toObject(CatchRequirementsDetails.class);

                                        String RequirementsNameCatch = catchRequirementsDetails.getRequirementsName();
                                        if (RequirementsNameCatch != null) {
                                            Requirements[secondcounter] = RequirementsNameCatch;
                                            secondcounter++;
                                        }
                                    }
                                    if (Requirements.length == 0){
                                        Requirements = new String [1];
                                        Requirements[0] = "None";
                                        Log.d("Req Length is 1: ", "Requirements content:" + Requirements[0]);
                                        ArrayAdapter AA = new ArrayAdapter (context, R.layout.dropdown_item_custom, Requirements);
                                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        //Setting the ArrayAdapter data on the Spinner
                                        RequirementsSpinner.setAdapter(AA);
                                    }
                                    else {
                                        Log.d("Req Length is " + Requirements.length +": ", "Requirements content:" + Requirements[0]);
                                        ArrayAdapter AA = new ArrayAdapter (context, R.layout.dropdown_item_custom, Requirements);
                                        AA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        //Setting the ArrayAdapter data on the Spinner
                                        RequirementsSpinner.setAdapter(AA);
                                    }

                                }
                            });

                    dialogBuilder.setView(DialogView);
                    dialog = dialogBuilder.create();
                    dialog.show();
                }
            });
        }



    @Override
    public int getItemCount() {
        return StationNames.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView StationName;
        ImageView imgSignatures;
        LinearLayout SlotLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            StationName = itemView.findViewById(R.id.clearanceslotstation);
            imgSignatures = itemView.findViewById(R.id.clearanceslotsignature);
            SlotLayout = (LinearLayout) itemView.findViewById(R.id.SlotLayout);


        }
    }

}
