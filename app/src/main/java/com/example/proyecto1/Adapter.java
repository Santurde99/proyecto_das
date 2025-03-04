package com.example.proyecto1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder>{

    private ArrayList<Generic_Upgrade> itemList;
    private ArrayList<Generic_Upgrade> filteredList; // Lista con solo los disponibles y comprados
    private OnItemClickListener listener; // Interfaz para manejar el clic en el boton y pasar el evento a la actividad

    public Adapter() {
        this.itemList = Data_Load.getDL().get_upgrade_list();
        this.filteredList = new ArrayList<>();

        // Filtrar los elementos según su estado
        for (Generic_Upgrade item : itemList) {
            if (item.get_status() != 0) { // Solo añadir elementos con estado != 0
                filteredList.add(item);
            }
        }
    }

    // Interfaz para el listener
    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }

    // Asignar el listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener; //Establecemos que el listener pasa a la activity
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView; // Referencia a la CardView
        public ImageView imageView;
        public TextView textView;
        public Button button;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView); // Asignar la CardView
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
            button = itemView.findViewById(R.id.button);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_upgrades, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Generic_Upgrade currentItem = filteredList.get(position); // Usar la lista filtrada

        // Configurar la imagen y la descripción
        holder.cardView.setTag(currentItem.get_id());
        holder.imageView.setImageResource(currentItem.get_img());
        holder.textView.setText(currentItem.get_description());

        // Configurar el estado del botón y el estilo de la tarjeta
        switch (currentItem.get_status()) {
            case 1: // Estado normal
                holder.button.setEnabled(true);
                holder.cardView.setAlpha(1.0f); // Opacidad normal
                break;
            case 2: // Estado bloqueado
                holder.button.setEnabled(false);
                holder.cardView.setAlpha(0.5f); // Opacidad reducida (tono grisáceo)
                break;
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(currentItem.get_id());
                }
            }
        });
    }

    public void disable_button(int itemId) {
        // Buscar el Item en la lista por su ID
        for (int i = 0; i < itemList.size(); i++) {
            Generic_Upgrade upgrade = itemList.get(i);
            if (upgrade.get_id() == itemId) {
                // Cambiar el estado del Item
                upgrade.disable_upgrade(); // Estado bloqueado

                // Notificar al Adapter que el elemento ha cambiado
                notifyItemChanged(i);
                break;
            }
        }
    }



    @Override
    public int getItemCount() {
        return filteredList.size();
    }
}
