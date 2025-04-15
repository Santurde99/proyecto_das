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

public class UserLogin_Worker extends Worker {

    // Keys para los datos
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_LOGIN_RESULT = "login_valid";

    public UserLogin_Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 1. Obtener credenciales sin hashear
        Data inputData = getInputData();
        String username = inputData.getString(KEY_USERNAME);
        String plainPassword = inputData.getString(KEY_PASSWORD);

        if (username == null || plainPassword == null) {
            return Result.failure();
        }

        HttpURLConnection urlConnection = null;
        try {
            // Configurar conexión
            URL dest = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/account.php");
            urlConnection = (HttpURLConnection) dest.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            // Crear JSON con credenciales (password SIN hashear)
            JSONObject requestJson = new JSONObject();
            requestJson.put("action", "login");
            requestJson.put(KEY_USERNAME, username);
            requestJson.put(KEY_PASSWORD, plainPassword); // Enviamos la contraseña sin hashear

            // Enviar petición
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(requestJson.toString());
            out.close();

            // Procesar respuesta
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                //Parsear respuesta JSON
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());

                // servidor devuelve {"valid": true/false}
                boolean isValid = (Boolean) jsonResponse.get("valid");

                return Result.success(
                        new Data.Builder()
                                .putBoolean(KEY_LOGIN_RESULT, isValid)
                                .build()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return Result.failure();
    }
}