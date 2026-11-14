package com.sprintly.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.RemoteViews;

/** Today's Routine - 3x2. Scrollable list of every habit for today; tap one to tick / untick it. */
public class RoutineWidget extends AppWidgetProvider {
    public static final String ACTION_TOGGLE = "com.sprintly.app.TOGGLE_ROUTINE";
    public static final String ACTION_SCROLL = "com.sprintly.app.SCROLL_ROUTINE";

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
        m.notifyAppWidgetViewDataChanged(ids, R.id.rlist);
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String act = i.getAction();
        if (ACTION_TOGGLE.equals(act)) {
            WidgetStore.toggle(c, i.getIntExtra("i", -1));
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            int[] ids = m.getAppWidgetIds(new ComponentName(c, RoutineWidget.class));
            // partial update keeps the list where the user scrolled it
            RemoteViews part = new RemoteViews(c.getPackageName(), R.layout.widget_routine);
            fillHeader(part, c);
            m.partiallyUpdateAppWidget(ids, part);
            m.notifyAppWidgetViewDataChanged(ids, R.id.rlist);
        } else if (ACTION_SCROLL.equals(act)) {
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            int[] ids = m.getAppWidgetIds(new ComponentName(c, RoutineWidget.class));
            RemoteViews part = new RemoteViews(c.getPackageName(), R.layout.widget_routine);
            part.setRelativeScrollPosition(R.id.rlist, i.getIntExtra("dir", 1) * 2);
            m.partiallyUpdateAppWidget(ids, part);
        }
    }

    static PendingIntent scroll(Context c, int code, int dir) {
        Intent it = new Intent(c, RoutineWidget.class).setAction(ACTION_SCROLL).putExtra("dir", dir);
        return PendingIntent.getBroadcast(c, code, it, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static RemoteViews build(Context c) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_routine);
        fillHeader(rv, c);
        rv.setRemoteAdapter(R.id.rlist, new Intent(c, RoutineWidgetService.class));
        rv.setEmptyView(R.id.rlist, R.id.rempty);
        rv.setTextViewText(R.id.rempty, WidgetStore.synced(c) ? "No habits yet" : "Open Sprintly once to load your routine");
        Intent tpl = new Intent(c, RoutineWidget.class).setAction(ACTION_TOGGLE);
        rv.setPendingIntentTemplate(R.id.rlist,
            PendingIntent.getBroadcast(c, 22, tpl, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));
        rv.setOnClickPendingIntent(R.id.rup, scroll(c, 20, -1));
        rv.setOnClickPendingIntent(R.id.rdown, scroll(c, 21, 1));
        rv.setOnClickPendingIntent(R.id.title, NotesWidget.open(c, 310, "day", false));
        return rv;
    }

    /** Ring, percentage, "left of" and streak - the part that changes when a habit is ticked. */
    static void fillHeader(RemoteViews rv, Context c) {
        int total = WidgetStore.num(c, "total");
        int done = Math.min(WidgetStore.num(c, "done"), total);
        int pct = total == 0 ? 0 : done * 100 / total;
        rv.setImageViewBitmap(R.id.ring, ring(c, pct));
        rv.setTextViewText(R.id.pct, pct + "%");
        rv.setTextViewText(R.id.left, (total - done) + " left of " + total);
        rv.setTextViewText(R.id.streak, String.valueOf(WidgetStore.num(c, "streak")));
    }

    private static Bitmap ring(Context c, int pct) {
        int s = (int) (52 * c.getResources().getDisplayMetrics().density);
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        float w = s * 0.1f;
        RectF r = new RectF(w / 2, w / 2, s - w / 2, s - w / 2);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(0xFF243044);
        cv.drawArc(r, 0f, 360f, false, p);
        p.setColor(0xFFE8702A);
        if (pct > 0) cv.drawArc(r, -90f, 360f * pct / 100f, false, p);
        return b;
    }
}
