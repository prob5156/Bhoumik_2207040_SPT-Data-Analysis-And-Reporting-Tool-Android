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

public class ClientLocationsActivity extends AppCompatActivity {
    private DBHelper db;
    private LinearLayout llLocationsContainer;
    private EditText tfSearchLocations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_locations);
        db = new DBHelper(this);

        TextView lblHeader = findViewById(R.id.lblHeader);
        TextView lblClientName = findViewById(R.id.lblClientName);
        TextView lblPhoneNumber = findViewById(R.id.lblPhoneNumber);
        Button btnEnterNewLocation = findViewById(R.id.btnEnterNewLocation);
        Button btnBackLocations = findViewById(R.id.btnBackLocations);
        tfSearchLocations = findViewById(R.id.tfSearchLocations);
        llLocationsContainer = findViewById(R.id.llLocationsContainer);

        // Header based on role
        if ("SENIOR".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Senior Executive Engineer");
        else if ("SUB".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Subconductor Engineer");
        else lblHeader.setText("");

        lblClientName.setText(AppSession.clientName);
        lblPhoneNumber.setText(AppSession.phoneNumber);

        // Hide enter new location for CLIENT and SUB
        if ("CLIENT".equalsIgnoreCase(AppSession.role) || "SUB".equalsIgnoreCase(AppSession.role)) {
            btnEnterNewLocation.setVisibility(View.GONE);
        } else {
            btnEnterNewLocation.setVisibility(View.VISIBLE);
        }

        btnEnterNewLocation.setOnClickListener(v -> startActivity(new Intent(ClientLocationsActivity.this, EnterNewClientActivity.class)));

        btnBackLocations.setOnClickListener(v -> {
            if ("CLIENT".equalsIgnoreCase(AppSession.role)) {
                startActivity(new Intent(ClientLocationsActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(ClientLocationsActivity.this, ModifiersDashboardActivity.class));
            }
        });

        // load and display locations via method reference to avoid self-initialization issues
        Runnable loadLocations = this::refreshLocations;

        // initial load
        runOnUiThread(loadLocations);
    }

    private void refreshLocations() {
        llLocationsContainer.removeAllViews();
        try (Cursor c = db.fetchLocationsByClient(AppSession.clientId)) {
            List<View> addedViews = new ArrayList<>();
            while (c.moveToNext()) {
                int locId = c.getInt(c.getColumnIndexOrThrow("id"));
                String locationName = c.getString(c.getColumnIndexOrThrow("location_name"));
                int holes = c.getInt(c.getColumnIndexOrThrow("bore_holes"));

                LinearLayout locationBox = new LinearLayout(this);
                locationBox.setOrientation(LinearLayout.VERTICAL);
                locationBox.setPadding(8,8,8,8);
                LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                boxParams.setMargins(8,8,8,8);
                locationBox.setLayoutParams(boxParams);
                locationBox.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                Button locationButton = new Button(this);
                locationButton.setText(locationName);
                locationButton.setMinWidth(300);
                locationButton.setMinHeight(140);
                locationButton.setAllCaps(false);

                locationButton.setOnClickListener(v -> {
                    AppSession.editLocationId = locId;
                    AppSession.editLocationName = locationName;
                    AppSession.editBoreHoles = holes;
                    startActivity(new Intent(ClientLocationsActivity.this, BoreholeDashboardActivity.class));
                });

                locationBox.addView(locationButton);

                if ("SENIOR".equalsIgnoreCase(AppSession.role)) {
                    LinearLayout btnRow = new LinearLayout(this);
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);

                    Button editBtn = new Button(this);
                    editBtn.setText("Edit");
                    editBtn.setOnClickListener(v -> {
                        AppSession.editLocationId = locId;
                        AppSession.editLocationName = locationName;
                        AppSession.editBoreHoles = holes;
                        startActivity(new Intent(ClientLocationsActivity.this, EditLocationActivity.class));
                    });

                    Button deleteBtn = new Button(this);
                    deleteBtn.setText("Delete");
                    deleteBtn.setOnClickListener(v -> {
                        new AlertDialog.Builder(ClientLocationsActivity.this)
                                .setTitle("Confirm Delete")
                                .setMessage("Are you sure you want to delete this location? This action cannot be undone.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int removed = db.deleteLocation(locId);
                                        if (removed > 0) {
                                            Toast.makeText(ClientLocationsActivity.this, "Location deleted", Toast.LENGTH_SHORT).show();
                                            // reload
                                            runOnUiThread(ClientLocationsActivity.this::refreshLocations);
                                        } else {
                                            Toast.makeText(ClientLocationsActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });

                    btnRow.addView(editBtn);
                    Space sp = new Space(this);
                    sp.setMinimumWidth(12);
                    btnRow.addView(sp);
                    btnRow.addView(deleteBtn);
                    locationBox.addView(btnRow);
                }

                llLocationsContainer.addView(locationBox);
                addedViews.add(locationBox);
            }

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

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
