package com.sprintly.app;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/** Data shared between the web app (through SprintlyWidgetsPlugin) and the home-screen widgets. */
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

    /** Remember where a widget tap wants the app to go ("notes", "notes_new", "todo"). */
    public static void setOpen(Context c, String target) { p(c).edit().putString("open", target).apply(); }

    /** Called by the app: replaces the widget data with what the app currently holds. */
    public static void save(Context c, String routine, String notes, String todos, int streak,
                            int total, int done, String dk, String tdk, String ack) {
        p(c).edit().putString("routine", routine).putString("notes", notes).putString("todos", todos)
            .putInt("streak", streak).putInt("total", total).putInt("done", done)
            .putString("dk", dk).putString("tdk", tdk).putBoolean("synced", true).apply();
        // forget items the app has confirmed, keep (and keep showing) the ones it has not seen yet
        try {
            JSONArray acked = new JSONArray(ack == null ? "[]" : ack);
            JSONArray adds = arr(c, "adds"), keep = new JSONArray();
            for (int i = 0; i < adds.length(); i++) {
                JSONObject a = adds.getJSONObject(i);
                boolean gone = false;
                for (int k = 0; k < acked.length(); k++) if (a.optString("id").equals(acked.optString(k))) gone = true;
                if (!gone) keep.put(a);
            }
            p(c).edit().putString("adds", keep.toString()).apply();
            showPending(c, keep);
        } catch (Exception ignored) {}
        refresh(c);
    }

    private static void showPending(Context c, JSONArray adds) throws Exception {
        JSONArray todos = arr(c, "todos"), notes = arr(c, "notes");
        for (int i = 0; i < adds.length(); i++) {
            JSONObject a = adds.getJSONObject(i);
            if ("todo".equals(a.optString("k"))) todos.put(tempTodo(a));
            else notes = prepend(notes, tempNote(a));
        }
        p(c).edit().putString("todos", todos.toString()).putString("notes", notes.toString()).apply();
    }

    private static JSONObject tempTodo(JSONObject a) throws Exception {
        return new JSONObject().put("id", a.optString("id")).put("t", a.optString("t"))
            .put("d", false).put("d0", false).put("p", "med");
    }

    private static JSONObject tempNote(JSONObject a) throws Exception {
        String t = a.optString("t");
        if (t.isEmpty()) t = a.optString("b").split("\n")[0];
        return new JSONObject().put("id", a.optString("id")).put("t", t);
    }

    private static JSONArray prepend(JSONArray old, JSONObject first) throws Exception {
        JSONArray out = new JSONArray();
        out.put(first);
        for (int i = 0; i < old.length(); i++) out.put(old.get(i));
        return out;
    }

    /** Something typed on a widget: show it at once and hand it to the app next time it opens. */
    public static void addPending(Context c, String kind, String title, String body) {
        try {
            String id = "w~" + System.currentTimeMillis();
            JSONObject a = new JSONObject().put("id", id).put("k", kind).put("t", title).put("b", body);
            JSONArray adds = arr(c, "adds");
            adds.put(a);
            p(c).edit().putString("adds", adds.toString()).apply();
            if ("todo".equals(kind)) {
                JSONArray todos = arr(c, "todos");
                todos.put(tempTodo(a));
                p(c).edit().putString("todos", todos.toString()).apply();
            } else {
                p(c).edit().putString("notes", prepend(arr(c, "notes"), tempNote(a)).toString()).apply();
            }
        } catch (Exception ignored) {}
        refresh(c);
    }

    public static void toggle(Context c, int i) { flip(c, "routine", i, true); }

    public static void toggleTodo(Context c, int i) { flip(c, "todos", i, false); }

    private static void flip(Context c, String key, int i, boolean routine) {
        try {
            JSONArray a = arr(c, key);
            if (i < 0 || i >= a.length()) return;
            JSONObject o = a.getJSONObject(i);
            boolean d = !o.optBoolean("d");
            o.put("d", d);
            SharedPreferences.Editor e = p(c).edit().putString(key, a.toString());
            if (routine) e.putInt("done", Math.max(0, Math.min(num(c, "total"), num(c, "done") + (d ? 1 : -1))));
            e.apply();
        } catch (Exception ignored) {}
    }

    public static int todosLeft(Context c) {
        JSONArray a = arr(c, "todos");
        int n = 0;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && !o.optBoolean("d")) n++;
        }
        return n;
    }

    /** What the app needs to apply: ticks made on the widgets and items typed on them. */
    public static JSONObject snapshot(Context c) {
        JSONObject o = new JSONObject();
        try {
            o.put("routine", arr(c, "routine"));
            o.put("todos", arr(c, "todos"));
            o.put("adds", arr(c, "adds"));
            o.put("dk", p(c).getString("dk", ""));
            o.put("tdk", p(c).getString("tdk", ""));
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
        int[] tids = m.getAppWidgetIds(new ComponentName(c, TodoWidget.class));
        for (int id : tids) m.updateAppWidget(id, TodoWidget.build(c));
        if (tids.length > 0) m.notifyAppWidgetViewDataChanged(tids, R.id.tlist);
    }
}
