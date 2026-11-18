package com.sprintly.app;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import org.json.JSONArray;
import org.json.JSONObject;

/** Feeds the Notes widget: one page per note (title + content), swipe to move to the next note. */
public class NotesWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new Factory(getApplicationContext(), id);
    }

    static class Factory implements RemoteViewsFactory {
        private final Context c;
        private final int widgetId;
        private JSONArray a = new JSONArray();
        private int lines = 3;

        Factory(Context c, int widgetId) { this.c = c; this.widgetId = widgetId; }

        @Override public void onCreate() {}

        @Override
        public void onDataSetChanged() {
            a = WidgetStore.arr(c, "notes");
            lines = bodyLines();
        }

        /** How many content lines fit in the widget's current height, so a note never spills outside its space. */
        private int bodyLines() {
            try {
                Bundle o = AppWidgetManager.getInstance(c).getAppWidgetOptions(widgetId);
                int h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110);
                // widget padding 24 + header 30 + gaps 10 + "Add a note" 34 + card padding 16 + title 17
                int free = h - 24 - 30 - 10 - 34 - 16 - 17 - 3;
                return Math.max(1, Math.min(12, free / 14));
            } catch (Exception e) { return 3; }
        }

        @Override public int getCount() { return a.length(); }

        @Override
        public RemoteViews getViewAt(int pos) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_note_item);
            JSONObject o = a.optJSONObject(pos);
            String t = o == null ? "" : o.optString("t");
            String b = o == null ? "" : o.optString("b");
            rv.setTextViewText(R.id.item_title, t);
            rv.setTextViewText(R.id.item_body, b);
            rv.setInt(R.id.item_body, "setMaxLines", lines);
            rv.setViewVisibility(R.id.item_body, b.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
            Intent fill = new Intent();
            fill.putExtra("sprintly_open", "notes");
            rv.setOnClickFillInIntent(R.id.item_root, fill);
            return rv;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return false; }
    }
}
