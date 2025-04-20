package com.example.proyecto1;

import java.util.ArrayList;
import java.util.StringJoiner;

public class Generic_Upgrade {
    private final int id;
    private final String name;
    private final String description;
    protected int status; // 0 = sin desbloquear, 1 = disponible, 2 = comprado
    private final int image; //Path a su imagen
    protected int price; //Precio de la mejora
    private final int[] unlocks; // Ids de mejoras que desbloquea
    private final int upgrade_value;//Valor de la cantidad de mejora que da (ej +3)
    private final int kind; //Tipo de mejora, fija = 0 o porcentual = 1
    private final int upgrade_target; //Objetivo, pasivo = 0, click = 1, otros = 2-n


    //Constructora
    public Generic_Upgrade(int id, String name, String desc, int kind, int upgrade_target,int status, int img, int price, int upgrade_value, int[] unlocks){
        this.id = id;
        this.name = name;
        this.description = desc;
        this.kind = kind;
        this.upgrade_target = upgrade_target;
        this.status = status;
        this.image = img;
        this.price = price;
        this.upgrade_value = upgrade_value;
        this.unlocks = unlocks.clone();
    }

    //Getters
    public int get_id(){
        return this.id;
    }

    public String get_name(){
        return this.name;
    }

    public String get_description(){
        return this.description;
    }

    public int get_kind() {return this.kind;}

    public int get_upgrade_target() {return this.upgrade_target;}

    public int get_status(){
        return this.status;
    }


    public int get_img(){
        return this.image;
    }

    public int get_price(){ return this.price;}

    public int get_upgrade_value(){
        return this.upgrade_value;
    }

    public int[] get_unlocks(){ return this.unlocks;}

    //Necesario para la bd
    public String get_unlocks_as_string(){

        StringJoiner joiner = new StringJoiner(",");
        for (int num : this.unlocks) {
            joiner.add(String.valueOf(num));
        }
        return joiner.toString();
    }

    public int[] get_upgrade(){
        return new int[]{this.kind,this.upgrade_target,this.upgrade_value};
    }

    //Metodos generales
    
    //Comprueba si esta disponible para comprar, si se puede activar la activa
    public boolean unlock_upgrade(){
        this.status = 1;
        return true;
    }

    //Comprueba si se puede comprar, si se puede comprar, la compra
    public boolean buy_upgrade(int balance){
        boolean is_buyable = false;
        if (balance >= this.price){
            is_buyable = true;
            Generic_Upgrade next = Data_Load.getDL().get_upgrade_by_id(this.id + 1);
            if (next != null) {
                next.unlock_upgrade();
            }

        }
        return is_buyable;

    }

    public void disable_upgrade(){
        if (this.status == 1){
            this.status = 2;
        }
    }




}
