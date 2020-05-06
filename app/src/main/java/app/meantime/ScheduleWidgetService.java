package app.meantime;

import android.content.Intent;
import android.widget.RemoteViewsService;
import android.widget.Toast;

public class ScheduleWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScheduleWidgetListProvider(getApplicationContext(), intent);
    }
}
