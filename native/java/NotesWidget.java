package com.sprintly.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import org.json.JSONArray;

/** Notes - 2x2. Shows ONE note per page (title + content in a fixed space); swipe or use the arrows for the next note. Tapping a note opens the Notes page in the app. */
public class NotesWidget extends AppWidgetProvider {
    public static final String ACTION_SCROLL = "com.sprintly.app.SCROLL_NOTES";

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (ACTION_SCROLL.equals(i.getAction())) {
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            int[] ids = m.getAppWidgetIds(new ComponentName(c, NotesWidget.class));
            RemoteViews part = new RemoteViews(c.getPackageName(), R.layout.widget_notes);
            if (i.getIntExtra("dir", 1) < 0) part.showPrevious(R.id.nlist); else part.showNext(R.id.nlist);
            m.partiallyUpdateAppWidget(ids, part);
        }
    }

    static PendingIntent scroll(Context c, int code, int dir) {
        Intent it = new Intent(c, NotesWidget.class).setAction(ACTION_SCROLL).putExtra("dir", dir);
        return PendingIntent.getBroadcast(c, code, it, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c, id));
        m.notifyAppWidgetViewDataChanged(ids, R.id.nlist);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, android.os.Bundle opts) {
        m.updateAppWidget(id, build(c, id));
        m.notifyAppWidgetViewDataChanged(id, R.id.nlist);
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

    static RemoteViews build(Context c, int widgetId) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_notes);
        JSONArray a = WidgetStore.arr(c, "notes");
        rv.setTextViewText(R.id.ncount, String.valueOf(a.length()));
        Intent svc = new Intent(c, NotesWidgetService.class);
        svc.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        svc.setData(android.net.Uri.parse(svc.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.nlist, svc);
        rv.setEmptyView(R.id.nlist, R.id.nempty);
        rv.setTextViewText(R.id.nempty, WidgetStore.synced(c) ? "No notes yet" : "Open Sprintly once to load your notes");
        rv.setPendingIntentTemplate(R.id.nlist, open(c, 300, "notes", true));
        rv.setOnClickPendingIntent(R.id.ntitle, open(c, 301, "notes", false));
        rv.setOnClickPendingIntent(R.id.nadd, TodoWidget.quickAdd(c, 302, "note"));
        rv.setOnClickPendingIntent(R.id.nup, scroll(c, 304, -1));
        rv.setOnClickPendingIntent(R.id.ndown, scroll(c, 305, 1));
        return rv;
    }
}
