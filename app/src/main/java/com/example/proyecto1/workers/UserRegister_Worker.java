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

/*
Worker para gestionar la creación de cuentas
 */


public class UserRegister_Worker extends Worker {

    public UserRegister_Worker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Data inputData = getInputData();
        String username = inputData.getString("username");
        String password = inputData.getString("password");

        HttpURLConnection urlConnection = null;
        try {
            //Preparamos la conexion
            URL dest = new URL("http://tuserver.com/registro_usuario.php");
            urlConnection = (HttpURLConnection) dest.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json"); // (página 17)

            //Preparamos los datos
            JSONObject parametersJSON = new JSONObject();
            parametersJSON.put("username", username);
            parametersJSON.put("password", password);

            // Enviamos los datos
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(parametersJSON.toString());
            out.close();

            //Esperamos respuesta
            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
                in.close();

                // Procesamos la respuesta
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(result.toString());


                Data outputData = new Data.Builder()
                        .putString("status", (String) jsonResponse.get("status"))
                        .putString("message", (String) jsonResponse.get("message"))
                        .build();

                return Result.success(outputData);
            } else {
                return Result.failure();
            }
        } catch (Exception e) {
            return Result.failure();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}