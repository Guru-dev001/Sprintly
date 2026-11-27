package com.sprintly.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import org.json.JSONArray;

/** Notes - fixed 2x2. Shows one note at a time (title + content fitted to the card); swipe for the next. Tap opens the Notes page. */
public class NotesWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
        m.notifyAppWidgetViewDataChanged(ids, R.id.nlist);
    }

    static PendingIntent open(Context c, int code, String target, boolean template) {
        Intent i = new Intent(c, MainActivity.class)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (!template) i.putExtra("sprintly_open", target);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | (template ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_IMMUTABLE);
        return PendingIntent.getActivity(c, code, i, flags);
    }

    static RemoteViews build(Context c) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_notes);
        JSONArray a = WidgetStore.arr(c, "notes");
        rv.setTextViewText(R.id.ncount, a.length() + (a.length() == 1 ? " note" : " notes"));
        rv.setRemoteAdapter(R.id.nlist, new Intent(c, NotesWidgetService.class));
        rv.setEmptyView(R.id.nlist, R.id.nempty);
        rv.setTextViewText(R.id.nempty, WidgetStore.synced(c) ? "No notes yet" : "Open Sprintly once to load your notes");
        rv.setPendingIntentTemplate(R.id.nlist, open(c, 300, "notes", true));
        rv.setOnClickPendingIntent(R.id.ntitle, open(c, 301, "notes", false));
        rv.setOnClickPendingIntent(R.id.nadd, TodoWidget.quickAdd(c, 302, "note"));
        return rv;
    }
}
