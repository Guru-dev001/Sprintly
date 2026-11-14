package com.sprintly.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

/** Today's Routine - 3x2 */
public class RoutineWidget extends AppWidgetProvider {
    public static final String ACTION_TOGGLE = "com.sprintly.app.TOGGLE_ROUTINE";
    private static final int[] ROWS = {R.id.row0, R.id.row1, R.id.row2, R.id.row3};
    private static final int[] TXT = {R.id.txt0, R.id.txt1, R.id.txt2, R.id.txt3};
    private static final int[] BOX = {R.id.box0, R.id.box1, R.id.box2, R.id.box3};

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        if (ACTION_TOGGLE.equals(i.getAction())) {
            WidgetStore.toggle(c, i.getIntExtra("i", -1));
            WidgetStore.refresh(c);
        }
    }

    static RemoteViews build(Context c) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_routine);
        JSONArray a = WidgetStore.arr(c, "routine");
        int total = WidgetStore.num(c, "total");
        int done = Math.min(WidgetStore.num(c, "done"), total);
        for (int k = 0; k < 4; k++) {
            if (k >= a.length()) { rv.setViewVisibility(ROWS[k], View.GONE); continue; }
            JSONObject o = a.optJSONObject(k);
            if (o == null) { rv.setViewVisibility(ROWS[k], View.GONE); continue; }
            boolean d = o.optBoolean("d");
            SpannableString s = new SpannableString(o.optString("t"));
            if (d) s.setSpan(new StrikethroughSpan(), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            rv.setViewVisibility(ROWS[k], View.VISIBLE);
            rv.setTextViewText(TXT[k], s);
            rv.setTextColor(TXT[k], d ? 0xFF8E99AB : 0xFFF3EEE6);
            rv.setImageViewResource(BOX[k], d ? R.drawable.ic_check_on : R.drawable.ic_check_off);
            Intent it = new Intent(c, RoutineWidget.class).setAction(ACTION_TOGGLE).putExtra("i", k);
            rv.setOnClickPendingIntent(ROWS[k], PendingIntent.getBroadcast(c, k, it,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }
        int pct = total == 0 ? 0 : done * 100 / total;
        rv.setImageViewBitmap(R.id.ring, ring(c, pct));
        rv.setTextViewText(R.id.pct, pct + "%");
        rv.setTextViewText(R.id.left, (total - done) + " left of " + total);
        rv.setTextViewText(R.id.streak, String.valueOf(WidgetStore.num(c, "streak")));
        Intent open = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (open != null) {
            rv.setOnClickPendingIntent(R.id.title,
                PendingIntent.getActivity(c, 100, open, PendingIntent.FLAG_IMMUTABLE));
        }
        return rv;
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
