package com.sprintly.app;

import android.content.Intent;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(SprintlyWidgetsPlugin.class);
        super.onCreate(savedInstanceState);
        remember(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        remember(intent);
    }

    /** A widget tap says where to go (e.g. the Notes page); the web app picks it up with SprintlyWidgets.pull(). */
    private void remember(Intent i) {
        if (i == null) return;
        String t = i.getStringExtra("sprintly_open");
        if (t != null) {
            WidgetStore.setOpen(this, t);
            i.removeExtra("sprintly_open");
        }
    }
}
