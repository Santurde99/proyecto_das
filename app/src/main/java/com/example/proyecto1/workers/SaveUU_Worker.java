package com.example.proyecto1.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SaveUU_Worker extends Worker {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_UPGRADES_JSON = "upgrades_json";

    public SaveUU_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String username = inputData.getString(KEY_USERNAME);
        String upgradesJson = inputData.getString(KEY_UPGRADES_JSON);

        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("action", "save");
            requestJson.put(KEY_USERNAME, username);
            requestJson.put("upgrades", upgradesJson);

            URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/user_upgrades.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(requestJson.toString());
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