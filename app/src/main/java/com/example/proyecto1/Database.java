package com.example.proyecto1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


public class Database extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    public Database(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Crear tabla de puntos
        db.execSQL(
                //Id autoincremental como clave primaria, puntos se refiere a los nuggets y date a la fecha del guardado
                "CREATE TABLE points (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "points INTEGER NOT NULL," +
                        "date TEXT NOT NULL)"
        );
        //Crear tabla de mejoras
        db.execSQL(
                // this.upgrade_list.add(new Generic_Upgrade(1,"Pico de madera","Gana +5 pepitas por toque",0,1,1,0,R.drawable.placeholder,10,5,new int[]{2}));
                "CREATE TABLE IF NOT EXISTS upgrades (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "kind INTEGER NOT NULL, " +
                        "upgrade_target INTEGER NOT NULL, " +
                        "status INTEGER NOT NULL, " +
                        "required_upgrade INTEGER NOT NULL, " +
                        "archieved_upgrades INTEGER NOT NULL, " +
                        "images INTEGER NOT NULL, " +
                        "price INTEGER NOT NULL, " +
                        "upgrade_value INTEGER NOT NULL, " +
                        "unlocks TEXT)" //Revisar
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}