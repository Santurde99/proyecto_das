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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadUU_Worker extends Worker {
    private static final String KEY_USERNAME = "username";
    private static final String KEY_RESULT = "upgrades_data";

    public LoadUU_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String username = getInputData().getString(KEY_USERNAME);
        if (username == null) {
            return Result.failure();
        }

        try {
            URL url = new URL("http://tuserver.com/upgrades_api.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("action", "load");
            json.put(KEY_USERNAME, username);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(json.toString());
            out.close();

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
            }
            return Result.failure();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}