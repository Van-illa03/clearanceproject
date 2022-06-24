package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);


        contextOfApplicationadmin = getApplicationContext();
        drawerLayout = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminProfileFragment()).commit();

        ActionBarDrawerToggle NavToggle = new ActionBarDrawerToggle(AdminMainActivity.this,drawerLayout,toolbar,R.string.Nav_Drawer_Open,R.string.Nav_Drawer_Close);

        drawerLayout.addDrawerListener(NavToggle);
        NavToggle.syncState();


        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminProfileFragment()).commit();
            navigationView.setCheckedItem(R.id.adminprofile);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.adminprofile:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminProfileFragment()).commit();
                break;
            case R.id.verifystaff:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminVerifyStaffFragment()).commit();
                break;
            case R.id.viewstation:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminViewStationFragment()).commit();
                break;
            case R.id.addstation:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminAddStationFragment()).commit();
                break;
            case R.id.pendingrequirements:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminPendingRequirementsFragment()).commit();
                break;
            case R.id.AdminReport:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new AdminReportFragment()).commit();
                break;
            case R.id.logoutadmin:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), FrontScreen.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if(!drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.openDrawer(GravityCompat.START);
        }
        else {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), "Click again to exit",    Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    }

    public static Context contextOfApplicationadmin;
    public static Context getContextOfApplicationadmin()
    {
        return contextOfApplicationadmin;
    }
}