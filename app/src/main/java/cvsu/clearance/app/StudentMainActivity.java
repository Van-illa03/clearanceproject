package cvsu.clearance.app;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class StudentMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        drawerLayout = findViewById(R.id.nav_view_student);
        Toolbar toolbar = findViewById(R.id.toolbar_student);
        NavigationView navigationView = findViewById(R.id.navigationView_student);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_student,new AdminProfileFragment()).commit();

        ActionBarDrawerToggle NavToggle = new ActionBarDrawerToggle(StudentMainActivity.this,drawerLayout,toolbar,R.string.Nav_Drawer_Open,R.string.Nav_Drawer_Close);

        drawerLayout.addDrawerListener(NavToggle);
        NavToggle.syncState();


        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_student,new StudentProfileFragment()).commit();
            navigationView.setCheckedItem(R.id.studentprofile);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.staffprofile:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_student,new StudentProfileFragment()).commit();
                break;
            case R.id.assignedstation:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_student,new AdminVerifyStaffFragment()).commit();

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

    public static Context contextOfApplicationstudent;
    public static Context getContextOfApplicationstudent()
    {
        return contextOfApplicationstudent;
    }
}