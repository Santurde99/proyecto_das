package com.example.proyecto1;

import com.example.proyecto1.workers.NewGS_Worker;
import com.example.proyecto1.workers.NewUU_Worker;
import com.example.proyecto1.workers.UserLogin_Worker;
import com.example.proyecto1.workers.UserRegister_Worker;
import com.example.proyecto1.workers.LoadUU_Worker;
import com.example.proyecto1.workers.LoadGS_Worker;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.json.JSONObject;
import org.json.JSONException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Menu_activity extends AppCompatActivity {

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private int load_points;
    private int load_click_points;
    private int load_passive_points;
    private float load_click_multiplier;
    private float load_passive_multiplier;
    private String load_date;
    private ConstraintLayout layout;
    private int idle_gained_points;
    private String username;


    private String gs_data;
    private String uu_data;


    private EditText user;
    private EditText pass;
    private Button login;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Language_Helper.loadLocale(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu_activity_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

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

        Context context = getApplicationContext();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_user();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_user();
            }
        });

    }

    private void login_user() {
        String username = user.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }
        this.username = username;

        // Workers para el proceso de login
        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(UserLogin_Worker.class)
                .setInputData(new Data.Builder()
                        .putString(UserLogin_Worker.KEY_USERNAME, username)
                        .putString(UserLogin_Worker.KEY_PASSWORD, password)
                        .build())
                .build();

        OneTimeWorkRequest loadGSRequest = new OneTimeWorkRequest.Builder(LoadGS_Worker.class)
                .setInputData(new Data.Builder()
                        .putString(LoadGS_Worker.KEY_USERNAME, username)
                        .build())
                .build();

        OneTimeWorkRequest loadUURequest = new OneTimeWorkRequest.Builder(LoadUU_Worker.class)
                .setInputData(new Data.Builder()
                        .putString(LoadUU_Worker.KEY_USERNAME, username)
                        .build())
                .build();

        // Ejecutamos los workers en cadena
        WorkManager.getInstance(this)
                .beginWith(loginRequest)
                .then(loadGSRequest)
                .then(loadUURequest)
                .enqueue();

        // Observador para el worker de login
        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data outputData = workInfo.getOutputData();
                            boolean loginSuccess = outputData.getBoolean(UserLogin_Worker.KEY_LOGIN_RESULT, false);
                            Log.d("LOGIN_WORKER", "Resultado del login: " + (loginSuccess ? "ÉXITO" : "FALLIDO"));
                            Log.d("LOGIN_WORKER", "Datos completos: " + outputData.toString());
                        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Log.e("LOGIN_WORKER", "Error en el worker de login");
                            Toast.makeText(this, getString(R.string.incorrect_login), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Observador para el worker de carga de estado del juego
        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(loadGSRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data outputData = workInfo.getOutputData();
                            String upgradesData = outputData.getString(LoadGS_Worker.KEY_RESULT);
                            Log.d("LOAD_GS_WORKER", "Estado del juego cargado correctamente");
                            Log.d("LOAD_GS_WORKER", "Datos recibidos: " + upgradesData);
                            this.gs_data = upgradesData;
                        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Log.e("LOAD_GS_WORKER", "Error al cargar el estado del juego");
                        }
                    }
                });

        // Observador para el worker de carga de upgrades
        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(loadUURequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data outputData = workInfo.getOutputData();
                            String upgradesData = outputData.getString(LoadUU_Worker.KEY_RESULT);
                            Log.d("LOAD_UU_WORKER", "Upgrades cargados correctamente");
                            Log.d("LOAD_UU_WORKER", "Datos de upgrades: " + upgradesData);
                            this.uu_data = upgradesData;
                            load_game();

                            //Lanzamos intent a main
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

                            Data_Load.getDL().setUsername(username);

                            // Iniciar la nueva actividad
                            startActivity(intent);
                            // Destruir la actividad actual
                            finish();

                        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Log.e("LOAD_UU_WORKER", "Error al cargar los upgrades");
                        }
                    }
                });
    }

    private void register_user() {
        String username = user.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_fill_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        //Worker de registro
        OneTimeWorkRequest registerRequest = new OneTimeWorkRequest.Builder(UserRegister_Worker.class)
                .setInputData(new Data.Builder()
                        .putString(UserRegister_Worker.KEY_USERNAME, username)
                        .putString(UserRegister_Worker.KEY_PASSWORD, password)
                        .build())
                .build();

        // Workers para crear una "nueva partida" en la BD
        OneTimeWorkRequest newGSRequest = new OneTimeWorkRequest.Builder(NewGS_Worker.class)
                .setInputData(new Data.Builder()
                        .putString(NewGS_Worker.KEY_USERNAME, username)
                        .build())
                .build();

        OneTimeWorkRequest newUURequest = new OneTimeWorkRequest.Builder(NewUU_Worker.class)
                .setInputData(new Data.Builder()
                        .putString(NewUU_Worker.KEY_USERNAME, username)
                        .putInt(NewUU_Worker.KEY_UPGRADE_ID, 1)  // Valor por defecto
                        .putInt(NewUU_Worker.KEY_STATUS, 0)       // Valor por defecto
                        .build())
                .build();

        // Ejecutamos los workers en el orden correcto
        WorkManager.getInstance(this)
                .beginWith(registerRequest)
                .then(newGSRequest)
                .then(newUURequest)
                .enqueue();

        // comprobamos cada worker
        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(registerRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo == null) {
                            Toast.makeText(this, getString(R.string.acc_create_error) , Toast.LENGTH_SHORT).show();
                    }
                });

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(newGSRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo == null) {
                        Toast.makeText(this, getString(R.string.acc_create_error), Toast.LENGTH_SHORT).show();
                    }
                });

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(registerRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo == null) {
                        Toast.makeText(this, "Error al generar los datos del usuario" , Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(this, getString(R.string.acc_create_succes) , Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void load_game() {
        try {
            // Parsear el JSON
            JSONObject jsonObject = new JSONObject(this.gs_data);
            JSONObject data = jsonObject.getJSONObject("data");

            // Extraer los valores del JSON
            this.load_points = data.getInt("points");
            this.load_click_points = data.getInt("click_points");
            this.load_passive_points = data.getInt("passive_points");
            this.load_click_multiplier = (float) data.getDouble("click_multiplier");
            this.load_passive_multiplier = (float) data.getDouble("passive_multiplier");
            this.load_date = data.getString("last_saved");

            // Calcular puntos ganados en ausencia
            add_afk_points();

            Log.d("LOAD_GAME_STATE", "Datos cargados correctamente: " +
                    "\nPuntos: " + load_points +
                    "\nClick Points: " + load_click_points +
                    "\nPassive Points: " + load_passive_points +
                    "\nClick Multiplier: " + load_click_multiplier +
                    "\nPassive Multiplier: " + load_passive_multiplier +
                    "\nÚltima conexión: " + load_date +
                    "\nPuntos ganados en idle: " + idle_gained_points);

            Data_Load.getDL().load_from_database(this.getApplicationContext(), this,this.uu_data);

        } catch (JSONException e) {
            Log.e("LOAD_GAME_STATE", "Error al parsear JSON: " + e.getMessage());
            Toast.makeText(this, getString(R.string.load_parse_error), Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Log.e("LOAD_GAME_STATE", "Error: " + e.getMessage());
        }
    }


    private void add_afk_points() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime last_login = LocalDateTime.parse(this.load_date.trim(), formatter);
            LocalDateTime now = LocalDateTime.now();

            Duration duration = Duration.between(last_login, now);
            long seconds_since_last_login = duration.getSeconds();

            this.idle_gained_points = Math.round(seconds_since_last_login * ((this.load_passive_points * this.load_passive_multiplier) / 2));
            this.load_points += this.idle_gained_points;

            Log.d("AFK_POINTS", "Puntos ganados: " + this.idle_gained_points);
        } catch (Exception e) {
            Log.e("DATE_ERROR", "Error al parsear fecha: " + e.getMessage(), e);
            this.idle_gained_points = 0; // Valor por defecto si falla
        }
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