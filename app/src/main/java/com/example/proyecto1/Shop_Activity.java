package com.example.proyecto1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;



public class Shop_Activity extends AppCompatActivity implements Adapter.OnItemClickListener{

    private RecyclerView recyclerView;
    private Adapter adapter;
    private int nuggets_actual_balance; //VALOR DE NUGGETS AL ABRIR LA TIENDA, ESTE NO SE ACTUALIZA
    private int debt = 0; //Deuda para restar en los puntos al volver a la pestaña original
    private ArrayList<Integer> bought_upgrades_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, callback);

        this.nuggets_actual_balance = getIntent().getIntExtra("balance", 0);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Añade más elementos según sea necesario
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(this); //Le informamos al adapter de que la actividad se ocupara de gestionar los botones
    }

    private final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Intent intent = new Intent();
            intent.putIntegerArrayListExtra("bought", bought_upgrades_list);
            setResult(RESULT_OK, intent);
            finish(); // Cierra la actividad
        }
    };

    /**
     * 1. comprobar si existe
     * 2. comprobar si se puede comprar y actualizar estado
     * 3. obtener el precio para restarlo al balance actual y posteriormente al real que sigue sumando
     * 4. mandar señal a las mejoras que dependen de la actual
     * 5. recargar el adapter
     */
    @Override
    public void onItemClick(int itemId) {
        System.out.println(itemId);
        Generic_Upgrade upgrade = Data_Load.getDL(null).get_upgrade_by_id(itemId);
        System.out.println(upgrade);
        if (upgrade == null){
            Toast.makeText(this,"Error, no existe la mejora con ID: " +itemId,Toast.LENGTH_SHORT).show();
        }else{
            if (upgrade.buy_upgrade(nuggets_actual_balance)) {
                nuggets_actual_balance = nuggets_actual_balance - upgrade.get_price();
                //Desactivamos la tarjeta
                adapter.disable_button(itemId);
                //Añadimos la id de la mejora a la lista de mejoras compradas para tratarla en main
                this.bought_upgrades_list.add(itemId);
                Toast.makeText(this, "Se ha comprado la mejora", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No tienes suficientes pepitas", Toast.LENGTH_SHORT).show();

            }
        }

    }

}