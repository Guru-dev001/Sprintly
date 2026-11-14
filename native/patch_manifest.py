import sys
p = sys.argv[1]
s = open(p, encoding="utf-8").read()
perms = ["android.permission.POST_NOTIFICATIONS", "android.permission.SCHEDULE_EXACT_ALARM",
         "android.permission.USE_EXACT_ALARM", "android.permission.RECEIVE_BOOT_COMPLETED"]
add = "".join('    <uses-permission android:name="%s" />\n' % x for x in perms if x not in s)
recv = '''
        <receiver android:name=".RoutineWidget" android:exported="false">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/routine_widget_info" />
        </receiver>
        <receiver android:name=".NotesWidget" android:exported="false">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/notes_widget_info" />
        </receiver>
'''
assert "</application>" in s and "</manifest>" in s
s = s.replace("</application>", recv + "    </application>", 1)
s = s.replace("</manifest>", add + "</manifest>", 1)
open(p, "w", encoding="utf-8").write(s)
print("manifest patched")
