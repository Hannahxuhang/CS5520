package edu.neu.madcourse.hangxu.fcm;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FCMAsyncTask extends AsyncTask<Void, Void, Void> {

    private final static String TAG = FCMAsyncTask.class.getSimpleName();
    private final static String SERVER_KEY = "key=AAAAWC-YCNI:APA91bFPTDt3CzZ5C9rwXcKJndJTSlYVu486OP_PAr66SEu3jzfieG27gtWQleziDihmPoNRTywDOxNGu49f6xoo4PHJPDrf3GO0VaCf4Xz8bM1SBlEtIhaAYa3eQcqfTzABMDsYsaXw";
    private final static String SEND_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private final static String PATH = "/topics/";

    private final String sendPath;
    private final String data;
    private final String notificationTitle;
    private final String notificationBody;

    public FCMAsyncTask(String sendPath, String data, String notificationTitle, String notificationBody) {
        this.sendPath = sendPath;
        this.data = data;
        this.notificationTitle = notificationTitle;
        this.notificationBody = notificationBody;
    }

    @Override
    public Void doInBackground(Void... params) {
        JSONObject jPayLoad = new JSONObject();
        JSONObject jNotification = new JSONObject();

        try {
            jNotification.put("title", notificationTitle);
            jNotification.put("body", notificationBody);
            jNotification.put("sound", "default");

            jPayLoad.put("to", PATH + sendPath);
            jPayLoad.put("priority", "high");
            jPayLoad.put("notification", jNotification);

            if (data != null) {
                JSONObject jData = new JSONObject();
                jData.put("data", data);
                jPayLoad.put("data", jData);
            }

            URL url = new URL(SEND_MESSAGE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // send FCM message content
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayLoad.toString().getBytes());
            outputStream.close();

            // read FCM message content
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }
}
