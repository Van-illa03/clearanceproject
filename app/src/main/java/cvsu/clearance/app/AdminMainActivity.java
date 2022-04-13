package cvsu.clearance.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

public class AdminMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

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
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    public static Context contextOfApplicationadmin;
    public static Context getContextOfApplicationadmin()
    {
        return contextOfApplicationadmin;
    }
}