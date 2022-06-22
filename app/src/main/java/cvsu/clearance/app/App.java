package cvsu.clearance.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_1_ID = "Downloads";


    @Override
    public void onCreate(){
        super.onCreate();

        createNotificationChannel();

    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel Downloads = new NotificationChannel(CHANNEL_1_ID, "Downloads", NotificationManager.IMPORTANCE_HIGH);
            Downloads.setDescription("Downloads Notification in CVSU Clearance APP");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(Downloads);

        }


    }

}
