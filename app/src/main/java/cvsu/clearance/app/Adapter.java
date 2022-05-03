package cvsu.clearance.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    List<String> StationNames;
    String [] Signatures;
    LayoutInflater layoutInflater;
    Context context;
    StorageReference mStorageRef;

    public Adapter (Context ctx, List<String> stationNames, String [] signatures){
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mStorageRef = FirebaseStorage.getInstance().getReference("signatures");

        holder.StationName.setText(StationNames.get(position));

        StorageReference fileReference = mStorageRef.child(Signatures[position]
                + ".jpg");

        GlideApp.with(context)
                .load(fileReference)
                .into((holder.imgSignatures));

    }

    @Override
    public int getItemCount() {
        return StationNames.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView StationName;
        ImageView imgSignatures;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            StationName = itemView.findViewById(R.id.clearanceslotstation);
            imgSignatures = itemView.findViewById(R.id.clearanceslotsignature);
        }
    }

}
