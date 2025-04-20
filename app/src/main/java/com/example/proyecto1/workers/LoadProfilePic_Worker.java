package com.example.proyecto1.workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.proyecto1.Data_Load;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadProfilePic_Worker extends Worker {
    private static final String TAG = "LoadProfilePicWorker";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_IMAGE_DATA = "image_data";
    public static final String KEY_ERROR_MSG = "error_message";

    public LoadProfilePic_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        Log.d(TAG, "Worker inicializado");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando doWork()");

        // 1. Obtener datos de entrada
        String username = getInputData().getString(KEY_USERNAME);
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Nombre de usuario no proporcionado");
            return Result.failure(createErrorOutput("Nombre de usuario requerido"));
        }
        Log.d(TAG, "Usuario a cargar: " + username);

        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            // 2. Crear solicitud JSON
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("action", "load_profile_pic");
            jsonRequest.put("username", username);
            String requestBody = jsonRequest.toString();
            Log.d(TAG, "Cuerpo de la solicitud: " + requestBody);

            // 3. Configurar conexión HTTP
            URL url = new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/account.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000); // 15 segundos timeout
            conn.setReadTimeout(15000);
            Log.d(TAG, "Conexión HTTP configurada");

            // 4. Enviar solicitud
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(requestBody);
                writer.flush();
                Log.d(TAG, "Solicitud enviada al servidor");
            }

            // 5. Procesar respuesta
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Código de respuesta HTTP: " + responseCode);

            InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
                Log.w(TAG, "Respuesta de error del servidor");
            }

            // 6. Leer respuesta
            StringBuilder response = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            String responseBody = response.toString();
            Log.d(TAG, "Respuesta del servidor: " + responseBody);

            // 7. Parsear JSON
            if (responseBody.isEmpty()) {
                Log.e(TAG, "Respuesta vacía del servidor");
                return Result.failure(createErrorOutput("Respuesta vacía del servidor"));
            }

            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
            Log.d(TAG, "JSON parseado correctamente");

            // 8. Verificar estado
            if (!"success".equals(jsonResponse.get("status"))) {
                String errorMsg = (String) jsonResponse.getOrDefault("message", "Estado desconocido");
                Log.e(TAG, "Error en la respuesta: " + errorMsg);
                return Result.failure(createErrorOutput(errorMsg));
            }

            // 9. Obtener imagen
            String imageBase64 = (String) jsonResponse.get("image_data");
            if (imageBase64 == null || imageBase64.isEmpty()) {
                Log.w(TAG, "No se encontró imagen de perfil para el usuario");
                return Result.success(); // Éxito pero sin imagen
            }

            // 10. Verificar formato de imagen
            try {
                byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length); // Solo para validar
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Formato de imagen inválido", e);
                return Result.failure(createErrorOutput("Formato de imagen inválido"));
            }

            //Guardamos en Data_Load la foto de perfil para evitar limitaciones de peso de la clase Data
            Data_Load.getDL().setProfPic(imageBase64);

            Log.d(TAG, "Worker completado con éxito");
            return Result.success();

        } catch (IOException e) {
            Log.e(TAG, "Error de conexión: " + e.getMessage(), e);
            return Result.failure(createErrorOutput("Error de conexión: " + e.getMessage()));
        } catch (ParseException e) {
            Log.e(TAG, "Error al parsear JSON: " + e.getMessage(), e);
            return Result.failure(createErrorOutput("Error en formato de respuesta"));
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado: " + e.getMessage(), e);
            return Result.failure(createErrorOutput("Error inesperado"));
        } finally {
            // 12. Cerrar recursos
            if (conn != null) {
                conn.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "Error al cerrar reader", e);
                }
            }
        }
    }

    private Data createErrorOutput(String message) {
        return new Data.Builder()
                .putString(KEY_ERROR_MSG, message)
                .build();
    }
}