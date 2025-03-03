package com.example.proyecto1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Timer;
import java.util.TimerTask;

public class Main_Activity extends AppCompatActivity {

    private TextView nuggets_view;
    private Button dig_button;
    private Button shop_button;
    private int nuggets = 0;
    private int click_points = 1;
    private Timer timer;
    private int passive_points = 0;


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
        
        //Identificar botones
        nuggets_view = findViewById(R.id.nuggets_v);
        dig_button = findViewById(R.id.dig_b);
        shop_button = findViewById(R.id.shop_b);
        
        //Lógica de la ganancia pasiva (Añadir mejoras)
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
                        nuggets = nuggets + passive_points;
                        nuggets_view.setText(String.valueOf(nuggets));
                    }
                });
            }
        }, 0, 1000); // Retraso inicial de 0 ms, intervalo de 1000 ms (1 segundo)

        //Lógica del boton de cavar
        dig_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nuggets = nuggets + click_points;
                nuggets_view.setText(String.valueOf(nuggets));

            }
        });

        shop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Activity.this, Shop_Activity.class);
                intent.putExtra("balance", nuggets);
                startActivity(intent);
            }
        });
    }
}