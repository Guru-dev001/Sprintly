package com.sprintly.app;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** JS bridge used by index.html: SprintlyWidgets.sync({...}) and SprintlyWidgets.pull() */
@CapacitorPlugin(name = "SprintlyWidgets")
public class SprintlyWidgetsPlugin extends Plugin {
    @PluginMethod
    public void sync(PluginCall call) {
        JSArray routine = call.getArray("routine", new JSArray());
        JSArray notes = call.getArray("notes", new JSArray());
        WidgetStore.save(getContext(), routine.toString(), notes.toString(),
            call.getInt("streak", 0), call.getInt("total", 0), call.getInt("done", 0),
            call.getString("dk", ""));
        call.resolve();
    }

    @PluginMethod
    public void pull(PluginCall call) {
        try {
            call.resolve(new JSObject(WidgetStore.snapshot(getContext()).toString()));
        } catch (Exception e) {
            call.reject("pull failed");
        }
    }
}
