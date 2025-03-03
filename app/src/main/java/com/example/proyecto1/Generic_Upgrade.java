package com.example.proyecto1;

import java.util.ArrayList;

public class Generic_Upgrade {
    private final int id;
    private final String name;
    private final String description;
    private int status; // 0 = sin desbloquear, 1 = disponible, 2 = comprado
    private final int required_upgrades; //Cantidad de mejoras previas que tienen que desbloquearse
    private final int image; //Path a su imagen
    private final int price; //Precio de la mejora
    private int[] unlocks; // Ids de mejoras que desbloquea
    private int upgrade_value;//Valor del valor que da
    private int kind; //Tipo de mejora, fija = 0 o porcentual = 1
    private int upgrade_target; //Objetivo, pasivo = 0, click = 1, otros = 2-n


    //Constructora
    public Generic_Upgrade(int id, String name, String desc, int kind, int upgrade_target,int status, int req_upg, int img, int price, int upgrade_value, int[] unlocks){
        this.id = id;
        this.name = name;
        this.description = desc;
        this.required_upgrades = req_upg;
        this.status = status;
        this.image = img;
        this.price = price;
        this.upgrade_value = upgrade_value;
        this.unlocks = unlocks.clone();
        this.kind = kind;
        this.upgrade_target = upgrade_target;
    }

    //Getters
    public int get_id(){
        return this.id;
    }

    public int get_img(){
        return this.image;
    }

    public String get_name(){
        return this.name;
    }

    public String get_description(){
        return this.description;
    }

    public int get_status(){
        return this.status;
    }

    public int get_upgrade_value(){
        return this.upgrade_value;
    }
    
    public int[] get_unlocks(){
        return this.unlocks;
    }

    public int get_price(){
        return this.price;
    }

    public Triple get_upgrade(){
        return new Triple(this.kind,this.upgrade_target,this.upgrade_value);
    }

    //Metodos generales
    
    //Comprueba si esta disponible para comprar, si se puede activar la activa
    public boolean unlock_upgrade(int active_upgrades){
        boolean is_available = false;
        active_upgrades++;
        if ((active_upgrades >= this.required_upgrades) && (status == 1)){
            this.status = 2;
            is_available = true;
        } else if (status == 2) {
            is_available = true;
        }
        return is_available;
    }

    //Comprueba si se puede comprar, si se puede comprar, la compra
    public boolean buy_upgrade(int balance){
        boolean is_buyable = false;
        if (balance > this.price){
            is_buyable = true;
        }
        return is_buyable;

    }

    public void disable_upgrade(){
        if (this.status == 2){
            this.status = 3;
        }
    }




}
