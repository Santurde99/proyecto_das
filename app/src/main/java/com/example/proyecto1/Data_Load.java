package com.example.proyecto1;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class Data_Load {

    private static Data_Load the_dataload;
    private final ArrayList<Generic_Upgrade> upgrade_list = new ArrayList<>();
    private final Context app_context;

    private Data_Load(Boolean load_data, Context context) {
        this.app_context = context;
        if (load_data) {
            load_from_database(context);
        } else {
            generate_upgrades();
            first_save(context);
        }
    }

    public static Data_Load getDL() {
        return the_dataload;
    }

    public Generic_Upgrade get_upgrade_by_id(int id) {
        for (Generic_Upgrade element : this.upgrade_list) {
            if (element.get_id() == id) {
                return element;
            }
        }
        return null;
    }

    public static void generate_list(Boolean load_data, Context context) {
        the_dataload = new Data_Load(load_data, context);
    }

    private void generate_upgrades() {
        //KIND -> 0 = valor fijo, 1 = porcentual
        //UPGRADE_TARGET -> 0 = pasivo, 1 = click, n = mejora concreta
        this.upgrade_list.add(new Generic_Upgrade(1, app_context.getString(R.string.u1_title), app_context.getString(R.string.u1_desc), 0, 1, 1, 0, 0, R.drawable.pickaxe, 20, 6, new int[]{2,3}));
        this.upgrade_list.add(new Generic_Upgrade(2, app_context.getString(R.string.u2_title), app_context.getString(R.string.u2_desc), 0, 1, 0, 1, 0, R.drawable.pickaxe, 500, 8, new int[]{4}));
        this.upgrade_list.add(new Generic_Upgrade(3, app_context.getString(R.string.u3_title), app_context.getString(R.string.u3_desc), 0, 0, 0, 1, 0, R.drawable.drill, 1000, 5, new int[]{5}));
        this.upgrade_list.add(new Repeatable_Upgrade(4, app_context.getString(R.string.u4_title), app_context.getString(R.string.u4_desc), 0, 1, 0, 1, 0, R.drawable.pickaxe_repeat, 1500, 6, new int[]{6}));
        this.upgrade_list.add(new Generic_Upgrade(5, app_context.getString(R.string.u5_title), app_context.getString(R.string.u5_desc), 0, 1, 0, 1, 0, R.drawable.pickaxe, 5000, 11, new int[]{7}));
        this.upgrade_list.add(new Generic_Upgrade(6, app_context.getString(R.string.u6_title), app_context.getString(R.string.u6_desc), 0, 0, 0, 2, 0, R.drawable.drill, 7500, 12, new int[]{8}));
        this.upgrade_list.add(new Repeatable_Upgrade(7, app_context.getString(R.string.u7_title), app_context.getString(R.string.u7_desc), 1, 1, 0, 1, 0, R.drawable.pickaxe_repeat, 10000, 5, new int[]{9}));
        this.upgrade_list.add(new Generic_Upgrade(8, app_context.getString(R.string.u8_title), app_context.getString(R.string.u8_desc), 0, 0, 0, 1, 0, R.drawable.drill, 20000, 15, new int[]{10}));
        this.upgrade_list.add(new Repeatable_Upgrade(9, app_context.getString(R.string.u9_title), app_context.getString(R.string.u9_desc), 1, 1, 0, 1, 0, R.drawable.pickaxe_repeat, 50000, 10, new int[]{11}));
        this.upgrade_list.add(new Generic_Upgrade(10, app_context.getString(R.string.u10_title), app_context.getString(R.string.u10_desc), 0, 0, 0, 1, 0, R.drawable.drill, 75000, 40, new int[]{12}));
        this.upgrade_list.add(new Repeatable_Upgrade(11, app_context.getString(R.string.u11_title), app_context.getString(R.string.u11_desc), 1, 1, 0, 1, 0, R.drawable.pickaxe_repeat, 120000, 15, new int[]{13}));
        this.upgrade_list.add(new Generic_Upgrade(12, app_context.getString(R.string.u12_title), app_context.getString(R.string.u12_desc), 0, 0, 0, 1, 0, R.drawable.drill, 200000, 100, new int[]{}));
        this.upgrade_list.add(new Repeatable_Upgrade(13, app_context.getString(R.string.u13_title), app_context.getString(R.string.u13_desc), 0, 0, 0, 1, 0, R.drawable.drill_repeat, 50000, 100, new int[]{}));

    }

    private void load_from_database(Context contexto) {
        DbConnector connector = new DbConnector(contexto);

        // Recorrer el cursor
        try (Cursor cursor = connector.get_whole_table("upgrades")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Extraer los datos de cada columna
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    int kind = cursor.getInt(cursor.getColumnIndexOrThrow("kind"));
                    int upgrade_target = cursor.getInt(cursor.getColumnIndexOrThrow("upgrade_target"));
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
                    int required_upgrade = cursor.getInt(cursor.getColumnIndexOrThrow("required_upgrade"));
                    int archieved_upgrades = cursor.getInt(cursor.getColumnIndexOrThrow("archieved_upgrades"));
                    int images = cursor.getInt(cursor.getColumnIndexOrThrow("images"));
                    int price = cursor.getInt(cursor.getColumnIndexOrThrow("price"));
                    int upgrade_value = cursor.getInt(cursor.getColumnIndexOrThrow("upgrade_value"));
                    String unlocks_string = cursor.getString(cursor.getColumnIndexOrThrow("unlocks"));
                    int repeateable = cursor.getInt(cursor.getColumnIndexOrThrow("repeatable"));
                    int[] unlocks = {};
                    if (!unlocks_string.isEmpty()) {
                        //Convertimos el string a un array
                        unlocks = Arrays.stream(unlocks_string.split(","))
                                .mapToInt(Integer::parseInt)
                                .toArray();
                    }
                    //Añadimos la nueva clase
                    if (repeateable == 1) {
                        this.upgrade_list.add(new Repeatable_Upgrade(id, name, description, kind, upgrade_target, status, required_upgrade, archieved_upgrades, images, price, upgrade_value, unlocks));
                    } else {
                        this.upgrade_list.add(new Generic_Upgrade(id, name, description, kind, upgrade_target, status, required_upgrade, archieved_upgrades, images, price, upgrade_value, unlocks));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Maneja cualquier otra excepción que pueda ocurrir
            Log.e("Error", "Ocurrió un error al recorrer las filas", e);
        }
    }

    public void save_upgrades(Context contexto){

        DbConnector connector = new DbConnector(contexto);
        try (SQLiteDatabase db = connector.getWritableDatabase()) {
            for (Generic_Upgrade upgrade : upgrade_list) {
                // Obtener los valores usando los getters
                int id = upgrade.get_id();
                String name = upgrade.get_name();
                String desc = upgrade.get_description();
                int upgrade_target = upgrade.get_upgrade_target();
                int status = upgrade.get_status();
                int price = upgrade.get_price();
                int archieved_upgrades = upgrade.get_archieved_upgrades();
                int repeatable;
                if (upgrade instanceof Repeatable_Upgrade) {
                    repeatable = 1;
                } else {
                    repeatable = 0;
                }

                // Crear un ContentValues para almacenar los valores a actualizar
                ContentValues values = new ContentValues();
                values.put("upgrade_target", upgrade_target);
                values.put("status", status);
                values.put("price", price);
                values.put("name",name);
                values.put("description",desc);
                values.put("archieved_upgrades", archieved_upgrades);
                values.put("repeatable", repeatable);

                // Actualizar la fila correspondiente en la base de datos
                int rowsAffected = db.update("upgrades", values, "id = ?", new String[]{String.valueOf(id)});

                // Verificar si la actualización fue exitosa
                if (rowsAffected > 0) {
                    Log.d("Actualizacion", "Fila actualizada con ID: " + id);
                } else {
                    Log.d("Actualizacion", "No se encontró ninguna fila con ID: " + id);
                }
            }
        } catch (SQLException e) {
            // Manejar cualquier excepción de SQL
            Log.e("Actualizacion", "Error al actualizar la base de datos", e);
        }
        // Cerrar la base de datos
    }

    private void first_save(Context contexto){
        DbConnector connector = new DbConnector(contexto);

        try (SQLiteDatabase db = connector.getWritableDatabase()) {
            for (Generic_Upgrade upgrade : upgrade_list) {
                // Obtener los valores usando los getters
                int id = upgrade.get_id();
                String name = upgrade.get_name();
                String description = upgrade.get_description();
                int kind = upgrade.get_kind();
                int upgrade_target = upgrade.get_upgrade_target();
                int status = upgrade.get_status();
                int required_upgrade = upgrade.get_required_upgrades();
                int archieved_upgrades = upgrade.get_archieved_upgrades();
                int images = upgrade.get_img();
                int price = upgrade.get_price();
                int upgrade_value = upgrade.get_upgrade_value();
                String unlocks = upgrade.get_unlocks_as_string();
                int repeatable;
                if (upgrade instanceof Repeatable_Upgrade) {
                    repeatable = 1;
                } else {
                    repeatable = 0;
                }

                // Crear un ContentValues para almacenar los valores a insertar
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("name", name);
                values.put("description", description);
                values.put("kind", kind);
                values.put("upgrade_target", upgrade_target);
                values.put("status", status);
                values.put("required_upgrade", required_upgrade);
                values.put("archieved_upgrades", archieved_upgrades);
                values.put("images", images);
                values.put("price", price);
                values.put("upgrade_value", upgrade_value);
                values.put("unlocks", unlocks);
                values.put("repeatable", repeatable);

                // Insertar la fila en la base de datos
                long newRowId = db.insert("upgrades", null, values);

                // Verificar si la inserción fue exitosa
                if (newRowId != -1) {
                    Log.d("Inserción", "Fila insertada con ID: " + newRowId);
                } else {
                    Log.d("Inserción", "Error al insertar la fila con ID: " + id);
                }
            }
        } catch (SQLException e) {
            // Manejar cualquier excepción de SQL
            Log.e("Inserción", "Error al insertar en la base de datos", e);
        }
        // Cerrar la base de datos
    }

    public ArrayList<Generic_Upgrade> get_upgrade_list(){
        return upgrade_list;
    }
}
