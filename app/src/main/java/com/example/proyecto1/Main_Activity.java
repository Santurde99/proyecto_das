package com.example.proyecto1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.proyecto1.workers.SaveGS_Worker;
import com.example.proyecto1.workers.UserLogin_Worker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Main_Activity extends AppCompatActivity {

    private TextView nuggets_view;
    private ImageButton dig_button;
    private ImageButton shop_button;
    private ImageButton save_button;
    private ImageButton options_button;
    private ImageButton map_button;
    private static int nuggets;
    private int click_points = 1;
    private Timer timer;
    private int passive_points = 0;
    private float passive_multiplier = 1.0f;
    private float click_multiplier = 1.0f;
    private int notification_count = 0;

    private static final int REQUEST_CODE = 1; // Código de solicitud

    private LifecycleOwner lco;

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
        Notification_Helper.createNotificationChannel(this);
        this.lco = this;
        //Cargamos los datos
        this.nuggets = getIntent().getIntExtra("points", 0);
        this.click_points = getIntent().getIntExtra("click_points", 0);
        this.passive_points = getIntent().getIntExtra("passive_points", 0);
        this.click_multiplier = getIntent().getFloatExtra("click_multiplier", 0);
        this.passive_multiplier = getIntent().getFloatExtra("passive_multiplier", 0);
        int idle_gained = getIntent().getIntExtra("idle_points", 0);

        if (idle_gained > 0) {
            Toast.makeText(this, getString(R.string.return_toast_1_1) + " "+idle_gained+" " + getString(R.string.return_toast_1_2), Toast.LENGTH_LONG).show();
        }


        //Identificar botones
        nuggets_view = findViewById(R.id.nuggets_v);
        dig_button = findViewById(R.id.dig_b);
        shop_button = findViewById(R.id.shop_b);
        save_button = findViewById(R.id.save_button);
        options_button = findViewById(R.id.options_button);
        map_button = findViewById(R.id.map_b);


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
                        if ((nuggets > 1000) &&(notification_count == 0)){
                            Notification_Helper.showNotification(Main_Activity.this,getString(R.string.notif_2_title),getString(R.string.notif_2_desc));
                            notification_count = notification_count +1;
                        }
                    }
                });
            }
        }, 0, 1000); //intervalo de 1000 ms (1 segundo)

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
                Data_Load.getDL().save_upgrades(getApplicationContext(),lco); //Guardar las mejoras
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

        //------------------------------//Boton de mapa//--------------------------------------

        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Activity.this, Map_Activity.class);
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


    public static void add_nuggets(int amount){
        nuggets = nuggets +amount;
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

        int[] upgrade_info;

        if (upgrade instanceof Repeatable_Upgrade) {
            nuggets = nuggets- ((Repeatable_Upgrade) upgrade).get_previous_price();
            upgrade_info = upgrade.get_upgrade();
        }else {
            nuggets = nuggets - upgrade.get_price();
            upgrade_info = upgrade.get_upgrade();
        }


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



    private void save_stats(Context context) {
        OneTimeWorkRequest gs_save_request = new OneTimeWorkRequest.Builder(SaveGS_Worker.class)
                .setInputData(new Data.Builder()
                        .putString(SaveGS_Worker.KEY_USERNAME, Data_Load.getDL().getUsername())
                        .putInt(SaveGS_Worker.KEY_POINTS, nuggets)
                        .putInt(SaveGS_Worker.KEY_CLICK_POINTS, click_points)
                        .putInt(SaveGS_Worker.KEY_PASSIVE_POINTS, passive_points)
                        .putDouble(SaveGS_Worker.KEY_CLICK_MULTIPLIER, click_multiplier)
                        .putDouble(SaveGS_Worker.KEY_PASSIVE_MULTIPLIER, passive_multiplier)
                        .build())
                .build();

        // Obtener el WorkManager y encolar el trabajo
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(gs_save_request);

        // Observar el resultado del trabajo
        workManager.getWorkInfoByIdLiveData(gs_save_request.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            // Mostrar Toast de éxito
                            runOnUiThread(() -> Toast.makeText(
                                    Main_Activity.this,
                                    "Datos guardados correctamente",
                                    Toast.LENGTH_SHORT).show());
                        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                            // Mostrar Toast de error
                            runOnUiThread(() -> Toast.makeText(
                                    Main_Activity.this,
                                    "Error al guardar los datos",
                                    Toast.LENGTH_SHORT).show());
                        }
                    }
                });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_diag_title))
                .setMessage(getString(R.string.exit_diag_text))
                .setPositiveButton(getString(R.string.exit_diag_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Data_Load.getDL().save_upgrades(getApplicationContext(),lco); //Guardar las mejoras
                        save_stats(getApplicationContext()); //Guardar la puntuacion y multiplicadores
                        finishAffinity();
                        System.exit(0);
                    }
                })
                .setNegativeButton(getString(R.string.exit_diag_no) , new DialogInterface.OnClickListener() {
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