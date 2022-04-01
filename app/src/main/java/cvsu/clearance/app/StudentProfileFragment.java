package cvsu.clearance.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentProfileFragment extends Fragment{
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mStore;
    Button logoutButton;
    Activity currentActivity = this.getActivity();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.studentprofilefragment,container,false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore  =   FirebaseFirestore.getInstance();
        logoutButton = view.findViewById(R.id.logoutButton);
        TextView User = (TextView) view.findViewById(R.id.WelcomeStudent);
        TextView DisplayEmail = view.findViewById(R.id.DisplayEmail);
        TextView DisplayStdNo = view.findViewById(R.id.DisplayStdNo);
        TextView DisplayCourse = view.findViewById(R.id.DisplayCourse);

        String[] languages = getResources().getStringArray(R.array.roles);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(currentActivity, "You are not logged in. Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getContext(), LoginScreen.class));

        }
        else {
            User.setText(""+mUser.getDisplayName());

        }

        DocumentReference docRef = mStore.collection("Students").document(mUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Retrieve data", "DocumentSnapshot data: " + document.getData());

                        String DocuEmail = (String) document.get("Email");
                        DisplayEmail.setText(DocuEmail);
                        String DocuStdNo = (String) document.get("StdNo");
                        DisplayStdNo.setText(DocuStdNo);
                        String DocuCourse = (String) document.get("Course");
                        DisplayCourse.setText(DocuCourse);
                    } else {
                        Log.d("Failed Retrieve data", "No such document");
                    }
                } else {
                    Log.d("Error", "get failed with ", task.getException());
                }
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), FrontScreen.class));


            }
        });
        return view;
    }
}
