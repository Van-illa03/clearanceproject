package cvsu.clearance.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class App extends Application {
    public static final String CHANNEL_1_ID = "Downloads";
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mStore;
    @Override
    public void onCreate(){
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStore = FirebaseFirestore.getInstance();

        createNotificationChannel();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sessionManagement();
            }
        }, 500);




    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel Downloads = new NotificationChannel(CHANNEL_1_ID, "Downloads", NotificationManager.IMPORTANCE_HIGH);
            Downloads.setDescription("Downloads Notification for CVSU Clearance APP");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(Downloads);

        }


    }

    public void sessionManagement() {

        if (mUser!=null){

            String currentUser = mUser.getUid();

            mStore.collection("Admin").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                        String docuId = document.getId();
                        if(docuId.equals(currentUser)){
                            adminStart();
                            break;
                        }
                    }
                }
            });

            mStore.collection("Staff").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                        String docuId = document.getId();
                        if(docuId.equals(currentUser)){
                            staffStart();
                            break;
                        }
                    }
                }
            });

            mStore.collection("Students").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                        String docuId = document.getId();
                        if(docuId.equals(currentUser)){
                            studentStart();
                            break;
                        }
                    }
                }
            });

        }

        else{
            Intent intent= new Intent(App.this, FrontScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }




    }

    private void adminStart(){
        Intent intent= new Intent(App.this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void studentStart(){
        Intent intent= new Intent(App.this, StudentMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void staffStart(){
        Intent intent= new Intent(App.this, StaffMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }




}
