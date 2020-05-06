package app.meantime;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class ScheduleWidgetReceiver extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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

            Intent createIntent = new Intent(context, CreateActivity.class);
            PendingIntent pendingIntentCreate = PendingIntent.getActivity(context, 0, createIntent, 0);
            views.setOnClickPendingIntent(R.id.add, pendingIntentCreate);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(action != null && action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, ScheduleWidgetReceiver.class);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.list_reminders);
        }
        super.onReceive(context, intent);
    }

    public static void refreshList(Context context){
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, ScheduleWidgetReceiver.class));
        context.sendBroadcast(intent);
    }
}
