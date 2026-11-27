package com.sprintly.app;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import org.json.JSONArray;
import org.json.JSONObject;

/** Feeds the swipeable note cards of the Notes widget. */
public class NotesWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    static class Factory implements RemoteViewsFactory {
        private final Context c;
        private JSONArray a = new JSONArray();

        Factory(Context c) { this.c = c; }

        @Override public void onCreate() {}
        @Override public void onDataSetChanged() { a = WidgetStore.arr(c, "notes"); }
        @Override public void onDestroy() {}
        @Override public int getCount() { return a.length(); }

        @Override
        public RemoteViews getViewAt(int pos) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_note_item);
            JSONObject o = a.optJSONObject(pos);
            String t = o == null ? "" : o.optString("t"), b = o == null ? "" : o.optString("b");
            rv.setTextViewText(R.id.item_title, t.isEmpty() ? "Untitled" : t);
            rv.setTextViewText(R.id.item_body, b);
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
