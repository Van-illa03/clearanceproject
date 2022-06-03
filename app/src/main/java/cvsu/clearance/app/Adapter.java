package cvsu.clearance.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
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
    TextView SlotViewStationName, SlotViewStationLocation;

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


        mStore.collection("Students").document(mUser.getUid()).collection("Stations").document(StationNames.get(position)).collection("Requirements").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean hasIncompleteRequirements = false;
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                            if (documentSnapshot.getString("Status").equals("Incomplete")) {
                                hasIncompleteRequirements = true;
                            }
                        }

                        if (hasIncompleteRequirements == true) {
                            holder.StationName.setText(StationNames.get(position));
                        }
                        else if (hasIncompleteRequirements == false){
                            holder.StationName.setText(StationNames.get(position));
                            StorageReference fileReference = mStorageRef.child(Signatures.get(position)
                                    + ".jpg");

                            GlideApp.with(context)
                                    .load(fileReference)
                                    .into((holder.imgSignatures));
                        }
                    }
                });

            //experimental
            holder.SlotLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    holder.getBindingAdapterPosition();
                    dialogBuilder = new AlertDialog.Builder(context);
                    final View DialogView = inflater.inflate(R.layout.showstationdetails,null);

                    SlotViewStationName = (TextView) DialogView.findViewById(R.id.SignSlotStationNameText);
                    SlotViewStationLocation = (TextView) DialogView.findViewById((R.id.SignSlotStationLocationText));


                    mStore.collection("SigningStation").document(StationNames.get(position)).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    CatchStationDetails catchStationDetails = documentSnapshot.toObject(CatchStationDetails.class);
                                    SlotViewStationName.setText(catchStationDetails.getSigning_Station_Name());
                                    SlotViewStationLocation.setText(catchStationDetails.getLocation());

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
