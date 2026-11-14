package com.sprintly.app;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/** Data shared between the web app (through SprintlyWidgetsPlugin) and both home-screen widgets. */
public final class WidgetStore {
    private static final String PREFS = "sprintly_widget";

    private WidgetStore() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static JSONArray arr(Context c, String key) {
        try { return new JSONArray(p(c).getString(key, "[]")); } catch (Exception e) { return new JSONArray(); }
    }

    static int num(Context c, String key) { return p(c).getInt(key, 0); }

    static boolean synced(Context c) { return p(c).getBoolean("synced", false); }

    /** Remember where a widget tap wants the app to go ("notes" or "notes_new"). */
    public static void setOpen(Context c, String target) { p(c).edit().putString("open", target).apply(); }

    public static void save(Context c, String routine, String notes, int streak, int total, int done, String dk) {
        p(c).edit().putString("routine", routine).putString("notes", notes)
            .putInt("streak", streak).putInt("total", total).putInt("done", done)
            .putString("dk", dk).putString("deleted", "[]").putBoolean("synced", true).apply();
        refresh(c);
    }

    public static void toggle(Context c, int i) {
        try {
            JSONArray a = arr(c, "routine");
            if (i < 0 || i >= a.length()) return;
            JSONObject o = a.getJSONObject(i);
            boolean d = !o.optBoolean("d");
            o.put("d", d);
            int done = Math.max(0, Math.min(num(c, "total"), num(c, "done") + (d ? 1 : -1)));
            p(c).edit().putString("routine", a.toString()).putInt("done", done).apply();
        } catch (Exception ignored) {}
    }

    public static JSONObject snapshot(Context c) {
        JSONObject o = new JSONObject();
        try {
            o.put("routine", arr(c, "routine"));
            o.put("deleted", arr(c, "deleted"));
            o.put("dk", p(c).getString("dk", ""));
            o.put("open", p(c).getString("open", ""));
            p(c).edit().remove("open").apply();
        } catch (Exception ignored) {}
        return o;
    }

    public static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] rids = m.getAppWidgetIds(new ComponentName(c, RoutineWidget.class));
        for (int id : rids) m.updateAppWidget(id, RoutineWidget.build(c));
        if (rids.length > 0) m.notifyAppWidgetViewDataChanged(rids, R.id.rlist);
        int[] nids = m.getAppWidgetIds(new ComponentName(c, NotesWidget.class));
        for (int id : nids) m.updateAppWidget(id, NotesWidget.build(c));
        if (nids.length > 0) m.notifyAppWidgetViewDataChanged(nids, R.id.nlist);
    }
}
