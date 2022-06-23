package cvsu.clearance.app;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class StudentMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

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

            case R.id.studentprofile:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_student,new StudentProfileFragment()).commit();
                break;
            case R.id.clearanceform:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_student,new StudentClearanceFragment()).commit();
                break;
            case R.id.clearanceprocedure:
                getSupportFragmentManager().beginTransaction().replace(R.id.frag_container_student,new StudentClearanceProcedureFragment()).commit();
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

    public static Context contextOfApplicationstudent;
    public static Context getContextOfApplicationstudent()
    {
        return contextOfApplicationstudent;
    }
}