package dev.portpocket;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Services extends Activity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Set<String> busy = new HashSet<>();
    private SharedPreferences prefs;
    private LinearLayout page, cards;
    private EditText host, port;

    private TextView text(LinearLayout parent, String value, int size) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); parent.addView(t); return t;
    }
    private void button(LinearLayout parent, String title, Runnable action) {
        Button b = new Button(this); b.setText(title); parent.addView(b);
        b.setOnClickListener(v -> action.run());
    }
    private LinearLayout column() {
        LinearLayout col = new LinearLayout(this); col.setOrientation(1); return col;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("serviceChecks", MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this); page = column();
        int pad = Math.round(16 * getResources().getDisplayMetrics().density);
        page.setPadding(pad,pad,pad,pad); scroll.addView(page); setContentView(scroll);
        TextView heading = text(page,"Tailnet services",30);
        android.graphics.drawable.Drawable plug = getDrawable(R.drawable.plug);
        int iconSize = Math.round(56 * getResources().getDisplayMetrics().density);
        plug.setBounds(0,0,iconSize,iconSize);
        heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
        heading.setCompoundDrawablesRelative(plug,null,null,null);
        text(page,"Connect Tailscale, then add a device name or Tailscale IP and the TCP port your service uses.",16);
        host = new EditText(this); host.setSingleLine(); host.setHint("Device name or IP"); page.addView(host);
        port = new EditText(this); port.setSingleLine(); port.setInputType(2); port.setHint("TCP port"); page.addView(port);
        button(page,"Save service",this::save);
        button(page,"Open Tailscale",() -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.tailscale.ipn");
            if (launch != null) startActivity(launch);
            else Toast.makeText(this,"Tailscale is unavailable on this device",Toast.LENGTH_LONG).show();
        });
        cards = column(); page.addView(cards); render();
    }
    private void save() {
        String name = host.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (name.isEmpty() || name.length()>253 || !name.matches("[a-z0-9:.\\-]+")) {
            host.setError("Use a hostname or IP address"); return;
        }
        int number;
        try { number = Integer.parseInt(port.getText().toString()); }
        catch (NumberFormatException e) { number=0; }
        if (number<1 || number>65535) { port.setError("Enter 1 to 65535"); return; }
        String key = name+" "+number;
        if (!prefs.contains(key)) prefs.edit().putString(key,"").apply();
        host.setText(""); port.setText(""); render();
    }
    private void render() {
        cards.removeAllViews();
        for (String key : new TreeSet<>(prefs.getAll().keySet())) {
            LinearLayout card = column(); card.setPadding(0,24,0,0); cards.addView(card);
            text(card,key.replace(" ","  /  TCP "),22);
            String history = prefs.getString(key,"");
            text(card,history.isEmpty() ? "Ready for first check" : history,15);
            LinearLayout actions = new LinearLayout(this); card.addView(actions);
            button(actions,busy.contains(key) ? "Checking…" : "Check now",() -> check(key));
            button(actions,"Forget",() -> new AlertDialog.Builder(this).setMessage("Remove this service and its history?")
                .setNegativeButton("Cancel",null).setPositiveButton("Remove",(d,w) -> {
                    prefs.edit().remove(key).apply(); render();
                }).show());
        }
    }
    private void check(String key) {
        if (!busy.add(key)) return;
        render();
        worker.execute(() -> {
            String[] parts = key.split(" ");
            String result = diagnose(parts[0],Integer.parseInt(parts[1]));
            runOnUiThread(() -> {
                busy.remove(key);
                if (isDestroyed() || !prefs.contains(key)) return;
                String previous = prefs.getString(key,"");
                String time = java.text.DateFormat.getDateTimeInstance().format(new Date());
                StringBuilder history = new StringBuilder(time+" · "+result);
                if (!previous.isEmpty()) {
                    String[] lines = previous.split("\n");
                    for (int i=0; i<Math.min(2,lines.length); i++) history.append("\n").append(lines[i]);
                }
                prefs.edit().putString(key,history.toString()).apply(); render();
            });
        });
    }
    static String diagnose(String name, int port) {
        try (Socket socket = new Socket()) {
            InetSocketAddress address = new InetSocketAddress(name,port);
            long start = System.nanoTime();
            socket.connect(address,3000);
            return "Reachable · TCP connected in "+((System.nanoTime()-start)/1000000)+" ms. If the app still fails, check its logs.";
        } catch (UnknownHostException e) {
            return "Name unresolved · Check the device name and MagicDNS, or try its Tailscale IP.";
        } catch (SocketTimeoutException e) {
            return "Timed out · Check that both devices are online and the tailnet policy permits this port.";
        } catch (ConnectException e) {
            String detail = String.valueOf(e.getMessage()).toLowerCase(Locale.ROOT);
            if (detail.contains("refused")) return "Refused · Check that the service is running and listening on the tailnet interface.";
            return "Connection failed · Check Tailscale connectivity, the destination address and the server firewall.";
        } catch (IOException | SecurityException e) {
            return "Unavailable · Check the phone connection and the route to this device.";
        }
    }
    @Override protected void onDestroy() { worker.shutdownNow(); super.onDestroy(); }
}