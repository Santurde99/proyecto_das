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

public class NewUU_Worker extends Worker {
    public static final String KEY_USERNAME = "username";
    public static final String KEY_UPGRADE_ID = "upgrade_id";
    public static final String KEY_STATUS = "status";

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
        json.put(KEY_UPGRADE_ID, inputData.getInt(KEY_UPGRADE_ID, 1));
        json.put(KEY_STATUS, inputData.getInt(KEY_STATUS, 0));

        try {
            URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/user_upgrades.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(json.toString());
            out.close();


            int statusCode = urlConnection.getResponseCode();


            //Para tomar el stream adecuado para logear
            BufferedReader in;
            if (urlConnection.getResponseCode() >= 400) {
                in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            Log.e("ServerRawResponse", String.valueOf(response));


            if (statusCode == 200) {

                JSONParser parser = new JSONParser();
                JSONObject responseJson = (JSONObject) parser.parse(String.valueOf(response));

                if ("success".equals(responseJson.get("status"))) {
                    return Result.success();
                }
            }
            Log.e("UserRegister_Worker", "Código de respuesta: " + statusCode);
            Log.e("ServerRawResponse", response.toString());
            return Result.failure();
        } catch (Exception e) {
            Log.e("UserRegister_Worker", "Error en doWork: " + e.getMessage(), e);
            return Result.failure();
        }
    }
}