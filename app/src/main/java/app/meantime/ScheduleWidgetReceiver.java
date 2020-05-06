package app.meantime;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
