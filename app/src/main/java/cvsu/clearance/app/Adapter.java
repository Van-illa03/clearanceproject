package cvsu.clearance.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
        mStorageRef = FirebaseStorage.getInstance().getReference("signatures");
        LayoutInflater inflater;
        inflater = LayoutInflater.from(context);

            holder.StationName.setText(StationNames.get(position));
            StorageReference fileReference = mStorageRef.child(Signatures.get(position)
                    + ".jpg");

            GlideApp.with(context)
                    .load(fileReference)
                    .into((holder.imgSignatures));

            //experimental
            holder.SlotLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    holder.getBindingAdapterPosition();
                    dialogBuilder = new AlertDialog.Builder(context);
                    final View DialogView = inflater.inflate(R.layout.showstationdetails,null);
                    SignatureImg = (ImageView) DialogView.findViewById(R.id.Signature);

                    StorageReference fileReference = mStorageRef.child(Signatures.get(position)
                            + ".jpg");

                    GlideApp.with(context)
                            .load(fileReference)
                            .into((SignatureImg));


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
            SlotLayout = itemView.findViewById(R.id.SlotLayout);


        }
    }

}
