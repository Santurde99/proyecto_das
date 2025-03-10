package com.example.proyecto1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class Menu_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Comprobar si es la primera vez que se inicia la app comprobando la bd
        if (getDatabasePath("database.db").exists()){
            load_data();
        } else{
            first_time_setup();
        }

    }

    private void first_time_setup(){
        //Al mandar el parametro false, estamos haciendo que intente darnos da
        Data_Load dl = Data_Load.getDL(false);
    }

    private void load_data(){

    }
}