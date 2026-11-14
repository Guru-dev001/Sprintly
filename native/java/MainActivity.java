package com.sprintly.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(SprintlyWidgetsPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
