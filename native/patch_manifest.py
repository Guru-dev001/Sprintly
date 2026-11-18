import sys
p = sys.argv[1]
s = open(p, encoding="utf-8").read()
perms = ["android.permission.POST_NOTIFICATIONS", "android.permission.SCHEDULE_EXACT_ALARM",
         "android.permission.USE_EXACT_ALARM", "android.permission.RECEIVE_BOOT_COMPLETED"]
add = "".join('    <uses-permission android:name="%s" />\n' % x for x in perms if x not in s)
recv = '''
        <receiver android:name=".RoutineWidget" android:label="Sprintly Routine" android:exported="false">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/routine_widget_info" />
        </receiver>
        <receiver android:name=".NotesWidget" android:label="Sprintly Notes" android:exported="false">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/notes_widget_info" />
        </receiver>
        <receiver android:name=".TodoWidget" android:label="Sprintly To-do" android:exported="false">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/todo_widget_info" />
        </receiver>
        <service android:name=".TodoWidgetService" android:permission="android.permission.BIND_REMOTEVIEWS" android:exported="false" />
        <activity android:name=".QuickAddActivity" android:theme="@style/QuickAddTheme" android:exported="false"
            android:excludeFromRecents="true" android:taskAffinity="" android:launchMode="singleTask"
            android:windowSoftInputMode="stateVisible|adjustResize" />
        <service android:name=".RoutineWidgetService" android:permission="android.permission.BIND_REMOTEVIEWS" android:exported="false" />
        <service android:name=".NotesWidgetService" android:permission="android.permission.BIND_REMOTEVIEWS" android:exported="false" />
'''
assert "</application>" in s and "</manifest>" in s
s = s.replace("</application>", recv + "    </application>", 1)
s = s.replace("</manifest>", add + "</manifest>", 1)
open(p, "w", encoding="utf-8").write(s)
print("manifest patched")
