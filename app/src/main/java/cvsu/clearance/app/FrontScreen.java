package cvsu.clearance.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class FrontScreen extends AppCompatActivity {

    Button buttonStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_screen);

        buttonStart =   findViewById(R.id.buttonStart);


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Temporarily StaffActivity starting point for testing purposes.
                startActivity(new Intent(getApplicationContext(),StaffActivity.class));
            }
        });
    }
}