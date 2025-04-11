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

public class NewUU_Worker extends Worker {
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UPGRADE_ID = "upgrade_id";
    private static final String KEY_STATUS = "status";

    public NewUU_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        JSONObject json = new JSONObject();
        json.put("action", "new");
        json.put(KEY_USERNAME, inputData.getString(KEY_USERNAME));
        json.put(KEY_UPGRADE_ID, inputData.getInt(KEY_UPGRADE_ID, 0));
        json.put(KEY_STATUS, inputData.getInt(KEY_STATUS, 0));

        try {
            URL url = new URL("http://tuserver.com/upgrades_api.php");
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