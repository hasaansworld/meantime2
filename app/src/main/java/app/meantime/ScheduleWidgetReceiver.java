package app.meantime;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ScheduleWidgetReceiver extends AppWidgetProvider {

    SharedPreferences sharedPreferences;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        for(int i = 0; i < appWidgetIds.length; i++){
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, ScheduleWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_widget);
            views.setRemoteAdapter(R.id.list_reminders, intent);
            Intent detailsIntent = new Intent(context, ReminderActivity.class);
            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(detailsIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.list_reminders, pendingIntent);
            views.setEmptyView(R.id.list_reminders, R.id.empty);

            Intent createIntent = new Intent(context, CreateActivity.class);
            PendingIntent pendingIntentCreate = PendingIntent.getActivity(context, 0, createIntent, 0);
            views.setOnClickPendingIntent(R.id.add, pendingIntentCreate);

            views.setOnClickPendingIntent(R.id.today, getPendingSelfIntent(context, "ACTION_TODAY", appWidgetId));
            views.setOnClickPendingIntent(R.id.tomorrow, getPendingSelfIntent(context, "ACTION_TOMORROW", appWidgetId));

            String widgetMode = sharedPreferences.getString("widgetMode"+appWidgetId, "today");
            views.setViewVisibility(R.id.indicator_today, widgetMode != null && widgetMode.equals("today") ? View.VISIBLE : View.INVISIBLE);
            views.setViewVisibility(R.id.indicator_tomorrow, widgetMode != null && widgetMode.equals("tomorrow") ? View.VISIBLE : View.INVISIBLE);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra("id", String.valueOf(appWidgetId));
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }

    private void updateAppWidget(Context context, RemoteViews views, int appWidgetId){
        Intent intentUpdate = new Intent(context, ScheduleWidgetReceiver.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context,
                appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        //views.setOnClickPendingIntent(R.id.button_update, pendingUpdate);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);

        final String action = intent.getAction();
        if(action != null && action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, ScheduleWidgetReceiver.class);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.list_reminders);
        }
        else if(action != null && action.equals("ACTION_TODAY")){
            switchMode(context, intent, "today");
        }
        else if(action != null && action.equals("ACTION_TOMORROW")){
            switchMode(context, intent, "tomorrow");
        }
        super.onReceive(context, intent);
    }

    private void switchMode(Context context, Intent intent, String mode){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("widgetMode"+intent.getStringExtra("id"), mode);
        editor.apply();
        refreshList(context);
        onUpdate(context, AppWidgetManager.getInstance(context), new int[]{Integer.parseInt(intent.getStringExtra("id"))});
    }

    public static void refreshList(Context context){
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, ScheduleWidgetReceiver.class));
        context.sendBroadcast(intent);
    }
}
