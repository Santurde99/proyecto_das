package com.example.proyecto1.workers;

import android.content.Context;
import android.util.Log;

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

public class LoadGS_Worker extends Worker {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_RESULT = "game_state_data";

    public LoadGS_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
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
            URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/game_state.php");
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
                    Data outputData = new Data.Builder()
                            .putString(KEY_RESULT, responseJson.toJSONString())
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