package dev.portpocket;
import android.app.Activity;
import android.os.*;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Dashboard extends Activity {
    private final TreeSet<Integer> ports = new TreeSet<>(Arrays.asList(3000,4173,5173,5432,6379,8000,8080,8888));
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> timer;
    private final Map<Integer,String> states = new TreeMap<>();
    private ServerSocket demo;
    private LinearLayout layout, rows;
    private EditText search;
    private Spinner filter;
    private Button toggle;
    private TextView summary;
    private int generation;
    private boolean active;

    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private TextView label(String text, int size) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextSize(size); v.setPadding(0,dp(6),0,dp(6)); return v;
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        layout = new LinearLayout(this); layout.setOrientation(1);
        layout.setBackgroundColor(Color.rgb(17,24,29)); layout.setPadding(dp(18),dp(12),dp(18),0);
        setContentView(layout);
        TextView heading = label("Port Pocket",32);
        android.graphics.drawable.Drawable plug = getDrawable(R.drawable.plug);
        int iconSize = Math.round(56 * getResources().getDisplayMetrics().density);
        plug.setBounds(0,0,iconSize,iconSize);
        heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
        heading.setCompoundDrawablesRelative(plug,null,null,null);
        layout.addView(heading);
        Button tailnet = new Button(this); tailnet.setText("Tailnet services"); layout.addView(tailnet);
        tailnet.setOnClickListener(v -> startActivity(new android.content.Intent(this,Services.class)));
        layout.addView(label("This phone  /  IPv4 loopback · TCP",15));
        summary = label("Checking ports…",16); summary.setTextColor(0xff84dfbb); layout.addView(summary);
        search = new EditText(this); search.setSingleLine(); search.setHint("Search or add port number");
        search.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); layout.addView(search);
        Button add = new Button(this); add.setText("Watch port"); layout.addView(add);
        filter = new Spinner(this);
        filter.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"All watched ports","Available","Occupied","Restricted"}));
        layout.addView(filter);
        toggle = new Button(this); toggle.setText("Start demo listener"); layout.addView(toggle);
        ScrollView scroll = new ScrollView(this); rows = new LinearLayout(this); rows.setOrientation(1);
        scroll.addView(rows); layout.addView(scroll);
        for (String p : getPreferences(Context.MODE_PRIVATE).getStringSet("ports", Collections.emptySet())) ports.add(Integer.parseInt(p));
        add.setOnClickListener(v -> {
            try {
                int port = Integer.parseInt(search.getText().toString());
                if (port < 1 || port > 65535) throw new NumberFormatException();
                ports.add(port);
                Set<String> stored = new HashSet<>();
                for (int n : ports) stored.add(String.valueOf(n));
                getPreferences(0).edit().putStringSet("ports",stored).apply();
                search.setText(""); refresh();
            } catch (NumberFormatException e) { search.setError("Enter a port from 1 to 65535"); }
        });
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { render(); }
            public void afterTextChanged(android.text.Editable s) {}
        });
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p,View v,int i,long id) { render(); }
        });
        toggle.setOnClickListener(v -> worker.execute(() -> {
            try {
                if (demo == null) demo = PortCheck.bind(0); else closeDemo();
                runOnUiThread(() -> { if (active) refresh(); });
            } catch (IOException e) { runOnUiThread(() -> Toast.makeText(this,"Listener unavailable",0).show()); }
        }));
    }
    private void closeDemo() {
        if (demo != null) { try { demo.close(); } catch (IOException ignored) {} demo = null; }
    }
    private int owned = -1;
    private void refresh() {
        if (!active) return;
        final int epoch = generation;
        List<Integer> snapshot = new ArrayList<>(ports);
        worker.execute(() -> {
            int own = demo == null ? -1 : demo.getLocalPort();
            Map<Integer,String> next = new TreeMap<>();
            for (int p : snapshot) next.put(p, p == own ? "Occupied" : PortCheck.status(p));
            if (own > 0) next.put(own,"Occupied");
            runOnUiThread(() -> {
                if (!active || generation != epoch) return;
                owned = own; states.clear(); states.putAll(next);
                toggle.setText(own > 0 ? "Stop listener · " + own : "Start demo listener");
                summary.setText(states.size()+" watched   ·   "+Collections.frequency(states.values(),"Occupied")+" occupied");
                render();
            });
        });
    }
    private void render() {
        rows.removeAllViews();
        String query = search.getText().toString();
        for (Map.Entry<Integer,String> item : states.entrySet()) {
            if (!String.valueOf(item.getKey()).contains(query)) continue;
            if (filter.getSelectedItemPosition()>0 && !item.getValue().equals(filter.getSelectedItem())) continue;
            String owner = item.getKey()==owned ? "  ·  Port Pocket / PID "+android.os.Process.myPid() : "";
            rows.addView(label(item.getKey()+"     "+item.getValue()+owner,18));
        }
        if (rows.getChildCount()==0) rows.addView(label("No matching ports",16));
    }
    @Override protected void onResume() {
        super.onResume(); active=true; generation++;
        timer = worker.scheduleWithFixedDelay(() -> runOnUiThread(this::refresh),0,3,TimeUnit.SECONDS);
    }
    @Override protected void onPause() {
        active=false; generation++; timer.cancel(false); worker.execute(this::closeDemo); super.onPause();
    }
    @Override protected void onDestroy() { worker.shutdown(); super.onDestroy(); }
}