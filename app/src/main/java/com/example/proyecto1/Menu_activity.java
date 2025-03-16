package com.example.proyecto1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

public class Menu_activity extends AppCompatActivity {

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private int load_points;
    private int load_click_points;
    private int load_passive_points;
    private float load_click_multiplier;
    private float load_passive_multiplier;
    private String load_date;
    private Boolean ready;
    private ConstraintLayout layout;
    private int idle_gained_points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.ready = false;

        super.onCreate(savedInstanceState);
        Language_Helper.loadLocale(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Pedir permiso de notificaciones
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Notification_Helper.showNotification(this, getString(R.string.notif_1_title), getString(R.string.notif_1_desc));
                    } else {
                        Toast.makeText(this, getString(R.string.notif_toast), Toast.LENGTH_LONG).show();
                    }
                });

        askForPermission();


        layout = findViewById(R.id.main);

        //Comprobar si es la primera vez que se inicia la app comprobando la bd
        Context context = getApplicationContext();
        if (getDatabasePath("database.db").exists()){
            //Existe la base de datos, cargamos los datos y los pasamos en un intent
            load_stats(context);
            Data_Load.generate_list(true,context);
            add_afk_points();
            ready = true;

        } else{
            //No existe la base de datos, debe crearse e inicializarse
            Data_Load.generate_list(false,context);
            first_start_stats(context);
            this.idle_gained_points =  0;
            ready = true;

        }

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ready) {
                    // Crear un Intent para iniciar la nueva actividad
                    Intent intent = new Intent(Menu_activity.this, Main_Activity.class);
                    // Pasar datos a la nueva actividad
                    intent.putExtra("points", load_points);
                    intent.putExtra("date", load_date);
                    intent.putExtra("click_points", load_click_points);
                    intent.putExtra("passive_points", load_passive_points);
                    intent.putExtra("click_multiplier", load_click_multiplier);
                    intent.putExtra("passive_multiplier", load_passive_multiplier);
                    intent.putExtra("idle_points", idle_gained_points);

                    // Iniciar la nueva actividad
                    startActivity(intent);
                    // Destruir la actividad actual
                    finish();
                }
            }
        });

    }

    private void load_stats(Context context){
        DbConnector connector = new DbConnector(context);

        // Recorrer el cursor
        try (Cursor cursor = connector.get_whole_table("points")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Extraer los datos de cada columna
                    this.load_points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                    this.load_date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    this.load_click_points = cursor.getInt(cursor.getColumnIndexOrThrow("click_points"));
                    this.load_passive_points = cursor.getInt(cursor.getColumnIndexOrThrow("passive_points"));
                    this.load_click_multiplier = cursor.getFloat(cursor.getColumnIndexOrThrow("click_multiplier"));
                    this.load_passive_multiplier = cursor.getFloat(cursor.getColumnIndexOrThrow("passive_multiplier"));

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Maneja cualquier otra excepción que pueda ocurrir
            Log.e("Error", "Ocurrió un error al cargar los puntos", e);
        }
    }

    private void first_start_stats(Context context){
        DbConnector connector = new DbConnector(context);
        try (SQLiteDatabase db = connector.getWritableDatabase()) {

            //Creamos el primer archivo de guardado
            int points = 0;

            LocalDateTime now = LocalDateTime.now();
            String date_formated = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // Formato ISO-860

            this.load_points = points;
            this.load_date = date_formated;
            this.load_click_points = 1;
            this.load_passive_points = 0;
            this.load_click_multiplier = 1.0f;
            this.load_passive_multiplier = 1.0f;

            // Crear un ContentValues para almacenar los valores a actualizar
            ContentValues values = new ContentValues();
            values.put("points", points);
            values.put("date",date_formated);
            values.put("click_points", this.load_click_points);
            values.put("passive_points", this.load_passive_points);
            values.put("click_multiplier", this.load_click_multiplier);
            values.put("passive_multiplier", this.load_passive_multiplier);
            values.put("date", date_formated);


            // Actualizar la fila correspondiente en la base de datos
            long newRowId = db.insert("points",null, values);

            // Verificar si la inserción fue exitosa
            if (newRowId != -1) {
                Log.d("Inserción", "Fila insertada con ID: " + newRowId);
            } else {
                Log.d("Inserción", "Error al insertar la fila con ID: " + newRowId);
            }

        } catch (SQLException e) {
            // Manejar cualquier excepción de SQL
            Log.e("Actualizacion", "Error al actualizar la base de datos", e);
        }
    }

    private void add_afk_points(){
        LocalDateTime last_login = LocalDateTime.parse(this.load_date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(last_login, now);
        long seconds_since_last_login = duration.getSeconds();
        this.idle_gained_points = Math.round(seconds_since_last_login + ((this.load_passive_points * this.load_passive_multiplier)/2));
        this.load_points = this.load_points + this.idle_gained_points;
    }

    private void askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Pedir el permiso al usuario
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}