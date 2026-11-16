package com.sprintly.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** To-do list widget (resizable). Scrollable list of today's to-dos; tap to tick; "Add a to-do" opens a typing pop-up. */
public class TodoWidget extends AppWidgetProvider {
    public static final String ACTION_TOGGLE = "com.sprintly.app.TOGGLE_TODO";
    public static final String ACTION_SCROLL = "com.sprintly.app.SCROLL_TODO";

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
        m.notifyAppWidgetViewDataChanged(ids, R.id.tlist);
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String act = i.getAction();
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, TodoWidget.class));
        if (ACTION_TOGGLE.equals(act)) {
            WidgetStore.toggleTodo(c, i.getIntExtra("i", -1));
            RemoteViews part = new RemoteViews(c.getPackageName(), R.layout.widget_todo);
            part.setTextViewText(R.id.tcount, count(c));
            m.partiallyUpdateAppWidget(ids, part);
            m.notifyAppWidgetViewDataChanged(ids, R.id.tlist);
        } else if (ACTION_SCROLL.equals(act)) {
            RemoteViews part = new RemoteViews(c.getPackageName(), R.layout.widget_todo);
            part.setRelativeScrollPosition(R.id.tlist, i.getIntExtra("dir", 1) * 2);
            m.partiallyUpdateAppWidget(ids, part);
        }
    }

    static String count(Context c) {
        return WidgetStore.todosLeft(c) + " left";
    }

    static PendingIntent scroll(Context c, int code, int dir) {
        Intent it = new Intent(c, TodoWidget.class).setAction(ACTION_SCROLL).putExtra("dir", dir);
        return PendingIntent.getBroadcast(c, code, it, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static PendingIntent quickAdd(Context c, int code, String kind) {
        Intent it = new Intent(c, QuickAddActivity.class).setAction("com.sprintly.app.QUICK_ADD_" + kind)
            .putExtra("kind", kind).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(c, code, it, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static RemoteViews build(Context c) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_todo);
        rv.setTextViewText(R.id.tcount, count(c));
        rv.setRemoteAdapter(R.id.tlist, new Intent(c, TodoWidgetService.class));
        rv.setEmptyView(R.id.tlist, R.id.tempty);
        rv.setTextViewText(R.id.tempty, WidgetStore.synced(c) ? "No to-dos for today" : "Open Sprintly once to load your to-dos");
        Intent tpl = new Intent(c, TodoWidget.class).setAction(ACTION_TOGGLE);
        rv.setPendingIntentTemplate(R.id.tlist,
            PendingIntent.getBroadcast(c, 40, tpl, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));
        rv.setOnClickPendingIntent(R.id.tup, scroll(c, 41, -1));
        rv.setOnClickPendingIntent(R.id.tdown, scroll(c, 42, 1));
        rv.setOnClickPendingIntent(R.id.tadd, quickAdd(c, 43, "todo"));
        rv.setOnClickPendingIntent(R.id.ttitle, NotesWidget.open(c, 44, "todo", false));
        return rv;
    }
}
