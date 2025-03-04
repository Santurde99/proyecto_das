package com.example.proyecto1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
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
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
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
        int[] target_value = new int[1]; //USAMOS UN ARRAY DE 1 POSICION COMO PUNTERO


        //Comprobamos el objetivo de la mejora
        if (upgrade_info[1] == 1) {target_value[0] = this.click_points; }
        else{target_value[0] = this.passive_points;}

        //Comprobamos si va a ser un valor fijo o porcentual y se aplica
        if (upgrade_info[0] == 0) {
            target_value[0] = target_value[0] + upgrade.get_upgrade_value();
        }else{
            target_value[0] = target_value[0] * upgrade.get_upgrade_value();
        }

        int[] unlocks = upgrade.get_unlocks(); //Tomamos las ids de las mejoras que desbloquea esta mejora
        for (int num : unlocks){
            Generic_Upgrade temp_upgrade = Data_Load.getDL().get_upgrade_by_id(num);
            temp_upgrade.unlock_upgrade();
        }
        Toast.makeText(this,": " + click_points ,Toast.LENGTH_SHORT).show();

    }


}