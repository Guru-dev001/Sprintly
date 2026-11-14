package com.sprintly.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

/** Notes - 2x2 */
public class NotesWidget extends AppWidgetProvider {
    public static final String ACTION_DELETE = "com.sprintly.app.DELETE_NOTE";
    private static final int[] ROWS = {R.id.nrow0, R.id.nrow1, R.id.nrow2};
    private static final int[] TXT = {R.id.ntxt0, R.id.ntxt1, R.id.ntxt2};
    private static final int[] DEL = {R.id.ndel0, R.id.ndel1, R.id.ndel2};

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (ACTION_DELETE.equals(i.getAction())) {
            WidgetStore.deleteNote(c, i.getIntExtra("i", -1));
            WidgetStore.refresh(c);
        }
    }

    static RemoteViews build(Context c) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_notes);
        JSONArray a = WidgetStore.arr(c, "notes");
        rv.setTextViewText(R.id.ncount, a.length() + (a.length() == 1 ? " note" : " notes"));
        for (int k = 0; k < 3; k++) {
            JSONObject o = k < a.length() ? a.optJSONObject(k) : null;
            if (o == null) { rv.setViewVisibility(ROWS[k], View.GONE); continue; }
            rv.setViewVisibility(ROWS[k], View.VISIBLE);
            rv.setTextViewText(TXT[k], o.optString("t"));
            Intent it = new Intent(c, NotesWidget.class).setAction(ACTION_DELETE).putExtra("i", k);
            rv.setOnClickPendingIntent(DEL[k], PendingIntent.getBroadcast(c, 10 + k, it,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }
        Intent open = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (open != null) {
            PendingIntent pi = PendingIntent.getActivity(c, 200, open, PendingIntent.FLAG_IMMUTABLE);
            rv.setOnClickPendingIntent(R.id.nadd, pi);
            rv.setOnClickPendingIntent(R.id.ntitle, pi);
        }
        return rv;
    }
}
