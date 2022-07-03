package cvsu.clearance.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StaffMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);

        StaffMainActivity.contextOfApplicationstaff = getContextOfApplicationstaff();

        drawerLayout = findViewById(R.id.nav_view_staff);
        Toolbar toolbar = findViewById(R.id.toolbar_staff);
        NavigationView navigationView = findViewById(R.id.navigationView_staff);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_staff,new AdminProfileFragment()).commit();

        ActionBarDrawerToggle NavToggle = new ActionBarDrawerToggle(StaffMainActivity.this,drawerLayout,toolbar,R.string.Nav_Drawer_Open,R.string.Nav_Drawer_Close);

        drawerLayout.addDrawerListener(NavToggle);
        NavToggle.syncState();


        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_staff,new StaffProfileFragment()).commit();
            navigationView.setCheckedItem(R.id.staffprofile);
        }



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.staffprofile:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_staff,new StaffProfileFragment()).commit();
                break;
            case R.id.scanqr:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_staff,new StaffScanQRFragment()).commit();
                break;
            case R.id.requirements:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_staff,new StaffRequirementsFragment()).commit();
                break;
            case R.id.staffreport:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_staff,new StaffReportFragment()).commit();
                break;
            case R.id.logoutstaff:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), FrontScreen.class));
                finish();
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
                FirebaseAuth.getInstance().signOut();
                finishAffinity();
                System.exit(0);
            } else {
                Toast.makeText(getBaseContext(), "Click again to exit",    Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    }
    public static Context contextOfApplicationstaff;
    public static Context getContextOfApplicationstaff()
    {
        return StaffMainActivity.contextOfApplicationstaff;
    }


}