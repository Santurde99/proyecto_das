package com.example.proyecto1.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SaveGS_Worker extends Worker {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_POINTS = "points";
    public static final String KEY_CLICK_POINTS = "click_points";
    public static final String KEY_PASSIVE_POINTS = "passive_points";
    public static final String KEY_CLICK_MULTIPLIER = "click_multiplier";
    public static final String KEY_PASSIVE_MULTIPLIER = "passive_multiplier";

    public SaveGS_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        JSONObject json = new JSONObject();
        json.put("action", "save");
        json.put(KEY_USERNAME, inputData.getString(KEY_USERNAME));
        json.put(KEY_POINTS, inputData.getInt(KEY_POINTS, 0));
        json.put(KEY_CLICK_POINTS, inputData.getInt(KEY_CLICK_POINTS, 1));
        json.put(KEY_PASSIVE_POINTS, inputData.getInt(KEY_PASSIVE_POINTS, 0));
        json.put(KEY_CLICK_MULTIPLIER, inputData.getDouble(KEY_CLICK_MULTIPLIER, 1.0));
        json.put(KEY_PASSIVE_MULTIPLIER, inputData.getDouble(KEY_PASSIVE_MULTIPLIER, 1.0));

        try {
            URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/game_state.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(json.toString());
            out.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = in.readLine();
                in.close();

                JSONParser parser = new JSONParser();
                JSONObject responseJson = (JSONObject) parser.parse(response);

                if ("success".equals(responseJson.get("status"))) {
                    return Result.success();
                }
            }
            return Result.failure();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
