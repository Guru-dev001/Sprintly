package com.sprintly.app;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import org.json.JSONArray;
import org.json.JSONObject;

/** Feeds the scrollable to-do list. */
public class TodoWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    static class Factory implements RemoteViewsFactory {
        private final Context c;
        private JSONArray a = new JSONArray();

        Factory(Context c) { this.c = c; }

        @Override public void onCreate() {}
        @Override public void onDataSetChanged() { a = WidgetStore.arr(c, "todos"); }
        @Override public void onDestroy() {}
        @Override public int getCount() { return a.length(); }

        @Override
        public RemoteViews getViewAt(int pos) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_todo_item);
            JSONObject o = a.optJSONObject(pos);
            boolean d = o != null && o.optBoolean("d");
            String pri = o == null ? "med" : o.optString("p", "med");
            SpannableString s = new SpannableString(o == null ? "" : o.optString("t"));
            if (d && s.length() > 0) s.setSpan(new StrikethroughSpan(), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            rv.setTextViewText(R.id.t_text, s);
            rv.setTextColor(R.id.t_text, d ? 0xFF8E99AB : 0xFFF3EEE6);
            rv.setImageViewResource(R.id.t_box, d ? R.drawable.ic_check_on : R.drawable.ic_check_off);
            int color = "high".equals(pri) ? 0xFFDE3C31 : ("low".equals(pri) ? 0xFF4A90E2 : 0xFFE8A82A);
            rv.setInt(R.id.t_pri, "setBackgroundColor", color);
            Intent fill = new Intent();
            fill.putExtra("i", pos);
            rv.setOnClickFillInIntent(R.id.t_root, fill);
            return rv;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return false; }
    }
}
