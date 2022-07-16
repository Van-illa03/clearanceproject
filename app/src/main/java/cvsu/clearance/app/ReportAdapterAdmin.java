package cvsu.clearance.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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

public class ReportAdapterAdmin extends RecyclerView.Adapter<ReportAdapterAdmin.ViewHolder> {
    List<String> ReportID;
    LayoutInflater layoutInflater;
    Context context;
    FirebaseFirestore mStore;
    FirebaseAuth mAuth;
    FirebaseUser mUser;


    public ReportAdapterAdmin(Context ctx, List<String> reportID){
        this.ReportID = reportID;
        this.context = ctx;
        this.layoutInflater = LayoutInflater.from(ctx);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View reportview = layoutInflater.inflate(R.layout.admin_report_layout,parent,false);
        return new ViewHolder(reportview);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();


        //fetching the report data based on the index
         mStore.collection("CompletedClearance").document(ReportID.get(position)).get()
                  .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                  @Override
                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                  DocumentSnapshot reportdatadocument = task.getResult();
                  Log.d("Report: ", "" + reportdatadocument.getString("Name"));
                  holder.StudentName.setText(reportdatadocument.getString("Name"));
                  holder.StudentNumber.setText(reportdatadocument.getString("StudentNumber"));
                  holder.Course.setText(reportdatadocument.getString("Course"));
                  holder.Status.setText(reportdatadocument.getString("Status"));
                  holder.Date.setText(reportdatadocument.getString("Date"));
                  holder.Time.setText(reportdatadocument.getString("Time"));
                   }
               });


        }



    @Override
    public int getItemCount() {
        return ReportID.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout ReportLayout;
        TextView StudentName, StudentNumber, Course, Status, Date, Time;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ReportLayout = (LinearLayout) itemView.findViewById(R.id.AdminReportLayout);
            StudentName = itemView.findViewById(R.id.adminreportlayout_NameText);
            StudentNumber = itemView.findViewById(R.id.adminreportlayout_StdNoText);
            Course = itemView.findViewById(R.id.adminreportlayout_CourseText);
            Status = itemView.findViewById(R.id.adminreportlayout_StatusText);
            Date = itemView.findViewById(R.id.adminreportlayout_DateText);
            Time = itemView.findViewById(R.id.adminreportlayout_TimeText);


        }
    }

}
