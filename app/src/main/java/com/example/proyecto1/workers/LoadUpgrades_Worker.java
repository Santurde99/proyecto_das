package com.example.proyecto1.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadUpgrades_Worker extends Worker {
    public static final String KEY_RESULT = "upgrades_list";

    public LoadUpgrades_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/upgrades.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONParser parser = new JSONParser();
                JSONObject responseJson = (JSONObject) parser.parse(response.toString());

                if ("success".equals(responseJson.get("status"))) {
                    JSONArray upgrades = (JSONArray) responseJson.get("data");
                    Data outputData = new Data.Builder()
                            .putString(KEY_RESULT, upgrades.toJSONString())
                            .build();
                    return Result.success(outputData);
                }
            } else {
                Log.e("LoadUpgrades_Worker", "HTTP error code: " + conn.getResponseCode());
            }
            return Result.failure();
        } catch (Exception e) {
            Log.e("LoadUpgrades_Worker", "Error in doWork: " + e.getMessage(), e);
            return Result.failure();
        }
    }
}