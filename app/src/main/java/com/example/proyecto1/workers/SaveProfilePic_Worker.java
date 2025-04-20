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
import org.json.simple.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SaveProfilePic_Worker extends Worker {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_IMAGE_URI = "image_uri";
    private static final int TARGET_SIZE = 420; // Tamaño máximo en píxeles

    public SaveProfilePic_Worker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {

            Data inputData = getInputData();
            String username = inputData.getString(KEY_USERNAME);
            String imageUri = inputData.getString(KEY_IMAGE_URI);

            if (username == null || imageUri == null) {
                return Result.failure();
            }

            // 1. Obtener imagen original
            InputStream imageStream = getApplicationContext().getContentResolver()
                    .openInputStream(android.net.Uri.parse(imageUri));
            Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);
            imageStream.close();

            // 2. Redimensionar (manteniendo aspect ratio)
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();
            float ratio = (float) originalWidth / originalHeight;

            int newWidth, newHeight;
            if (originalWidth > originalHeight) {
                newWidth = TARGET_SIZE;
                newHeight = (int) (TARGET_SIZE / ratio);
            } else {
                newHeight = TARGET_SIZE;
                newWidth = (int) (TARGET_SIZE * ratio);
            }

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    originalBitmap, newWidth, newHeight, true);
            originalBitmap.recycle();

            // 3. Convertir a Base64
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream);
            String imageBase64 = Base64.encodeToString(byteStream.toByteArray(), Base64.DEFAULT);

            // 4. Enviar al servidor
            HttpURLConnection conn = (HttpURLConnection) new URL("http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/agutierrez186/WEB/account.php")
                    .openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("action", "save_profile_pic");
            json.put("username", username);
            json.put("image_data", imageBase64);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(json.toString());
            out.close();

            return conn.getResponseCode() == 200 ? Result.success() : Result.failure();
        } catch (Exception e) {
            Log.e("Error","info: " + e);
            return Result.failure();
        }
    }
}