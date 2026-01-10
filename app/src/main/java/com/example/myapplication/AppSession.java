package com.example.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppSession {
    public static String role = "";
    public static String clientName = "";
    public static String phoneNumber = "";
    public static String clientPassword = "";
    public static int clientId = -1;
    public static int editLocationId = -1;
    public static String editLocationName = "";
    public static int editBoreHoles = 0;
    public static int selectedBorehole = -1;
    public static int selectedSptId = -1;
    public static List<Map<String,String>> locations = new ArrayList<>();
    public static List<Map<String,String>> clients = new ArrayList<>();

    public static void clearLocations(){ locations.clear(); }
    public static void clearClients(){ clients.clear(); }
    public static void addClient(String name, String phone){
        Map<String,String> m = new HashMap<>();
        m.put("clientName", name);
        m.put("phoneNumber", phone);
        clients.add(m);
    }
}
