package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.util.TypedValue;

public class ClientLocationsActivity extends AppCompatActivity {
    private DBHelper db;
    private LinearLayout llLocationsContainer;
    private EditText tfSearchLocations;
    private final List<View> addedViews = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_locations);
        db = new DBHelper(this);

        TextView lblHeader = findViewById(R.id.lblHeader);
        TextView lblClientName = findViewById(R.id.lblClientName);
        TextView lblPhoneNumber = findViewById(R.id.lblPhoneNumber);
        Button btnEnterNewLocation = findViewById(R.id.btnEnterNewLocation);
        tfSearchLocations = findViewById(R.id.tfSearchLocations);
        llLocationsContainer = findViewById(R.id.llLocationsContainer);

        // Header based on role
        if ("SENIOR".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Senior Executive Engineer");
        else if ("SUB".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Subconductor Engineer");
        else lblHeader.setText("");

        lblClientName.setText(AppSession.clientName);
        lblPhoneNumber.setText(AppSession.phoneNumber);

        // wire enter-new-location button (only one control on Android)
        if (btnEnterNewLocation != null) {
            boolean allowEnter = !"CLIENT".equalsIgnoreCase(AppSession.role) && !"SUB".equalsIgnoreCase(AppSession.role);
            btnEnterNewLocation.setVisibility(allowEnter ? View.VISIBLE : View.GONE);
            btnEnterNewLocation.setOnClickListener(v -> startActivity(new Intent(ClientLocationsActivity.this, EnterNewClientActivity.class)));
        }

        // load and render locations into llLocationsContainer (similar to JavaFX flow)
        refreshLocations();

        // search filtering
        tfSearchLocations.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String q = s == null ? "" : s.toString().trim().toLowerCase();
                for (View v : addedViews) {
                    if (v instanceof ViewGroup) {
                        ViewGroup vg = (ViewGroup) v;
                        if (vg.getChildCount() > 0 && vg.getChildAt(0) instanceof Button) {
                            Button b = (Button) vg.getChildAt(0);
                            String name = b.getText() == null ? "" : b.getText().toString().toLowerCase();
                            boolean visible = q.isEmpty() || name.contains(q);
                            vg.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }
                    }
                }
            }
        });
    }

    private void refreshLocations() {
        addedViews.clear();
        llLocationsContainer.removeAllViews();
        try (Cursor c = db.fetchLocationsByClient(AppSession.clientId)) {
            while (c != null && c.moveToNext()) {
                int locId = c.getInt(c.getColumnIndexOrThrow("id"));
                String locationName = c.getString(c.getColumnIndexOrThrow("location_name"));
                int holes = c.getInt(c.getColumnIndexOrThrow("bore_holes"));

                LinearLayout locationBox = new LinearLayout(this);
                locationBox.setOrientation(LinearLayout.VERTICAL);
                locationBox.setPadding(8,8,8,8);

                Button locationBtn = new Button(this);
                locationBtn.setText(locationName);
                locationBtn.setAllCaps(false);
                applyStickerStyle(locationBtn, Math.abs((locationName + "::locations").hashCode()));
                LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                locationBtn.setLayoutParams(lpBtn);

                // click to open borehole dashboard
                locationBtn.setOnClickListener(v -> {
                    AppSession.editLocationId = locId;
                    AppSession.editLocationName = locationName;
                    AppSession.editBoreHoles = holes;
                    startActivity(new Intent(ClientLocationsActivity.this, BoreholeDashboardActivity.class));
                });

                locationBox.addView(locationBtn);

                if ("SENIOR".equalsIgnoreCase(AppSession.role)) {
                    LinearLayout btnRow = new LinearLayout(this);
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);

                    Button editBtn = new Button(this);
                    editBtn.setText("Edit");
                    applyActionStyle(editBtn);
                    editBtn.setOnClickListener(v -> {
                        AppSession.editLocationId = locId;
                        AppSession.editLocationName = locationName;
                        AppSession.editBoreHoles = holes;
                        startActivity(new Intent(ClientLocationsActivity.this, EditLocationActivity.class));
                    });

                    Button deleteBtn = new Button(this);
                    deleteBtn.setText("Delete");
                    applyActionStyle(deleteBtn);
                    deleteBtn.setOnClickListener(v -> {
                        new AlertDialog.Builder(ClientLocationsActivity.this)
                                .setTitle("Delete Location")
                                .setMessage("Are you sure you want to delete this location? This action cannot be undone.")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    int removed = db.deleteLocation(locId);
                                    if (removed > 0) {
                                        Toast.makeText(ClientLocationsActivity.this, "Location deleted", Toast.LENGTH_SHORT).show();
                                        refreshLocations();
                                    } else {
                                        Toast.makeText(ClientLocationsActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });

                    Space sp = new Space(this);
                    sp.setMinimumWidth(12);

                    btnRow.addView(editBtn);
                    btnRow.addView(sp);
                    btnRow.addView(deleteBtn);
                    locationBox.addView(btnRow);
                }

                llLocationsContainer.addView(locationBox);
                addedViews.add(locationBox);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    private final int[] STICKER_PALETTE = new int[] {
            0xFF6EC6FF,
            0xFFFF8A65,
            0xFFAED581,
            0xFFF48FB1,
            0xFFFFF176,
            0xFFB39DDB
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
        double r = ((color >> 16) & 0xFF) / 255.0;
        double g = ((color >> 8) & 0xFF) / 255.0;
        double b = (color & 0xFF) / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        btn.setTextColor(luminance > 0.6 ? 0xFF222222 : Color.WHITE);
        int pad = (int)(getResources().getDisplayMetrics().density * 12f);
        btn.setPadding(pad, pad, pad, pad);
        btn.setAllCaps(false);
        btn.setElevation(getResources().getDisplayMetrics().density * 4f);
    }
}
