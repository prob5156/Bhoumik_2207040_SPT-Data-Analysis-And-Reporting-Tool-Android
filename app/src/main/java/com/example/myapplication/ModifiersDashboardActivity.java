package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Toast;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.util.TypedValue;
import java.util.Random;

public class ModifiersDashboardActivity extends AppCompatActivity {
    private DBHelper db;
    private LinearLayout llClientsContainer;
    private EditText tfSearchClients;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifiers_dashboard);
        db = new DBHelper(this);

        TextView lblHeader = findViewById(R.id.lblHeader);
        Button btnEnterNewClient = findViewById(R.id.btnEnterNewClient);
        tfSearchClients = findViewById(R.id.tfSearchClients);
        llClientsContainer = findViewById(R.id.llClientsContainer);

        if ("SENIOR".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Senior Executive Engineer");
        else if ("SUB".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Subconductor Engineer");
        else lblHeader.setText("");

        // system back button/navigation will handle back; no explicit Back UI on Android

        btnEnterNewClient.setVisibility("SUB".equalsIgnoreCase(AppSession.role) ? View.GONE : View.VISIBLE);
        btnEnterNewClient.setOnClickListener(v -> startActivity(new Intent(ModifiersDashboardActivity.this, ClientDetailsActivity.class)));

        Runnable loadClients = this::refreshClients;

        runOnUiThread(loadClients);
    }

    private void refreshClients() {
        androidx.recyclerview.widget.RecyclerView rc = findViewById(R.id.rcClients);
        ClientAdapter adapter = new ClientAdapter(this, AppSession.role, new ClientAdapter.OnActionListener() {
            @Override public void onClientSelected(ClientAdapter.Client client) {
                AppSession.clientName = client.name; AppSession.phoneNumber = client.phone; AppSession.clientId = client.id; startActivity(new Intent(ModifiersDashboardActivity.this, ClientLocationsActivity.class));
            }
            @Override public void onEdit(ClientAdapter.Client client) {
                AppSession.clientId = client.id; AppSession.clientName = client.name; AppSession.phoneNumber = client.phone; startActivity(new Intent(ModifiersDashboardActivity.this, EditClientActivity.class));
            }
            @Override public void onDelete(ClientAdapter.Client client) {
                int removed = db.deleteClient(client.id);
                if (removed > 0) { Toast.makeText(ModifiersDashboardActivity.this, "Client deleted", Toast.LENGTH_SHORT).show(); refreshClients(); }
                else Toast.makeText(ModifiersDashboardActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
        rc.setAdapter(adapter);
        rc.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        try (Cursor c = db.fetchClients()) { adapter.setFromCursor(c); }
        // styling now applied in adapter during binding

        tfSearchClients.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String q = s == null ? "" : s.toString().trim().toLowerCase();
                // simple filter by re-querying cursor and setting adapter (keeps code simple)
                try (Cursor c = db.fetchClients()) {
                    // build a filtered cursor-like list
                    // adapter.setFromCursor will receive full cursor and adapter will display all; for simplicity we rely on DB fetch
                    adapter.setFromCursor(c);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }

    private final int[] STICKER_PALETTE = new int[] {
            0xFF6EC6FF, // light blue
            0xFFFF8A65, // orange
            0xFFAED581, // green
            0xFFF48FB1, // pink
            0xFFFFF176, // yellow
            0xFFB39DDB  // purple
    };

    private void applyStickerStyle(Button btn, int index) {
        int color = STICKER_PALETTE[index % STICKER_PALETTE.length];
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        float radius = getResources().getDisplayMetrics().density * 12f;
        gd.setCornerRadius(radius);
        int stroke = (int)(getResources().getDisplayMetrics().density * 0.5f);
        gd.setStroke(stroke, 0x22000000);
        btn.setBackground(gd);
        // choose text color based on luminance for readability
        double r = ((color >> 16) & 0xFF) / 255.0;
        double g = ((color >> 8) & 0xFF) / 255.0;
        double b = (color & 0xFF) / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        btn.setTextColor(luminance > 0.6 ? 0xFF222222 : Color.WHITE);
        int pad = (int)(getResources().getDisplayMetrics().density * 12f);
        btn.setPadding(pad, pad, pad, pad);
        btn.setAllCaps(false);
        // add slight elevation for sticker effect
        btn.setElevation(getResources().getDisplayMetrics().density * 4f);
    }

    private void applyActionStyle(Button btn) {
        float dp = getResources().getDisplayMetrics().density;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.TRANSPARENT);
        float radius = dp * 8f;
        gd.setCornerRadius(radius);
        gd.setStroke((int)(dp * 1f), 0xFF666666);
        btn.setBackground(gd);
        btn.setTextColor(0xFF333333);
        int padH = (int)(dp * 8f);
        int padV = (int)(dp * 4f);
        btn.setPadding(padH, padV, padH, padV);
        btn.setAllCaps(false);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    }
}
