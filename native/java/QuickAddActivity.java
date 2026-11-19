package com.sprintly.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/** Small pop-up over the home screen for typing a new to-do or note straight from a widget. */
public class QuickAddActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean note = "note".equals(getIntent().getStringExtra("kind"));
        final String kind = note ? "note" : "todo";
        setContentView(R.layout.activity_quick_add);

        TextView title = (TextView) findViewById(R.id.qa_title);
        final EditText input = (EditText) findViewById(R.id.qa_input);
        final EditText body = (EditText) findViewById(R.id.qa_body);
        title.setText(note ? "New note" : "New to-do");
        input.setHint(note ? "Title" : "What needs to be done?");
        body.setVisibility(note ? View.VISIBLE : View.GONE);

        findViewById(R.id.qa_root).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        findViewById(R.id.qa_card).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { /* keep the card open */ }
        });
        findViewById(R.id.qa_cancel).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });

        final Runnable save = new Runnable() {
            @Override public void run() {
                String t = input.getText().toString().trim();
                String b = note ? body.getText().toString().trim() : "";
                if (t.isEmpty() && b.isEmpty()) { input.setError("Type something first"); return; }
                WidgetStore.addPending(QuickAddActivity.this, kind, t, b);
                finish();
            }
        };
        findViewById(R.id.qa_add).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { save.run(); }
        });
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent e) {
                if (!note && actionId == EditorInfo.IME_ACTION_DONE) { save.run(); return true; }
                return false;
            }
        });
        input.requestFocus();
    }
}
