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
        Button btnBackModifiers = findViewById(R.id.btnBackModifiers);
        Button btnEnterNewClient = findViewById(R.id.btnEnterNewClient);
        tfSearchClients = findViewById(R.id.tfSearchClients);
        llClientsContainer = findViewById(R.id.llClientsContainer);

        if ("SENIOR".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Senior Executive Engineer");
        else if ("SUB".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Subconductor Engineer");
        else lblHeader.setText("");

        boolean showBack = "SENIOR".equalsIgnoreCase(AppSession.role) || "SUB".equalsIgnoreCase(AppSession.role);
        btnBackModifiers.setVisibility(showBack ? View.VISIBLE : View.GONE);

        btnBackModifiers.setOnClickListener(v -> startActivity(new Intent(ModifiersDashboardActivity.this, LoginActivity.class)));

        btnEnterNewClient.setVisibility("SUB".equalsIgnoreCase(AppSession.role) ? View.GONE : View.VISIBLE);
        btnEnterNewClient.setOnClickListener(v -> startActivity(new Intent(ModifiersDashboardActivity.this, ClientDetailsActivity.class)));

        Runnable loadClients = this::refreshClients;

        runOnUiThread(loadClients);
    }

    private void refreshClients() {
        llClientsContainer.removeAllViews();
        List<View> added = new ArrayList<>();
        try (Cursor c = db.fetchClients()) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                String phone = c.getString(c.getColumnIndexOrThrow("phone"));
                String pwd = null;
                try { pwd = c.getString(c.getColumnIndexOrThrow("password")); } catch (Exception ignored) {}

                LinearLayout clientBox = new LinearLayout(this);
                clientBox.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bp.setMargins(8,8,8,8);
                clientBox.setLayoutParams(bp);
                clientBox.setPadding(8,8,8,8);
                clientBox.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                Button b = new Button(this);
                b.setText(name);
                b.setAllCaps(false);
                b.setOnClickListener(v -> {
                    AppSession.clientName = name;
                    AppSession.phoneNumber = phone;
                    AppSession.clientId = id;
                    startActivity(new Intent(ModifiersDashboardActivity.this, ClientLocationsActivity.class));
                });
                clientBox.addView(b);

                TextView phoneLabel = new TextView(this);
                phoneLabel.setText(phone);
                clientBox.addView(phoneLabel);

                if ("SENIOR".equalsIgnoreCase(AppSession.role)) {
                    TextView pwdLabel = new TextView(this);
                    pwdLabel.setText(pwd == null ? "" : pwd);
                    clientBox.addView(pwdLabel);

                    LinearLayout btnRow = new LinearLayout(this);
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);

                    Button editBtn = new Button(this);
                    editBtn.setText("Edit");
                    editBtn.setOnClickListener(v -> {
                        AppSession.clientId = id;
                        AppSession.clientName = name;
                        AppSession.phoneNumber = phone;
                        startActivity(new Intent(ModifiersDashboardActivity.this, EditClientActivity.class));
                    });

                    Button deleteBtn = new Button(this);
                    deleteBtn.setText("Delete");
                    deleteBtn.setOnClickListener(v -> {
                        int removed = db.deleteClient(id);
                        if (removed > 0) {
                            Toast.makeText(this, "Client deleted", Toast.LENGTH_SHORT).show();
                            runOnUiThread(this::refreshClients);
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    btnRow.addView(editBtn);
                    btnRow.addView(deleteBtn);
                    clientBox.addView(btnRow);
                }

                llClientsContainer.addView(clientBox);
                added.add(clientBox);
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        // search
        tfSearchClients.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String q = s == null ? "" : s.toString().trim().toLowerCase();
                for (View v : added) {
                    if (v instanceof ViewGroup) {
                        ViewGroup vg = (ViewGroup) v;
                        if (vg.getChildCount() > 0 && vg.getChildAt(0) instanceof Button) {
                            Button btn = (Button) vg.getChildAt(0);
                            String name = btn.getText() == null ? "" : btn.getText().toString().toLowerCase();
                            vg.setVisibility(q.isEmpty() || name.contains(q) ? View.VISIBLE : View.GONE);
                        }
                    }
                }
            }
        });
    }
}
