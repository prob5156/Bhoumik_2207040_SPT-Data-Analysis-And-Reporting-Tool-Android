package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {/**/
    private static final String NAME = "spt.db";
    private static final int VERSION = 1;
    private static final String TAG = "DBHelper";
    private FirebaseFirestore firestore;

    public DBHelper(Context ctx) {
        super(ctx, NAME, null, VERSION);
        try {
            FirebaseApp.initializeApp(ctx);
            firestore = FirebaseFirestore.getInstance();
        } catch (Exception ex) {
            Log.w(TAG, "Firebase init failed", ex);
            firestore = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS raw_data(id INTEGER PRIMARY KEY AUTOINCREMENT, depth REAL, n1 INTEGER, n2 INTEGER, n3 INTEGER, comment TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS spt_data(id INTEGER PRIMARY KEY AUTOINCREMENT, borehole_id INTEGER, location_id INTEGER, sample_code TEXT DEFAULT '', depth REAL, n1 INTEGER, n2 INTEGER, n3 INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS clients(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, password TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS locations(id INTEGER PRIMARY KEY AUTOINCREMENT, client_id INTEGER, location_name TEXT, bore_holes INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS visual_classification(id INTEGER PRIMARY KEY AUTOINCREMENT, borehole_id INTEGER, location_id INTEGER, color_code TEXT, sand_percentage REAL, silt_percentage REAL, clay_percentage REAL, from_depth REAL, to_depth REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No-op for now
    }

    public long insertClient(String name, String phone, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        cv.put("password", password);
        return db.insert("clients", null, cv);
    }

    public long insertClientWithRemote(String name, String phone, String password) {
        long id = insertClient(name, phone, password);
        if (id > 0 && firestore != null) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("name", name);
            doc.put("phone", phone);
            doc.put("password", password);
            firestore.collection("clients").document(String.valueOf(id)).set(doc)
                    .addOnSuccessListener(a -> Log.d(TAG, "Saved client to Firestore: " + id))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to save client to Firestore", e));
        }
        return id;
    }

    public Cursor fetchClients() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM clients", null);
    }

    public long insertLocation(int clientId, String locationName, int holes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("client_id", clientId);
        cv.put("location_name", locationName);
        cv.put("bore_holes", holes);
        long id = db.insert("locations", null, cv);
        if (id > 0 && firestore != null) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("client_id", clientId);
            doc.put("location_name", locationName);
            doc.put("bore_holes", holes);
            firestore.collection("locations").document(String.valueOf(id)).set(doc)
                    .addOnSuccessListener(a -> Log.d(TAG, "Saved location to Firestore: " + id))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to save location to Firestore", e));
        }
        return id;
    }

    public int updateLocation(int id, String locationName, int holes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("location_name", locationName);
        cv.put("bore_holes", holes);
        return db.update("locations", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteLocation(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("locations", "id=?", new String[]{String.valueOf(id)});
    }

    public long insertSptData(int boreholeId, int locationId, String sampleCode, double depth, int n1, int n2, int n3) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("borehole_id", boreholeId);
        cv.put("location_id", locationId);
        cv.put("sample_code", sampleCode);
        cv.put("depth", depth);
        cv.put("n1", n1);
        cv.put("n2", n2);
        cv.put("n3", n3);
        long id = db.insert("spt_data", null, cv);
        if (id > 0 && firestore != null) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("borehole_id", boreholeId);
            doc.put("location_id", locationId);
            doc.put("sample_code", sampleCode);
            doc.put("depth", depth);
            doc.put("n1", n1);
            doc.put("n2", n2);
            doc.put("n3", n3);
            firestore.collection("spt_data").document(String.valueOf(id)).set(doc)
                    .addOnSuccessListener(a -> Log.d(TAG, "Saved spt_data to Firestore: " + id))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to save spt_data to Firestore", e));
        }
        return id;
    }

    public Cursor fetchSptDataByBorehole(int boreholeId, int locationId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM spt_data WHERE borehole_id=? AND location_id=?", new String[]{String.valueOf(boreholeId), String.valueOf(locationId)});
    }

    public int deleteSptData(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("spt_data", "id=?", new String[]{String.valueOf(id)});
    }

    public int updateClient(int id, String name, String phone, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        cv.put("password", password);
        return db.update("clients", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteClient(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("clients", "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor fetchLocationsByClient(int clientId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM locations WHERE client_id=?", new String[]{String.valueOf(clientId)});
    }
}
