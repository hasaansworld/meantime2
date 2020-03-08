package dot.albums;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.auth.PhoneAuthProvider;

public class ForegroundService extends Service {

    public ForegroundService(){

    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1,new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopForeground(true);
            }
        }, 1000);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
