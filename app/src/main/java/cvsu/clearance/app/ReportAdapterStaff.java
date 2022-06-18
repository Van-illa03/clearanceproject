package cvsu.clearance.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReportAdapterStaff extends RecyclerView.Adapter<ReportAdapterStaff.ViewHolder> {
    List<String> ReportID;
    LayoutInflater layoutInflater;
    Context context;
    FirebaseFirestore mStore;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String CurrentStaff;

    public ReportAdapterStaff(Context ctx, List<String> reportID){
        this.ReportID = reportID;
        this.context = ctx;
        this.layoutInflater = LayoutInflater.from(ctx);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View reportview = layoutInflater.inflate(R.layout.staff_report_layout,parent,false);
        return new ViewHolder(reportview);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();
        CurrentStaff = mUser.getUid();


        //getting the staff's signing station
        mStore.collection("Staff").document(CurrentStaff).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()){

                                String StaffStation = document.getString("Station");

                                //fetching the report data based on the index
                                mStore.collection("SigningStation").document(StaffStation).collection("Report").document(ReportID.get(position)).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot reportdatadocument = task.getResult();

                                                holder.StudentName.setText(reportdatadocument.getString("Name"));
                                                holder.StudentNumber.setText(reportdatadocument.getString("StudentNumber"));
                                                holder.Course.setText(reportdatadocument.getString("Course"));
                                                holder.RequirementName.setText(reportdatadocument.getString("RequirementName"));
                                                holder.Status.setText(reportdatadocument.getString("Status"));
                                                holder.Date.setText(reportdatadocument.getString("Timestamp"));
                                            }
                                        });
                            }
                        }
                    }
                });

        }



    @Override
    public int getItemCount() {
        return ReportID.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout ReportLayout;
        TextView StudentName, StudentNumber, Course, RequirementName, Status, Date;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ReportLayout = (LinearLayout) itemView.findViewById(R.id.StaffReportLayout);
            StudentName = itemView.findViewById(R.id.reportlayout_NameText);
            StudentNumber = itemView.findViewById(R.id.reportlayout_StdNoText);
            Course = itemView.findViewById(R.id.reportlayout_CourseText);
            RequirementName = itemView.findViewById(R.id.reportlayout_ReqText);
            Status = itemView.findViewById(R.id.reportlayout_StatusText);
            Date = itemView.findViewById(R.id.reportlayout_DateText);


        }
    }

}
