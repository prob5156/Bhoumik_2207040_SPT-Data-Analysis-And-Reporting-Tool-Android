package com.example.myapplication.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.myapplication.DBHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ExportImportHelper {

    // Export clients table to CSV in app external files directory (Downloads if available)
    public static File exportClientsToCsv(Context ctx) throws Exception {
        DBHelper dbHelper = new DBHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("clients", null, null, null, null, null, "id ASC");

        File dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) dir = ctx.getFilesDir();
        File out = new File(dir, "clients_export.csv");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8))) {
            // header
            bw.write("id,name,phone,password");
            bw.newLine();
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                String phone = c.getString(c.getColumnIndexOrThrow("phone"));
                String pass = c.getString(c.getColumnIndexOrThrow("password"));
                String line = String.format(Locale.ROOT, "%d,\"%s\",\"%s\",\"%s\"", id, escape(name), escape(phone), escape(pass));
                bw.write(line);
                bw.newLine();
            }
        } finally {
            c.close();
            db.close();
        }

        return out;
    }

    // Import clients from CSV file (simple parser, expects header id,name,phone,password)
    public static int importClientsFromCsv(Context ctx, File csvFile) throws Exception {
        DBHelper dbHelper = new DBHelper(ctx);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int inserted = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                // naive CSV split: split on commas but handle quoted fields
                String[] parts = parseCsvLine(line);
                if (parts.length < 4) continue;
                ContentValues cv = new ContentValues();
                cv.put("name", unescape(parts[1]));
                cv.put("phone", unescape(parts[2]));
                cv.put("password", unescape(parts[3]));
                long id = db.insertWithOnConflict("clients", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                if (id != -1) inserted++;
            }
        } finally {
            db.close();
        }

        return inserted;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\"\"");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            String inner = s.substring(1, s.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return s;
    }

    private static String[] parseCsvLine(String line) {
        // very small CSV parser: supports quoted fields containing commas and double-quote escaping
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
