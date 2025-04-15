package com.example.proyecto1.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;

public class UserRegister_Worker extends Worker {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_STATUS = "status";
    public static final String KEY_MESSAGE = "message";

    public UserRegister_Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String username = inputData.getString(KEY_USERNAME);
        String password = inputData.getString(KEY_PASSWORD);

        HttpURLConnection urlConnection = null;
        try {
            // Preparar la conexión
            URL dest = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/account.php");
            urlConnection = (HttpURLConnection) dest.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            // Preparar los datos
            JSONObject parametersJSON = new JSONObject();
            parametersJSON.put(KEY_USERNAME, username);
            parametersJSON.put(KEY_PASSWORD, password);
            parametersJSON.put("action", "register");

            Log.i("json", String.valueOf(parametersJSON));

            // Enviar los datos
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(parametersJSON.toString());
            out.close();

            // Esperar respuesta
            int statusCode = urlConnection.getResponseCode();

            //Extraemos la info de la respuesta para logs
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();

            if (statusCode == 200) {
                // Procesar la respuesta
                Log.e("ServerResponse", result.toString());
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(result.toString());

                String status = (String) jsonResponse.get(KEY_STATUS);
                String message = (String) jsonResponse.get(KEY_MESSAGE);

                // Si el estado es "error" y el mensaje indica que el usuario ya existe
                if ("error".equals(status) && "El usuario ya existe".equals(message)) {
                    // Consideramos esto como un fallo
                    Data outputData = new Data.Builder()
                            .putString("status", status)
                            .putString("message", message)
                            .build();
                    return Result.failure(outputData);
                } else if ("success".equals(status)) {
                    // Registro exitoso
                    Data outputData = new Data.Builder()
                            .putString("status", status)
                            .putString("message", message)
                            .build();
                    return Result.success(outputData);
                } else {
                    // Otro tipo de error
                    return Result.failure();
                }
            } else {
                Log.e("UserRegister_Worker", "Código de respuesta: " + statusCode);
                return Result.failure();
            }

        } catch (FileNotFoundException e) {
            Log.e("HTTP", "Endpoint not found", e);
            return Result.failure();
        } catch (IOException e) {
            Log.e("HTTP", "Network error", e);
            return Result.failure();
        } catch (Exception e) {
            Log.e("UserRegister_Worker", "Error en doWork: " + e.getMessage(), e);
            return Result.failure();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}