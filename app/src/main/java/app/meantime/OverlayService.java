package app.meantime;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

public class OverlayService extends Service {
  private WindowManager windowManager;
  private WindowManager.LayoutParams params;
  private View overlayView;
  private FrameLayout notificationsLayout;
  private Handler handler;
  private Runnable runnable;
  private int notificationId = 1700;
  private List<DataReminder> remindersList;
  private Ringtone r;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String reminderId = intent.getStringExtra("reminderId");
    addNotification(reminderId);
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    setTheme(R.style.AppTheme);

    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    remindersList = new ArrayList<>();

    overlayView = LayoutInflater.from(this).inflate(R.layout.overlay, null);
    notificationsLayout = overlayView.findViewById(R.id.notifications_layout);

    params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? TYPE_APPLICATION_OVERLAY : TYPE_PHONE,
            FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
    params.gravity = Gravity.TOP;
    windowManager.addView(overlayView, params);

  }

  @SuppressLint("ClickableViewAccessibility")
  private void addNotification(String reminderId){
    Realm realm = Realm.getDefaultInstance();
    DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", reminderId).findFirst();
    remindersList.add(reminder);
    View notificationView = LayoutInflater.from(this).inflate(R.layout.custom_notification_medium, null);
    notificationsLayout.addView(notificationView);
    TextView time = notificationView.findViewById(R.id.time);
    TextView title = notificationView.findViewById(R.id.title);
    TextView description = notificationView.findViewById(R.id.description);
    ImageView circle = notificationView.findViewById(R.id.circle);
    if(reminder != null){
      time.setText(reminder.getTime());
      title.setText("Reminder: \""+reminder.getTitle()+"\"");
      description.setText(reminder.getDescription() != null && !reminder.getDescription().equals("") ? reminder.getDescription() : "No description.");
    }
    else{
      circle.setVisibility(View.GONE);
      time.setVisibility(View.GONE);
      title.setText("You may have pending reminders");
      description.setText("Tap to see.");
    }
    LinearLayout layout = notificationView.findViewById(R.id.layout);
    layout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            remindersList.remove(reminder);
            notificationsLayout.removeView(notificationView);
            Intent i = new Intent(OverlayService.this, FullScreenReminderActivity.class);
            i.putExtra("id", reminderId);
            i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            if(Build.VERSION.SDK_INT >= 21)
              i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_NEW_TASK);
            else
              i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            OverlayService.this.startActivity(i);
          }
        }, 400);
      }
    });

    notificationView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        notificationView.setTranslationY(0-notificationView.getHeight()-20);
        showAnimation(notificationView);
        notificationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
      }
    });

    layout.setOnTouchListener(new View.OnTouchListener() {
      private int initialX;
      private float initialTouchX;
      private float Xdiff;
      boolean shouldClick = true;
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
          case MotionEvent.ACTION_DOWN:
            initialX = Math.round(notificationView.getTranslationX());
            //get the touch location
            initialTouchX = event.getRawX();
            return false;
          case MotionEvent.ACTION_MOVE:
            Xdiff = Math.round(event.getRawX() - initialTouchX);
            //Calculate the X coordinates of the view.
            float finalX = initialX + (int) Xdiff;
            if(Math.abs(finalX) > (float)notificationView.getWidth()/8) {
              notificationView.setTranslationX(finalX);
              if (shouldClick) {
                layout.setClickable(false);
                shouldClick = false;
              }
            }
            return false;
          case MotionEvent.ACTION_UP:
            if(Math.abs(notificationView.getTranslationX()) > (float)notificationView.getWidth()/3) {
              if (notificationView.getTranslationX() < 0)
                dismissLeft(notificationView, reminder);
              else
                dismissRight(notificationView, reminder);
            }
            else
              moveToCenter(notificationView, layout);
            return false;
        }
        return true;
      }
    });

    playNotificationSound();

    if(handler != null && runnable != null)
      handler.removeCallbacks(runnable);
    finishService();
  }

  private void playNotificationSound(){
    try{
      Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      if(r != null && r.isPlaying())
        r.stop();
      r = RingtoneManager.getRingtone(this, notification);
      r.play();
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }

  private void showAnimation(View v){
    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "translationY", 0-v.getHeight()-20, 0);
    objectAnimator.setDuration(300);
    objectAnimator.start();
  }

  private void hideAnimation(View v){
    if(v != null) {
      ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "translationY", 0, 0 - v.getHeight() - 20);
      objectAnimator.setDuration(300);
      objectAnimator.start();
    }
  }

  private void dismissLeft(View v, DataReminder reminder){
    if(v != null){
      ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "translationX", v.getTranslationX(), 0-v.getWidth()-20);
      objectAnimator.setDuration(300);
      objectAnimator.start();
      objectAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          super.onAnimationEnd(animation);
          notificationsLayout.removeView(v);
          remindersList.remove(reminder);
        }
      });
    }
  }

  private void dismissRight(View v, DataReminder reminder){
    if(v != null){
      ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "translationX", v.getTranslationX(), v.getWidth()+20);
      objectAnimator.setDuration(300);
      objectAnimator.start();
      objectAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          super.onAnimationEnd(animation);
          notificationsLayout.removeView(v);
          remindersList.remove(reminder);
        }
      });
    }
  }

  private void moveToCenter(View v, View v2){
    if(v != null){
      ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "translationX", v.getTranslationX(), 0);
      objectAnimator.setDuration(100);
      objectAnimator.start();
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          if(v2 != null)
            v2.setClickable(true);
        }
      }, 150);

    }
  }

  private void finishService(){
    handler = new Handler();
    runnable = new Runnable() {
      @Override
      public void run() {
        View upperNotification = notificationsLayout.getChildAt(notificationsLayout.getChildCount()-1);
        if(upperNotification != null) upperNotification.setClickable(false);
        hideAnimation(upperNotification);
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            if(remindersList.size() > 0) {
              sendNotification(remindersList.get(remindersList.size() - 1));
              remindersList.remove(remindersList.size()-1);
            }
            if(notificationsLayout.getChildCount() > 1){
              notificationsLayout.removeView(upperNotification);
              finishService();
            }
            else {
              windowManager.removeView(overlayView);
              stopSelf();
            }
          }
        }, 350);
      }
    };
    handler.postDelayed(runnable, 5600);
  }

  private void sendNotification(DataReminder reminder){
    Intent intent = new Intent(this, FullScreenReminderActivity.class);
    intent.putExtra("id", reminder.getReminderId());
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

    createNotificationChannel();
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
            .setSmallIcon(Build.VERSION.SDK_INT >= 21 ? R.drawable.ic_notifications_none_black_24dp : R.drawable.ic_notifications_none_white_24dp);
    builder.setContentTitle("Reminder: \"" + reminder.getTitle() + "\"");
    builder.setContentText("Today at "+reminder.getTime())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setVibrate(new long[]{100, 200, 300})
            .setChannelId("1")
            .setAutoCancel(true);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    notificationManager.notify(notificationId, builder.build());
    notificationId++;
  }


  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "Low Importance Reminders";
      String description = "Get simple notifications about low importance reminders.";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel("1", name, importance);
      channel.setDescription(description);
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

}
