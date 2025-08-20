package com.example.kosplus.func;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class OneSignalNotification {
    private static String REST_API_KEY = "os_v2_app_pbmtqkvlnfhvjal7rht6o7ui5waumjcbdecejfeqdymwbzyqwznhsb77konc2lwv3hb6qn7d66jdow5ntcv7vge63kkkhqnufwsdeay";
    private static String APP_ID = "7859382a-ab69-4f54-817f-89e7e77e88ed";
    public static void sendNotificationToAllUsers(String title, String message) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL("https://onesignal.com/api/v1/notifications");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Authorization", "Basic " + REST_API_KEY);
                conn.setRequestMethod("POST");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("app_id", APP_ID);
                jsonBody.put("included_segments", new JSONArray().put("All")); // Gửi tất cả user
                jsonBody.put("headings", new JSONObject().put("en", title));
                jsonBody.put("contents", new JSONObject().put("en", message));

                Log.d("KOS Plus", "OneSignalDebug JSON Body: " + jsonBody.toString());

                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(jsonBody.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int httpResponse = conn.getResponseCode();
                Log.d("KOS Plus", "OneSignalDebug Response Code: " + httpResponse);

                InputStream inputStream;
                if (httpResponse >= 200 && httpResponse < 400) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                Log.d("OneSignalDebug", "Response Body: " + response);

            } catch (Exception e) {
                Log.e("OneSignalDebug", "Error sending notification", e);
            }
        });
    }


    public static void sendNotificationToUser( String userId, String title, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(userId).child("token");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String subId = snapshot.getValue(String.class);
                    if (subId != null && !subId.isEmpty()) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                URL url = new URL("https://onesignal.com/api/v1/notifications");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setUseCaches(false);
                                conn.setDoOutput(true);
                                conn.setDoInput(true);

                                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                conn.setRequestProperty("Authorization", "Basic " + REST_API_KEY);
                                conn.setRequestMethod("POST");

                                JSONObject jsonBody = new JSONObject();
                                jsonBody.put("app_id", APP_ID);
                                JSONArray playerIds = new JSONArray();

                                playerIds.put(subId);  // Gửi đến subId
                                jsonBody.put("include_player_ids", playerIds); // Gửi riêng
                                jsonBody.put("headings", new JSONObject().put("en", title));
                                jsonBody.put("contents", new JSONObject().put("en", message));

                                Log.d("OneSignalDebug", "Gửi đến playerId: " + subId);
                                Log.d("OneSignalDebug", "JSON Body: " + jsonBody.toString());

                                OutputStream outputStream = conn.getOutputStream();
                                outputStream.write(jsonBody.toString().getBytes("UTF-8"));
                                outputStream.flush();
                                outputStream.close();

                                int httpResponse = conn.getResponseCode();
                                Log.d("OneSignalDebug", "Response Code: " + httpResponse);

                                InputStream inputStream;
                                if (httpResponse >= 200 && httpResponse < 400) {
                                    inputStream = conn.getInputStream();
                                } else {
                                    inputStream = conn.getErrorStream();
                                }

                                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                                String response = s.hasNext() ? s.next() : "";
                                Log.d("OneSignalDebug", "Response Body: " + response);

                            } catch (Exception e) {
                                Log.e("OneSignalDebug", "Lỗi khi gửi thông báo", e);
                            }
                        });
                    }
                } else {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            URL url = new URL("https://onesignal.com/api/v1/notifications");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setUseCaches(false);
                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            conn.setRequestProperty("Authorization", "Basic " + REST_API_KEY);
                            conn.setRequestMethod("POST");

                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("app_id", APP_ID);
                            JSONArray playerIds = new JSONArray();
                            playerIds.put(userId);
                            jsonBody.put("include_player_ids", playerIds); // Gửi riêng
                            jsonBody.put("headings", new JSONObject().put("en", title));
                            jsonBody.put("contents", new JSONObject().put("en", message));

                            Log.d("OneSignalDebug", "Gửi đến playerId: " + userId);
                            Log.d("OneSignalDebug", "JSON Body: " + jsonBody.toString());

                            OutputStream outputStream = conn.getOutputStream();
                            outputStream.write(jsonBody.toString().getBytes("UTF-8"));
                            outputStream.flush();
                            outputStream.close();

                            int httpResponse = conn.getResponseCode();
                            Log.d("OneSignalDebug", "Response Code: " + httpResponse);

                            InputStream inputStream;
                            if (httpResponse >= 200 && httpResponse < 400) {
                                inputStream = conn.getInputStream();
                            } else {
                                inputStream = conn.getErrorStream();
                            }

                            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                            String response = s.hasNext() ? s.next() : "";
                            Log.d("OneSignalDebug", "Response Body: " + response);

                        } catch (Exception e) {
                            Log.e("OneSignalDebug", "Lỗi khi gửi thông báo", e);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
