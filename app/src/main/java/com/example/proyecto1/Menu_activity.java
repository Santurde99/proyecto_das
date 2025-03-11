package com.example.proyecto1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Menu_activity extends AppCompatActivity {

    private int load_points;
    private String load_date;
    private Boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.ready = false;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Comprobar si es la primera vez que se inicia la app comprobando la bd
        Context context = getApplicationContext();
        if (getDatabasePath("database.db").exists()){
            //Existe la base de datos, cargamos los datos y los pasamos en un intent
            Data_Load.generate_list(true,context);
        } else{
            //No existe la base de datos, debe crearse e inicializarse
            Data_Load.generate_list(false,context);
            first_start_stats(context);
        }

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

            // Crear un ContentValues para almacenar los valores a actualizar
            ContentValues values = new ContentValues();
            values.put("points", points);
            values.put("date",date_formated);


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

}