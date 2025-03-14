package com.example.proyecto1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Main_Activity extends AppCompatActivity {

    private TextView nuggets_view;
    private ImageButton dig_button;
    private ImageButton shop_button;
    private ImageButton save_button;
    private ImageButton options_button;
    private int nuggets;
    private int click_points = 1;
    private Timer timer;
    private int passive_points = 0;
    private float passive_multiplier = 1.0f;
    private float click_multiplier = 1.0f;

    private static final int REQUEST_CODE = 1; // Código de solicitud


    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setup general de la actividad
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Cargamos los datos
        this.nuggets = getIntent().getIntExtra("points", 0);
        this.click_points = getIntent().getIntExtra("click_points", 0);
        this.passive_points = getIntent().getIntExtra("passive_points", 0);
        this.click_multiplier = getIntent().getFloatExtra("click_multiplier", 0);
        this.passive_multiplier = getIntent().getFloatExtra("passive_multiplier", 0);
        int idle_gained = getIntent().getIntExtra("idle_points", 0);

        if (idle_gained > 0) {
            Toast.makeText(this, "¡Has generado: " + idle_gained + " pepitas en tu ausencia!", Toast.LENGTH_LONG).show();
        }


        //Identificar botones
        nuggets_view = findViewById(R.id.nuggets_v);
        dig_button = findViewById(R.id.dig_b);
        shop_button = findViewById(R.id.shop_b);
        save_button = findViewById(R.id.save_button);
        options_button = findViewById(R.id.options_button);


        //-------------------------//Lógica de la ganancia pasiva//---------------------------
        // Crear un Timer
        timer = new Timer();

        // Programar la tarea cada segundo
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Actualizar el TextView en el hilo principal
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nuggets = Math.round(nuggets + (passive_points * passive_multiplier));
                        nuggets_view.setText(String.valueOf(nuggets));
                    }
                });
            }
        }, 0, 1000); // Retraso inicial de 0 ms, intervalo de 1000 ms (1 segundo)

        //Lógica del boton de cavar
        dig_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nuggets = Math.round(nuggets + (click_points * click_multiplier));
                nuggets_view.setText(String.valueOf(nuggets));
            }
        });

        //---------------------------------//Boton de tienda//----------------------------------------
        shop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Activity.this, Shop_Activity.class);
                intent.putExtra("balance", nuggets);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //------------------------------//Boton de guardado//--------------------------------------
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data_Load.getDL().save_upgrades(getApplicationContext()); //Guardar las mejoras
                save_stats(getApplicationContext()); //Guardar la puntuacion y multiplicadores
            }
        });

        //------------------------------//Boton de opciones//--------------------------------------
        options_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Activity.this, Options_Activity.class);
                startActivity(intent);
            }
        });


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Mostrar el cuadro de diálogo cuando se presione el botón de retroceso
                showExitDialog();
            }
        };

        // Registrar el callback con el OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<Integer> bought_upgrades = data.getIntegerArrayListExtra("bought");
            if (bought_upgrades != null){
                for (int num : bought_upgrades) {
                    apply_upgrades(num);
                }
                nuggets_view.setText(String.valueOf(nuggets));
            }
        }
    }

    private void apply_upgrades(int id){
        Generic_Upgrade upgrade = Data_Load.getDL().get_upgrade_by_id(id);

        nuggets = nuggets-upgrade.get_price();
        int[] upgrade_info = upgrade.get_upgrade();


        //Comprobamos el objetivo de la mejora
        if (upgrade_info[1] == 1) {
            if (upgrade_info[0] == 0) {
                this.click_points += upgrade.get_upgrade_value();
            } else {
                float percentage = (float) upgrade.get_upgrade_value() / 100;
                this.click_multiplier = this.click_multiplier + percentage;
            }
        } else {
            if (upgrade_info[0] == 0) {
                this.passive_points += upgrade.get_upgrade_value();
            } else {
                float percentage = (float) upgrade.get_upgrade_value() / 100;
                this.passive_multiplier = this.passive_multiplier + percentage;
            }
        }
        //Debug//Toast.makeText(this,"a" + pasive_multiplier,Toast.LENGTH_SHORT).show();

    }



    private void save_stats(Context context){

        DbConnector connector = new DbConnector(context);
        try (SQLiteDatabase db = connector.getWritableDatabase()) {

            int id = 1; //De momento siempre se guarda en el mismo slot, el primero, no hay opcion de tener mas archivos de guardado

            LocalDateTime now = LocalDateTime.now();
            String date_formated = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // Formato ISO-860

            // Crear un ContentValues para almacenar los valores a actualizar
            ContentValues values = new ContentValues();
            values.put("points", this.nuggets);
            values.put("click_points", this.click_points);
            values.put("passive_points", this.passive_points);
            values.put("click_multiplier", this.click_multiplier);
            values.put("passive_multiplier", this.passive_multiplier);
            values.put("date", date_formated);

            // Actualizar la fila correspondiente en la base de datos
            int rowsAffected = db.update("points", values, "id = ?", new String[]{String.valueOf(id)});

            // Verificar si la actualización fue exitosa
            if (rowsAffected > 0) {
                Log.d("Actualizacion", "Fila actualizada con ID: " + id);
                Toast.makeText(this,"Se ha guardado la partida",Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Actualizacion", "No se encontró ninguna fila con ID: " + id);
            }
        } catch (SQLException e) {
            // Manejar cualquier excepción de SQL
            Log.e("Actualizacion", "Error al actualizar la base de datos", e);
        }
        // Cerrar la base de datos
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Salir de la mina")
                .setMessage("¿Estás seguro de que quieres salir?")
                .setPositiveButton("Salir y guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Data_Load.getDL().save_upgrades(getApplicationContext()); //Guardar las mejoras
                        save_stats(getApplicationContext()); //Guardar la puntuacion y multiplicadores
                        finishAffinity();
                    }
                })
                .setNegativeButton("Aún no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


}