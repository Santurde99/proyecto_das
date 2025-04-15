package com.example.proyecto1;

import com.example.proyecto1.workers.NewGS_Worker;
import com.example.proyecto1.workers.NewUU_Worker;
import com.example.proyecto1.workers.UserLogin_Worker;
import com.example.proyecto1.workers.UserRegister_Worker;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Menu_activity_new extends AppCompatActivity {

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

    private EditText user;
    private EditText pass;
    private Button login;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.ready = false;

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

    private void login_user(){
        String username = user.getText().toString().trim();
        String password = pass.getText().toString().trim();

        // Validar que no estén vacíos
        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear datos para pasarle al worker
        Data inputData = new Data.Builder()
                .putString(UserLogin_Worker.KEY_USERNAME, username)
                .putString(UserLogin_Worker.KEY_PASSWORD, password)
                .build();

        // Configurar la solicitud de trabajo
        OneTimeWorkRequest loginRequest =
                new OneTimeWorkRequest.Builder(UserLogin_Worker.class)
                        .setInputData(inputData)
                        .build();

        // Observar el resultado del Worker
        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            // Procesar el resultado
                            Data outputData = workInfo.getOutputData();
                            boolean loginValido = outputData.getBoolean(UserLogin_Worker.KEY_LOGIN_RESULT, false);

                            if (loginValido) {
                                Toast.makeText(Menu_activity_new.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                                // Aquí puedes navegar a otra actividad o realizar acciones post-login
                            } else {
                                Toast.makeText(Menu_activity_new.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                            }
                        } else if (workInfo != null && workInfo.getState() == WorkInfo.State.FAILED) {
                            Toast.makeText(Menu_activity_new.this, "Error en el login", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Encolar el trabajo
        WorkManager.getInstance(this).enqueue(loginRequest);
    }

    private void register_user() {
        String username = user.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete ambos campos", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Error al crear la cuenta" , Toast.LENGTH_SHORT).show();
                    }
                });

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(newGSRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo == null) {
                        Toast.makeText(this, "Error al generar los datos nuevos del usuario" , Toast.LENGTH_SHORT).show();
                    }
                });

        WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(registerRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo == null) {
                        Toast.makeText(this, "Error en el registro" , Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(this, "Cuenta creada correctamente" , Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void load_game_state(Context context){
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